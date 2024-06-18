package raica.pwmanager.consts;

import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import raica.pwmanager.cofig.SecurityConfig;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 統一管理API的常數。
 */
public class ApiConst {

    private ApiConst() {
    }

    /**
     * 存放常數路徑。
     */
    public static class Path {

        private Path() {
        }

        /**
         * 註冊。
         */
        public static final String REGISTER = "/api/auth/register";

        /**
         * 郵箱激活。
         */
        public static final String EMAIL_ACTIVATION = "/api/auth/register/activation/email";

        /**
         * 登入。
         */
        public static final String LOGIN = "/api/auth/login";

        /**
         * 登入的MFA二階段身份驗證。
         */
        public static final String LOGIN_MFA_VERIFICATION = "/api/auth/login/verification";

        /**
         * 刷新AccessToken，需要認證RefreshToken通過才能訪問。
         */
        public static final String RENEW_ACCESS_TOKEN = "/api/auth/renew/access-token";


        /**
         * 獲取用戶信息
         */
        public static final String QUERY_USER = "/api/user";

        /**
         * 更新用戶信息
         */
        public static final String EDIT_USER = "/api/user";

        /**
         * 更改用戶密碼
         */
        public static final String EDIT_USER_MAIN_PASSWORD = "/api/user/password";

        /**
         * 添加密碼
         */
        public static final String ADD_PASSWORD = "/api/password";

        /**
         * 獲取密碼列表
         */
        public static final String QUERY_PASSWORD_LIST = "/api/passwords";

        /**
         * 根據標籤查詢密碼列表
         */
        public static final String QUERY_PASSWORD_LIST_BY_TAG = "/api/passwords/{tagId}";

        /**
         * 獲取單個密碼
         */
        public static final String QUERY_PASSWORD = "/api/password/{id}";

        /**
         * 更新密碼
         */
        public static final String EDIT_PASSWORD = "/api/password/{id}";

        /**
         * 刪除密碼
         */
        public static final String DELETE_PASSWORD = "/api/password/{id}";

        /**
         * 添加分類
         */
        public static final String ADD_CATEGORY = "/api/category";

        /**
         * 獲取分類列表
         */
        public static final String QUERY_CATEGORY_LIST = "/api/categories";

        /**
         * 更新分類
         */
        public static final String EDIT_CATEGORY = "/api/category/{id}";

        /**
         * 刪除分類
         */
        public static final String DELETE_CATEGORY = "/api/category/{id}";

        /**
         * 添加標籤
         */
        public static final String ADD_TAG = "/api/tag";

        /**
         * 獲取標籤列表
         */
        public static final String QUERY_TAG_LIST = "/api/tags";

        /**
         * 更新標籤
         */
        public static final String EDIT_TAG = "/api/tag/{id}";

        /**
         * 刪除標籤
         */
        public static final String DELETE_TAG = "/api/tag/{id}";


    }


    /**
     * 存放各路徑對應的HttpMethod常數
     */
    public static class HttpMethods {

        private HttpMethods() {
        }

        public static final HttpMethod REGISTER = HttpMethod.POST;

        public static final HttpMethod EMAIL_ACTIVATION = HttpMethod.GET;

        public static final HttpMethod LOGIN = HttpMethod.POST;

        public static final HttpMethod LOGIN_MFA_VERIFICATION = HttpMethod.POST;

        public static final HttpMethod RENEW_ACCESS_TOKEN = HttpMethod.POST;

        public static final HttpMethod QUERY_USER = HttpMethod.GET;

        public static final HttpMethod EDIT_USER = HttpMethod.PUT;

        public static final HttpMethod EDIT_USER_MAIN_PASSWORD = HttpMethod.PUT;

        public static final HttpMethod ADD_PASSWORD = HttpMethod.POST;

        public static final HttpMethod QUERY_PASSWORD_LIST = HttpMethod.GET;

        public static final HttpMethod QUERY_PASSWORD_LIST_BY_TAG = HttpMethod.GET;

        public static final HttpMethod QUERY_PASSWORD = HttpMethod.GET;

        public static final HttpMethod EDIT_PASSWORD = HttpMethod.PUT;

        public static final HttpMethod DELETE_PASSWORD = HttpMethod.DELETE;

        public static final HttpMethod ADD_CATEGORY = HttpMethod.POST;

        public static final HttpMethod QUERY_CATEGORY_LIST = HttpMethod.GET;

        public static final HttpMethod EDIT_CATEGORY = HttpMethod.PUT;

        public static final HttpMethod DELETE_CATEGORY = HttpMethod.DELETE;

        public static final HttpMethod ADD_TAG = HttpMethod.POST;

        public static final HttpMethod QUERY_TAG_LIST = HttpMethod.GET;

        public static final HttpMethod EDIT_TAG = HttpMethod.PUT;

        public static final HttpMethod DELETE_TAG = HttpMethod.DELETE;

    }

    /**
     * 不受保護API的路徑，不需要登入。
     */
    public enum PubApi {

        REGISTER(Path.REGISTER, HttpMethods.REGISTER),

        EMAIL_ACTIVATION(Path.EMAIL_ACTIVATION, HttpMethods.EMAIL_ACTIVATION),

        LOGIN(Path.LOGIN, HttpMethods.LOGIN),

        LOGIN_MFA_VERIFICATION(Path.LOGIN_MFA_VERIFICATION, HttpMethods.LOGIN_MFA_VERIFICATION),

        /**
         * 這支API的認證邏輯做在服務器裡，不做在Security chain裡，比較好維護，所以設為不受保護API。
         */
        RENEW_ACCESS_TOKEN(Path.RENEW_ACCESS_TOKEN, HttpMethods.RENEW_ACCESS_TOKEN);

        @Getter
        private final String path;

        @Getter
        private final HttpMethod method;

        PubApi(String path, HttpMethod method) {
            this.path = path;
            this.method = method;
        }

    }

    /**
     * 受保護API的路徑，需要登入後才能訪問。
     */
    public enum ProtApi {

        QUERY_USER(Path.QUERY_USER, HttpMethods.QUERY_USER),

        EDIT_USER(Path.EDIT_USER, HttpMethods.EDIT_USER),

        EDIT_USER_MAIN_PASSWORD(Path.EDIT_USER_MAIN_PASSWORD, HttpMethods.EDIT_USER_MAIN_PASSWORD),

        ADD_PASSWORD(Path.ADD_PASSWORD, HttpMethods.ADD_PASSWORD),

        QUERY_PASSWORD_LIST(Path.QUERY_PASSWORD_LIST, HttpMethods.QUERY_PASSWORD_LIST),

        QUERY_PASSWORD_LIST_BY_TAG(Path.QUERY_PASSWORD_LIST_BY_TAG, HttpMethods.QUERY_PASSWORD_LIST_BY_TAG),

        QUERY_PASSWORD(Path.QUERY_PASSWORD, HttpMethods.QUERY_PASSWORD),

        EDIT_PASSWORD(Path.EDIT_PASSWORD, HttpMethods.EDIT_PASSWORD),

        DELETE_PASSWORD(Path.DELETE_PASSWORD, HttpMethods.DELETE_PASSWORD),

        ADD_CATEGORY(Path.ADD_CATEGORY, HttpMethods.ADD_CATEGORY),

        QUERY_CATEGORY_LIST(Path.QUERY_CATEGORY_LIST, HttpMethods.QUERY_CATEGORY_LIST),

        EDIT_CATEGORY(Path.EDIT_CATEGORY, HttpMethods.EDIT_CATEGORY),

        DELETE_CATEGORY(Path.DELETE_CATEGORY, HttpMethods.DELETE_CATEGORY),

        ADD_TAG(Path.ADD_TAG, HttpMethods.ADD_TAG),

        QUERY_TAG_LIST(Path.QUERY_TAG_LIST, HttpMethods.QUERY_TAG_LIST),

        EDIT_TAG(Path.EDIT_TAG, HttpMethods.EDIT_TAG),

        DELETE_TAG(Path.DELETE_TAG, HttpMethods.DELETE_TAG);

        @Getter
        private final String path;

        @Getter
        private final HttpMethod method;

        ProtApi(String path, HttpMethod method) {
            this.path = path;
            this.method = method;
        }

    }


    /**
     * 得到所有公開Api列舉的Set。
     */
    private static Set<PubApi> getAllPubApiSet() {
        return Stream.of(ApiConst.PubApi.values())
                .collect(Collectors.toSet());
    }


    /**
     * 將所有公開Api列舉包裝成AntPathRequestMatcher[]，供{@link SecurityConfig#securityFilterChain}進行設置。
     */
    public static AntPathRequestMatcher[] getAllPubApiMatcher() {
        return getAllPubApiSet().stream()
                .map(publicApiEnum -> new AntPathRequestMatcher(publicApiEnum.getPath(), publicApiEnum.getMethod().name()))
                .toArray(AntPathRequestMatcher[]::new);
    }

    /**
     * 得到所有受保護Api列舉的Set。
     */
    private static Set<ProtApi> getAllProtApiSet() {
        return Stream.of(ApiConst.ProtApi.values())
                .collect(Collectors.toSet());
    }


    /**
     * 將所有受保護Api列舉包裝成AntPathRequestMatcher[]，供{@link SecurityConfig#securityFilterChain}進行設置。
     */
    public static AntPathRequestMatcher[] getAllProtApiMatcher() {
        return getAllProtApiSet().stream()
                .map(protectedApiEnum -> new AntPathRequestMatcher(protectedApiEnum.getPath(), protectedApiEnum.getMethod().name()))
                .toArray(AntPathRequestMatcher[]::new);
    }


}
