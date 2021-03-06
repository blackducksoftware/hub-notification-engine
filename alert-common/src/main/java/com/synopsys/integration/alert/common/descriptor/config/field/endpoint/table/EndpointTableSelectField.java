/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointField;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

public class EndpointTableSelectField extends EndpointField {
    private boolean paged;
    private boolean searchable;
    private boolean useRowAsValue;
    private final List<TableSelectColumn> columns;

    public EndpointTableSelectField(String key, String label, String description) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT, "Select", AbstractFunctionController.API_FUNCTION_URL);
        this.paged = false;
        this.searchable = true;
        this.useRowAsValue = false;
        this.columns = new LinkedList<>();
    }

    public EndpointTableSelectField applyPaged(boolean paged) {
        this.paged = paged;
        return this;
    }

    public EndpointTableSelectField applySearchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public EndpointTableSelectField applyUseRowAsValue(boolean useRowAsValue) {
        this.useRowAsValue = useRowAsValue;
        return this;
    }

    public EndpointTableSelectField applyColumns(List<TableSelectColumn> columns) {
        if (columns != null) {
            this.columns.addAll(columns);
        }
        return this;
    }

    public EndpointTableSelectField applyColumn(TableSelectColumn tableSelectColumn) {
        if (null != tableSelectColumn) {
            columns.add(tableSelectColumn);
        }
        return this;
    }

    public boolean isPaged() {
        return paged;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public boolean isUseRowAsValue() {
        return useRowAsValue;
    }

    public List<TableSelectColumn> getColumns() {
        return columns;
    }

}
