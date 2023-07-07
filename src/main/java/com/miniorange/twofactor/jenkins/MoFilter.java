/*
 * Copyright (c) 2023
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.miniorange.twofactor.jenkins;

import static com.miniorange.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.*;
import static com.miniorange.twofactor.constants.MoPluginUrls.Urls.*;

import com.miniorange.twofactor.jenkins.tfaMethodsConfig.MoOtpOverEmailConfig;
import com.miniorange.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.util.PluginServletFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;

@Extension
public class MoFilter implements Filter {
  public static Map<String, Boolean> userAuthenticationStatus = new HashMap<>();
  public static Map<String, Boolean> moPluginSettings = new HashMap<>();
  private static final Logger LOGGER = Logger.getLogger(MoFilter.class.getName());

  @Override
  public void init(FilterConfig filterConfig) {
    try {
      moPluginSettings.put(ENABLE_2FA.getKey(), MoGlobalConfig.get().getEnableTfa());
    } catch (Exception e) {
      LOGGER.fine(
          "Exception while initializing filter for global TFA authentication, error is "
              + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @Initializer
  public static void setUpFilter() throws ServletException {
    LOGGER.fine("Setting up the filter for the two-factor plugin");
    PluginServletFilter.addFilter(new MoFilter());
  }

  private String getRedirectUrlForTfaAuthentication(User user) {

    LOGGER.fine(" Calculating redirection url for 2FA authentication");
    String redirectUrl = null;
    int totalConfiguredMethods = 0;
    int totalEnabledMethods = 0;
    for (UserProperty property : user.getAllProperties()) {
      switch (property.getClass().getSimpleName()) {
        case "MoSecurityQuestionConfig":
          if (MoGlobalConfig.get().isEnableSecurityQuestionsAuthentication()) {
            totalEnabledMethods++;
            if (((MoSecurityQuestionConfig) property).isConfigured()) {
              redirectUrl = MO_USER_AUTH.getUrl() + "/" + MO_SECURITY_QUESTION_AUTH.getUrl() + "/";
              totalConfiguredMethods++;
            }
          }
          break;
        case "MoOtpOverEmailConfig":
          if (MoGlobalConfig.get().isEnableOtpOverEmailAuthentication()) {
            totalEnabledMethods++;
            if (((MoOtpOverEmailConfig) property).isConfigured()) {
              redirectUrl = MO_USER_AUTH.getUrl() + "/" + MO_OTP_OVER_EMAIL_AUTH.getUrl() + "/";
              totalConfiguredMethods++;
              break;
            }
          }
          break;
      }
    }

    if ((totalEnabledMethods != 0) && (totalConfiguredMethods == 0)) {
      LOGGER.fine(
          "User has not configured any authentication method, redirecting to user configuration");
      redirectUrl = "user/" + user.getId() + "/" + MO_USER_CONFIG.getUrl() + "/";
    } else if ((totalEnabledMethods != 0) && totalConfiguredMethods > 1) {
      LOGGER.fine(
          "User has configured multiple authentication methods, redirecting to user authentication");
      redirectUrl = MO_USER_AUTH.getUrl() + "/";
    } else if (totalEnabledMethods == 0) {
      LOGGER.fine("Admin has not enabled any authentication methods, terminating 2FA");
      redirectUrl = "SKIP_FILTER";
    }

    LOGGER.fine("Redirecting to url " + redirectUrl);
    return redirectUrl;
  }

  private String sanitizeRequestURI(String requestURI) {
    requestURI = requestURI.trim();
    requestURI = requestURI.substring(0, Math.min(requestURI.length(), 60));
    return requestURI;
  }

  private boolean urlsToAvoidRedirect(String url, List<String> urlsToCheck) {
    for (String avoidUrl : urlsToCheck) {
      if (url.contains(avoidUrl)) {
        return true;
      }
    }
    return false;
  }

  private boolean JenkinsUrlsToAvoidRedirect(String url) {
    List<String> jenkinsUrls =
        Arrays.asList(
            "/logout",
            "/login",
            "/adjuncts",
            "/static",
            "PopupContent",
            "/ajaxBuildQueue",
            "/ajaxExecutors",
            "/descriptorByName",
            "/checkPluginUrl",
            "/log");
    return urlsToAvoidRedirect(url, jenkinsUrls);
  }

  private boolean tfaPluginUrlsToAvoidRedirect(String url) {
    List<String> tfaPluginUrls =
        Arrays.asList(
            MO_USER_CONFIG.getUrl() + "/",
            MO_SECURITY_QUESTION_CONFIG.getUrl(),
            "/miniorange-two-factor",
            MO_OTP_OVER_EMAIL_CONFIG.getUrl(),
            MO_USER_AUTH.getUrl() + "/");
    return urlsToAvoidRedirect(url, tfaPluginUrls);
  }

  private boolean guardConditions(User user, String url) {
    return user == null
        || !moPluginSettings.getOrDefault(ENABLE_2FA.getKey(), false)
        || userAuthenticationStatus.getOrDefault(user.getId(), false)
        || tfaPluginUrlsToAvoidRedirect(url)
        || JenkinsUrlsToAvoidRedirect(url);
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      HttpServletRequest req = (HttpServletRequest) servletRequest;
      User user = User.current();
      HttpSession session = req.getSession();

      if (guardConditions(user, req.getPathInfo())) {
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      String redirectUrl = getRedirectUrlForTfaAuthentication(user);

      if (redirectUrl.equals("SKIP_FILTER")) {
        filterChain.doFilter(servletRequest, servletResponse);
        return;
      }

      HttpServletResponse rsp = (HttpServletResponse) servletResponse;
      String relayState = req.getRequestURI();

      if (session.getAttribute("tfaRelayState") == null) {
        session.setAttribute("tfaRelayState", sanitizeRequestURI(relayState));
      }

      LOGGER.fine(
          req.getRequestURI()
              + " is being redirecting for 2FA, saved relay state is "
              + relayState);

      rsp.sendRedirect(Jenkins.get().getRootUrl() + redirectUrl);
    } catch (Exception e) {
      filterChain.doFilter(servletRequest, servletResponse);
      LOGGER.fine("Error in filter processing " + e.getMessage());
    }
  }

  @Override
  public void destroy() {}
}
