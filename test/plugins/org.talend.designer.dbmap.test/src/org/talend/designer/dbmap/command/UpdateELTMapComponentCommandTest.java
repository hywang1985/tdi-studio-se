package org.talend.designer.dbmap.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;

public class UpdateELTMapComponentCommandTest {

    private static final String SOURCE_COMPONENT_NAME = "tELTPostgresqlInput";

    private static final String TARGET_COMPONENT_NAME = "tELTPostgresqlMap";

    private static IComponent sourceComponent;

    private static IComponent targetComponent;

    private Node sourceNode;

    private Node targetNode;

    private IConnection connection;

    private List<ExternalDbMapTable> inputs;

    private List<ExternalDbMapTable> outputs;

    @BeforeClass
    public static void beforeClass() {
        sourceComponent = ComponentsFactoryProvider.getInstance().get(SOURCE_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
        targetComponent = ComponentsFactoryProvider.getInstance().get(TARGET_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
    }

    @Before
    public void before() {
        Process process = new Process(createProperty());
        
        sourceNode = new Node(sourceComponent, process);
        INodeConnector connector = new NodeConnector(sourceNode);
        connector.setName("connector");
        connector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
        ArrayList<INodeConnector> connectors = new ArrayList<INodeConnector>();
        connectors.add(connector);
        sourceNode.setListConnector(connectors);
        
        targetNode = new Node(targetComponent, process);
        DbMapComponent component = new DbMapComponent();
        targetNode.setExternalNode(component);
        connection = new Connection(sourceNode, targetNode, EConnectionType.FLOW_MAIN, "connector", "meta", "oldTable", true);

        inputs = (List<ExternalDbMapTable>) targetNode.getExternalData().getInputTables();
        ExternalDbMapTable input = new ExternalDbMapTable();
        input.setName("oldTable");
        input.setTableName("oldTable");
        inputs.add(input);
        
        outputs = (List<ExternalDbMapTable>) targetNode.getExternalData().getOutputTables();
        ExternalDbMapTable output = new ExternalDbMapTable();
        ExternalDbMapEntry entry = new ExternalDbMapEntry("oldTable", "oldTable.column");
        output.setMetadataTableEntries(new ArrayList<ExternalDbMapEntry>());
        output.getMetadataTableEntries().add(entry);
        outputs.add(output);
    }

    @Test
    public void testUpdateELTMapComponentCommand() throws InterruptedException {
        UpdateELTMapComponentCommand command = new UpdateELTMapComponentCommand(targetNode, connection, "oldTable", "newTable");
        command.execute();
        validateResult("newTable");
        command.undo();
        validateResult("oldTable");
        command.redo();
        validateResult("newTable");
    }

    private void validateResult(String value) {
        assertEquals(value, connection.getName());
        assertEquals(value, inputs.get(0).getName());
        assertEquals(value, inputs.get(0).getTableName());
        assertEquals(value + ".column", outputs.get(0).getMetadataTableEntries().get(0).getExpression());
    }

    private static Property createProperty() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        return property;
    }
}
