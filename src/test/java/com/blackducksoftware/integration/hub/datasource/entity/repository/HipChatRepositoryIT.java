package com.blackducksoftware.integration.hub.datasource.entity.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.GlobalHipChatRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HipChatRepositoryIT {

    @Autowired
    private GlobalHipChatRepository repository;

    @Test
    public void testSaveEntity() {
        final String apiKey = "api_key";
        final Integer roomId = 1;
        final Boolean notify = true;
        final String color = "green";
        final GlobalHipChatConfigEntity entity = new GlobalHipChatConfigEntity(apiKey, roomId, notify, color);
        final GlobalHipChatConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final GlobalHipChatConfigEntity foundEntity = repository.findOne(savedEntity.getId());
        assertEquals(apiKey, foundEntity.getApiKey());
        assertEquals(roomId, foundEntity.getRoomId());
        assertEquals(notify, foundEntity.getNotify());
        assertEquals(color, foundEntity.getColor());
    }
}
