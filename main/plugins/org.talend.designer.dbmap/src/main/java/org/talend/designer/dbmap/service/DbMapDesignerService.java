package org.talend.designer.dbmap.service;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.designer.core.IDbMapDesignerService;
import org.talend.designer.dbmap.DbMapComponent;
import org.talend.designer.dbmap.command.UpdateELTMapComponentCommand;
import org.talend.designer.dbmap.external.data.ExternalDbMapEntry;
import org.talend.designer.dbmap.external.data.ExternalDbMapTable;

public class DbMapDesignerService implements IDbMapDesignerService {

    @Override
    public Command getUpdateELTMapComponentCommand(INode targetNode, IConnection connection, String oldConnectionName,
            String newConnectionName) {
        return new UpdateELTMapComponentCommand(targetNode, connection, oldConnectionName, newConnectionName);
    }
}
