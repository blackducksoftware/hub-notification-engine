package com.synopsys.integration.alert.performance.model;

import java.util.UUID;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;

public class BlackDuckPerformanceProperties {
    private String blackDuckProviderKey;
    private String blackDuckProviderUrl;
    private String blackDuckApiToken;
    private String blackDuckTimeout;
    private String blackDuckProviderUniqueName;
    private String blackDuckProjectName;
    private String blackDuckProjectVersion;

    public BlackDuckPerformanceProperties() {
        this.blackDuckProviderKey = new BlackDuckProviderKey().getUniversalKey();

        TestProperties testProperties = new TestProperties();
        this.blackDuckProviderUrl = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL);
        this.blackDuckApiToken = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY);
        this.blackDuckTimeout = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT);
        this.blackDuckProviderUniqueName = blackDuckProviderUrl + UUID.randomUUID();
        this.blackDuckProjectName = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PROJECT_NAME);
        this.blackDuckProjectVersion = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PROJECT_VERSION);

    }

    public String getBlackDuckProviderKey() {
        return blackDuckProviderKey;
    }

    public String getBlackDuckProviderUrl() {
        return blackDuckProviderUrl;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public String getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public String getBlackDuckProviderUniqueName() {
        return blackDuckProviderUniqueName;
    }

    public String getBlackDuckProjectName() {
        return blackDuckProjectName;
    }

    public String getBlackDuckProjectVersion() {
        return blackDuckProjectVersion;
    }
}
