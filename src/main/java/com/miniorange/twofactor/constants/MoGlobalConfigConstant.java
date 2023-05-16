package com.miniorange.twofactor.constants;

public class MoGlobalConfigConstant {
  public enum AdminConfiguration {
    ENABLE_2FA("ENABLE_MO_TFA_AUTHENTICATION");
    private final String setting;

    AdminConfiguration(String setting) {
      this.setting = setting;
    }

    public String getSetting() {
      return setting;
    }
  }
}
