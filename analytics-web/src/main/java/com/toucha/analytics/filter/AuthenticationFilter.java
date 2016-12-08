package com.toucha.analytics.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.toucha.analytics.utils.AuthAccessTokenVerifyUtil;

public class AuthenticationFilter implements Filter {

    public static AuthenticationFilter authenticationFilter;

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        servletResponse.setHeader("Access-Control-Allow-Origin", "*");
        servletResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        servletResponse.setHeader("Access-Control-Max-Age", "3600");
        servletResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with");

         chain.doFilter(request, response);
        // get the accessToken
        /*String bearerToken = servletRequest.getHeader("authorization");

        if (bearerToken != null && (bearerToken.startsWith("Bearer ") || bearerToken.startsWith("bearer "))) {
            String token = bearerToken.substring(7);
            String[] uniqueId = new String[] { "" };
            int result = AuthAccessTokenVerifyUtil.validateToken(token, uniqueId);

            if (result == 0 && !uniqueId[0].equals("")) {
                // succeed
                request.setAttribute("uniqueId", uniqueId[0]);
                chain.doFilter(request, response);
            } else if (result == 2) {
                // expired token
                servletResponse.setStatus(401);
                servletResponse.setHeader("accessToken", "expired");
                response.getOutputStream().flush();
            } else {
                // invalid token Forbidden
                servletResponse.setStatus(401);
                servletResponse.setHeader("accessToken", "invalid");
                response.getOutputStream().flush();
            }

        } else {
            // Token required
            servletResponse.setStatus(401);
            response.getOutputStream().flush();
        }*/
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        authenticationFilter = this;
    }

}
