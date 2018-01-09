package com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global;

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
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepository;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class GlobalEmailRepositoryIT {

    @Autowired
    private GlobalEmailRepository repository;

    @Test
    public void testSaveEntity() {
        final String mailSmtpHost = "smtp_host";
        final String mailSmtpUser = "smtp_user";
        final String mailSmtpPassword = "smtp_password";
        final Integer mailSmtpPort = 88;
        final Integer mailSmtpConnectionTimeout = 100;
        final Integer mailSmtpTimeout = 200;
        final String mailSmtpFrom = "smtp_From";
        final String mailSmtpLocalhost = "smtp_localhost";
        final Boolean mailSmtpEhlo = true;
        final Boolean mailSmtpAuth = true;
        final String mailSmtpDnsNotify = "smtp_dns_notify";
        final String mailSmtpDnsRet = "smtp_dsn_ret";
        final Boolean mailSmtpAllow8bitmime = true;
        final Boolean mailSmtpSendPartial = true;
        final String emailTemplateDirectory = "template_directory_path";
        final String emailTemplateLogoImage = "logo_image_path";
        final String emailSubjectLine = "email_subject_line";
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom, mailSmtpLocalhost, mailSmtpEhlo, mailSmtpAuth,
                mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial, emailTemplateDirectory, emailTemplateLogoImage, emailSubjectLine);
        final GlobalEmailConfigEntity savedEntity = repository.save(entity);
        final long count = repository.count();
        assertEquals(1, count);
        final GlobalEmailConfigEntity foundEntity = repository.findOne(savedEntity.getId());
        assertEquals(mailSmtpHost, foundEntity.getMailSmtpHost());
        assertEquals(mailSmtpUser, foundEntity.getMailSmtpUser());
        assertEquals(mailSmtpPassword, foundEntity.getMailSmtpPassword());
        assertEquals(mailSmtpPort, foundEntity.getMailSmtpPort());
        assertEquals(mailSmtpConnectionTimeout, foundEntity.getMailSmtpConnectionTimeout());
        assertEquals(mailSmtpTimeout, foundEntity.getMailSmtpTimeout());
        assertEquals(mailSmtpFrom, foundEntity.getMailSmtpFrom());
        assertEquals(mailSmtpLocalhost, foundEntity.getMailSmtpLocalhost());
        assertEquals(mailSmtpEhlo, foundEntity.getMailSmtpEhlo());
        assertEquals(mailSmtpAuth, foundEntity.getMailSmtpAuth());
        assertEquals(mailSmtpDnsNotify, foundEntity.getMailSmtpDnsNotify());
        assertEquals(mailSmtpDnsRet, foundEntity.getMailSmtpDnsRet());
        assertEquals(mailSmtpAllow8bitmime, foundEntity.getMailSmtpAllow8bitmime());
        assertEquals(mailSmtpSendPartial, foundEntity.getMailSmtpSendPartial());
        assertEquals(emailTemplateDirectory, foundEntity.getEmailTemplateDirectory());
        assertEquals(emailTemplateLogoImage, foundEntity.getEmailTemplateLogoImage());
        assertEquals(emailSubjectLine, foundEntity.getEmailSubjectLine());
    }
}
