package com.miniorange.twofactor.jenkins;


import com.miniorange.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import hudson.Extension;

import hudson.init.Initializer;
import hudson.model.User;
import hudson.util.PluginServletFilter;
import jenkins.model.Jenkins;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.miniorange.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA;

@Extension
public class MoFilter implements Filter {
    public static Map<String, Boolean> userAuthenticationStatus = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger(MoFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        try {
            userAuthenticationStatus.put(ENABLE_2FA.getSetting(), MoGlobalConfig.get().getEnableTfa());
        } catch (Exception e) {
            LOGGER.fine("Exception while initializing filter for global TFA authentication, error is " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @Initializer
    public static void setUpFilter() throws ServletException {
        LOGGER.fine("Setting up the filter for the two-factor plugin");
        PluginServletFilter.addFilter(new MoFilter());
    }

    private String sanitizeRequestURI( String requestURI) {
        requestURI = requestURI.trim();
        requestURI = requestURI.substring(0, Math.min(requestURI.length(), 60)); 
        return requestURI;
    }

    private boolean JenkinsUrlsToAvoidRedirect(String url) {
        return url.contains("/logout") ||
                url.contains("/login") ||
                url.contains("/adjuncts") ||
                url.contains("/static") ||
                url.contains("PopupContent") ||
                url.contains("/ajaxBuildQueue") ||
                url.contains("/ajaxExecutors") ||
                url.contains("/descriptorByName") ||
                url.contains("/checkPluginUrl") ||
                url.contains("/log");
    }

    private boolean tfaPluginUrlsToAvoidRedirect(String url) {
        return url.contains("tfaConfiguration/") ||
                url.contains("/MoSecurityQuestionIcon.png") ||
                url.contains("/securityQuestion/") ||
                url.contains("/miniorange-two-factor") ||
                url.contains("/tfaUserAuth");
    }

    private boolean guardConditions(User user, String url) {
        return user == null ||
                !userAuthenticationStatus.getOrDefault(ENABLE_2FA.getSetting(), false) ||
                userAuthenticationStatus.getOrDefault(user.getId(), false) ||
                tfaPluginUrlsToAvoidRedirect(url) ||
                JenkinsUrlsToAvoidRedirect(url);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try{
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            User user = User.current();
            HttpSession session = req.getSession();

            if (guardConditions(user, req.getPathInfo())) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            HttpServletResponse rsp = (HttpServletResponse) servletResponse;

            MoSecurityQuestionConfig userTfaData = user.getProperty(MoSecurityQuestionConfig.class);

            String relayState = req.getRequestURI();

            if (session.getAttribute("tfaRelayState") == null) {
                session.setAttribute("tfaRelayState", sanitizeRequestURI(relayState));
            }

            LOGGER.fine(req.getRequestURI() + " is being redirecting for 2FA, saved relay state is " + relayState);
            if (userTfaData.getFirstSecurityQuestion().equals("")) {
                rsp.sendRedirect(Jenkins.get().getRootUrl() + "user/" + user.getId() + "/tfaConfiguration/");
            } else {
                rsp.sendRedirect(Jenkins.get().getRootUrl() + "tfaUserAuth/");
            }
        } catch (Exception e) {
            filterChain.doFilter(servletRequest, servletResponse);
            LOGGER.fine("Error in filter processing " + e.getMessage());
        }


    }


    @Override
    public void destroy() {
    }
}
