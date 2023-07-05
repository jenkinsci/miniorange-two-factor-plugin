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
package com.miniorange.twofactor.jenkins.tfaMethodsAuth;

import static com.miniorange.twofactor.constants.MoPluginUrls.Urls.MO_OTP_OVER_EMAIL_AUTH;
import static com.miniorange.twofactor.jenkins.MoFilter.userAuthenticationStatus;

import com.miniorange.twofactor.jenkins.MoGlobalConfig;
import com.miniorange.twofactor.jenkins.tfaMethodsConfig.MoOtpOverEmailConfig;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.tasks.Mailer;
import hudson.tasks.SMTPAuthentication;
import hudson.util.FormApply;
import hudson.util.Secret;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
public class MoOtpOverEmailAuth implements Action, Describable<MoOtpOverEmailAuth> {

  private static final Logger LOGGER = Logger.getLogger(MoOtpOverEmailAuth.class.getName());
  private static final Map<String, String> sentOtp = new HashMap<>();
  public Map<String, Boolean> showWrongCredentialWarning = new HashMap<>();
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
    return MO_OTP_OVER_EMAIL_AUTH.getUrl();
  }

  @Override
  public String getUrlName() {
    return MO_OTP_OVER_EMAIL_AUTH.getUrl();
  }

  @SuppressWarnings("unused")
  public boolean isOtpSentToUser() {
    return !sentOtp.getOrDefault(user.getId(), "").equals("");
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
    Random random = new Random();
    char[] otp = new char[len];

    for (int i = 0; i < len; i++) {
      otp[i] = numbers.charAt(random.nextInt(numbers.length()));
    }
    return String.valueOf(otp);
  }

  public void sendMail() {
    try {
      LOGGER.fine("Sending mail for otpOverEmail method");

      Session session = null;
      try {
        Mailer.DescriptorImpl mailerDescriptor =
                Jenkins.get().getDescriptorByType(Mailer.DescriptorImpl.class);

        String smtpHost = mailerDescriptor.getSmtpHost();
        String smtpPort = mailerDescriptor.getSmtpPort();
        SMTPAuthentication authentication = mailerDescriptor.getAuthentication();

        assert authentication != null;
        String smtpUsername = authentication.getUsername();
        Secret smtpPassword = authentication.getPassword();
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort != null ? smtpPort : "587");
        properties.put("mail.smtp.auth", "true");
        session =
                Session.getInstance(
                        properties,
                        new Authenticator() {
                          protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(smtpUsername, Secret.toString(smtpPassword));
                          }
                        });
      } catch (Exception e) {
        LOGGER.fine(
                "Error in sending mail, can not make session : some problem in Jenkins Mailer plugin");
      }

      Message message = new MimeMessage(session);
      String senderEmailAddress = MoGlobalConfig.get().getOtpOverEmailDto().getSenderEmailAddress();
      message.setFrom(new InternetAddress(senderEmailAddress));
      String email = getUserEmailAddress();
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
      message.setSubject("Jenkins 2FA Verification Code");
      String otpToSend = createOtp(5);
      sentOtp.put(user.getId(), otpToSend);
      message.setContent(
              "<html><body><h1>Jenkins Account Verification Code</h1><p>Your verification code is: "
                      + sentOtp.get(user.getId())
                      + "</p></body></html>",
              "text/html");
      Transport.send(message);
    } catch (Exception e) {
      LOGGER.fine("Failed to send mail to user " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  public void doResendOtp(StaplerRequest req, StaplerResponse rsp)
          throws ServletException, IOException {
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

    if (sentOtp.get(user.getId()) == null) {
      return;
    }

    boolean isOtpOverEmailConfigured = isOtpOverEmailConfigured();
    net.sf.json.JSONObject json = req.getSubmittedForm();
    String redirectUrl = req.getContextPath() + "./";
    try {
      String userInputOtp = json.getString("emailOtpForVerification");
      HttpSession session = req.getSession(false);
      if (!isOtpOverEmailConfigured) {
        LOGGER.fine("Authenticating the OTP to set OTPOverEmailConfig for user");
        MoOtpOverEmailConfig otpOverEmailConfig = user.getProperty(MoOtpOverEmailConfig.class);
        if (userInputOtp.equals(sentOtp.get(user.getId()))) {
          LOGGER.fine("Otp is authentic");
          otpOverEmailConfig.setConfigured(true);
          userAuthenticationStatus.put(user.getId(), true);
          showWrongCredentialWarning.put(user.getId(), false);
          sentOtp.remove(user.getId());

          if (session != null) {
            redirectUrl = (String) session.getAttribute("tfaRelayState");
            session.removeAttribute("tfaRelayState");
          } else {
            LOGGER.fine("Entered wrong otp for otpOverEmailConfig");
            redirectUrl = "./";
            showWrongCredentialWarning.put(user.getId(), true);
          }
        } else {
          LOGGER.fine("Entered wrong otp for otpOverEmailConfig");
          redirectUrl = "./";
          showWrongCredentialWarning.put(user.getId(), true);
        }

        user.save();
      } else {
        LOGGER.fine("Authenticating the OTPOverEmail  OTP to login user");
        if (userInputOtp.equals(sentOtp.get(user.getId()))) {
          LOGGER.fine("User is authentic");
          userAuthenticationStatus.put(user.getId(), true);
          showWrongCredentialWarning.put(user.getId(), false);
          sentOtp.remove(user.getId());
          if (session != null) {
            redirectUrl = (String) session.getAttribute("tfaRelayState");
            session.removeAttribute("tfaRelayState");
          }

        } else {
          LOGGER.fine("User is not authentic");
          redirectUrl = "./";
          showWrongCredentialWarning.put(user.getId(), true);
        }
      }
      if (redirectUrl == null) redirectUrl = Jenkins.get().getRootUrl();
      LOGGER.fine("Redirecting user from otpOverEmailAuth to " + redirectUrl);
      FormApply.success(redirectUrl).generateResponse(req, rsp, null);

    } catch (Exception e) {
      LOGGER.fine("Something went wrong in Otp Over Email, Form is not filled correctly ");
      throw new Exception("Something went wrong in Otp Over Email, exception is " + e.getMessage());
    }

    LOGGER.fine("Redirecting user from otpOverEmailAuth to " + redirectUrl);
    FormApply.success(redirectUrl).generateResponse(req, rsp, null);
  }

  @Override
  public MoOtpOverEmailAuth.DescriptorImpl getDescriptor() {
    return (MoOtpOverEmailAuth.DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
  }

  @SuppressWarnings("unused")
  public static final MoOtpOverEmailAuth.DescriptorImpl DESCRIPTOR =
          new MoOtpOverEmailAuth.DescriptorImpl();

  @Extension
  public static class DescriptorImpl extends Descriptor<MoOtpOverEmailAuth> {}
}
