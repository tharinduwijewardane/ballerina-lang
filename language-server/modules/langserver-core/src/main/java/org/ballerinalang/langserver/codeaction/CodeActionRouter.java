/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.LSClientLogger;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.codeaction.CodeActionUtil.codeActionNodeType;
import static org.ballerinalang.langserver.codeaction.CodeActionUtil.findCursorDetails;

/**
 * Represents the Code Action router.
 *
 * @since 1.1.1
 */
public class CodeActionRouter {

    /**
     * Returns a list of supported code actions.
     *
     * @param syntaxTree        code action node type
     * @param cursorDiagnostics list of diagnostics of the cursor position
     * @param allDiagnostics    list of all diagnostics
     * @param ctx               {@link LSContext}
     * @return list of code actions
     */
    public static List<CodeAction> getAvailableCodeActions(SyntaxTree syntaxTree,
                                                           List<Diagnostic> cursorDiagnostics,
                                                           List<Diagnostic> allDiagnostics, LSContext ctx) {
        List<CodeAction> codeActions = new ArrayList<>();
        CodeActionProvidersHolder codeActionProvidersHolder = CodeActionProvidersHolder.getInstance();
        // Get available node-type based code-actions
        Optional<Pair<CodeActionNodeType, NonTerminalNode>> nodeTypeAndNode = codeActionNodeType(syntaxTree, ctx);
        if (nodeTypeAndNode.isPresent()) {
            CodeActionNodeType nodeType = nodeTypeAndNode.get().getLeft();
            NonTerminalNode node = nodeTypeAndNode.get().getRight();
            codeActionProvidersHolder.getActiveNodeBasedProviders(nodeType).forEach(provider -> {
                try {
                    List<CodeAction> codeActionsOut = provider.getNodeBasedCodeActions(node, nodeType, allDiagnostics,
                                                                                       syntaxTree, ctx);
                    if (codeActionsOut != null) {
                        codeActions.addAll(codeActionsOut);
                    }
                } catch (Exception e) {
                    String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                    LSClientLogger.logError(msg, e, null, (Position) null);
                }
            });
        }
        // Get available diagnostics based code-actions
        if (cursorDiagnostics != null && !cursorDiagnostics.isEmpty()) {
            for (Diagnostic diagnostic : cursorDiagnostics) {
                PositionDetails positionDetails = findCursorDetails(diagnostic.getRange(), syntaxTree, ctx);
                codeActionProvidersHolder.getActiveDiagnosticsBasedProviders().forEach(provider -> {
                    try {
                        List<CodeAction> codeActionsOut = provider.getDiagBasedCodeActions(diagnostic, positionDetails,
                                                                                           allDiagnostics, syntaxTree,
                                                                                           ctx);
                        if (codeActionsOut != null) {
                            codeActions.addAll(codeActionsOut);
                        }
                    } catch (Exception e) {
                        String msg = "CodeAction '" + provider.getClass().getSimpleName() + "' failed!";
                        LSClientLogger.logError(msg, e, null, (Position) null);
                    }
                });
            }
        }
        return codeActions;
    }
}
