/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.access.DBAUserPasswordManager;
import org.jkiss.dbeaver.model.access.DBAuthUtils;
import org.jkiss.dbeaver.model.navigator.DBNDataSource;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.navigator.NavigatorUtils;

import java.lang.reflect.InvocationTargetException;

public class ChangePasswordDialogHandler extends AbstractHandler {

    private static final Log log = Log.getLog(ChangePasswordDialogHandler.class);

    @Override
    public Object execute(ExecutionEvent executionEvent) throws ExecutionException {
        final ISelection selection = HandlerUtil.getCurrentSelection(executionEvent);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        final DBNNode node = NavigatorUtils.getSelectedNode(selection);
        if (node instanceof DBNDataSource) {
            DBNDataSource dataSource = (DBNDataSource) node;
            DBPDataSourceContainer dataSourceContainer = dataSource.getDataSourceContainer();
            DBPDataSource dbpDataSource = dataSourceContainer.getDataSource();
            if (dbpDataSource instanceof IAdaptable) {
                DBAUserPasswordManager changePassword = ((IAdaptable) dbpDataSource).getAdapter(DBAUserPasswordManager.class);
                if (changePassword != null) {
                    try {
                        UIUtils.runInProgressService(monitor ->
                            DBAuthUtils.promptAndChangePasswordForCurrentUser(monitor, dataSourceContainer, changePassword));
                    } catch (InvocationTargetException e) {
                        log.error(e.getTargetException());
                    } catch (InterruptedException e) {
                        DBWorkbench.getPlatformUI().showError("Change user password", "User password change error", e);
                    }
                }
            }
        }
        return null;
    }
}
