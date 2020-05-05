/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.query;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Sementic Negative test cases for query expressions.
 *
 * @since 1.2.0
 */
public class QuerySemanticNegativeTests {

    @Test
    public void testMultipleFromClauseStreamType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/query/query-semantics-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        validateError(compileResult, 0, "type stream not allowed here; to use from on a " +
                        "type stream, it should be the first from clause in the query.",
                24, 36);
    }
}