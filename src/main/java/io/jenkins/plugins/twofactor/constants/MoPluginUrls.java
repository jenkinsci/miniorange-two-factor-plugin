package io.jenkins.plugins.twofactor.constants;

public class MoPluginUrls {
  public enum Urls {
    MO_USER_CONFIG("tfaConfiguration"),
    MO_USER_AUTH("tfaUserAuth"),
    MO_SECURITY_QUESTION_CONFIG("securityQuestionConfig"),
    MO_SECURITY_QUESTION_AUTH("securityQuestionAuth"),
    MO_OTP_OVER_EMAIL_CONFIG("otpOverEmail"),
    MO_OTP_OVER_EMAIL_AUTH("otpOverEmailAuth"),
    MO_TFA_GLOBAL_CONFIG("tfaGlobalConfig"),
    MO_TFA_GLOBAL_ADVANCED_SETTINGS("advancedSettings"),
    MO_TFA_USER_MANAGEMENT("userManagement");


    private final String url;

    Urls(String question) {
      this.url = question;
    }

    public String getUrl() {
      return url;
    }
  }
}
