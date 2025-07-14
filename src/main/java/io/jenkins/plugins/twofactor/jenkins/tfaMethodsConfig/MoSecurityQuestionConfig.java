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
package io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig;

import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA_FOR_ALL_USERS;
import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.UtilityGlobalConstants.SESSION_2FA_VERIFICATION;
import static io.jenkins.plugins.twofactor.constants.MoPluginUrls.Urls.MO_SECURITY_QUESTION_CONFIG;
import static io.jenkins.plugins.twofactor.constants.MoPluginUrls.Urls.MO_USER_CONFIG;
import static io.jenkins.plugins.twofactor.constants.MoSecurityQuestionsConstant.SecurityQuestions;
import static io.jenkins.plugins.twofactor.constants.MoSecurityQuestionsConstant.SecurityQuestions.SELECT_SECURITY_QUESTION;
import static io.jenkins.plugins.twofactor.constants.MoSecurityQuestionsConstant.UserSecurityQuestionKey.*;
import static io.jenkins.plugins.twofactor.jenkins.MoFilter.moPluginSettings;
import static io.jenkins.plugins.twofactor.jenkins.MoFilter.userAuthenticationStatus;
import static jenkins.model.Jenkins.get;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import io.jenkins.plugins.twofactor.jenkins.MoGlobalConfig;
import io.jenkins.plugins.twofactor.jenkins.MoUserAuth;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.lang.NonNull;

public class MoSecurityQuestionConfig extends UserProperty implements Action {
  private static final Logger LOGGER = Logger.getLogger(MoSecurityQuestionConfig.class.getName());
  private Secret firstSecurityQuestion;
  private Secret secondSecurityQuestion;
  private Secret customSecurityQuestion;
  private Secret firstSecurityQuestionAnswer;
  private Secret secondSecurityQuestionAnswer;
  private Secret customSecurityQuestionAnswer;
  private boolean isConfigured;

  @DataBoundConstructor
  public MoSecurityQuestionConfig(
      Secret firstSecurityQuestion,
      Secret secondSecurityQuestion,
      Secret customSecurityQuestion,
      Secret firstSecurityQuestionAnswer,
      Secret secondSecurityQuestionAnswer,
      Secret customSecurityQuestionAnswer,
      boolean isConfigured) {
    this.firstSecurityQuestion = firstSecurityQuestion;
    this.secondSecurityQuestion = secondSecurityQuestion;
    this.customSecurityQuestion = customSecurityQuestion;
    this.firstSecurityQuestionAnswer = firstSecurityQuestionAnswer;
    this.secondSecurityQuestionAnswer = secondSecurityQuestionAnswer;
    this.customSecurityQuestionAnswer = customSecurityQuestionAnswer;
    this.isConfigured = isConfigured;
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "Security question";
  }

  @Override
  public String getUrlName() {
    return MO_SECURITY_QUESTION_CONFIG.getUrl();
  }

  @SuppressWarnings("unused")
  public boolean isUserAuthenticatedFromTfa() {
    return userAuthenticationStatus.getOrDefault(user.getId(), false);
  }

  @SuppressWarnings("unused")
  public String getBaseUrl() {
    return get().getRootUrl();
  }

  private boolean isFormFilledCorrectly(net.sf.json.JSONObject json) {
    return !json.getString(USER_FIRST_SECURITY_QUESTION.getKey())
            .equals(json.getString(USER_SECOND_SECURITY_QUESTION.getKey()))
        && !json.getString(USER_FIRST_SECURITY_QUESTION.getKey())
            .equals(SELECT_SECURITY_QUESTION.getQuestion())
        && !json.getString(USER_SECOND_SECURITY_QUESTION.getKey())
            .equals(SELECT_SECURITY_QUESTION.getQuestion())
        && StringUtils.isNotBlank(json.getString(USER_CUSTOM_SECURITY_QUESTION.getKey()))
        && StringUtils.isNotBlank(json.getString(USER_FIRST_SECURITY_QUESTION_ANSWER.getKey()))
        && StringUtils.isNotBlank(json.getString(USER_SECOND_SECURITY_QUESTION_ANSWER.getKey()))
        && StringUtils.isNotBlank(json.getString(USER_CUSTOM_SECURITY_QUESTION_ANSWER.getKey()));
  }

  @SuppressWarnings("unused")
  @RequirePOST
  public void doSaveSecurityQuestion(StaplerRequest req, StaplerResponse rsp) throws Exception {
    Jenkins.get().checkPermission(Jenkins.READ);
    LOGGER.fine("Saving user security questions");
    net.sf.json.JSONObject json = req.getSubmittedForm();
    String redirectUrl = req.getContextPath() + "./";
    boolean firstTimeUserLogIn = false;
    User user = User.current();
    try {
      if (isFormFilledCorrectly(json)) {
        if (user != null) {
          MoSecurityQuestionConfig userSecurityQuestion =
              user.getProperty(MoSecurityQuestionConfig.class);
          userSecurityQuestion.setFirstSecurityQuestion(
              Secret.fromString(json.getString(USER_FIRST_SECURITY_QUESTION.getKey())));
          userSecurityQuestion.setSecondSecurityQuestion(
              Secret.fromString(json.getString(USER_SECOND_SECURITY_QUESTION.getKey())));
          userSecurityQuestion.setCustomSecurityQuestion(
              Secret.fromString(json.getString(USER_CUSTOM_SECURITY_QUESTION.getKey())));
          userSecurityQuestion.setFirstSecurityQuestionAnswer(
              Secret.fromString(json.getString(USER_FIRST_SECURITY_QUESTION_ANSWER.getKey())));
          userSecurityQuestion.setSecondSecurityQuestionAnswer(
              Secret.fromString(json.getString(USER_SECOND_SECURITY_QUESTION_ANSWER.getKey())));
          userSecurityQuestion.setCustomSecurityQuestionAnswer(
              Secret.fromString(json.getString(USER_CUSTOM_SECURITY_QUESTION_ANSWER.getKey())));
          userSecurityQuestion.setConfigured(true);
          user.save();
        }
        HttpSession session = req.getSession(false);
        assert user != null;
        if (session != null) {
          redirectUrl = (String) session.getAttribute("tfaRelayState");
          session.removeAttribute("tfaRelayState");
          session.setAttribute(user.getId() + SESSION_2FA_VERIFICATION.getKey(), "true");
          userAuthenticationStatus.put(user.getId(), true);
        }

        if (redirectUrl != null) {
          LOGGER.fine("Saved security questions, redirecting user to " + redirectUrl);
          FormApply.success(redirectUrl).generateResponse(req, rsp, null);
        }
      }
    } catch (Exception e) {
      LOGGER.fine("Error in saving security questions, Form is not filled correctly ");
      throw new Exception("Cannot save security questions, exception is " + e.getMessage());
    }
    if (redirectUrl == null) {
      redirectUrl = Jenkins.get().getRootUrl();
    }
    LOGGER.fine("Redirecting user to " + redirectUrl);
    FormApply.success(redirectUrl).generateResponse(req, rsp, null);
  }

  @SuppressWarnings("unused")
  @RequirePOST
  public void doReset(StaplerRequest req, StaplerResponse rsp)
      throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.READ);
    try {
      MoSecurityQuestionConfig userSecurityQuestion =
          user.getProperty(MoSecurityQuestionConfig.class);
      userSecurityQuestion.setFirstSecurityQuestion(Secret.fromString(""));
      userSecurityQuestion.setSecondSecurityQuestion(Secret.fromString(""));
      userSecurityQuestion.setCustomSecurityQuestion(Secret.fromString(""));
      userSecurityQuestion.setFirstSecurityQuestionAnswer(Secret.fromString(""));
      userSecurityQuestion.setSecondSecurityQuestionAnswer(Secret.fromString(""));
      userSecurityQuestion.setCustomSecurityQuestionAnswer(Secret.fromString(""));
      userSecurityQuestion.setConfigured(false);
      LOGGER.fine("Resetting the security question authentication method");
      user.save();
    } catch (Exception e) {
      LOGGER.fine("Error in resetting the configuration " + e.getMessage());
    }
    FormApply.success(req.getReferer())
            .generateResponse(req, rsp, null);
  }

  public String getFirstSecurityQuestion(User user) {
    return user.getProperty(MoSecurityQuestionConfig.class).firstSecurityQuestion.getPlainText();
  }

  public String getSecondSecurityQuestion(User user) {
    return user.getProperty(MoSecurityQuestionConfig.class).secondSecurityQuestion.getPlainText();
  }

  public String getCustomSecurityQuestion(User user) {
    return user.getProperty(MoSecurityQuestionConfig.class).customSecurityQuestion.getPlainText();
  }

  public String getFirstSecurityQuestionAnswer(User user) {
    return user.getProperty(MoSecurityQuestionConfig.class)
        .firstSecurityQuestionAnswer
        .getPlainText();
  }

  public String getSecondSecurityQuestionAnswer(User user) {
    return user.getProperty(MoSecurityQuestionConfig.class)
        .secondSecurityQuestionAnswer
        .getPlainText();
  }

  public String getCustomSecurityQuestionAnswer(User user) {
    return user.getProperty(MoSecurityQuestionConfig.class)
        .customSecurityQuestionAnswer
        .getPlainText();
  }

  public void setFirstSecurityQuestion(Secret firstSecurityQuestion) {
    this.firstSecurityQuestion = firstSecurityQuestion;
  }

  public void setSecondSecurityQuestion(Secret secondSecurityQuestion) {
    this.secondSecurityQuestion = secondSecurityQuestion;
  }

  @SuppressWarnings("unused")
  public void setCustomSecurityQuestion(Secret customSecurityQuestion) {
    this.customSecurityQuestion = customSecurityQuestion;
  }

  public void setFirstSecurityQuestionAnswer(Secret firstSecurityQuestionAnswer) {
    this.firstSecurityQuestionAnswer = firstSecurityQuestionAnswer;
  }

  public void setSecondSecurityQuestionAnswer(Secret secondSecurityQuestionAnswer) {
    this.secondSecurityQuestionAnswer = secondSecurityQuestionAnswer;
  }

  public void setCustomSecurityQuestionAnswer(Secret customSecurityQuestionAnswer) {
    this.customSecurityQuestionAnswer = customSecurityQuestionAnswer;
  }

  public boolean isConfigured() {
    return isConfigured;
  }

  public void setConfigured(boolean Configured) {
    isConfigured = Configured;
  }

  @Override
  public UserPropertyDescriptor getDescriptor() {
    return new DescriptorImpl();
  }

  @SuppressWarnings("unused")
  public static final MoSecurityQuestionConfig.DescriptorImpl DESCRIPTOR =
      new MoSecurityQuestionConfig.DescriptorImpl();

  @Extension
  public static class DescriptorImpl extends UserPropertyDescriptor {
    public DescriptorImpl() {
      super(MoSecurityQuestionConfig.class);
    }
    public String getContextPath() {
      return MoUserAuth.getContextPath();
    }
    @Override
    public UserProperty newInstance(User user) {
      return new MoSecurityQuestionConfig(
          Secret.fromString(""),
          Secret.fromString(""),
          Secret.fromString(""),
          Secret.fromString(""),
          Secret.fromString(""),
          Secret.fromString(""),
          false);
    }

    @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Intentionally returning null to hide from UI")
    @Override
    public String getDisplayName() {
      return null;
    }

    @SuppressWarnings("unused")
    public Boolean showInUserProfile() {
      return moPluginSettings.getOrDefault(ENABLE_2FA_FOR_ALL_USERS.getKey(), false)
          && MoGlobalConfig.get().isEnableSecurityQuestionsAuthentication();
    }

    @SuppressWarnings("unused")
    public String getUserId() {
      User currentUser = User.current();
      if (currentUser == null) {
        return "";
      }

      return currentUser.getId();
    }

    private ListBoxModel fillSecurityQuestion() {
      ListBoxModel securityQuestionsDropDown = new ListBoxModel();
      for (SecurityQuestions securityQuestion : SecurityQuestions.values()) {
        securityQuestionsDropDown.add(new ListBoxModel.Option(securityQuestion.getQuestion()));
      }
      return securityQuestionsDropDown;
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public ListBoxModel doFillFirstSecurityQuestionItems() {
      Jenkins.get().checkPermission(Jenkins.READ);
      return fillSecurityQuestion();
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public ListBoxModel doFillSecondSecurityQuestionItems() {
      Jenkins.get().checkPermission(Jenkins.READ);
      return fillSecurityQuestion();
    }

    private FormValidation validateForm(boolean condition, String errorMessage) {
      if (condition) {
        return FormValidation.error(errorMessage);
      }
      return FormValidation.ok();
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public FormValidation doCheckFirstSecurityQuestion(
        @QueryParameter String firstSecurityQuestion,
        @QueryParameter String secondSecurityQuestion) {
      Jenkins.get().checkPermission(Jenkins.READ);
      return validateForm(
          firstSecurityQuestion.equals(SELECT_SECURITY_QUESTION.getQuestion())
              || firstSecurityQuestion.equals(secondSecurityQuestion),
          "Please select a valid security question");
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public FormValidation doCheckFirstSecurityQuestionAnswer(
        @QueryParameter String firstSecurityQuestionAnswer,
        @QueryParameter String secondSecurityQuestion) {
      Jenkins.get().checkPermission(Jenkins.READ);
      return validateForm(
          StringUtils.isBlank(firstSecurityQuestionAnswer), "Please Enter valid answer");
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public FormValidation doCheckSecondSecurityQuestion(
        @QueryParameter String secondSecurityQuestion,
        @QueryParameter String firstSecurityQuestion) {
      Jenkins.get().checkPermission(Jenkins.READ);
      return validateForm(
          secondSecurityQuestion.equals(SELECT_SECURITY_QUESTION.getQuestion())
              || secondSecurityQuestion.equals(firstSecurityQuestion),
          "Please select a valid security question");
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public FormValidation doCheckSecondSecurityQuestionAnswer(
        @QueryParameter String secondSecurityQuestionAnswer) {
      Jenkins.get().checkPermission(Jenkins.READ);
      return validateForm(
          StringUtils.isBlank(secondSecurityQuestionAnswer), "Please Enter valid answer");
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public FormValidation doCheckCustomSecurityQuestion(
        @QueryParameter String customSecurityQuestion) {
      Jenkins.get().checkPermission(Jenkins.READ);
      return validateForm(
          StringUtils.isBlank(customSecurityQuestion), "Please select a valid security question");
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public FormValidation doCheckCustomSecurityQuestionAnswer(
        @QueryParameter String customSecurityQuestionAnswer) {
      Jenkins.get().checkPermission(Jenkins.READ);
      return validateForm(
          StringUtils.isBlank(customSecurityQuestionAnswer), "Please Enter valid answer");
    }
  }
}
