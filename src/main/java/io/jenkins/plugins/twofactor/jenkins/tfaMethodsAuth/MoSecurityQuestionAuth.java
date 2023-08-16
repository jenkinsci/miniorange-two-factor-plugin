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
package io.jenkins.plugins.twofactor.jenkins.tfaMethodsAuth;

import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.UtilityGlobalConstants.SESSION_2FA_VERIFICATION;
import static io.jenkins.plugins.twofactor.jenkins.MoFilter.userAuthenticationStatus;
import static jenkins.model.Jenkins.get;

import hudson.model.*;
import hudson.util.FormApply;
import io.jenkins.plugins.twofactor.constants.MoPluginUrls;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

// @Extension
public class MoSecurityQuestionAuth implements Action {
  private static final Logger LOGGER = Logger.getLogger(MoSecurityQuestionAuth.class.getName());
  int firstRandomSecurityQuestionIndex = 0;
  int secondRandomSecurityQuestionIndex = 1;
  private String[] securityQuestionArray;
  private String[] securityAnswerArray;
  public Map<String, Boolean> showWrongCredentialWarning = new HashMap<>();

  private static final Random RANDOM = new Random();

  private final User user;

  public MoSecurityQuestionAuth() {
    this.user = User.current();

    try {
      if (user != null) {
        MoSecurityQuestionConfig securityQuestion =
            user.getProperty(MoSecurityQuestionConfig.class);
        this.securityQuestionArray =
            new String[] {
              securityQuestion.getFirstSecurityQuestion(user),
              securityQuestion.getSecondSecurityQuestion(user),
              securityQuestion.getCustomSecurityQuestion(user)
            };
        this.securityAnswerArray =
            new String[] {
              securityQuestion.getFirstSecurityQuestionAnswer(user),
              securityQuestion.getSecondSecurityQuestionAnswer(user),
              securityQuestion.getCustomSecurityQuestionAnswer(user)
            };
      }
    } catch (Exception e) {
      LOGGER.fine(
          "Error in getting security questions and answers for user authentication "
              + e.getMessage());
    }
  }

  @Override
  public String getIconFileName() {
    return "";
  }

  @Override
  public String getDisplayName() {
    return MoPluginUrls.Urls.MO_SECURITY_QUESTION_AUTH.getUrl();
  }

  @Override
  public String getUrlName() {
    return MoPluginUrls.Urls.MO_SECURITY_QUESTION_AUTH.getUrl();
  }

  @SuppressWarnings("unused")
  public String getUserId() {
    return user != null ? user.getId() : "";
  }

  private void initializeRandomTwoIndex() {
    firstRandomSecurityQuestionIndex = RANDOM.nextInt(3);
    secondRandomSecurityQuestionIndex = RANDOM.nextInt(2);
    if (secondRandomSecurityQuestionIndex >= firstRandomSecurityQuestionIndex) {
      secondRandomSecurityQuestionIndex++;
    }
  }

  @SuppressWarnings("unused")
  public String getFirstRandomSecurityQuestion() {
    initializeRandomTwoIndex();
    return securityQuestionArray[firstRandomSecurityQuestionIndex];
  }

  @SuppressWarnings("unused")
  public String getSecondRandomSecurityQuestion() {
    return securityQuestionArray[secondRandomSecurityQuestionIndex];
  }

  private String getFirstRandomSecurityQuestionAnswer() {
    return securityAnswerArray[firstRandomSecurityQuestionIndex];
  }

  private String getSecondRandomSecurityQuestionAnswer() {
    return securityAnswerArray[secondRandomSecurityQuestionIndex];
  }

  @SuppressWarnings("unused")
  public boolean getShowWrongCredentialWarning() {
    return showWrongCredentialWarning.getOrDefault(user.getId(), false);
  }

  private boolean validateUserAnswers(net.sf.json.JSONObject formData) {
    return formData
            .get("userFirstAuthenticationAnswer")
            .toString()
            .equals(getFirstRandomSecurityQuestionAnswer())
        && formData
            .get("userSecondAuthenticationAnswer")
            .toString()
            .equals(getSecondRandomSecurityQuestionAnswer());
  }

  @SuppressWarnings("unused")
  @RequirePOST
  public void doSecurityQuestionAuthenticate(
      StaplerRequest staplerRequest, StaplerResponse staplerResponse) throws Exception {
    net.sf.json.JSONObject formData = staplerRequest.getSubmittedForm();
    HttpSession session = staplerRequest.getSession(false);
    String redirectUrl = get().getRootUrl();
    LOGGER.fine("Authenticating user tfa security answers");
    try {
      if (user == null) return;
      if (validateUserAnswers(formData)) {
        LOGGER.fine(user.getId() + " user is authentic");
        if (session != null) {
          redirectUrl = (String) session.getAttribute("tfaRelayState");
          session.removeAttribute("tfaRelayState");
          session.setAttribute(user.getId() + SESSION_2FA_VERIFICATION.getKey(), "true");
          userAuthenticationStatus.put(user.getId(), true);
        }
        showWrongCredentialWarning.put(user.getId(), false);
      } else {
        LOGGER.fine("User is not authentic");
        redirectUrl = "./";
        showWrongCredentialWarning.put(user.getId(), true);
      }
      LOGGER.fine("Redirecting user to " + redirectUrl);

      if (redirectUrl == null) redirectUrl = Jenkins.get().getRootUrl();

      FormApply.success(redirectUrl).generateResponse(staplerRequest, staplerResponse, null);
    } catch (Exception e) {
      LOGGER.fine("Exception while authenticating/Logging out the user " + e.getMessage());
    }
  }
}
