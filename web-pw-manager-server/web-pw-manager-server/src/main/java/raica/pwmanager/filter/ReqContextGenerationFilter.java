package raica.pwmanager.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import raica.pwmanager.consts.FilterOrderNumberConst;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyContentCachingReqWrapper;
import raica.pwmanager.entities.bo.MyRequestContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 將自己做的MyRequestContext物件設置進request物件中，方便後續業務邏輯取用。
 * 在這一層中還只是實例化上下文物件，之後的過濾鏈才會視情況對裡面的成員變數賦值。
 */
@Component
@WebFilter
@Order(FilterOrderNumberConst.REQ_CONTEXT_GENERATION_FILTER)
public class ReqContextGenerationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MyContentCachingReqWrapper reqWrapper = (MyContentCachingReqWrapper) request;
        ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) response;

        request.setAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT, new MyRequestContext());

        filterChain.doFilter(reqWrapper, resWrapper);
    }

}
