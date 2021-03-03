/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semantic.api.test.symbolbynode;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AbsResourcePathAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceAttachPointKind;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.annotations.Test;

import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.SERVICE_DECLARATION;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for looking up a symbol given a service decl.
 *
 * @since 2.0.0
 */
@Test
public class SymbolByServiceDeclTest extends SymbolByNodeTest {

    @Override
    String getTestSourcePath() {
        return "test-src/symbol-by-node/symbol_by_service_decl_test.bal";
    }

    @Override
    NodeVisitor getNodeVisitor(SemanticModel model) {
        return new NodeVisitor() {

            @Override
            public void visit(ServiceDeclarationNode serviceDeclarationNode) {
                ServiceDeclarationSymbol symbol =
                        (ServiceDeclarationSymbol) assertSymbol(serviceDeclarationNode, model, SERVICE_DECLARATION,
                                                                null);

                ServiceAttachPoint attachPoint = symbol.attachPoint().get();
                assertEquals(attachPoint.kind(), ServiceAttachPointKind.ABSOLUTE_RESOURCE_PATH);
                assertEquals(((AbsResourcePathAttachPoint) attachPoint).get().toString(), "[foo, bar]");

                TypeSymbol type = symbol.typeDescriptor().get();
                assertEquals(type.typeKind(), TypeDescKind.TYPE_REFERENCE);
                assertEquals(((TypeReferenceTypeSymbol) type).name(), "HelloWorld");

                for (Node member : serviceDeclarationNode.members()) {
                    member.accept(this);
                }
            }

            @Override
            public void visit(ClassDefinitionNode classDefinitionNode) {
                // Do nothing. This is to prevent traversing the listener class.
            }

            @Override
            public void visit(ObjectFieldNode objectFieldNode) {
                assertSymbol(objectFieldNode, model, CLASS_FIELD, objectFieldNode.fieldName().text());
                assertSymbol(objectFieldNode.fieldName(), model, CLASS_FIELD, objectFieldNode.fieldName().text());
            }

            @Override
            public void visit(FunctionDefinitionNode functionDefinitionNode) {
                if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                    assertSymbol(functionDefinitionNode, model, METHOD,
                                 "$" + functionDefinitionNode.functionName().text() + "$" +
                                         functionDefinitionNode.relativeResourcePath().get(0).toString());
                }

                if (functionDefinitionNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                    assertSymbol(functionDefinitionNode, model, METHOD, functionDefinitionNode.functionName().text());
                }
            }
        };
    }

    @Override
    void verifyAssertCount() {
        assertEquals(getAssertCount(), 6);
    }

    private Symbol assertSymbol(Node node, SemanticModel model, SymbolKind kind, String name) {
        Optional<Symbol> symbol = model.symbol(node);
        assertEquals(symbol.get().kind(), kind);
        if (name != null) {
            assertEquals(symbol.get().getName().get(), name);
        }
        incrementAssertCount();
        return symbol.get();
    }
}
