// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.scd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.scd.model.SurrogateCreationType;
import org.talend.designer.scd.model.SurrogateKey;
import org.talend.designer.scd.model.Type3Field;
import org.talend.designer.scd.model.VersionEndType;
import org.talend.designer.scd.model.VersionStartType;
import org.talend.designer.scd.model.Versioning;
import org.talend.designer.scd.ui.ScdSection;

/**
 * Manage the state of ui input and element parameters.
 */
public class ScdManager {

    private static final String GENERATE_COLUMN = "Column generated by scd editor."; //$NON-NLS-1$

    private ScdComponent component;

    private int dialogResponse;

    private Map<String, IElementParameter> paramsMap;

    private List<String> sourceKeys;

    private List<SurrogateKey> surrogateKeys;

    private List<String> type0Table;

    private List<String> type1Table;

    private List<String> type2Table;

    private Versioning versionData;

    private List<Type3Field> type3Table;

    private ScdSection source;

    private List<ScdSection> targets;

    private List<String> unusedFields;

    /**
     * DOC hcw ScdManager constructor comment.
     * 
     * @param scdComponent
     */
    public ScdManager(ScdComponent scdComponent) {
        component = scdComponent;
        targets = new ArrayList<ScdSection>();
    }

    public void setUnusedFieldsSource(ScdSection section) {
        source = section;
    }

    public void addUnusedFieldsTarget(ScdSection section) {
        targets.add(section);
    }

    public void fireFieldChange() {
        if (source == null) {
            return;
        }
        List<String> unusedFields = getInputColumnNames();
        List<String> usedFields = new ArrayList<String>();
        for (ScdSection scd : targets) {
            usedFields.addAll(scd.getUsedFields());
        }
        unusedFields = removeAll(unusedFields, usedFields);
        // update unused field table
        source.onUnusedFieldsChange(unusedFields);
    }

    public List<String> getUnusedFields() {
        List<String> unused = getInputColumnNames();
        return removeAll(unused, getUsedColumns());
    }

    public List<String> getOutputColumnNames() {
        return getColumnNames(getOutputColumns(component));
    }

    public List<String> getInputColumnNames() {
        return getColumnNames(getInputColumns(component));
    }

    private List<String> getColumnNames(List<IMetadataColumn> columns) {
        List<String> names = new ArrayList<String>();
        if (columns != null) {
            for (IMetadataColumn col : columns) {
                names.add(col.getLabel());
            }
        }
        return names;
    }

    public List<IMetadataColumn> getInputColumns(INode node) {
        List<IMetadataColumn> inputSchema = Collections.emptyList();
        List<? extends IConnection> incomingConnections = node.getIncomingConnections();
        if (incomingConnections != null && incomingConnections.size() > 0) {
            for (IConnection incomingConnection : incomingConnections) {
                if (incomingConnection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                    IMetadataTable schemaTable = incomingConnection.getMetadataTable();
                    if (schemaTable != null) {
                        inputSchema = schemaTable.getListColumns();
                    }
                }
            }
        }
        return inputSchema;
    }

    /**
     * DOC hcw Comment method "getOutputColumns".
     * 
     * @param node
     * @return
     */
    public List<IMetadataColumn> getOutputColumns(INode node) {

        List<IMetadataTable> metaDataList = node.getMetadataList();
        List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
        if (metaDataList != null) {
            for (IMetadataTable table : metaDataList) {
                columns.addAll(table.getListColumns());
            }
        }
        return columns;
    }

    /**
     * DOC hcw Comment method "removeAll".
     * 
     * @param unused
     * @param usedColumns
     * @return
     */
    private List<String> removeAll(List<String> unused, List<String> usedColumns) {
        List<String> result = new ArrayList<String>(unused.size());

        Map<String, Boolean> lookup = new HashMap<String, Boolean>();
        for (String col : usedColumns) {
            lookup.put(col, Boolean.TRUE);
        }

        for (String field : unused) {
            if (lookup.get(field) == null) {
                result.add(field);
            }
        }
        return result;
    }

    public int getDialogResponse() {
        return dialogResponse;
    }

    public void setDialogResponse(int dialogResponse) {
        this.dialogResponse = dialogResponse;
    }

    // @SuppressWarnings("unchecked")
    // public IElementParameter createParameter(String name, EParameterFieldType
    // field, Object value) {
    // IElementParameter param = new ElementParameter(null);
    // param.setField(field);
    // param.setCategory(EComponentCategory.BASIC);
    // param.setValue(value);
    // param.setName(name);
    // param.setShow(false);
    // param.setNumRow(1);
    // List list = component.getElementParameters();
    // list.add(param);
    // return param;
    // }

    public void restoreUIData() {
        List<? extends IElementParameter> list = component.getElementParameters();
        initParamsMap(list);

        reloadSurrogateKeyParameter();
        reloadSourceKeyParameter();
        reloadType0Parameter();
        reloadType1Parameter();
        reloadType2Parameter();
        reloadType3Parameter();
    }

    /**
     * DOC hcw Comment method "getUsedColumn".
     * 
     * @return
     */
    private List<String> getUsedColumns() {
        List<String> usedColumns = new ArrayList<String>();
        addAll(usedColumns, sourceKeys, type0Table, type1Table, type2Table);

        usedColumns.addAll(getUsedColumns(versionData));
        usedColumns.addAll(getUsedType3Columns(type3Table));
        usedColumns.addAll(getUsedColumns(surrogateKeys));
        return usedColumns;
    }

    private void addAll(List<String> usedColumns, List<String>... tables) {
        for (List<String> table : tables) {
            if (table != null) {
                usedColumns.addAll(table);
            }
        }
    }

    /**
     * DOC hcw Comment method "getUsedColumns".
     * 
     * @param surrogateKeys2
     * @return
     */
    private Collection<? extends String> getUsedColumns(List<SurrogateKey> keys) {
        if (keys == null) {
            return Collections.EMPTY_LIST;
        } else {
            List<String> columns = new ArrayList<String>();
            for (SurrogateKey key : keys) {
                if (key.getCreation() == SurrogateCreationType.INPUT_FIELD) {
                    if (StringUtils.isNotEmpty(key.getComplement())) {
                        columns.add(key.getComplement());
                    }
                }
                // if (StringUtils.isNotEmpty(key.getColumn())) {
                // columns.add(key.getColumn());
                // }
            }
            return columns;
        }
    }

    /**
     * DOC hcw Comment method "getUsedType3Columns".
     * 
     * @param type3Table2
     * @return
     */
    private Collection<? extends String> getUsedType3Columns(List<Type3Field> type3) {
        if (type3 == null) {
            return Collections.EMPTY_LIST;
        } else {
            List<String> columns = new ArrayList<String>();
            for (Type3Field field : type3) {
                // columns.add(field.getCurrentValue());
                if (StringUtils.isNotEmpty(field.getCurrentValue())) {
                    columns.add(field.getCurrentValue());
                }
            }
            return columns;
        }
    }

    // /**
    // * DOC hcw Comment method "getUsedColumns".
    // *
    // * @param type3Table2
    // * @return
    // */
    // private Collection<? extends String> getUsedColumns(Map<String, String>
    // type3) {
    // if (type3 == null) {
    // return Collections.EMPTY_LIST;
    // } else {
    // List<String> columns = new ArrayList<String>();
    // columns.addAll(type3.keySet());
    // columns.addAll(type3.values());
    // return columns;
    // }
    // }

    /**
     * DOC hcw Comment method "getUsedColumns".
     * 
     * @param versionData2
     * @return
     */
    private Collection<? extends String> getUsedColumns(Versioning ver) {
        if (ver == null) {
            return Collections.EMPTY_LIST;
        } else {
            List<String> columns = new ArrayList<String>();
            // columns.add(ver.getStartName());
            // columns.add(ver.getEndName());
            if (ver.getStartType() == VersionStartType.INPUT_FIELD && StringUtils.isNotEmpty(ver.getStartComplement())) {
                columns.add(ver.getStartComplement());
            }
            // if (ver.isActiveChecked()) {
            // columns.add(ver.getActiveName());
            // }
            // if (ver.isVersionChecked()) {
            // columns.add(ver.getVersionName());
            // }

            return columns;
        }
    }

    public void reloadSourceKeyParameter() {
        IElementParameter param = paramsMap.get(ScdParameterConstants.SOURCE_KEYS_PARAM_NAME);
        List<Map<String, String>> values = (List<Map<String, String>>) param.getValue();
        sourceKeys = convertTableParameterValue(values);
    }

    /**
     * DOC hcw Comment method "convertParameterValue".
     * 
     * @param values
     * @return
     */
    private List<String> convertTableParameterValue(List<Map<String, String>> values) {
        List<String> columns = new ArrayList<String>();
        for (Map<String, String> entry : values) {
            for (String value : entry.values()) {
                if (value != null) {
                    columns.add(value);
                }
            }
        }
        return columns;
    }

    public void saveUIData(List<String> unusedFields, List<String> sourceKeys, List<SurrogateKey> surrogateKeys,
            List<String> type0Table, List<String> type1Table, List<String> type2Table, Versioning versionData,
            List<Type3Field> type3Table) {
        this.unusedFields = unusedFields;
        this.sourceKeys = sourceKeys;
        this.surrogateKeys = surrogateKeys;
        this.type0Table = type0Table;
        this.type1Table = type1Table;
        this.type2Table = type2Table;
        this.versionData = versionData;
        this.type3Table = type3Table;
    }

    public void updateElementParameters() {
        List<? extends IElementParameter> list = component.getElementParameters();
        initParamsMap(list);
        updateSourceKeyParameter(sourceKeys);
        // java component only support one key now
        if (surrogateKeys != null && surrogateKeys.size() > 0) {
            updateSurrogateKeyParameter(surrogateKeys.get(0));
        }
        updateType0Parameter(type0Table);
        updateType1Parameter(type1Table);
        updateType2Parameter(type2Table, versionData);
        updateType3Parameter(type3Table);
        setUpdateComponent(list);
    }

    /**
     * DOC hcw Comment method "initParamsMap".
     * 
     * @param list
     */
    private void initParamsMap(List<? extends IElementParameter> list) {
        paramsMap = new HashMap<String, IElementParameter>();
        for (IElementParameter param : list) {
            paramsMap.put(param.getName(), param);
        }
    }

    private void setUpdateComponent(List<? extends IElementParameter> list) {
        // Updates component parameter
        for (IElementParameter elementParameter : list) {
            if (EParameterName.UPDATE_COMPONENTS.getName().equals(elementParameter.getName())) {
                elementParameter.setValue(Boolean.TRUE);
            }
        }
    }

    // update parameters
    public void updateSourceKeyParameter(List<String> keys) {
        IElementParameter param = paramsMap.get(ScdParameterConstants.SOURCE_KEYS_PARAM_NAME);
        List<Map<String, String>> values = createTableFieldValues(ScdParameterConstants.SOURCE_KEYS_ITEM_NAME, keys);
        param.setValue(values);
    }

    /**
     * DOC hcw Comment method "createTableFieldValues".
     * 
     * @param itemName
     * @param keys
     * @return
     */
    private List<Map<String, String>> createTableFieldValues(String itemName, List<String> keys) {
        List<Map<String, String>> table = new ArrayList<Map<String, String>>();
        if (keys != null) {
            for (String key : keys) {
                Map<String, String> row = new HashMap<String, String>();
                row.put(itemName, key);
                table.add(row);
            }
        }
        return table;
    }

    public void reloadType0Parameter() {
        IElementParameter useL0 = paramsMap.get(ScdParameterConstants.USE_L0);
        if (useL0 != null && useL0.getValue().equals(Boolean.TRUE)) {
            IElementParameter l0FieldsParam = paramsMap.get(ScdParameterConstants.L0_FIELDS_PARAM_NAME);
            List<Map<String, String>> values = (List<Map<String, String>>) l0FieldsParam.getValue();
            type0Table = convertTableParameterValue(values);
        }
    }

    public void reloadType1Parameter() {
        IElementParameter useL1 = paramsMap.get(ScdParameterConstants.USE_L1);
        if (useL1 != null && useL1.getValue().equals(Boolean.TRUE)) {
            IElementParameter l1FieldsParam = paramsMap.get(ScdParameterConstants.L1_FIELDS_PARAM_NAME);
            List<Map<String, String>> values = (List<Map<String, String>>) l1FieldsParam.getValue();
            type1Table = convertTableParameterValue(values);
        }
    }

    public void reloadType2Parameter() {
        IElementParameter useL2 = paramsMap.get(ScdParameterConstants.USE_L2);
        if (useL2 != null && useL2.getValue().equals(Boolean.TRUE)) {
            versionData = new Versioning();
            IElementParameter l2FieldsParam = paramsMap.get(ScdParameterConstants.L2_FIELDS_PARAM_NAME);
            List<Map<String, String>> values = (List<Map<String, String>>) l2FieldsParam.getValue();
            type2Table = convertTableParameterValue(values);

            // start date
            versionData.setStartName(getStringParameter(ScdParameterConstants.L2_STARTDATE_FIELD));

            IElementParameter param = paramsMap.get(ScdParameterConstants.L2_STARTDATE_VALUE);
            if (param != null) {
                versionData.setStartType(VersionStartType.getTypeByValue((String) param.getValue()));

                if (versionData.getStartType() == VersionStartType.INPUT_FIELD) {
                    versionData.setStartComplement(getStringParameter(ScdParameterConstants.L2_STARTDATE_INPUT_FIELD));
                }
            }

            // end date
            versionData.setEndName(getStringParameter(ScdParameterConstants.L2_ENDDATE_FIELD));

            param = paramsMap.get(ScdParameterConstants.L2_ENDDATE_VALUE);
            if (param != null) {
                versionData.setEndType(VersionEndType.getTypeByValue((String) param.getValue()));

                if (versionData.getEndType() == VersionEndType.FIXED_YEAR) {
                    versionData.setEndComplement(getStringParameter(ScdParameterConstants.L2_ENDDATE_FIXED_VALUE));
                }
            }

            // version
            versionData.setVersionChecked(getBooleanParameter(ScdParameterConstants.USE_L2_VERSION));
            if (versionData.isVersionChecked()) {
                versionData.setVersionName(getStringParameter(ScdParameterConstants.L2_VERSION_FIELD));
            }

            // activate
            versionData.setActiveChecked(getBooleanParameter(ScdParameterConstants.USE_L2_ACTIVE));
            if (versionData.isActiveChecked()) {
                versionData.setActiveName(getStringParameter(ScdParameterConstants.L2_ACTIVE_FIELD));
            }

        }
    }

    public void reloadType3Parameter() {
        IElementParameter useL3 = paramsMap.get(ScdParameterConstants.USE_L3);
        if (useL3 != null && useL3.getValue().equals(Boolean.TRUE)) {
            IElementParameter l3FieldsParam = paramsMap.get(ScdParameterConstants.L3_FIELDS_PARAM_NAME);

            type3Table = new ArrayList<Type3Field>();
            List<Map<String, String>> values = (List<Map<String, String>>) l3FieldsParam.getValue();
            for (Map<String, String> entry : values) {
                String current = entry.get(ScdParameterConstants.L3_ITEM_CURRENT_VALUE);
                String previous = entry.get(ScdParameterConstants.L3_ITEM_PREV_VALUE);

                type3Table.add(new Type3Field(current, previous));
            }
        }

    }

    /**
     * DOC hcw Comment method "reloadSurrogateKeyParameter".
     */
    private void reloadSurrogateKeyParameter() {
        surrogateKeys = new ArrayList<SurrogateKey>();
        SurrogateKey key = new SurrogateKey();
        surrogateKeys.add(key);

        key.setColumn(getStringParameter(ScdParameterConstants.SURROGATE_KEY));
        key.setCreation(SurrogateCreationType.getTypeByValue(getStringParameter(ScdParameterConstants.SK_CREATION)));

        if (key.getCreation() == SurrogateCreationType.INPUT_FIELD) {
            key.setComplement(getStringParameter(ScdParameterConstants.SK_INPUT_FIELD));
        } else if (key.getCreation() == SurrogateCreationType.ROUTINE) {
            key.setComplement(getStringParameter(ScdParameterConstants.SK_ROUTINE));
        } else if (key.getCreation() == SurrogateCreationType.DB_SEQUENCE) {
            key.setComplement(getStringParameter(ScdParameterConstants.SK_DB_SEQUENCE));
        }
    }

    private String getStringParameter(String name) {
        IElementParameter param = paramsMap.get(name);
        if (param == null) {
            return null;
        } else {
            return (String) param.getValue();
        }
    }

    private boolean getBooleanParameter(String name) {
        IElementParameter param = paramsMap.get(name);
        return ((Boolean) param.getValue()).booleanValue();
    }

    public void updateType0Parameter(List<String> keys) {
        IElementParameter useL0 = paramsMap.get(ScdParameterConstants.USE_L0);
        if (useL0 == null) {
            return;
        }
        IElementParameter l0FieldsParam = paramsMap.get(ScdParameterConstants.L0_FIELDS_PARAM_NAME);
        if (keys == null || keys.size() == 0) {
            useL0.setValue(Boolean.FALSE);
            l0FieldsParam.setValue(null);
        } else {
            useL0.setValue(Boolean.TRUE);
            List<Map<String, String>> values = createTableFieldValues(ScdParameterConstants.L0_FIELDS_ITEM_NAME, keys);
            l0FieldsParam.setValue(values);
        }
    }

    public void updateType1Parameter(List<String> keys) {
        IElementParameter useL1 = paramsMap.get(ScdParameterConstants.USE_L1);
        IElementParameter l1FieldsParam = paramsMap.get(ScdParameterConstants.L1_FIELDS_PARAM_NAME);
        if (keys == null || keys.size() == 0) {
            useL1.setValue(Boolean.FALSE);
            l1FieldsParam.setValue(null);
        } else {
            useL1.setValue(Boolean.TRUE);
            List<Map<String, String>> values = createTableFieldValues(ScdParameterConstants.L1_FIELDS_ITEM_NAME, keys);
            l1FieldsParam.setValue(values);
        }
    }

    public void updateType2Parameter(List<String> keys, Versioning ver) {
        IElementParameter useL2 = paramsMap.get(ScdParameterConstants.USE_L2);
        IElementParameter l2FieldsParam = paramsMap.get(ScdParameterConstants.L2_FIELDS_PARAM_NAME);
        if (keys == null || keys.size() == 0) {
            useL2.setValue(Boolean.FALSE);
            l2FieldsParam.setValue(null);
        } else {
            useL2.setValue(Boolean.TRUE);
            List<Map<String, String>> values = createTableFieldValues(ScdParameterConstants.L1_FIELDS_ITEM_NAME, keys);
            l2FieldsParam.setValue(values);

            // start date
            updateElementParameter(ScdParameterConstants.L2_STARTDATE_FIELD, ver.getStartName());

            updateElementParameter(ScdParameterConstants.L2_STARTDATE_VALUE, ver.getStartType().getValue());

            if (ver.getStartType() == VersionStartType.INPUT_FIELD) {
                updateElementParameter(ScdParameterConstants.L2_STARTDATE_INPUT_FIELD, ver.getStartComplement());
            }

            // end date
            updateElementParameter(ScdParameterConstants.L2_ENDDATE_FIELD, ver.getEndName());

            updateElementParameter(ScdParameterConstants.L2_ENDDATE_VALUE, ver.getEndType().getValue());

            if (ver.getEndType() == VersionEndType.FIXED_YEAR) {
                updateElementParameter(ScdParameterConstants.L2_ENDDATE_FIXED_VALUE, ver.getEndComplement());
            }

            // version
            updateElementParameter(ScdParameterConstants.USE_L2_VERSION, ver.isVersionChecked());

            if (ver.isVersionChecked()) {
                updateElementParameter(ScdParameterConstants.L2_VERSION_FIELD, ver.getVersionName());
            }

            // activate
            updateElementParameter(ScdParameterConstants.USE_L2_ACTIVE, Boolean.valueOf(ver.isActiveChecked()));

            if (ver.isActiveChecked()) {
                updateElementParameter(ScdParameterConstants.L2_ACTIVE_FIELD, ver.getActiveName());
            }
        }
    }

    public void updateType3Parameter(List<Type3Field> model) {
        IElementParameter useL3 = paramsMap.get(ScdParameterConstants.USE_L3);
        if (useL3 == null) {
            return;
        }
        if (model == null || model.size() == 0) {
            useL3.setValue(Boolean.FALSE);
        } else {
            useL3.setValue(Boolean.TRUE);

            List<Map<String, String>> table = new ArrayList<Map<String, String>>();
            for (Type3Field field : model) {
                Map<String, String> row = new HashMap<String, String>();
                row.put(ScdParameterConstants.L3_ITEM_CURRENT_VALUE, field.getCurrentValue());
                row.put(ScdParameterConstants.L3_ITEM_PREV_VALUE, field.getPreviousValue());
                table.add(row);
            }

            updateElementParameter(ScdParameterConstants.L3_FIELDS_PARAM_NAME, table);
        }
    }

    public void updateSurrogateKeyParameter(SurrogateKey key) {
        if (key == null) {
            return;
        }
        updateElementParameter(ScdParameterConstants.SURROGATE_KEY, key.getColumn());

        updateElementParameter(ScdParameterConstants.SK_CREATION, key.getCreation().getValue());

        if (key.getCreation() == SurrogateCreationType.INPUT_FIELD) {
            updateElementParameter(ScdParameterConstants.SK_INPUT_FIELD, key.getComplement());
        } else if (key.getCreation() == SurrogateCreationType.ROUTINE) {
            updateElementParameter(ScdParameterConstants.SK_ROUTINE, key.getComplement());
        } else if (key.getCreation() == SurrogateCreationType.DB_SEQUENCE) {
            updateElementParameter(ScdParameterConstants.SK_DB_SEQUENCE, key.getComplement());
        }

    }

    public void updateElementParameter(String name, Object value) {
        IElementParameter param = paramsMap.get(name);
        if (param != null) {
            param.setValue(value);
        }
    }

    public List<String> getSourceKeys() {
        return sourceKeys;
    }

    public List<SurrogateKey> getSurrogateKeys() {
        return surrogateKeys;
    }

    public List<String> getType0Table() {
        return type0Table;
    }

    public List<String> getType1Table() {
        return type1Table;
    }

    public List<String> getType2Table() {
        return type2Table;
    }

    public Versioning getVersionData() {
        return versionData;
    }

    public List<Type3Field> getType3Table() {
        return type3Table;
    }

    public ScdComponent getComponent() {
        return component;
    }

    public String[] getSurrogateCreationTypeNames() {
        List<String> allTypeNames = new ArrayList<String>();
        IElementParameter param = getComponent().getElementParameter(EParameterName.SK_CREATION.getName());
        for (SurrogateCreationType type : SurrogateCreationType.values()) {
            if (param != null) {
                if (ArrayUtils.contains(param.getListItemsDisplayCodeName(), type.name())) {
                    allTypeNames.add(type.getName());
                }
            } else
                allTypeNames.add(type.getName());
        }
        return allTypeNames.toArray(new String[0]);
    }

    public boolean enableOracle() {
        IElementParameter param = getComponent().getElementParameter(EParameterName.PROPERTY_TYPE.getName());
        if (param != null && param.getRepositoryValue() != null) {
            String value = param.getRepositoryValue().toLowerCase();
            if (value.endsWith("oracle") || value.endsWith("ingres")) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOC chuang Comment method "removeGeneratedColumns".
     * 
     * @param metadataList
     * @param unusedFields
     * @return
     * @return
     */
    private Map<String, IMetadataColumn> removeUnusedAndGeneratedColumns(List<IMetadataTable> metadataList,
            List<String> unusedFields) {
        Map<String, IMetadataColumn> columnsMap = new HashMap<String, IMetadataColumn>();
        if (metadataList != null) {
            for (IMetadataTable table : metadataList) {
                List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>(table.getListColumns());
                // remove from backend
                for (int i = columns.size() - 1; i >= 0; i--) {
                    IMetadataColumn col = columns.get(i);
                    if (unusedFields.contains(col.getLabel()) || GENERATE_COLUMN.equals(col.getComment())) {
                        // remove unused field or generated field from schema
                        table.getListColumns().remove(i);
                    } else {
                        columnsMap.put(col.getLabel(), col);
                    }
                }
            }
        }
        return columnsMap;
    }

    /**
     * DOC chuang Comment method "cloneColumn".
     * 
     * @param column
     * @param label
     * @return
     */
    private IMetadataColumn cloneColumn(IMetadataColumn column, String label) {
        IMetadataColumn clone = column.clone();

        clone.setLabel(label);
        clone.setOriginalDbColumnName(label);
        clone.setComment(GENERATE_COLUMN);
        clone.setKey(false);
        return clone;
    }

    /**
     * DOC chuang Comment method "createOutputSchema".
     * 
     * @param unusedFields
     */
    public void createOutputSchema() {
        // the unused columns must not be present in the output schema and also
        // remove all generated column from
        // output schema
        Map<String, IMetadataColumn> outputColumns = removeUnusedAndGeneratedColumns(component.getMetadataList(), unusedFields);

        IMetadataTable schema = component.getMetadataList().get(0);

        // adding all used columns to output schema
        Map<String, IMetadataColumn> inputColumnsMap = new HashMap<String, IMetadataColumn>();
        List<IMetadataColumn> inputColumns = getInputColumns(component);
        for (IMetadataColumn column : inputColumns) {
            inputColumnsMap.put(column.getLabel(), column);
            if (!unusedFields.contains(column.getLabel()) && outputColumns.get(column.getLabel()) == null) {
                schema.getListColumns().add(cloneColumn(column, column.getLabel()));
            }
        }

        // on TYPE 3 columns, every "previous value" must create an output
        // column with the same attribute as the
        // "current value" column
        if (type3Table != null) {
            for (Type3Field type3 : type3Table) {
                if (outputColumns.get(type3.getPreviousValue()) != null || StringUtils.isEmpty(type3.getPreviousValue())) {
                    continue;
                }
                IMetadataColumn column = inputColumnsMap.get(type3.getCurrentValue());
                IMetadataColumn previous = cloneColumn(column, type3.getPreviousValue());
                outputColumns.put(previous.getLabel(), previous);
                schema.getListColumns().add(previous);
            }
        }

        // removeVersionFieldsFromOutput(outputColumns, schema, new String[] {
        // versionData.getStartName(),
        // versionData.getEndName(),
        // versionData.getVersionName(), versionData.getActiveName() });
        // for versionning columns {start, end} are datetime data type, version
        // is an integer, active is a
        // boolean
        ECodeLanguage lang = LanguageManager.getCurrentLanguage();
        if (type2Table != null && !type2Table.isEmpty()) {
            // start date
            createMetadataColumn(outputColumns, schema, versionData.getStartName(), Date.class, lang);
            // end date
            IMetadataColumn endColumn = createMetadataColumn(outputColumns, schema, versionData.getEndName(), Date.class, lang);
            if (endColumn != null) {
                endColumn.setNullable(true);
            }
            if (versionData.isVersionChecked()) {
                createMetadataColumn(outputColumns, schema, versionData.getVersionName(), Integer.class, lang);
            }
            if (versionData.isActiveChecked()) {
                createMetadataColumn(outputColumns, schema, versionData.getActiveName(), Boolean.class, lang);
            }
        }
        fixKeyColumnsInOutputSchema(schema, inputColumnsMap, lang);
        // sort column by name
        Collections.sort(schema.getListColumns(), new Comparator<IMetadataColumn>() {

            public int compare(IMetadataColumn o1, IMetadataColumn o2) {

                return o1.getLabel().compareTo(o2.getLabel());
            }

        });

    }

    /**
     * DOC chuang Comment method "fixKeyColumnsInOutputSchema".
     * 
     * @param schema
     * @param inputColumnsMap
     * @param lang
     */
    private void fixKeyColumnsInOutputSchema(IMetadataTable schema, Map<String, IMetadataColumn> inputColumnsMap,
            ECodeLanguage lang) {
        Map<String, IMetadataColumn> columnsMap = new HashMap<String, IMetadataColumn>();
        for (IMetadataColumn column : schema.getListColumns()) {
            columnsMap.put(column.getLabel(), column);
        }

        // all source keys are not keys in the output schema
        if (sourceKeys != null && !sourceKeys.isEmpty()) {
            for (String key : sourceKeys) {
                IMetadataColumn column = columnsMap.get(key);
                if (column != null && GENERATE_COLUMN.equals(column.getComment())) {
                    column.setKey(false);
                }
            }
        }

        if (surrogateKeys != null && !surrogateKeys.isEmpty()) {
            for (SurrogateKey key : surrogateKeys) {
                if (StringUtils.isEmpty(key.getColumn())) {
                    // name is missing
                    continue;
                }
                IMetadataColumn column = columnsMap.get(key.getColumn());
                if (column == null) {
                    // if not exist in output schema, create surrogate key
                    if (key.getCreation() == SurrogateCreationType.INPUT_FIELD) {
                        IMetadataColumn inputCol = inputColumnsMap.get(key.getComplement());
                        if (inputCol != null) {
                            column = inputCol.clone();
                            column.setLabel(key.getColumn());
                            column.setOriginalDbColumnName(key.getColumn());
                            column.setComment(GENERATE_COLUMN);
                            column.setKey(true); // set as key in output schema
                            schema.getListColumns().add(column);
                        }
                    } else {
                        column = createMetadataColumn(columnsMap, schema, key.getColumn(), Integer.class, lang);
                        column.setKey(true); // set as key in output schema
                        if (key.getCreation() == SurrogateCreationType.ROUTINE) {
                            // routine is treated as string now
                            column.setTalendType(getType(String.class, lang));
                        } else if (key.getCreation() == SurrogateCreationType.DB_SEQUENCE) {
                            column.setTalendType(getType(String.class, lang)); // /
                        }
                    }
                }
            }
        }

    }

    /**
     * DOC chuang Comment method "removeVersionFieldsFromOutput".
     * 
     * @param outputColumns
     * @param schema
     * @param strings
     */
    private void removeVersionFieldsFromOutput(Map<String, IMetadataColumn> outputColumns, IMetadataTable schema, String[] fields) {
        for (String field : fields) {
            if (field != null) {
                IMetadataColumn column = outputColumns.get(field);
                if (column != null) {
                    schema.getListColumns().remove(column);
                }
            }
        }
    }

    /**
     * DOC chuang Comment method "createMetadataColumn".
     * 
     * @param outputColumns
     * 
     * @param schema
     * 
     * @param lang
     * @return
     */
    private IMetadataColumn createMetadataColumn(Map<String, IMetadataColumn> outputColumns, IMetadataTable schema, String name,
            Class clazz, ECodeLanguage lang) {
        if (outputColumns.get(name) != null) {
            return null;
        }
        IMetadataColumn column = new MetadataColumn();
        column.setLabel(name);
        column.setOriginalDbColumnName(name);
        column.setTalendType(getType(clazz, lang));
        column.setComment(GENERATE_COLUMN);
        schema.getListColumns().add(column);
        outputColumns.put(column.getLabel(), column);
        return column;
    }

    private String getType(Class clazz, ECodeLanguage lang) {
        switch (lang) {
        case JAVA:
            if (clazz.equals(Date.class)) {
                return JavaTypesManager.DATE.getId();
            } else if (clazz.equals(Integer.class)) {
                return JavaTypesManager.INTEGER.getId();
            } else if (clazz.equals(Boolean.class)) {
                return JavaTypesManager.BOOLEAN.getId();
            } else if (clazz.equals(String.class)) {
                return JavaTypesManager.STRING.getId();
            }
            break;
        case PERL:
            // see MetadataTalendType.PERL_TYPES
            if (clazz.equals(Date.class)) {
                return "datetime"; //$NON-NLS-1$
            } else if (clazz.equals(Integer.class)) {
                return "int"; //$NON-NLS-1$
            } else if (clazz.equals(Boolean.class)) {
                return "boolean"; //$NON-NLS-1$
            } else if (clazz.equals(String.class)) {
                return "string"; //$NON-NLS-1$
            }
            break;
        }
        return null;
    }
}
