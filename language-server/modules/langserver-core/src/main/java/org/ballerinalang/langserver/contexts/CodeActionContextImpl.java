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
package org.ballerinalang.langserver.contexts;

import io.ballerina.compiler.api.SemanticModel;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.util.List;
import java.util.Optional;

/**
 * Language server context implementation.
 *
 * @since 1.2.0
 */
public class CodeActionContextImpl extends AbstractDocumentServiceContext implements CodeActionContext {

    private Position cursorPosition;
    private List<Diagnostic> diagnostics;
    private final CodeActionParams params;
    private PositionDetails positionDetails;

    public CodeActionContextImpl(LSOperation operation,
                                 String fileUri,
                                 WorkspaceManager wsManager,
                                 CodeActionParams params) {
        super(operation, fileUri, wsManager);
        this.params = params;
    }

    @Override
    public Position cursorPosition() {
        if (this.cursorPosition == null) {
            int line = params.getRange().getStart().getLine();
            int col = params.getRange().getStart().getCharacter();
            this.cursorPosition = new Position(line, col);
        }
        
        return this.cursorPosition;
    }

    @Override
    public List<Diagnostic> allDiagnostics() {
        if (diagnostics == null) {
            Optional<SemanticModel> semanticModel = this.workspace().semanticModel(this.filePath());
            semanticModel.ifPresent(model -> this.diagnostics = CodeActionUtil.toDiagnostics(model.diagnostics()));
        }

        return this.diagnostics;
    }

    @Override
    public List<Diagnostic> cursorDiagnostics() {
        return params.getContext().getDiagnostics();
    }

    @Override
    public void setPositionDetails(PositionDetails positionDetails) {
        this.positionDetails = positionDetails;
    }

    @Override
    public PositionDetails positionDetails() {
        return this.positionDetails;
    }

    /**
     * Represents Language server code action context Builder.
     *
     * @since 2.0.0
     */
    protected static class CodeActionContextBuilder extends AbstractContextBuilder<CodeActionContextBuilder> {

        private final CodeActionParams params;

        public CodeActionContextBuilder(CodeActionParams params) {
            super(LSContextOperation.TXT_CODE_ACTION);
            this.params = params;
        }

        public CodeActionContext build() {
            return new CodeActionContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.params);
        }

        @Override
        public CodeActionContextBuilder self() {
            return this;
        }
    }
}
