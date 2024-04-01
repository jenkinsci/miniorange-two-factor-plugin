package io.jenkins.plugins.twofactor.jenkins;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import io.jenkins.plugins.twofactor.jenkins.dto.MoAdvanceSettingsDTO;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdvanceSettingsConstants.DEFAULT_OTP_EMAIL_SUBJECT;
import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdvanceSettingsConstants.DEFAULT_OTP_EMAIL_TEMPLATE;
import static io.jenkins.plugins.twofactor.constants.MoPluginUrls.Urls.MO_TFA_GLOBAL_ADVANCED_SETTINGS;
import static org.apache.commons.lang.StringUtils.isBlank;

public class MoGlobalAdvancedSettings implements Action, Describable<MoGlobalAdvancedSettings> {
  private static final Logger LOGGER = Logger.getLogger(MoGlobalAdvancedSettings.class.getName());

  private final MoAdvanceSettingsDTO moAdvanceSettingsDto;

  public MoGlobalAdvancedSettings(MoAdvanceSettingsDTO moAdvanceSettingsDto) {
    this.moAdvanceSettingsDto = moAdvanceSettingsDto;
  }

  @Override
  public String getIconFileName() {
    return "symbol-settings";
  }

  @Override
  public String getDisplayName() {
    return "advanced-settings";
  }

  @Override
  public String getUrlName() {
    return MO_TFA_GLOBAL_ADVANCED_SETTINGS.getUrl();
  }

  @SuppressWarnings("unused")
  @Override
  public Descriptor<MoGlobalAdvancedSettings> getDescriptor() {
    Jenkins jenkins = Jenkins.get();
    return (Descriptor<MoGlobalAdvancedSettings>) jenkins.getDescriptorOrDie(getClass());
  }


  @SuppressWarnings("unused")
  @RequirePOST
  @Restricted(NoExternalUse.class)
  public void doSaveAdvancedSettingsConfiguration(StaplerRequest request, StaplerResponse response)
      throws ServletException, IOException {
    try {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      JSONObject json = request.getSubmittedForm();
      MoGlobalConfig moGlobalConfig = MoGlobalConfig.get();
      moGlobalConfig.configure(request, json);
    } catch (Exception e) {
      LOGGER.fine("Error while submitting global configurations, error: " + e.getMessage());
    }

    FormApply.success("./").generateResponse(request, response, null);
  }

  public String getCustomOTPEmailSubject() {
    String subject = null;
    if(moAdvanceSettingsDto != null) {
      subject = moAdvanceSettingsDto.getCustomOTPEmailSubject();
    }
    if(isBlank(subject)){
      subject = DEFAULT_OTP_EMAIL_SUBJECT.getValue();
    }
    return subject;
  }
  public String getCustomOTPEmailTemplate() {
    String template = null;
    if(moAdvanceSettingsDto != null) {
      template = moAdvanceSettingsDto.getCustomOTPEmailTemplate();
    }
    if(isBlank(template)){
      template = DEFAULT_OTP_EMAIL_TEMPLATE.getValue();
    }
    return template;
  }

  @SuppressWarnings("unused")
  @Extension
  public static final class DescriptorImpl extends Descriptor<MoGlobalAdvancedSettings> {
    public DescriptorImpl() {}

    @POST
    @SuppressWarnings("unused")
    public FormValidation doCheckSkipTfaForApi(@QueryParameter Boolean skipTfaForApi) {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      return FormValidation.warning("Available in premium version");
    }

    @POST
    @SuppressWarnings("unused")
    public FormValidation doCheckEnableTfaOnBuild(@QueryParameter Boolean enableTfaOnBuild) {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      return FormValidation.warning("Available in premium version");
    }

  }
}
