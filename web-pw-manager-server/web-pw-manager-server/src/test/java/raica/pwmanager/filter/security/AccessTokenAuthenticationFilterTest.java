package raica.pwmanager.filter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.exception.AuthorizationFieldEmptyException;
import raica.pwmanager.util.JWTUtil;
import raica.pwmanager.util.LogUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccessTokenAuthenticationFilterTest {

    @Mock
    private LogUtil mockLogUtil;

    @Mock
    private JWTUtil mockJWTUtil;

    @InjectMocks
    @Spy
    private AccessTokenAuthenticationFilter spyAccessTokenAuthenticationFilter; //待測元件


    @Test
    void GivenUserDetailWhichActivatedIsTrueAndEnabledIsTrue_WhenVerifyUserStatusAndGenerateAuthentication_ThenReturnAuthentication() {
        boolean stubActivated = true;
        boolean stubEnabled = true;
        Set<GrantedAuthority> stubAuthoritiesSet = Collections.emptySet();
        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", stubActivated, 0, stubEnabled, true, true, true, stubAuthoritiesSet);

        Authentication actual = spyAccessTokenAuthenticationFilter.verifyUserStatusAndGenerateAuthentication(inputMyUserDetails);

        Assertions.assertTrue(actual.isAuthenticated());
        Assertions.assertEquals(inputMyUserDetails, actual.getPrincipal());
        Assertions.assertNull(actual.getCredentials());
        Assertions.assertEquals(stubAuthoritiesSet.toString(), actual.getAuthorities().toString());
    }

    @Test
    void GivenUserDetailWhichActivatedIsFalse_WhenVerifyUserStatusAndGenerateAuthentication_ThenThrowDisabledException() {
        boolean stubActivated = false;
        boolean stubEnabled = true;
        Set<GrantedAuthority> stubAuthoritiesSet = Collections.emptySet();
        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", stubActivated, 0, stubEnabled, true, true, true, stubAuthoritiesSet);

        DisabledException actual = assertThrows(DisabledException.class, () -> {
            spyAccessTokenAuthenticationFilter.verifyUserStatusAndGenerateAuthentication(inputMyUserDetails);
        });

        Assertions.assertEquals("User is not allowed to access.", actual.getMessage());
    }

    @Test
    void GivenUserDetailWhichEnabledIsFalse_WhenVerifyUserStatusAndGenerateAuthentication_ThenThrowDisabledException() {
        boolean stubActivated = true;
        boolean stubEnabled = false;
        Set<GrantedAuthority> stubAuthoritiesSet = Collections.emptySet();
        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", stubActivated, 0, stubEnabled, true, true, true, stubAuthoritiesSet);

        DisabledException actual = assertThrows(DisabledException.class, () -> {
            spyAccessTokenAuthenticationFilter.verifyUserStatusAndGenerateAuthentication(inputMyUserDetails);
        });

        Assertions.assertEquals("User is not allowed to access.", actual.getMessage());
    }

    @Test
    void GivenUserDetailWhichActivatedAndEnabledAreBothFalse_WhenVerifyUserStatusAndGenerateAuthentication_ThenThrowDisabledException() {
        boolean stubActivated = false;
        boolean stubEnabled = false;
        Set<GrantedAuthority> stubAuthoritiesSet = Collections.emptySet();
        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", stubActivated, 0, stubEnabled, true, true, true, stubAuthoritiesSet);

        DisabledException actual = assertThrows(DisabledException.class, () -> {
            spyAccessTokenAuthenticationFilter.verifyUserStatusAndGenerateAuthentication(inputMyUserDetails);
        });

        Assertions.assertEquals("User is not allowed to access.", actual.getMessage());
    }

    @Test
    void GivenArgs_WhenSuccessfulUserAuthentication_ThenExecuteExpectedProcess() {
        MyUserDetails inputUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
        UsernamePasswordAuthenticationToken inputAuthentication = UsernamePasswordAuthenticationToken.authenticated(inputUserDetails, null, inputUserDetails.getAuthorities());
        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");

        try (MockedStatic<SecurityContextHolder> securityContextHolderMockStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            SecurityContextImpl mockSecurityContext = Mockito.mock(SecurityContextImpl.class);
            securityContextHolderMockStatic.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            spyAccessTokenAuthenticationFilter.successfulUserAuthentication(inputAuthentication, inputMyRequestContext, inputUserDetails);

            Assertions.assertEquals(1, inputUserDetails.getId());
            Mockito.verify(mockSecurityContext, Mockito.times(1)).setAuthentication(inputAuthentication);
        }
    }

    @Test
    void GivenArgs_WhenUnSuccessfulUserAuthentication_ThenExecuteExpectedProcess() {
        MyRequestContext inputMyRequestContext = new MyRequestContext().setUUID("stubUUID");
        Exception inputEx = new Exception("stubMsg");
        Mockito.when(mockLogUtil.composeLogPrefixForBusiness(Optional.empty(), inputMyRequestContext.getUUID())).thenReturn("stubPrefixForBusiness");

        spyAccessTokenAuthenticationFilter.unSuccessfulUserAuthentication(inputMyRequestContext, inputEx);

        Mockito.verify(mockLogUtil, Mockito.times(1)).logInfo(Mockito.any(Logger.class), Mockito.eq("stubPrefixForBusiness"), Mockito.eq("stubMsg"));
    }

    @Test
    void GivenArgs_WhenParseAccessTokenAndAuthenticateUser_ThenExecuteExpectedProcess() {
        HttpServletRequest mockInputRequest = Mockito.mock(HttpServletRequest.class);
        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
        String stubAccessToken = "Bearer stubAccessToken";
        Jws<Claims> mockAccessTokenJws = Mockito.mock(Jws.class);
        Claims stubAccessTokenPayload = Jwts.claims().add("userId", 1).build();
        MyUserDetails stubMyUserDetailsConvertedFromAccessTokenPayload = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
        Authentication mockAuthentication = Mockito.mock(UsernamePasswordAuthenticationToken.class);
        HttpServletResponse mockInputResponse = Mockito.mock(HttpServletResponse.class);

        Mockito.when(mockInputRequest.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT)).thenReturn(stubMyRequestContext);
        Mockito.when(mockInputRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(stubAccessToken);
        Mockito.when(mockJWTUtil.parseAccessToken("stubAccessToken")).thenReturn(mockAccessTokenJws);
        Mockito.when(mockAccessTokenJws.getPayload()).thenReturn(stubAccessTokenPayload);
        Mockito.doReturn(stubMyUserDetailsConvertedFromAccessTokenPayload).when(mockJWTUtil).generateMyUserDetailsByAccessTokenPayload(stubAccessTokenPayload);
        Mockito.when(spyAccessTokenAuthenticationFilter.verifyUserStatusAndGenerateAuthentication(stubMyUserDetailsConvertedFromAccessTokenPayload)).thenReturn(mockAuthentication);

        spyAccessTokenAuthenticationFilter.parseAccessTokenAndAuthenticateUser(mockInputRequest, mockInputResponse);

        Mockito.verify(mockJWTUtil, Mockito.times(1)).parseAccessToken("stubAccessToken");
        Mockito.doReturn(stubMyUserDetailsConvertedFromAccessTokenPayload).when(mockJWTUtil).generateMyUserDetailsByAccessTokenPayload(stubAccessTokenPayload);
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(1)).verifyUserStatusAndGenerateAuthentication(stubMyUserDetailsConvertedFromAccessTokenPayload);
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(1)).successfulUserAuthentication(mockAuthentication, stubMyRequestContext, stubMyUserDetailsConvertedFromAccessTokenPayload);
    }

    @Test
    void GivenRequestWithoutAuthorizationField_WhenParseAccessTokenAndAuthenticateUser_ThenExecuteExpectedProcess() {
        HttpServletRequest mockInputRequest = Mockito.mock(HttpServletRequest.class);
        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
        HttpServletResponse mockInputResponse = Mockito.mock(HttpServletResponse.class);

        Mockito.when(mockInputRequest.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT)).thenReturn(stubMyRequestContext);
        //模擬前端沒有攜帶此欄位
        Mockito.when(mockInputRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        ArgumentCaptor<AuthorizationFieldEmptyException> authorizationFieldEmptyExceptionArgumentCaptor = ArgumentCaptor.forClass(AuthorizationFieldEmptyException.class);

        spyAccessTokenAuthenticationFilter.parseAccessTokenAndAuthenticateUser(mockInputRequest, mockInputResponse);

        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(1)).unSuccessfulUserAuthentication(Mockito.eq(stubMyRequestContext), authorizationFieldEmptyExceptionArgumentCaptor.capture());
        Mockito.verify(mockJWTUtil, Mockito.times(0)).parseAccessToken(Mockito.anyString());
        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateMyUserDetailsByAccessTokenPayload(Mockito.any());
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(0)).verifyUserStatusAndGenerateAuthentication(Mockito.any());
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(0)).successfulUserAuthentication(Mockito.any(), Mockito.any(), Mockito.any());
        AuthorizationFieldEmptyException targetAuthorizationFieldEmptyException = authorizationFieldEmptyExceptionArgumentCaptor.getValue();
        Assertions.assertEquals("Authorization field of HTTP request header is empty.", targetAuthorizationFieldEmptyException.getMessage());
    }

    @Test
    void GivenRequestWithInvalidAccessToken_WhenParseAccessTokenAndAuthenticateUser_ThenExecuteExpectedProcess() {
        HttpServletRequest mockInputRequest = Mockito.mock(HttpServletRequest.class);
        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
        String stubAccessToken = "Bearer invalidAccessToken";
        HttpServletResponse mockInputResponse = Mockito.mock(HttpServletResponse.class);

        Mockito.when(mockInputRequest.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT)).thenReturn(stubMyRequestContext);
        Mockito.when(mockInputRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(stubAccessToken);
        //當AccessToken無法解析
        Mockito.when(mockJWTUtil.parseAccessToken("invalidAccessToken")).thenThrow(new JwtException("stubMsg"));
        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);

        spyAccessTokenAuthenticationFilter.parseAccessTokenAndAuthenticateUser(mockInputRequest, mockInputResponse);

        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(1)).unSuccessfulUserAuthentication(Mockito.eq(stubMyRequestContext), exceptionArgumentCaptor.capture());
        Mockito.verify(mockJWTUtil, Mockito.times(1)).parseAccessToken("invalidAccessToken");
        Mockito.verify(mockJWTUtil, Mockito.times(0)).generateMyUserDetailsByAccessTokenPayload(Mockito.any());
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(0)).verifyUserStatusAndGenerateAuthentication(Mockito.any());
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(0)).successfulUserAuthentication(Mockito.any(), Mockito.any(), Mockito.any());
        Exception targetException = exceptionArgumentCaptor.getValue();
        Assertions.assertEquals("stubMsg", targetException.getMessage());
    }

    @Test
    void GivenTheUserIsNotAllowToAccess_WhenParseAccessTokenAndAuthenticateUser_ThenExecuteExpectedProcess() {
        HttpServletRequest mockInputRequest = Mockito.mock(HttpServletRequest.class);
        MyRequestContext stubMyRequestContext = new MyRequestContext().setUUID("stubUUID");
        String stubAccessToken = "Bearer stubAccessToken";
        Jws<Claims> mockAccessTokenJws = Mockito.mock(Jws.class);
        Claims stubAccessTokenPayload = Jwts.claims().add("userId", 1).build();
        MyUserDetails stubMyUserDetailsConvertedFromAccessTokenPayload = new MyUserDetails(1, "stubName", "stubEmail", false, 0, false, true, true, true, Collections.emptySet());
        HttpServletResponse mockInputResponse = Mockito.mock(HttpServletResponse.class);

        Mockito.when(mockInputRequest.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT)).thenReturn(stubMyRequestContext);
        Mockito.when(mockInputRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(stubAccessToken);
        Mockito.when(mockJWTUtil.parseAccessToken("stubAccessToken")).thenReturn(mockAccessTokenJws);
        Mockito.when(mockAccessTokenJws.getPayload()).thenReturn(stubAccessTokenPayload);
        Mockito.doReturn(stubMyUserDetailsConvertedFromAccessTokenPayload).when(mockJWTUtil).generateMyUserDetailsByAccessTokenPayload(stubAccessTokenPayload);
        //假設用戶不被允許訪問受保護API
        Mockito.doThrow(new DisabledException("stubMsg")).when(spyAccessTokenAuthenticationFilter).verifyUserStatusAndGenerateAuthentication(stubMyUserDetailsConvertedFromAccessTokenPayload);
        ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);

        spyAccessTokenAuthenticationFilter.parseAccessTokenAndAuthenticateUser(mockInputRequest, mockInputResponse);

        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(1)).unSuccessfulUserAuthentication(Mockito.eq(stubMyRequestContext), exceptionArgumentCaptor.capture());
        Mockito.verify(mockJWTUtil, Mockito.times(1)).parseAccessToken("stubAccessToken");
        Mockito.verify(mockJWTUtil, Mockito.times(1)).generateMyUserDetailsByAccessTokenPayload(stubAccessTokenPayload);
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(1)).verifyUserStatusAndGenerateAuthentication(stubMyUserDetailsConvertedFromAccessTokenPayload);
        Mockito.verify(spyAccessTokenAuthenticationFilter, Mockito.times(0)).successfulUserAuthentication(Mockito.any(), Mockito.any(), Mockito.any());
        Exception targetException = exceptionArgumentCaptor.getValue();
        Assertions.assertEquals("stubMsg", targetException.getMessage());
    }

}