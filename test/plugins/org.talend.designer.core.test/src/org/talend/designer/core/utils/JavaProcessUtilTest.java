// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataNode;

/**
 * created by nrousseau on Jun 8, 2016 Detailled comment
 *
 */
public class JavaProcessUtilTest {

    @Test
    public void testFindMoreLibraries() {
        // ensure the CAMEL is not added as jar dependency
        DummyComponent comp = mock(DummyComponent.class);
        when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_CAMEL.getName());
        INode node = new DataNode(comp, ""); //$NON-NLS-1$
        IElementParameter param = new ElementParameter(node);
        param.setName("HOTLIBS");
        List<Map<String, Object>> table = new ArrayList<Map<String, Object>>();
        Map<String, Object> line = new HashMap<String, Object>();
        line.put("LIBPATH", "CAMEL");
        table.add(line);
        param.setValue(table);
        List<ModuleNeeded> modulesNeeded = new ArrayList<ModuleNeeded>();
        JavaProcessUtil.findMoreLibraries(null, modulesNeeded, param);
        assertTrue(modulesNeeded.isEmpty());

        table = new ArrayList<Map<String, Object>>();
        line = new HashMap<String, Object>();
        line.put("LIBPATH", "\"camel.jar\"");
        table.add(line);
        param.setValue(table);
        modulesNeeded = new ArrayList<ModuleNeeded>();
        JavaProcessUtil.findMoreLibraries(null, modulesNeeded, param);
        assertEquals(1, modulesNeeded.size());
        assertEquals("camel.jar", modulesNeeded.get(0).getModuleName());
    }

    @Test
    public void testGetChildrenModules_noChildren() {
        INode node = mock(INode.class);
        IComponent component = mock(IComponent.class);
        when(node.getComponent()).thenReturn(component);
        when(component.getName()).thenReturn("tLogRow");

        List<ModuleNeeded> childrenModules = JavaProcessUtil.getChildrenModules(node, Collections.emptySet(), false);
        assertTrue(childrenModules.isEmpty());
    }

    // @Test
    public void testGetChildrenModules_withChildren() {
        fail("Not impl yet!");
    }

    // @Test
    public void testGetChildrenModules_independent() {
        fail("Not impl yet!");
    }

    // @Test
    public void testGetChildrenModules_dynamicJob() {
        fail("Not impl yet!");
    }

}
