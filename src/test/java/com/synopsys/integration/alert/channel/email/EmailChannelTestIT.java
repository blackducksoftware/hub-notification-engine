package com.synopsys.integration.alert.channel.email;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.email.mock.MockEmailEntity;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.common.digest.model.DigestModel;
import com.synopsys.integration.alert.common.digest.model.ProjectData;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserGroupRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserGroupRelationRepositoryAccessor;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.test.annotation.ExternalConnectionTest;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendEmailTest() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalBlackDuckRepository globalRepository = Mockito.mock(GlobalBlackDuckRepository.class);

        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(globalRepository, testAlertProperties);
        globalProperties.setBlackDuckUrl(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final BlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final UserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor = new MockUserGroupRelationRepositoryAccessor();

        final BlackDuckUserEntity userEntity = (BlackDuckUserEntity) blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity("noreply@blackducksoftware.com", false));
        final BlackDuckGroupEntity groupEntity = (BlackDuckGroupEntity) blackDuckGroupRepositoryAccessor.saveEntity(new BlackDuckGroupEntity("IntegrationTest", true, "Href"));
        userGroupRelationRepositoryAccessor.addUserGroupRelation(userEntity.getId(), groupEntity.getId());

        EmailGroupChannel emailChannel = new EmailGroupChannel(gson, testAlertProperties, globalProperties, auditEntryRepository, null, null, null, blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor,
            userGroupRelationRepositoryAccessor);
        final Collection<ProjectData> projectData = createProjectData("Manual test project");
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
            notificationContent.getContent(), 1L, 1L);

        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final String smtpUser = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER);
        final String smtpPassword = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD);
        final Boolean smtpEhlo = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        final Boolean smtpAuth = Boolean.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        final Integer smtpPort = Integer.valueOf(properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final EmailGlobalConfigEntity emailGlobalConfigEntity = new EmailGlobalConfigEntity(smtpHost, smtpUser, smtpPassword, smtpPort, null, null, null, smtpFrom, null, null, null, smtpEhlo, smtpAuth, null, null, null, null, null, null,
            null,
            null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        emailChannel = Mockito.spy(emailChannel);
        Mockito.doReturn(emailGlobalConfigEntity).when(emailChannel).getGlobalConfigEntity();

        final MockEmailEntity mockEmailEntity = new MockEmailEntity();
        mockEmailEntity.setGroupName(properties.getProperty(TestPropertyKey.TEST_EMAIL_GROUP));
        emailChannel.sendAuditedMessage(event, mockEmailEntity.createEntity());
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        try (final OutputLogger outputLogger = new OutputLogger()) {
            final EmailGroupChannel emailChannel = new EmailGroupChannel(gson, null, null, null, null, null, null, null, null, null);
            final DigestModel digestModel = new DigestModel(null);
            final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
            final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), 1L, 1L);
            emailChannel.sendMessage(event, null);
            assertTrue(outputLogger.isLineContainingText("No configuration found with id"));
        }
    }

}
