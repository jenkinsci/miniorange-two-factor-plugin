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
package io.jenkins.plugins.twofactor.jenkins;

import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA_FOR_ALL_USERS;
import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdvanceSettingsConstants.DEFAULT_OTP_EMAIL_SUBJECT;
import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdvanceSettingsConstants.DEFAULT_OTP_EMAIL_TEMPLATE;
import static io.jenkins.plugins.twofactor.jenkins.MoFilter.moPluginSettings;

import hudson.Extension;
import hudson.XmlFile;
import io.jenkins.plugins.twofactor.jenkins.dto.MoAdvanceSettingsDTO;
import io.jenkins.plugins.twofactor.jenkins.dto.MoOtpOverEmailDto;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class MoGlobalConfig extends GlobalConfiguration {

  private static final Logger LOGGER = Logger.getLogger(MoGlobalConfig.class.getName());
  private Boolean enableTfa;
  private Boolean enableTfaForAllUsers;
  private Boolean enableSecurityQuestionsAuthentication;
  private MoOtpOverEmailDto otpOverEmailDto;
  private MoAdvanceSettingsDTO moAdvancedSettingsDTO;


  public MoGlobalConfig() {
    load();
  }

  public Boolean getEnableTfa() {
    return enableTfa != null ? enableTfa : false;
  }
  public MoGlobalAdvancedSettings getAdvancedSettings() {
    return new MoGlobalAdvancedSettings(MoGlobalConfig.get().getAdvancedSettingsDTO());
  }

  @SuppressWarnings("unused")
  @DataBoundSetter
  public void setEnableTfa(boolean unableTfa) {
    enableTfa = unableTfa;
    enableTfaForAllUsers = enableTfa;
  }

  @SuppressWarnings("unused")
  public Boolean isEnableSecurityQuestionsAuthentication() {
    return enableSecurityQuestionsAuthentication != null
        ? enableSecurityQuestionsAuthentication
        : false;
  }

  @SuppressWarnings("unused")
  @DataBoundSetter
  public void setEnableSecurityQuestionsAuthentication(
      boolean enableSecurityQuestionsAuthentication) {
    this.enableSecurityQuestionsAuthentication = enableSecurityQuestionsAuthentication;
  }

  @SuppressWarnings("unused")
  public Boolean isEnableOtpOverEmailAuthentication() {
    return otpOverEmailDto != null ? otpOverEmailDto.getIsEnabled() : false;
  }

  @SuppressWarnings("unused")
  public String getSenderEmailAddress() {
    return otpOverEmailDto != null ? otpOverEmailDto.getSenderEmailAddress() : "";
  }

  public MoOtpOverEmailDto getOtpOverEmailDto() {
    return otpOverEmailDto;
  }
  public MoAdvanceSettingsDTO getAdvancedSettingsDTO() {
    return moAdvancedSettingsDTO;
  }

  public void saveMoGlobalConfigViewForm(JSONObject formData) {
    try {
      enableTfa = formData.getBoolean("enableTfa");
      enableTfaForAllUsers = enableTfa;
      moPluginSettings.put(ENABLE_2FA_FOR_ALL_USERS.getKey(), enableTfa);
      enableSecurityQuestionsAuthentication = formData.getBoolean("enableSecurityQuestion");

      if (formData.containsKey("enableOtpOverEmail")) {
        JSONObject otpOverEmail = formData.getJSONObject("enableOtpOverEmail");
        String senderEmailAddress = otpOverEmail.getString("senderEmailAddress");

        if (senderEmailAddress.isEmpty()) {
          throw new UnsupportedOperationException("Sender Email address can not be kept as empty");
        }
        otpOverEmailDto = new MoOtpOverEmailDto(true, senderEmailAddress);
      } else {
        otpOverEmailDto = null;
      }

      if (moAdvancedSettingsDTO == null) {
        moAdvancedSettingsDTO = new MoAdvanceSettingsDTO(DEFAULT_OTP_EMAIL_SUBJECT.getValue(),DEFAULT_OTP_EMAIL_TEMPLATE.getValue() );
      }

      this.save();
      LOGGER.fine("Saving global configuration ");
    } catch (Exception e) {
      LOGGER.fine("Error in saving mo global configuration " + e.getMessage());
    }
  }

  public void saveGlobalAdvancedSettingsForm(JSONObject formData) {
    try {
      LOGGER.fine("Saving advanced setting details");
      String customOTPEmailSubject = formData.getString("customOTPEmailSubject");
      String customOTPEmailTemplate = formData.getString("customOTPEmailTemplate");
      moAdvancedSettingsDTO = new MoAdvanceSettingsDTO(customOTPEmailSubject, customOTPEmailTemplate);
      this.save();
    } catch (Exception e) {
      LOGGER.fine("Error in saving 2FA global advance settings");
    }
  }

  @Override
  public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

    try{

      String formPage = formData.getString("formPage");
      switch (formPage) {
        case "basicConfig":
          saveMoGlobalConfigViewForm(formData);
          break;
        case "advanceSettingsConfig":
          saveGlobalAdvancedSettingsForm(formData);
          break;
        default:
          LOGGER.fine("Error in saving 2FA global settings for " + formPage);
          break;
      }
      if (formData.has("enableTfa")) {
        saveMoGlobalConfigViewForm(formData);
      }
    } catch (Exception e){
      LOGGER.fine("Not saved 2FA global settings for our plugin");
      return false;
    }
    return true;
  }

  @Override
  protected XmlFile getConfigFile() {
    File pluginDir = new File(Jenkins.get().getRootDir(), "/plugins/moTfaGlobalConfig");
    return new XmlFile(new File(pluginDir, this.getId() + ".xml"));
  }

  public static MoGlobalConfig get() {
    final MoGlobalConfig config;
    try {
      config = GlobalConfiguration.all().get(MoGlobalConfig.class);
    } catch (IllegalStateException e) {
      LOGGER.fine("Error in fetching global configuration class " + e.getMessage());
      throw e;
    }
    return config;
  }
}
