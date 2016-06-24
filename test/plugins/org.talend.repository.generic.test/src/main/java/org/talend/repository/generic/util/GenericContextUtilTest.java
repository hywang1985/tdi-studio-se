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
package org.talend.repository.generic.util;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.TalendFileFactoryImpl;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.GenericConnParamName;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;

/**
 * created by ycbai on 2016年2月16日 Detailled comment
 *
 */
public class GenericContextUtilTest {

    @Test
    public void testExportAndRevertContextForComponentProperties() {
        String prefix = "testConn"; //$NON-NLS-1$
        GenericConnection connection = GenericMetadataFactory.eINSTANCE.createGenericConnection();

        TestProperties props = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        props.userId.setValue("1"); //$NON-NLS-1$
        TestNestedProperties nestedProps = (TestNestedProperties) props.getProperty("nestedProps"); //$NON-NLS-1$
        nestedProps.userName.setValue("testUserName"); //$NON-NLS-1$
        nestedProps.userPassword.setValue("testUserPassword"); //$NON-NLS-1$
        connection.setCompProperties(props.toSerialized());

        Set<IConnParamName> contextParams = new HashSet<>();
        contextParams.add(createConnParamName("userId")); //$NON-NLS-1$
        contextParams.add(createConnParamName("nestedProps.userName")); //$NON-NLS-1$
        contextParams.add(createConnParamName("nestedProps.userPassword")); //$NON-NLS-1$

        // Test export context
        GenericContextUtil.setPropertiesForContextMode(prefix, connection, contextParams);

        TestProperties deserProps = getPropertiesFromConnection(connection);
        assertEquals("context.testConn_userId", deserProps.userId.getValue()); //$NON-NLS-1$
        assertEquals("context.testConn_nestedProps_userName", deserProps.nestedProps.userName.getValue()); //$NON-NLS-1$
        assertEquals("context.testConn_nestedProps_userPassword", deserProps.nestedProps.userPassword.getValue()); //$NON-NLS-1$
        assertEquals(deserProps.userId.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), true);
        assertEquals(deserProps.nestedProps.userName.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), true);
        assertEquals(deserProps.nestedProps.userPassword.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), true);

        // Test revert context
        ContextType contextType = TalendFileFactoryImpl.eINSTANCE.createContextType();
        contextType.getContextParameter().add(createContextParameterType("testConn_userId", "1")); //$NON-NLS-1$ //$NON-NLS-2$
        contextType.getContextParameter().add(createContextParameterType("testConn_nestedProps_userName", "testUserName")); //$NON-NLS-1$ //$NON-NLS-2$
        contextType.getContextParameter().add(createContextParameterType("testConn_nestedProps_userPassword", "testUserPassword")); //$NON-NLS-1$ //$NON-NLS-2$

        GenericContextUtil.revertPropertiesForContextMode(connection, contextType);

        deserProps = getPropertiesFromConnection(connection);
        assertEquals("1", deserProps.userId.getValue()); //$NON-NLS-1$
        assertEquals("testUserName", deserProps.nestedProps.userName.getValue()); //$NON-NLS-1$
        assertEquals("testUserPassword", deserProps.nestedProps.userPassword.getValue()); //$NON-NLS-1$
        assertEquals(deserProps.userId.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), false);
        assertEquals(deserProps.nestedProps.userName.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), false);
        assertEquals(deserProps.nestedProps.userPassword.getTaggedValue(IGenericConstants.IS_CONTEXT_MODE), false);
    }

    private TestProperties getPropertiesFromConnection(GenericConnection connection) {
        SerializerDeserializer.Deserialized<TestProperties> d = Properties.Helper.fromSerializedPersistent(connection.getCompProperties(), TestProperties.class);
        TestProperties deserProps = d.object;
        return deserProps;
    }

    private GenericConnParamName createConnParamName(String paramName) {
        GenericConnParamName connParamName = new GenericConnParamName();
        connParamName.setName(paramName);
        connParamName.setContextVar(getValidContextVarName(paramName));
        return connParamName;
    }

    private ContextParameterType createContextParameterType(String paramName, String paramValue) {
        ContextParameterType contextParameter = TalendFileFactoryImpl.eINSTANCE.createContextParameterType();
        contextParameter.setName(paramName);
        contextParameter.setRawValue(paramValue);
        return contextParameter;
    }

    private String getValidContextVarName(String paramName) {
        return paramName.replace(IGenericConstants.EXP_SEPARATOR, IGenericConstants.UNDERLINE_SEPARATOR);
    }

}
