package raica.pwmanager.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.entities.po.User;
import raica.pwmanager.prop.AppInfoProps;
import raica.pwmanager.prop.JWTProps;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;


/**
 * JWTUtil幾乎是直接call第三方函式庫的元件，沒有什麼自己的邏輯，所以不mock第三方元件了，給定密鑰字串，去跑第三方元件的真實邏輯，測input和output是否合乎預期，來判斷JWT加解密是否正常。
 * 比起單元測試，更像整合測試(Integration test)。
 */
@SpringBootTest
class JWTUtilTest {

    @Mock
    private JWTProps jwtProps;

    @Mock
    private AppInfoProps appInfoProps;

    @Mock
    private JWTUtil.AuthenticationConverter mockAuthenticationConverter;

    @InjectMocks
    @Spy
    private JWTUtil spyJWTUtil; //待測元件


    @BeforeEach
    void stubComponentBeforeEachTestCase() {
        String accessTokenSecretKey = "accessTokenSecretKeyForUnitTestingInv";
        String refreshTokenSecretKey = "refreshTokenSecretKeyForUnitTestingInv";

        Mockito.when(jwtProps.getAccessTokenSecretKey()).thenReturn(Keys.hmacShaKeyFor(accessTokenSecretKey.getBytes()));
        Mockito.when(jwtProps.getRefreshTokenSecretKey()).thenReturn(Keys.hmacShaKeyFor(refreshTokenSecretKey.getBytes()));
        Mockito.when(appInfoProps.getAppName()).thenReturn("stubAppName");

        spyJWTUtil.initializeAfterStartUp();
    }

    @Test
    void GivenAccessToken_WhenParseAccessToken_ThenReturnExpectedJwsObj() {
        String inputAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdHViQWNjZXNzVG9rZW5QYXlsb2FkS2V5Ijoic3R1YkFjY2Vzc1Rva2VuUGF5bG9hZFZhbHVlIn0.7zodMUR889OvB9X98axA-k5lZGQ6AOHCSI-zjNwPiBU";
        Map<String, String> expectedJWTHeader = Map.of("alg","HS256", "typ", "JWT");
        Map<String, String> expectedJWTPayload = Map.of("stubAccessTokenPayloadKey","stubAccessTokenPayloadValue");
        Mockito.doCallRealMethod().when(spyJWTUtil).parseAccessToken(Mockito.anyString());

        Jws<Claims> actual = spyJWTUtil.parseAccessToken(inputAccessToken);
        JwsHeader targetHeader = actual.getHeader();
        Claims targetPayload = actual.getPayload();

        Assertions.assertEquals(expectedJWTHeader, targetHeader);
        Assertions.assertEquals(expectedJWTPayload, targetPayload);
    }

    @Test
    void GivenExpiredAccessToken_WhenParseAccessToken_ThenThrowSubclassOfJWTException() {
        // exp: 1672502400
        String inputExpiredAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NzI1MDI0MDAsInN0dWJBY2Nlc3NUb2tlblBheWxvYWRLZXkiOiJzdHViQWNjZXNzVG9rZW5QYXlsb2FkVmFsdWUifQ.C1GAXAaS-ZGK70hqcuyfi7PZTxlQW1J9eXxqZ9I-C7Y";
        Mockito.doCallRealMethod().when(spyJWTUtil).parseAccessToken(Mockito.anyString());

        ExpiredJwtException actual = Assertions.assertThrows(ExpiredJwtException.class, () -> {
            spyJWTUtil.parseAccessToken(inputExpiredAccessToken);
        });

        Assertions.assertInstanceOf(JwtException.class, actual);
    }

    @Test
    void GivenFabricatedAccessTokenPayload_WhenParseAccessToken_ThenThrowSubclassOfJWTException() {
        String inputModifiedAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdHViQWNjZXNzVG9rZW5QYXlsb2FkS2V5Ijoic3R1Yk1vZGlmaWVkQWNjZXNzVG9rZW5QYXlsb2FkVmFsdWVCeUhhY2tlciJ9.7zodMUR889OvB9X98axA-k5lZGQ6AOHCSI-zjNwPiBU";
        Mockito.doCallRealMethod().when(spyJWTUtil).parseAccessToken(Mockito.anyString());

        SignatureException actual = Assertions.assertThrows(SignatureException.class, () -> {
            spyJWTUtil.parseAccessToken(inputModifiedAccessToken);
        });

        Assertions.assertInstanceOf(JwtException.class, actual);
    }

    @Test
    void GivenInvalidAccessTokenFormat_WhenParseAccessToken_ThenThrowSubclassOfJWTException() {
        String inputInvalidAccessToken = "modifiedBySomeone---eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdHViQWNjZXNzVG9rZW5QYXlsb2FkS2V5Ijoic3R1YkFjY2Vzc1Rva2VuUGF5bG9hZFZhbHVlIn0.7zodMUR889OvB9X98axA-k5lZGQ6AOHCSI-zjNwPiBU";
        Mockito.doCallRealMethod().when(spyJWTUtil).parseAccessToken(Mockito.anyString());

        MalformedJwtException actual = Assertions.assertThrows(MalformedJwtException.class, () -> {
            spyJWTUtil.parseAccessToken(inputInvalidAccessToken);
        });

        Assertions.assertInstanceOf(JwtException.class, actual);
    }

    @Test
    void GivenAccessToken_WhenForceParsingExpiredAccessTokenAsClaims_ThenReturnExpectedClaims() {
        String inputAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdHViQWNjZXNzVG9rZW5QYXlsb2FkS2V5Ijoic3R1YkFjY2Vzc1Rva2VuUGF5bG9hZFZhbHVlIn0.7zodMUR889OvB9X98axA-k5lZGQ6AOHCSI-zjNwPiBU";
        Map<String, String> expectedJWTHeader = Map.of("alg","HS256","typ","JWT");
        Map<String, String> expectedJWTPayload = Map.of("stubAccessTokenPayloadKey","stubAccessTokenPayloadValue");
        Mockito.doCallRealMethod().when(spyJWTUtil).parseAccessToken(Mockito.anyString());

        Claims actual = spyJWTUtil.forceParsingExpiredAccessTokenAsClaims(inputAccessToken);

        Assertions.assertEquals(expectedJWTPayload.get("stubAccessTokenPayloadKey"), actual.get("stubAccessTokenPayloadKey"));
    }

    @Test
    void GivenExpiredAccessToken_WhenForceParsingExpiredAccessTokenAsClaims_ThenReturnExpectedClaims() {
        // exp: 1672502400
        String inputExpiredAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NzI1MDI0MDAsInN0dWJBY2Nlc3NUb2tlblBheWxvYWRLZXkiOiJzdHViQWNjZXNzVG9rZW5QYXlsb2FkVmFsdWUifQ.C1GAXAaS-ZGK70hqcuyfi7PZTxlQW1J9eXxqZ9I-C7Y";        Map<String, String> expectedJWTHeader = Map.of("alg","HS256");
        Map<String, String> expectedJWTPayload = Map.of("stubAccessTokenPayloadKey","stubAccessTokenPayloadValue");
        Mockito.doCallRealMethod().when(spyJWTUtil).parseAccessToken(Mockito.anyString());

        Claims actual = spyJWTUtil.forceParsingExpiredAccessTokenAsClaims(inputExpiredAccessToken);

        Assertions.assertTrue(actual.getExpiration().toInstant().compareTo(Instant.now()) <= 0); //表示真的過期了
        Assertions.assertEquals(expectedJWTPayload.get("stubAccessTokenPayloadKey"), actual.get("stubAccessTokenPayloadKey"));
    }

    @Test
    void GivenRefreshToken_WhenParseRefreshToken_ThenReturnExpectedJwsObj() {
        String inputRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdHViUmVmcmVzaFRva2VuUGF5bG9hZEtleSI6InN0dWJSZWZyZXNoVG9rZW5QYXlsb2FkVmFsdWUifQ.OlTiQ4PECs84qnLhJGX4mar8Th4hD2dyIIY2oT7h_kc";
        Map<String, String> expectedHeader = Map.of("alg","HS256", "typ","JWT");
        Map<String, String> expectedPayload = Map.of("stubRefreshTokenPayloadKey","stubRefreshTokenPayloadValue");
        Mockito.doCallRealMethod().when(spyJWTUtil).parseRefreshToken(Mockito.anyString());

        Jws<Claims> actual = spyJWTUtil.parseRefreshToken(inputRefreshToken);
        JwsHeader targetHeader = actual.getHeader();
        Claims targetPayload = actual.getPayload();

        Assertions.assertEquals(expectedHeader, targetHeader);
        Assertions.assertEquals(expectedPayload, targetPayload);
    }


    @Test
    void GivenExpiredRefreshToken_WhenParseRefreshToken_ThenThrowSubclassOfJWTException() {
        // exp: 1672502400
        String inputExpiredRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NzI1MDI0MDAsInN0dWJSZWZyZXNoVG9rZW5QYXlsb2FkS2V5Ijoic3R1YlJlZnJlc2hUb2tlblBheWxvYWRWYWx1ZSJ9.VC5y9PqmTEwBw3jOaCXekh5I_yKg_GZo30g0Crt82l8";
        Mockito.doCallRealMethod().when(spyJWTUtil).parseRefreshToken(Mockito.anyString());

        ExpiredJwtException actual = Assertions.assertThrows(ExpiredJwtException.class, () -> {
            spyJWTUtil.parseRefreshToken(inputExpiredRefreshToken);
        });

        Assertions.assertInstanceOf(JwtException.class, actual);
    }

    @Test
    void GivenFabricatedRefreshTokenPayload_WhenParseRefreshToken_ThenThrowSubclassOfJWTException() {
        String inputModifiedRefreshToken =  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdHViUmVmcmVzaFRva2VuUGF5bG9hZEtleSI6InN0dWJNb2RpZmllZFJlZnJlc2hUb2tlblBheWxvYWRWYWx1ZUJ5SGFja2VyIn0.OlTiQ4PECs84qnLhJGX4mar8Th4hD2dyIIY2oT7h_kc";
        Mockito.doCallRealMethod().when(spyJWTUtil).parseRefreshToken(Mockito.anyString());

        SignatureException actual = Assertions.assertThrows(SignatureException.class, () -> {
            spyJWTUtil.parseRefreshToken(inputModifiedRefreshToken);
        });

        Assertions.assertInstanceOf(JwtException.class, actual);
    }

    @Test
    void GivenInvalidRefreshTokenFormat_WhenParseRefreshToken_ThenThrowSubclassOfJWTException() {
        String inputInvalidRefreshToken = "modifiedBySomeone---eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdHViUmVmcmVzaFRva2VuUGF5bG9hZEtleSI6InN0dWJSZWZyZXNoVG9rZW5QYXlsb2FkVmFsdWUifQ.OlTiQ4PECs84qnLhJGX4mar8Th4hD2dyIIY2oT7h_kc";
        Mockito.doCallRealMethod().when(spyJWTUtil).parseRefreshToken(Mockito.anyString());

        MalformedJwtException actual = Assertions.assertThrows(MalformedJwtException.class, () -> {
            spyJWTUtil.parseRefreshToken(inputInvalidRefreshToken);
        });

        Assertions.assertInstanceOf(JwtException.class, actual);
    }

    @Test
    void GivenUser_WhenGenerateAccessToken_ThenReturnExpectedJWTString() {
        Date stubDataOfTokenExpiration = Date.from(Instant.ofEpochMilli(1716780440066L)); //2024-05-27 11:27:20 Asia/Taipei
        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
        String expected = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MTY3ODA0NDAsImlzcyI6InN0dWJBcHBOYW1lIiwic3ViIjoiQWNjZXNzIHRva2VuLiIsInVzZXJJZCI6MSwidXNlck5hbWUiOiJzdHViTmFtZSIsImVtYWlsIjoic3R1YkVtYWlsIiwiYWN0aXZhdGVkIjp0cnVlLCJtZmFUeXBlIjowfQ.ZIOa0T-cgHOAzoufwgvc09TvLP-HpbjIWKstg_ho8gI";

        String actual;
        try (MockedStatic<Date> dateMockStatic = Mockito.mockStatic(Date.class)) {
            dateMockStatic.when(() -> Date.from(Mockito.any(Instant.class))).thenReturn(stubDataOfTokenExpiration);
            Mockito.doCallRealMethod().when(spyJWTUtil).generateAccessToken(Mockito.any());

            actual = spyJWTUtil.generateAccessToken(inputMyUserDetails);
        }

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void GivenUser_WhenGenerateRefreshToken_ThenReturnExpectedJWTString() {
        Date stubDataOfTokenExpiration = Date.from(Instant.ofEpochMilli(1716780440066L)); //2024-05-27 11:27:20 Asia/Taipei
        MyUserDetails inputMyUserDetails = new MyUserDetails(1, "stubName", "stubEmail", true, 0, true, true, true, true, Collections.emptySet());
        String expected = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MTY3ODA0NDAsImlzcyI6InN0dWJBcHBOYW1lIiwic3ViIjoiUmVmcmVzaCB0b2tlbi4iLCJ1c2VySWQiOjF9.J68r_yt18YQqWtOYciRnundUKd0RjnZgkvl5xDmP71E";

        String actual;
        try (MockedStatic<Date> dateMockStatic = Mockito.mockStatic(Date.class)) {
            dateMockStatic.when(() -> Date.from(Mockito.any(Instant.class))).thenReturn(stubDataOfTokenExpiration);
            Mockito.doCallRealMethod().when(spyJWTUtil).generateRefreshToken(Mockito.any());

            actual = spyJWTUtil.generateRefreshToken(inputMyUserDetails);
        }

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void GivenAccessTokenPayload_WhenGenerateMyUserDetailsByAccessTokenPayload_ThenInvokeAuthenticationConverter() {
        Claims stubAccessTokenPayload = Jwts.claims().add("userId", 1).build();

        spyJWTUtil.generateMyUserDetailsByAccessTokenPayload(stubAccessTokenPayload);

        Mockito.verify(mockAuthenticationConverter, Mockito.times(1)).accessTokenPayloadToMyUserDetailsBo(stubAccessTokenPayload);
    }

    @Test
    void GivenUserPo_WhenGenerateMyUserDetailsByUserPo_ThenInvokeAuthenticationConverter() {
        User stubUserPo = new User();

        spyJWTUtil.generateMyUserDetailsByUserPo(stubUserPo);

        Mockito.verify(mockAuthenticationConverter, Mockito.times(1)).userPoToMyUserDetailsBo(stubUserPo);
    }

}