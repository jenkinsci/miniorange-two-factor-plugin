package com.miniorange.twofactor.constants;

public class MoGlobalConfigConstant {
  public enum AdminConfiguration {
    ENABLE_2FA("ENABLE_MO_TFA_AUTHENTICATION");
    private final String key;

    AdminConfiguration(String setting) {
      this.key = setting;
    }

    public String getKey() {
      return key;
    }
  }
}
