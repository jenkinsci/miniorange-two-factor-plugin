package io.jenkins.plugins.twofactor.constants;

import hudson.util.Secret;

public class MoGlobalConfigConstant {
  public enum AdminConfiguration {
    ENABLE_2FA_FOR_ALL_USERS(Secret.fromString("ENABLE_MO_TFA_AUTHENTICATION"));

    private final Secret key;

    AdminConfiguration(Secret setting) {
      this.key = setting;
    }

    public String getKey() {
      return key.getPlainText();
    }
  }

  public enum UtilityGlobalConstants {
    SESSION_2FA_VERIFICATION(Secret.fromString("_SESSION_2FA_VERIFICATION"));
    private final Secret key;

    UtilityGlobalConstants(Secret setting) {
      this.key = setting;
    }

    public String getKey() {
      return key.getPlainText();
    }
  }

  public enum AdvanceSettingsConstants{
    DEFAULT_OTP_EMAIL_SUBJECT("Jenkins 2FA Verification Code"),
    DEFAULT_OTP_EMAIL_TEMPLATE("<html><body><h1>Jenkins Account Verification Code</h1><p>Dear $username, <br></p><p>Your two factor verification code is: $otp </p><p>Please use this passcode to complete your action.</p> <br><br>Thank you.</body></html>");

    private final String value;

    AdvanceSettingsConstants(String setting) {
      this.value = setting;
    }

    public String getValue() {
      return value;
    }

  }
}
