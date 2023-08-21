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
}
