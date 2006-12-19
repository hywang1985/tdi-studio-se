// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.core.ui.editor.cmd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.SchemaTarget;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class ChangeValuesFromRepository extends Command {

    private Map<String, Object> oldValues;

    private Element elem;

    private Connection connection;

    private String value;

    private String propertyName;

    private String oldMetadata;

    public ChangeValuesFromRepository(Element elem, Connection connection, String propertyName, String value) {
        this.elem = elem;
        this.connection = connection;
        this.value = value;
        this.propertyName = propertyName;
        oldValues = new HashMap<String, Object>();

        setLabel(Messages.getString("PropertyChangeCommand.0")); //$NON-NLS-1$
    }

    private void refreshPropertyView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view = page.findView("org.eclipse.ui.views.PropertySheet");
        PropertySheet sheet = (PropertySheet) view;
        TabbedPropertySheetPage tabbedPropertySheetPage = (TabbedPropertySheetPage) sheet.getCurrentPage();
        tabbedPropertySheetPage.refresh();
    }

    @Override
    public void execute() {
        // Force redraw of Commponents propoerties
        elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(true));

        if (propertyName.equals(EParameterName.PROPERTY_TYPE.getName()) && (EmfComponent.BUILTIN.equals(value))) {
            for (IElementParameter param : elem.getElementParameters()) {
                param.setRepositoryValueUsed(false);
            }
        } else {
            oldValues.clear();
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)
                        && (!param.getName().equals(EParameterName.PROPERTY_TYPE.getName()))) {
                    Object objectValue = (Object) RepositoryToComponentProperty.getValue(connection, repositoryValue);
                    if (objectValue != null) {
                        oldValues.put(param.getName(), param.getValue());

                        if (param.getField().equals(EParameterFieldType.CLOSED_LIST)
                                && param.getRepositoryValue().equals("TYPE")) {
                            boolean found = false;
                            String[] list = param.getListRepositoryItems();
                            for (int i = 0; (i < list.length) && (!found); i++) {
                                if (objectValue.equals(list[i])) {
                                    found = true;
                                    elem.setPropertyValue(param.getName(), param.getListItemsValue()[i]);
                                }
                            }
                        } else {
                            if (param.getField().equals(EParameterFieldType.TABLE)
                                    && param.getRepositoryValue().equals("XML_MAPPING")) {
                                boolean found = false;
                                String[] list = param.getListRepositoryItems();
                                List<Map<String, Object>> tableInfo = (List<Map<String, Object>>) elem
                                        .getPropertyValue(param.getName());
                                int column = 0;
                                for (int i = 0; (i < list.length) && (!found); i++) {
                                    if (list[i].equals("XML_QUERY")) {
                                        column = i;
                                        found = true;
                                    }
                                }
                                EList schemaList = (EList) objectValue;
                                String[] names = param.getListItemsDisplayCodeName();
                                IMetadataTable table = ((Node) elem).getMetadataList().get(0);
                                for (int i = 0; i < schemaList.size(); i++) {
                                    Map<String, Object> line = tableInfo.get(i);
                                    if (table != null) {
                                        if (table.getListColumns().size() > i) {
                                            SchemaTarget schemaTarget = (SchemaTarget) schemaList.get(i);
                                            line.put(names[column], schemaTarget.getRelativeXPathQuery());
                                        }
                                    }
                                }
                            } else {
                                elem.setPropertyValue(param.getName(), objectValue);
                            }
                        }
                        param.setRepositoryValueUsed(true);
                    }
                }
            }
        }
        if (propertyName.equals(EParameterName.PROPERTY_TYPE.getName())) {
            elem.setPropertyValue(EParameterName.PROPERTY_TYPE.getName(), value);
        } else {
            oldMetadata = (String) elem.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
            elem.setPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), value);
        }
        refreshPropertyView();
    }

    @Override
    public void undo() {
        // Force redraw of Commponents propoerties
        elem.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), new Boolean(true));

        if (propertyName.equals(EParameterName.PROPERTY_TYPE.getName()) && (EmfComponent.BUILTIN.equals(value))) {
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)
                        && (!param.getName().equals(EParameterName.PROPERTY_TYPE.getName()))) {
                    param.setRepositoryValueUsed(true);
                }
            }
        } else {
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)) {
                    Object objectValue = (Object) RepositoryToComponentProperty.getValue(connection, repositoryValue);
                    if (objectValue != null) {
                        elem.setPropertyValue(param.getName(), oldValues.get(param.getName()));
                        param.setRepositoryValueUsed(false);
                    }
                }
            }
        }
        if (propertyName.equals(EParameterName.PROPERTY_TYPE.getName())) {
            if (value.equals(EmfComponent.BUILTIN)) {
                elem.setPropertyValue(EParameterName.PROPERTY_TYPE.getName(), EmfComponent.REPOSITORY);
            } else {
                elem.setPropertyValue(EParameterName.PROPERTY_TYPE.getName(), EmfComponent.BUILTIN);
            }
        } else {
            elem.setPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName(), oldMetadata);
        }
        refreshPropertyView();
    }
}
