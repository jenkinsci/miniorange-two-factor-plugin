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

import static hudson.tasks.Mailer.stringToAddress;
import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdvanceSettingsConstants.DEFAULT_OTP_EMAIL_SUBJECT;
import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdvanceSettingsConstants.DEFAULT_OTP_EMAIL_TEMPLATE;
import static io.jenkins.plugins.twofactor.jenkins.MoFilter.userAuthenticationStatus;
import static io.jenkins.plugins.twofactor.jenkins.MoUserAuth.allow2FaAccessAndRedirect;
import static org.apache.commons.lang.StringUtils.isBlank;
import hudson.Util;
import hudson.model.Action;
import hudson.model.User;
import hudson.tasks.Mailer;
import hudson.tasks.SMTPAuthentication;
import hudson.util.FormApply;
import hudson.util.Secret;
import io.jenkins.plugins.twofactor.constants.MoPluginUrls;
import io.jenkins.plugins.twofactor.jenkins.MoGlobalConfig;
import io.jenkins.plugins.twofactor.jenkins.MoUserAuth;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoOtpOverEmailConfig;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class MoOtpOverEmailAuth implements Action {

  private static final Logger LOGGER = Logger.getLogger(MoOtpOverEmailAuth.class.getName());
  private static final Map<String, String> sentOtp = new HashMap<>();
  public Map<String, Boolean> showWrongCredentialWarning = new HashMap<>();
  private static final Random RANDOM = new Random();

  private final User user;

  public MoOtpOverEmailAuth() {
    user = User.current();
  }

  @Override
  public String getIconFileName() {
    return "";
  }

  @Override
  public String getDisplayName() {
    return MoPluginUrls.Urls.MO_OTP_OVER_EMAIL_AUTH.getUrl();
  }

  @Override
  public String getUrlName() {
    return MoPluginUrls.Urls.MO_OTP_OVER_EMAIL_AUTH.getUrl();
  }

  public String getContextPath(){
    return MoUserAuth.getContextPath();
  }
  @SuppressWarnings("unused")
  public boolean isOtpSentToUser() {
    return !sentOtp.getOrDefault(user.getId(), "").isEmpty();
  }

  public String getUserEmailAddress() {
    return user.getProperty(hudson.tasks.Mailer.UserProperty.class).getAddress();
  }

  @SuppressWarnings("unused")
  public String getUserId() {
    return user != null ? user.getId() : "";
  }

  @SuppressWarnings("unused")
  public boolean isUserAuthenticatedFromTfa() {
    return userAuthenticationStatus.getOrDefault(user.getId(), false);
  }

  @SuppressWarnings("unused")
  public boolean getShowWrongCredentialWarning() {
    return showWrongCredentialWarning.getOrDefault(user.getId(), false);
  }

  public boolean isOtpOverEmailConfigured() {
    MoOtpOverEmailConfig otpOverEmailConfig = user.getProperty(MoOtpOverEmailConfig.class);
    return otpOverEmailConfig != null && otpOverEmailConfig.isConfigured();
  }

  private String createOtp(int len) {
    String numbers = "0123456789";
    char[] otp = new char[len];

    for (int i = 0; i < len; i++) {
      otp[i] = numbers.charAt(RANDOM.nextInt(numbers.length()));
    }
    return String.valueOf(otp);
  }

  private static jakarta.mail.Session createSession(
      String smtpHost,
      String smtpPort,
      boolean useSsl,
      boolean useTls,
      String smtpAuthUserName,
      Secret smtpAuthPassword) {
    final String SMTP_PORT_PROPERTY = "mail.smtp.port";
    final String SMTP_SOCKETFACTORY_PORT_PROPERTY = "mail.smtp.socketFactory.port";
    final String SMTP_SSL_ENABLE_PROPERTY = "mail.smtp.ssl.enable";
    final String SMTP_SSL_CHECKSERVERIDENTITY = "mail.smtp.ssl.checkserveridentity";

    smtpHost = Util.fixEmptyAndTrim(smtpHost);
    smtpPort = Util.fixEmptyAndTrim(smtpPort);
    smtpAuthUserName = Util.fixEmptyAndTrim(smtpAuthUserName);

    Properties props = new Properties(System.getProperties());
    if (smtpHost != null) {
      props.put("mail.smtp.host", smtpHost);
    }
    if (smtpPort != null) {
      props.put(SMTP_PORT_PROPERTY, smtpPort);
    }
    if (useSsl) {
      if (props.getProperty(SMTP_SOCKETFACTORY_PORT_PROPERTY) == null) {
        String port = smtpPort == null ? "465" : smtpPort;
        props.put(SMTP_PORT_PROPERTY, port);
        props.put(SMTP_SOCKETFACTORY_PORT_PROPERTY, port);
      }
      if (props.getProperty(SMTP_SSL_ENABLE_PROPERTY) == null) {
        props.put(SMTP_SSL_ENABLE_PROPERTY, "true");
        props.put(SMTP_SSL_CHECKSERVERIDENTITY, true);
      }
      props.put("mail.smtp.socketFactory.fallback", "false");
      if (props.getProperty("mail.smtp.ssl.checkserveridentity") == null) {
        props.put("mail.smtp.ssl.checkserveridentity", "true");
      }
    }
    if (useTls) {
      if (props.getProperty(SMTP_SOCKETFACTORY_PORT_PROPERTY) == null) {
        String port = smtpPort == null ? "587" : smtpPort;
        props.put(SMTP_PORT_PROPERTY, port);
        props.put(SMTP_SOCKETFACTORY_PORT_PROPERTY, port);
      }
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.starttls.required", "true");
    }
    if (smtpAuthUserName != null) props.put("mail.smtp.auth", "true");

    props.put("mail.smtp.timeout", "60000");
    props.put("mail.smtp.connectiontimeout", "60000");

    return jakarta.mail.Session.getInstance(
        props, getAuthenticator(smtpAuthUserName, Secret.toString(smtpAuthPassword)));
  }

  private static jakarta.mail.Authenticator getAuthenticator(
      final String smtpAuthUserName, final String smtpAuthPassword) {
    if (smtpAuthUserName == null) {
      return null;
    }
    return new jakarta.mail.Authenticator() {
      @Override
      protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
        return new jakarta.mail.PasswordAuthentication(smtpAuthUserName, smtpAuthPassword);
      }
    };
  }

  public void sendMail() {
    try {
      LOGGER.fine("Sending mail for otpOverEmail method");

      Mailer.DescriptorImpl mailerDescriptor = Mailer.descriptor();
      String smtpHost = mailerDescriptor.getSmtpHost();
      String senderEmailAddress = MoGlobalConfig.get().getOtpOverEmailDto().getSenderEmailAddress();
      SMTPAuthentication MailerAuthentication = mailerDescriptor.getAuthentication();
      String username = MailerAuthentication != null ? MailerAuthentication.getUsername() : null;
      Secret password = MailerAuthentication != null ? MailerAuthentication.getPassword() : null;
      boolean useSsl = mailerDescriptor.getUseSsl();
      boolean useTls = mailerDescriptor.getUseTls();
      String smtpPort = mailerDescriptor.getSmtpPort();
      String charset = mailerDescriptor.getCharset();
      String sendTestMailTo = getUserEmailAddress();

      jakarta.mail.internet.MimeMessage msg =
          new jakarta.mail.internet.MimeMessage(
              createSession(smtpHost, smtpPort, useSsl, useTls, username, password));

      String otpToSend = createOtp(5);
      sentOtp.put(user.getId(), otpToSend);

      String subject = MoGlobalConfig.get().getAdvancedSettings().getCustomOTPEmailSubject();
      if(isBlank(subject))
        subject = DEFAULT_OTP_EMAIL_SUBJECT.getValue();

      String template = MoGlobalConfig.get().getAdvancedSettings().getCustomOTPEmailTemplate();

      if(isBlank(template))
        template = DEFAULT_OTP_EMAIL_TEMPLATE.getValue();

      if (subject.contains("$username")) {
        subject = subject.replace("$username", user.getId());
      }

      if (subject.contains("$otp")) {
        subject = subject.replace("$otp", sentOtp.get(user.getId()));
      }

      if (template.contains("$username")) {
        template = template.replace("$username", user.getId());
      }

      if (template.contains("$otp")) {
        template = template.replace("$otp", sentOtp.get(user.getId()));
      }
      msg.setSubject(subject);
      msg.setContent(template, "text/html");
      msg.setFrom(stringToAddress(senderEmailAddress, charset));
      if (StringUtils.isNotBlank(sendTestMailTo)) {
        msg.setReplyTo(new jakarta.mail.Address[] {stringToAddress(sendTestMailTo, charset)});
      }

      msg.setSentDate(new Date());
      msg.setRecipient(
          jakarta.mail.Message.RecipientType.TO, stringToAddress(sendTestMailTo, charset));

      jakarta.mail.Transport.send(msg);
    }
    catch (RuntimeException e) {
      LOGGER.fine("Run time exception occur" + e.getMessage());
      throw e;
    }
    catch (Exception e) {
      LOGGER.fine("Failed in sending mail, error is " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @RequirePOST
  public void doResendOtp(StaplerRequest req, StaplerResponse rsp)
      throws ServletException, IOException {
    Jenkins.get().checkPermission(Jenkins.READ);
    try {
      sendMail();
    } catch (Exception e) {
      LOGGER.fine("Failed to send mail to user " + e.getMessage());
    }
    FormApply.success("./").generateResponse(req, rsp, null);
  }

  @SuppressWarnings("unused")
  @RequirePOST
  public void doSaveOrValidateOtpOverEmailConfig(StaplerRequest req, StaplerResponse rsp)
      throws Exception {
    Jenkins.get().checkPermission(Jenkins.READ);
    if (sentOtp.get(user.getId()) == null) {
      return;
    }

    boolean isOtpOverEmailConfigured = isOtpOverEmailConfigured();
    net.sf.json.JSONObject json = req.getSubmittedForm();
    String redirectUrl = req.getContextPath() + "./";
    try {
      String userInputOtp = json.getString("emailOtpForVerification");
      HttpSession session = req.getSession(false);
      String LoggingAction = "Authenticating the OTPOverEmail  OTP to login user";

      if (!isOtpOverEmailConfigured) {
        LoggingAction = "Authenticating the OTP to set OTPOverEmailConfig for user";
      }

      LOGGER.fine(LoggingAction);

      MoOtpOverEmailConfig otpOverEmailConfig = user.getProperty(MoOtpOverEmailConfig.class);

      if (userInputOtp.equals(sentOtp.get(user.getId()))) {
        LOGGER.fine("Otp is authentic");
        otpOverEmailConfig.setConfigured(true);
        sentOtp.remove(user.getId());
        redirectUrl = allow2FaAccessAndRedirect(session, user, showWrongCredentialWarning);
      } else {
        LOGGER.fine("Entered wrong otp for otpOverEmailConfig");
        redirectUrl = "./";
        showWrongCredentialWarning.put(user.getId(), true);
      }
      if (!isOtpOverEmailConfigured) {
        user.save();
      }

      if (redirectUrl == null) redirectUrl = Jenkins.get().getRootUrl();
      LOGGER.fine("Redirecting" + user.getId() + " from otpOverEmailAuth to " + redirectUrl);
      FormApply.success(redirectUrl).generateResponse(req, rsp, null);

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.fine("Something went wrong in Otp Over Email, Form is not filled correctly ");
      throw new Exception("Something went wrong in Otp Over Email, exception is " + e.getMessage());
    }

    LOGGER.fine("Redirecting user from otpOverEmailAuth to " + redirectUrl);
    FormApply.success(redirectUrl).generateResponse(req, rsp, null);
  }
}
