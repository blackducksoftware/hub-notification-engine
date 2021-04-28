import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Redirect, Route, withRouter } from 'react-router-dom';
import Navigation from 'Navigation';
import AboutInfo from 'component/AboutInfo';
import DistributionConfiguration from 'distribution/Index';
import LogoutConfirmation from 'component/common/LogoutConfirmation';
import { getDescriptors } from 'store/actions/descriptors';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import SlackGlobalConfiguration from 'global/channels/slack/SlackGlobalConfiguration';
import EmailGlobalConfiguration from 'global/channels/email/EmailGlobalConfiguration';
import JiraCloudGlobalConfiguration from 'global/channels/jira/cloud/JiraCloudGlobalConfiguration';
import { JIRA_CLOUD_INFO } from 'global/channels/jira/cloud/JiraCloudModel';
import { SLACK_INFO } from 'global/channels/slack/SlackModels';
import { EMAIL_INFO } from 'global/channels/email/EmailModels';
import { JIRA_SERVER_INFO } from 'global/channels/jira/server/JiraServerModel';
import { MSTEAMS_INFO } from 'global/channels/msteams/MSTeamsModel';
import MSTeamsGlobalConfiguration from 'global/channels/msteams/MSTeamsGlobalConfiguration';
import { AZURE_INFO } from 'global/channels/azure/AzureModel';
import AzureGlobalConfiguration from 'global/channels/azure/AzureGlobalConfiguration';
import { SCHEDULING_INFO } from 'global/components/scheduling/SchedulingModel';
import SchedulingConfiguration from 'global/components/scheduling/SchedulingConfiguration';
import { SETTINGS_INFO } from 'global/components/settings/SettingsModel';
import SettingsConfiguration from 'global/components/settings/SettingsConfiguration';
import { AUTHENTICATION_INFO } from 'global/components/auth/AuthenticationModel';
import AuthenticationConfiguration from 'global/components/auth/AuthenticationConfiguration';
import { BLACKDUCK_INFO, BLACKDUCK_URLS } from 'global/providers/blackduck/BlackDuckModel';
import BlackDuckProviderConfiguration from 'global/providers/blackduck/BlackDuckProviderConfiguration';
import BlackDuckConfiguration from 'global/providers/blackduck/BlackDuckConfiguration';
import { AUDIT_INFO } from 'global/components/audit/AuditModel';
import AuditPage from 'dynamic/loaded/audit/AuditPage';
import { CERTIFICATE_INFO } from 'global/components/certificates/CertificateModel';
import CertificatesPage from 'dynamic/loaded/certificates/CertificatesPage';
import { TASK_MANAGEMENT_INFO } from 'global/components/task/TaskManagementModel';
import TaskManagement from 'dynamic/loaded/tasks/TaskManagement';
import { USER_MANAGEMENT_INFO } from 'global/components/user/UserModel';
import UserManagement from 'dynamic/loaded/users/UserManagement';
import JiraServerGlobalConfiguration from "./global/channels/jira/server/JiraServerGlobalConfiguration";

const MainPage = ({
    descriptors, fetching, getDescriptorsRedux, csrfToken, autoRefresh
}) => {
    const [descriptorMap, setDescriptorMap] = useState({});

    useEffect(() => {
        getDescriptorsRedux();
    }, []);

    useEffect(() => {
        const newDescriptorMap = {};
        descriptors.forEach((descriptor) => {
            newDescriptorMap[descriptor.name] = descriptor;
        });
        setDescriptorMap(newDescriptorMap);
    }, [descriptors]);

    const createRoute = (uriPrefix, urlName, component) => (
        <Route
            exact
            key={urlName}
            path={`${uriPrefix}${urlName}`}
        >
            {component}
        </Route>
    );

    const doesDescriptorExist = (key) => Object.prototype.hasOwnProperty.call(descriptorMap, key);

    const providerUri = '/alert/providers/';
    const channelUri = '/alert/channels/';
    const componentUri = '/alert/components/';

    const page = (
        <div className="contentArea">
            <Route
                exact
                path="/alert/"
                render={() => (
                    <Redirect to="/alert/general/about" />
                )}
            />
            <Route
                exact
                key="blackduck-route"
                path={[`${BLACKDUCK_URLS.blackDuckConfigUrl}/:id?`, `${BLACKDUCK_URLS.blackDuckConfigCopyUrl}/:id?`]}
            >
                <BlackDuckConfiguration csrfToken={csrfToken} readonly={false} />
            </Route>
            {doesDescriptorExist(BLACKDUCK_INFO.key) && createRoute(providerUri, BLACKDUCK_INFO.url, <BlackDuckProviderConfiguration csrfToken={csrfToken} showRefreshButton={!autoRefresh} />)}
            {doesDescriptorExist(AZURE_INFO.key) && createRoute(channelUri, AZURE_INFO.url, <AzureGlobalConfiguration csrfToken={csrfToken} readonly={descriptorMap[AZURE_INFO.key].readonly} />)}
            {doesDescriptorExist(EMAIL_INFO.key) && createRoute(channelUri, EMAIL_INFO.url, <EmailGlobalConfiguration csrfToken={csrfToken} readonly={descriptorMap[EMAIL_INFO.key].readonly} />)}
            {doesDescriptorExist(JIRA_CLOUD_INFO.key) && createRoute(channelUri, JIRA_CLOUD_INFO.url, <JiraCloudGlobalConfiguration csrfToken={csrfToken} readonly={descriptorMap[JIRA_CLOUD_INFO.key].readonly} />)}
            {doesDescriptorExist(JIRA_SERVER_INFO.key) && createRoute(channelUri, JIRA_SERVER_INFO.url, <JiraServerGlobalConfiguration csrfToken={csrfToken} readonly={descriptorMap[JIRA_SERVER_INFO.key].readonly} />)}
            {doesDescriptorExist(MSTEAMS_INFO.key) && createRoute(channelUri, MSTEAMS_INFO.url, <MSTeamsGlobalConfiguration />)}
            {doesDescriptorExist(SLACK_INFO.key) && createRoute(channelUri, SLACK_INFO.url, <SlackGlobalConfiguration />)}
            <Route exact path="/alert/jobs/distribution" component={DistributionConfiguration} />
            {doesDescriptorExist(AUDIT_INFO.key) && createRoute(componentUri, AUDIT_INFO.url, <AuditPage />)}
            {doesDescriptorExist(AUTHENTICATION_INFO.key) && createRoute(componentUri, AUTHENTICATION_INFO.url, <AuthenticationConfiguration csrfToken={csrfToken} readonly={descriptorMap[AUTHENTICATION_INFO.key].readonly} />)}
            {doesDescriptorExist(CERTIFICATE_INFO.key) && createRoute(componentUri, CERTIFICATE_INFO.url, <CertificatesPage />)}
            {doesDescriptorExist(SCHEDULING_INFO.key) && createRoute(componentUri, SCHEDULING_INFO.url, <SchedulingConfiguration csrfToken={csrfToken} readonly={descriptorMap[SCHEDULING_INFO.key].readonly} />)}
            {doesDescriptorExist(SETTINGS_INFO.key) && createRoute(componentUri, SETTINGS_INFO.url, <SettingsConfiguration csrfToken={csrfToken} readonly={descriptorMap[SETTINGS_INFO.key].readonly} />)}
            {doesDescriptorExist(TASK_MANAGEMENT_INFO.key) && createRoute(componentUri, TASK_MANAGEMENT_INFO.url, <TaskManagement />)}
            {doesDescriptorExist(USER_MANAGEMENT_INFO.key) && createRoute(componentUri, USER_MANAGEMENT_INFO.url, <UserManagement />)}
            <Route exact path="/alert/general/about" component={AboutInfo} />
        </div>
    );

    const spinner = (
        <div>
            <div className="loginContainer">
                <div className="progressIcon">
                    <FontAwesomeIcon icon="spinner" className="alert-icon" size="5x" spin />
                </div>
            </div>
        </div>
    );

    const content = (fetching) ? spinner : page;

    return (
        <div>
            <Navigation descriptorMap={descriptorMap} />
            {content}
            <div className="modalsArea">
                <LogoutConfirmation />
            </div>
        </div>
    );
};

MainPage.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired,
    fetching: PropTypes.bool.isRequired,
    getDescriptorsRedux: PropTypes.func.isRequired,
    csrfToken: PropTypes.string.isRequired,
    autoRefresh: PropTypes.bool.isRequired
};

const mapStateToProps = (state) => ({
    descriptors: state.descriptors.items,
    fetching: state.descriptors.fetching,
    csrfToken: state.session.csrfToken,
    autoRefresh: state.refresh.autoRefresh
});

const mapDispatchToProps = (dispatch) => ({
    getDescriptorsRedux: () => dispatch(getDescriptors())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(MainPage));
