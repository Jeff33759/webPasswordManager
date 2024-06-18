package raica.pwmanager.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import raica.pwmanager.consts.FilterOrderNumberConst;
import raica.pwmanager.consts.RequestAttributeFieldName;
import raica.pwmanager.entities.bo.MyContentCachingReqWrapper;
import raica.pwmanager.entities.bo.MyRequestContext;
import raica.pwmanager.util.LogUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 生成UUID去代表一個API業務的生命週期。
 */
@Component
@WebFilter
@Order(FilterOrderNumberConst.UUID_FILTER)
public class UUIDFilter extends OncePerRequestFilter {

    @Autowired
    private LogUtil logUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MyContentCachingReqWrapper reqWrapper = (MyContentCachingReqWrapper) request;
        ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) response;

        MyRequestContext myReqContext = (MyRequestContext) request.getAttribute(RequestAttributeFieldName.MY_REQ_CONTEXT);
        myReqContext.setUUID(logUtil.generateUUIDForLogging()); //因為上面getAttribute得到的是reference，所以這裡set，會直接set進該實例

        filterChain.doFilter(reqWrapper, resWrapper);
    }

}
