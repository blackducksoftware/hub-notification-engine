package com.synopsys.integration.alert.web.api.home;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.web.security.authentication.saml.SAMLContext;

public class HomActionsTest {
    @Test
    public void testVerifyAuthenticationValid() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(token);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isSuccessful());
        assertFalse(response.hasContent());
    }

    @Test
    public void testVerifyAuthenticationInvalidAnonymousUser() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(token);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn(HomeActions.ROLE_ANONYMOUS);
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());
    }

    @Test
    public void testVerifyAuthenticationInvalidAuthentication() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(token);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.FALSE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());
    }

    @Test
    public void testVerifyAuthenticationInvalidCSRFToken() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(null);
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());
    }

    @Test
    public void testVerifySaml() {
        SAMLContext samlContext = Mockito.mock(SAMLContext.class);
        Mockito.when(samlContext.isSAMLEnabled()).thenReturn(Boolean.TRUE);
        HomeActions actions = new HomeActions(null, samlContext);
        ActionResponse<SAMLEnabledResponseModel> response = actions.verifySaml();
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        SAMLEnabledResponseModel samlEnabledResponseModel = response.getContent().orElse(null);
        assertTrue(samlEnabledResponseModel.getSamlEnabled());
    }
}
