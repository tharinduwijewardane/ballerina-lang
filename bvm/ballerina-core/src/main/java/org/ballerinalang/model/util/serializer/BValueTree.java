/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.util.serializer;

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

import java.io.Closeable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.model.util.serializer.BValueHelper.addHashValue;
import static org.ballerinalang.model.util.serializer.BValueHelper.createBString;
import static org.ballerinalang.model.util.serializer.BValueHelper.getHashCode;
import static org.ballerinalang.model.util.serializer.BValueHelper.wrapObject;
import static org.ballerinalang.model.util.serializer.ObjectHelper.getTrimmedClassName;

/**
 * Convert Java object graph into a tree of BValue objects.
 * <p>
 * Object reference sharing is tracked using an {@link IdentityHashMap},
 * repeated occurrences are marked by a link to previous occurrence.
 *
 * @since 0.982.0
 */
public class BValueTree implements BValueSerializer, Closeable {
    private static final BValueProvider bValueProvider = BValueProvider.getInstance();
    private final IdentityHashMap<Object, Object> identityMap = new IdentityHashMap<>();
    private final HashSet<String> repeatedReferenceSet = new HashSet<>();
    private final BRefValueArrays bRefValueArrays;
    private boolean isClosed;

    BValueTree() {
        isClosed = false;
        bRefValueArrays = new BRefValueArrays(this);
    }

    /**
     * Convert given Java object and it's references to {@link BValue} tree representation.
     *
     * @param src Java object to be converted
     * @return Converted {@link BValue} tree
     */
    BRefType toBValueTree(Object src) {
        try {
            BRefType tree = toBValue(src, src.getClass());
            BValueHelper.trimTree(tree, repeatedReferenceSet);
            return tree;
        } finally {
            closePrivate();
        }
    }

    public BRefType toBValue(Object src, Class<?> leftSideType) {
        if (src == null) {
            return null;
        }
        if (src instanceof String) {
            return createBString((String) src);
        }
        if (src.getClass().isArray()) {
            BRefType array = arrayFrom(src);
            if (array != null) {
                return array;
            }
        }
        if (src instanceof Character) {
            return new BInteger((long) (Character) src);
        }
        if (src instanceof Number) {
            BRefType num = numberToBValue(src);
            if (num != null) {
                return num;
            }
        }
        if (src instanceof Boolean) {
            return new BBoolean((Boolean) src);
        }
        if (src instanceof Enum) {
            return enumToBValue((Enum) src);
        }
        return convertReferenceSemanticObject(src, leftSideType);
    }

    private BMap mapToBValue(Map<Object, Object> source) {
        /*
         * Json dictionaries only allow strings to be keys, hence we have to transform original Map
         * so that we have some sort of Map<String, Value> representation.
         * Transformation:
         * Extract non-string typed key to a auxiliary dictionary as 'value'
         * and have an auto-generated *key* to represent the original complex key.
         * Finally add the auxiliary dictionary to target dictionary, using a special key.
         */
        BMap<String, BValue> target = new BMap<>();
        BMap<String, BValue> complexKeyMap = new BMap<>();
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) {
                target.put((String) key, toBValue(entry.getValue(), null));
            } else {
                String complexKeyHash = getHashCode(key, JsonSerializerConst.COMPLEX_KEY_TAG, null);
                target.put(complexKeyHash, toBValue(entry.getValue(), null));
                complexKeyMap.put(complexKeyHash, toBValue(key, null));
            }
        }
        if (!complexKeyMap.isEmpty()) {
            target.put(JsonSerializerConst.COMPLEX_KEY_MAP_TAG, complexKeyMap);
        }
        return wrapObject(JsonSerializerConst.MAP_TAG, target);
    }

    private BMap<String, BValue> listToBValue(List list) {
        BRefValueArray array = new BRefValueArray(new BArrayType(BTypes.typeAny));
        for (Object item : list) {
            array.append(toBValue(item, null));
        }
        BMap<String, BValue> bMap = wrapObject(JsonSerializerConst.LIST_TAG, array);
        bMap.put(JsonSerializerConst.LENGTH_TAG, new BInteger(list.size()));
        return bMap;
    }

    private BMap enumToBValue(Enum obj) {
        String fullEnumName = getTrimmedClassName(obj) + "." + obj.toString();
        BString name = createBString(fullEnumName);
        return wrapObject(JsonSerializerConst.ENUM_TAG, name);
    }


    private BRefType arrayFrom(Object src) {
        Class<?> srcClass = src.getClass();
        if (srcClass == int[].class) {
            return bRefValueArrays.from((int[]) src);
        }
        if (srcClass == long[].class) {
            return bRefValueArrays.from((long[]) src);
        }
        if (srcClass == double[].class) {
            return bRefValueArrays.from((double[]) src);
        }
        if (srcClass == float[].class) {
            return bRefValueArrays.from((float[]) src);
        }
        if (srcClass == char[].class) {
            return bRefValueArrays.from((char[]) src);
        }
        if (srcClass == byte[].class) {
            return bRefValueArrays.from((byte[]) src);
        }
        if (srcClass == short[].class) {
            return bRefValueArrays.from((short[]) src);
        }
        if (srcClass == String[].class
                || srcClass == Integer[].class
                || srcClass == Long[].class
                || srcClass == Double[].class
                || srcClass == Float[].class
                || srcClass == Character[].class
                || srcClass == Byte[].class
                || srcClass == Short[].class) {
            return bRefValueArrays.from((Object[]) src);
        }
        return null;
    }

    private BRefType numberToBValue(Object src) {
        Class<?> srcClass = src.getClass();
        if (srcClass == Integer.class) {
            return new BInteger(((Integer) src).longValue());
        }
        if (srcClass == Long.class) {
            return new BInteger((Long) src);
        }
        if (srcClass == Float.class) {
            return new BFloat(((Float) src).doubleValue());
        }
        if (srcClass == Double.class) {
            return new BFloat((Double) src);
        }
        if (srcClass == Byte.class) {
            return new BInteger(((Byte) src).longValue());
        }
        if (srcClass == Short.class) {
            return new BInteger(((Short) src).intValue());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BMap convertReferenceSemanticObject(Object obj, Class<?> leftSideType) {
        if (isPreviouslySeen(obj)) {
            return createExistingReferenceNode(obj);
        }
        registerAsSeen(obj);

        String className = getTrimmedClassName(obj);
        SerializationBValueProvider provider = bValueProvider.find(className);
        if (provider != null) {
            BMap<String, BValue> converted = (BMap<String, BValue>) provider.toBValue(obj, this);
            addHashValue(obj, converted);
            return converted;
        }
        if (obj instanceof Map) {
            BMap map = mapToBValue((Map) obj);
            addHashValue(obj, map);
            return map;
        }
        if (obj instanceof List) {
            BMap<String, BValue> map = listToBValue((List) obj);
            addHashValue(obj, map);
            return map;
        }
        if (obj.getClass().isArray()) {
            BMap<String, BValue> map = arrayToBValue(obj);
            addHashValue(obj, map);
            return map;
        }

        BMap map = convertToBValueViaReflection(obj, leftSideType);
        addHashValue(obj, map);
        return map;
    }

    private BMap<String, BValue> arrayToBValue(Object array) {
        BRefValueArray bArray = new BRefValueArray(new BArrayType(BTypes.typeAny));
        int arrayLength = Array.getLength(array);
        for (int i = 0; i < arrayLength; i++) {
            bArray.append(toBValue(Array.get(array, i), null));
        }

        BMap<String, BValue> bMap = wrapObject(JsonSerializerConst.ARRAY_TAG, bArray);
        bMap.put(JsonSerializerConst.LENGTH_TAG, new BInteger(arrayLength));
        Class<?> componentType = array.getClass().getComponentType();
        String trimmedName = getTrimmedClassName(componentType);
        bMap.put(JsonSerializerConst.COMPONENT_TYPE, createBString(trimmedName));
        return bMap;
    }

    private void registerAsSeen(Object obj) {
        identityMap.put(obj, obj);
    }

    private boolean isPreviouslySeen(Object obj) {
        return identityMap.containsKey(obj);
    }

    private BMap<String, BValue> createExistingReferenceNode(Object obj) {
        BMap<String, BValue> map = new BMap<>();
        BString hashCode = getHashCode(obj);
        map.put(JsonSerializerConst.EXISTING_TAG, hashCode);
        repeatedReferenceSet.add(hashCode.stringValue());
        return map;
    }

    private BMap convertToBValueViaReflection(Object obj, Class<?> leftSideType) {
        Class<?> objClass = obj.getClass();
        BMap<String, BValue> map = new BMap<>();
        HashMap<String, Field> allFields = ObjectHelper.getAllFields(objClass, 0);
        for (Map.Entry<String, Field> fieldEntry : allFields.entrySet()) {
            String fieldName = fieldEntry.getKey();
            Field field = fieldEntry.getValue();
            field.setAccessible(true);
            try {
                map.put(fieldName, toBValue(field.get(obj), field.getType()));
            } catch (IllegalAccessException e) {
                // field is set to be accessible
            }
        }

        if (leftSideType != objClass) {
            String className = getTrimmedClassName(obj);
            return wrapObject(className, map);
        } else {
            return map;
        }
    }

    private void closePrivate() {
        if (!isClosed) {
            identityMap.clear();
            repeatedReferenceSet.clear();
            isClosed = true;
        }
    }

    @Override
    public void close() {
        closePrivate();
    }
}
