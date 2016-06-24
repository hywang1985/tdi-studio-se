// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.utils;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

/**
 * 
 * created by ycbai on 2016年3月15日 Detailled comment
 *
 */
public class TestProperties extends FixedConnectorsComponentProperties {

    public Property<String> userId = newProperty("userId").setRequired(); //$NON-NLS-1$

    public SchemaProperties schema = new SchemaProperties("schema"); //$NON-NLS-1$

    public TestNestedProperties nestedProps = new TestNestedProperties("nestedProps"); //$NON-NLS-1$

    public TestContactProperties contactProps = new TestContactProperties("contactProps"); //$NON-NLS-1$

    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema"); //$NON-NLS-1$

    public TestProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(userId);
        form.addRow(nestedProps.getForm(Form.MAIN));
        form.addRow(contactProps.getForm(Form.MAIN));
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

}
