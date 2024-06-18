package raica.pwmanager.filter.security;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.entities.bo.MyUserDetails;
import raica.pwmanager.exception.AuthorizationFieldEmptyException;
import raica.pwmanager.util.JWTUtil;
import raica.pwmanager.util.LogUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * 解析AccessToken並進行身份驗證的過濾器。
 */
@Slf4j
@Component
public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private JWTUtil jwtUtil;


    /**
     * 需要被認證機制保護的API路徑。
     * */
    private final AntPathRequestMatcher[] shouldBeProtectedReqMatcher = ApiConst.getAllProtApiMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        this.parseAccessTokenAndAuthenticateUser(request, response);

        filterChain.doFilter(request, response);
    }


    void parseAccessTokenAndAuthenticateUser(HttpServletRequest request, HttpServletResponse response) {
        MyRequestContext myRequestContext = (MyRequestContext) request.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT);
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1. 檢查欄位空值
        if (authHeader == null) {
            this.unSuccessfulUserAuthentication(myRequestContext, new AuthorizationFieldEmptyException("Authorization field of HTTP request header is empty."));
            return;
        }

        // 2. 去掉RFC 6750標準規定的既有格式(Bearer <AccessToken>)，取出Token本體
        String accessToken = authHeader.replace("Bearer ", "");

        try {
            // 3. 解析token & 取出內容
            Claims payload = jwtUtil.parseAccessToken(accessToken).getPayload();

            // 4. 轉換JWT內容為userDetailsBo
            MyUserDetails myUserDetails = jwtUtil.generateMyUserDetailsByAccessTokenPayload(payload);

            // 5. 驗證用戶狀態是否被禁用 & 生成憑證
            Authentication auth = this.verifyUserStatusAndGenerateAuthentication(myUserDetails);

            // 6. 認證成功的流程
            this.successfulUserAuthentication(auth, myRequestContext, myUserDetails);
        } catch (Exception e) {
            this.unSuccessfulUserAuthentication(myRequestContext, e);
        }

    }


    /**
     * 當身份驗證發生失敗，在此進行一些預處理。
     * 後續會由{@link raica.pwmanager.handler.SecurityExceptionHandler}處理。
     */
    void unSuccessfulUserAuthentication(MyRequestContext myRequestContext, Exception e) {
        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForBusiness(Optional.empty(), myRequestContext.getUUID()),
                e.getMessage()
        );
    }


    void successfulUserAuthentication(Authentication auth, MyRequestContext myRequestContext, MyUserDetails myUserDetails) {
        myRequestContext.setMyUserDetailsOpt(Optional.of(myUserDetails));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    /**
     * 驗證代表使用者狀態的布林值，若用戶狀態可以正常使用，則生成一個憑證。
     *
     * @return {@link Authentication} - 若用戶沒被禁用，回傳一個認證通過的Authentication物件。
     * @throws DisabledException 如果用戶被禁用，拋出此例外。
     */
    Authentication verifyUserStatusAndGenerateAuthentication(MyUserDetails myUserDetails) throws DisabledException {

        if(!myUserDetails.getActivated() || !myUserDetails.isEnabled()) {
            throw new DisabledException("User is not allowed to access.");
        }

//		生成一個認證通過的Authentication物件
        return UsernamePasswordAuthenticationToken.authenticated(myUserDetails, null, myUserDetails.getAuthorities());
    }


    /**
     * 雖然在 {@link raica.pwmanager.cofig.SecurityConfig}有設置.requestMatchers(ApiConst.getAllPubApiMatcher()).permitAll()，但發現公開API仍然會跑進這個Filter，差別在於公開API即使沒有認證，SecurityChain也會放行。
     * 因為不希望每個訪問公開API的請求都經過unSuccessfulUserAuthentication印出LOG，故新增此忽略邏輯。
     *
     *
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Arrays.stream(shouldBeProtectedReqMatcher)
                .noneMatch(matcher -> matcher.matches(request)); //Stream有對遍歷優化，若提前匹配到，就不會遍歷所有元素
    }


}
