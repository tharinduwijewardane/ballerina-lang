/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.compiler.semantics.model.types.BEnumType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@code EnumInfo} contains metadata of a Ballerina Enum entry in the program file.
 *
 * @since 0.95
 */
public class EnumInfo extends StructureTypeInfo {

    public BEnumType enumType;

    public List<EnumeratorInfo> enumeratorInfoList = new ArrayList<>();

    public EnumInfo(int pkgNameCPIndex, int nameCPIndex) {
        super(pkgNameCPIndex, nameCPIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgNameCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EnumInfo && pkgNameCPIndex == (((EnumInfo) obj).pkgNameCPIndex)
                && nameCPIndex == (((EnumInfo) obj).nameCPIndex);
    }
}
