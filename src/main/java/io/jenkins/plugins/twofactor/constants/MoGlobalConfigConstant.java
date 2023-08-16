package io.jenkins.plugins.twofactor.constants;

public class MoGlobalConfigConstant {
  public enum AdminConfiguration {
    ENABLE_2FA_FOR_ALL_USERS("ENABLE_MO_TFA_AUTHENTICATION");

    private final String key;

    AdminConfiguration(String setting) {
      this.key = setting;
    }

    public String getKey() {
      return key;
    }
  }

  public enum UtilityGlobalConstants {
    SESSION_2FA_VERIFICATION("_SESSION_2FA_VERIFICATION");
    private final String key;

    UtilityGlobalConstants(String setting) {
      this.key = setting;
    }

    public String getKey() {
      return key;
    }
  }
}
