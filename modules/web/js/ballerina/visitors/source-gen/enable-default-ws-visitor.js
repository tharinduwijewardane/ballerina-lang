/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import ASTNode from '../../ast/node';
import ASTVisitor from '../ast-visitor';
import Factory from '../../ast/ballerina-ast-factory';

/**
 * Constructor for the Enable Format Visitor
 * @param parent
 * @constructor
 */
class EnableDefaultWSVisitor extends ASTVisitor {

    canVisit(node) {
        return node instanceof ASTNode;
    }

    visit(node) {
        node.whiteSpace.useDefault = true;
        if (Factory.isBinaryExpression(node)) {
            node.getRightExpression().accept(this);
            node.getLeftExpression().accept(this);
        } else if (Factory.isActionInvocationExpression(node)) {
            node.getArguments().forEach((arg) => {
                arg.accpt(this);
            })
        } else if (Factory.isConnectorInitExpression(node)) {
            node.getArgs().forEach((arg) => {
                arg.accpt(this);
            })
            node.getConnectorName().accpt(this);
        } else if (Factory.isCatchStatement(node)) {
            node.getParameter().accept(this);
        }
    }
}

export default EnableDefaultWSVisitor;
