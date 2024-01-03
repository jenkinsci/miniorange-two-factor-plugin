package io.jenkins.plugins.twofactor.jenkins.dto;

public class MoAdvanceSettingsDTO {

  private String customOTPEmailSubject;
  private String customOTPEmailTemplate;

  public MoAdvanceSettingsDTO(String customOTPEmailSubject, String customOTPEmailTemplate) {
    this.customOTPEmailSubject = customOTPEmailSubject;
    this.customOTPEmailTemplate = customOTPEmailTemplate;
  }

  public String getCustomOTPEmailSubject() {
    return customOTPEmailSubject;
  }

  public void setCustomOTPEmailSubject(String customOTPEmailSubject) {
    this.customOTPEmailSubject = customOTPEmailSubject;
  }

  public String getCustomOTPEmailTemplate() {
    return customOTPEmailTemplate;
  }

  public void setCustomOTPEmailTemplate(String customOTPEmailTemplate) {
    this.customOTPEmailTemplate = customOTPEmailTemplate;
  }
}
