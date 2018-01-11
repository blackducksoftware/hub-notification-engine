/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import static org.junit.Assert.assertEquals;

import java.io.ObjectStreamClass;

import org.json.JSONException;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.relation.DatabaseRelation;

public abstract class RelationTest<R extends DatabaseRelation> implements BaseEntityTest<R> {
    @Override
    @Test
    public void testEmptyEntity() throws JSONException {
        final R configEntity = createMockEmptyRelation();
        assertEquals(entitySerialId(), ObjectStreamClass.lookup(getEntityClass()).getSerialVersionUID());

        final int configHash = configEntity.hashCode();
        assertEquals(emptyEntityHashCode(), configHash);

        final R configEntityNew = createMockEmptyRelation();
        assertEquals(configEntity, configEntityNew);
    }

    @Override
    @Test
    public void testEntity() throws JSONException {
        final Long firstId = 13L;
        final Long secondId = 17L;

        final R configEntity = createMockRelation(firstId, secondId);

        final int configHash = configEntity.hashCode();
        assertEquals(entityHashCode(), configHash);

        final R configEntityNew = createMockRelation(firstId, secondId);
        assertEquals(configEntity, configEntityNew);
    }

    public abstract R createMockRelation(final Long firstId, final Long secondId);

    public abstract R createMockEmptyRelation();

}
