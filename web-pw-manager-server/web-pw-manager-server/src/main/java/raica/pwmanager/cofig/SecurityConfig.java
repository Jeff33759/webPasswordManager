package raica.pwmanager.cofig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import raica.pwmanager.consts.ApiConst;
import raica.pwmanager.filter.security.AccessTokenAuthenticationFilter;
import raica.pwmanager.handler.SecurityExceptionHandler;
import raica.pwmanager.prop.CorsProps;

import java.util.Arrays;
import java.util.List;


/**
 * Spring Security 5.4 後，不推薦使用繼承{@link WebSecurityConfigurerAdapter}的方式設置Security。
 * 這裡採用新版的做法來進行設置，讓設置的動作與{@link WebSecurityConfigurerAdapter}解耦。
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private AccessTokenAuthenticationFilter accessTokenAuthenticationFilter;

    @Autowired
    private SecurityExceptionHandler securityExHandler;

    @Autowired
    private CorsProps corsProps;

    /**
     * 對Security過濾鍊的認證機制進行客製化配置。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin().disable() //棄用Security預設的表單登入，實現自製的登入認證流程。
                .logout().disable()
                .csrf().disable() //棄用基於Cookie的Session機制，使用JWTs來進行授權驗證，基本上不會有CSRF的風險，因此關閉CSRF防禦機制
                .cors()
                .and()
//              設定API驗證匹配
                .authorizeRequests()
                .requestMatchers(ApiConst.getAllPubApiMatcher()).permitAll() //不需登入的API
                .requestMatchers(ApiConst.getAllProtApiMatcher()).authenticated() //需要登入的API，由AccessTokenAuthenticationFilter進行認證
                .antMatchers("/error").permitAll() // 當SpringBoot發生不可預期的例外時，會自動導到/error，所以放行該路徑，讓SpringBoot預設的BasicErrorController處理後續回應。
                .antMatchers(HttpMethod.OPTIONS).permitAll() //允許所有預檢請求
                .anyRequest().authenticated()
                .and()
//              設置過濾鍊
                .addFilterBefore(accessTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // 利用AccessToken進行認證
//              設置session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //停用session機制，因為用了Token，不再使用session來驗證或授權身份
                .and()
//				設置自訂義的異常處理器
                .exceptionHandling()
                .authenticationEntryPoint(this.securityExHandler) //當認證失敗
                .and()
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOrigins(corsProps.getAllowedOriginsList()); //允許的跨來源網域
        corsConfiguration.setAllowedMethods(corsProps.getAllowedMethodList()); //允許的方法
        corsConfiguration.applyPermitDefaultValues(); //其餘沒設置的值都使用預設值

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); //本伺服端哪些路徑要使用上面的設置，設定全部API都使用

        return source;
    }

}
