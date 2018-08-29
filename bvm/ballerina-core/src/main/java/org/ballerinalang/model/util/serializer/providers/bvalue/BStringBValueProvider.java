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
package org.ballerinalang.model.util.serializer.providers.bvalue;

import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

/**
 * Provide mapping between {@link BString} and {@link BValue} representation of it.
 *
 * @since 0.982.0
 */
public class BStringBValueProvider implements SerializationBValueProvider<BString> {
    private static final String B_STRING = BString.class.getSimpleName();

    @Override
    public String typeName() {
        return BString.class.getSimpleName();
    }

    @Override
    public Class<?> getType() {
        return BString.class;
    }

    @Override
    public BValue toBValue(BString bString, BValueSerializer serializer) {
        if (bString.stringValue() == null) {
            return null;
        }
        return BValueProviderHelper.wrap(B_STRING, bString);
    }

    @Override
    public BString toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            @SuppressWarnings("unchecked")
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            if (BValueProviderHelper.isWrapperOfType(wrapper, B_STRING)) {
                return (BString) BValueProviderHelper.getPayload(wrapper);
            }
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, B_STRING);
    }
}
