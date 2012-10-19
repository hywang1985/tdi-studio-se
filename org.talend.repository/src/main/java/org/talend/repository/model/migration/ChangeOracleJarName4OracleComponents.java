// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeOracleJarName4OracleComponents extends
		AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        String[] oracleCompNames = {"tOracleBulkExec","tOracleClose","tOracleCommit","tOracleConnection","tOracleInput",
        		"tOracleOutput","tOracleOutputBulk","tOracleOutputBulkExec","tOracleRollback","tOracleRow","tOracleSCD","tOracleSCDELT",
        		"tOracleSP","tOracleTableList","tAmazonOracleClose","tAmazonOracleCommit","tAmazonOracleConnection","tAmazonOracleInput",
        		"tAmazonOracleOutput","tAmazonOracleRollback","tAmazonOracleRow","tMondrianInput","tCreateTable","tOracleInvalidRows",
        		"tOracleValidRows","tELTOracleMap","tOracleCDC","tOracleSCDELT"}; //$NON-NLS-1$
        	
        	
    	IComponentConversion changeOracleDriverJarType = new IComponentConversion() {

	        public void transform(NodeType node) {
	        	ElementParameterType db_version = ComponentUtilities.getNodeProperty(node, "DB_VERSION"); //$NON-NLS-2$
	        	if (db_version != null) {
	        		String jar_value = db_version.getValue(); //$NON-NLS-3$
	        		if ("ojdbc6-11g.jar".equalsIgnoreCase(jar_value)) {
	        			db_version.setValue("ojdbc6.jar");
	        		} else if ("ojdbc5-11g.jar".equalsIgnoreCase(jar_value)) {
	        			db_version.setValue("ojdbc5.jar");
	        		} else if ("ojdbc14-10g.jar".equalsIgnoreCase(jar_value)) {
	        			db_version.setValue("ojdbc14.jar");
	        		} else if ("ojdbc14-9i.jar".equalsIgnoreCase(jar_value)) {
	//                			db_version.setValue("ojdbc14-9i.jar");
	        		} else if ("ojdbc12-8i.jar".equalsIgnoreCase(jar_value)) {
	        			db_version.setValue("ojdbc12.jar");
	        		}
	        	}
	        }
        
    	};
    	
    	for (String name : oracleCompNames) {
            IComponentFilter filter = new NameComponentFilter(name); //$NON-NLS-4$

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(changeOracleDriverJarType));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 10, 15, 10, 0, 0);
        return gc.getTime();
    }
}
