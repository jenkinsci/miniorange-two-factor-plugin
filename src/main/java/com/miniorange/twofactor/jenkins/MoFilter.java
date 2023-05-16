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

import static com.miniorange.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA;

import com.miniorange.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.User;
import hudson.util.PluginServletFilter;
import java.io.IOException;
import java.util.HashMap;
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

  private static final Logger LOGGER = Logger.getLogger(MoFilter.class.getName());

  @Override
  public void init(FilterConfig filterConfig) {
    try {
      userAuthenticationStatus.put(ENABLE_2FA.getSetting(), MoGlobalConfig.get().getEnableTfa());
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

  private String sanitizeRequestURI(String requestURI) {
    requestURI = requestURI.trim();
    requestURI = requestURI.substring(0, Math.min(requestURI.length(), 60));
    return requestURI;
  }

  private boolean JenkinsUrlsToAvoidRedirect(String url) {
    return url.contains("/logout")
        || url.contains("/login")
        || url.contains("/adjuncts")
        || url.contains("/static")
        || url.contains("PopupContent")
        || url.contains("/ajaxBuildQueue")
        || url.contains("/ajaxExecutors")
        || url.contains("/descriptorByName")
        || url.contains("/checkPluginUrl")
        || url.contains("/log");
  }

  private boolean tfaPluginUrlsToAvoidRedirect(String url) {
    return url.contains("tfaConfiguration/")
        || url.contains("/MoSecurityQuestionIcon.png")
        || url.contains("/securityQuestion/")
        || url.contains("/miniorange-two-factor")
        || url.contains("/tfaUserAuth");
  }

  private boolean guardConditions(User user, String url) {
    return user == null
        || !userAuthenticationStatus.getOrDefault(ENABLE_2FA.getSetting(), false)
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

      HttpServletResponse rsp = (HttpServletResponse) servletResponse;

      MoSecurityQuestionConfig userTfaData = user.getProperty(MoSecurityQuestionConfig.class);

      String relayState = req.getRequestURI();

      if (session.getAttribute("tfaRelayState") == null) {
        session.setAttribute("tfaRelayState", sanitizeRequestURI(relayState));
      }

      LOGGER.fine(
          req.getRequestURI()
              + " is being redirecting for 2FA, saved relay state is "
              + relayState);
      if (userTfaData.getFirstSecurityQuestion().equals("")) {
        rsp.sendRedirect(
            Jenkins.get().getRootUrl() + "user/" + user.getId() + "/tfaConfiguration/");
      } else {
        rsp.sendRedirect(Jenkins.get().getRootUrl() + "tfaUserAuth/");
      }
    } catch (Exception e) {
      filterChain.doFilter(servletRequest, servletResponse);
      LOGGER.fine("Error in filter processing " + e.getMessage());
    }
  }

  @Override
  public void destroy() {}
}
