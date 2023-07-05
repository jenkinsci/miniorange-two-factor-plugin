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
package com.miniorange.twofactor.jenkins;

import static com.miniorange.twofactor.constants.MoPluginUrls.Urls.MO_TFA_GLOBAL_CONFIG;
import static jenkins.model.Jenkins.get;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

@SuppressWarnings("unused")
@Extension
public class MoGlobalConfigView extends ManagementLink implements Describable<MoGlobalConfigView> {

  private static final Logger LOGGER = Logger.getLogger(MoGlobalConfigView.class.getName());

  public MoGlobalConfigView() {}

  @Override
  public String getIconFileName() {
    if (isAdmin()) return "/plugin/miniorange-two-factor/images/tfaIcon.png";
    return null;
  }

  @Override
  public String getDisplayName() {
    return "2FA Global Configurations";
  }

  @Override
  public String getUrlName() {
    return MO_TFA_GLOBAL_CONFIG.getUrl();
  }

  @Override
  public String getDescription() {
    return "Configure two factor settings for your jenkins instance";
  }

  public boolean getEnableTfa() {
    return MoGlobalConfig.get().getEnableTfa();
  }

  public boolean getEnableSecurityQuestion() {
    return MoGlobalConfig.get().isEnableSecurityQuestionsAuthentication();
  }

  public boolean getEnableOtpOverEmail() {
    return MoGlobalConfig.get().isEnableOtpOverEmailAuthentication();
  }

  public String getSenderEmailAddress() {
    return MoGlobalConfig.get().getSenderEmailAddress();
  }

  @RequirePOST
  public void doSaveGlobalTfaSettings(StaplerRequest req, StaplerResponse rsp)
      throws IOException, ServletException, Descriptor.FormException {
    try {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      JSONObject json = req.getSubmittedForm();
      MoGlobalConfig moGlobalConfig = MoGlobalConfig.get();
      boolean isDataSaved = moGlobalConfig.configure(req, json);
    } catch (Exception e) {
      LOGGER.fine("Error while submitting global configurations, error: " + e.getMessage());
      throw e;
    }

    FormApply.success("./").generateResponse(req, rsp, null);
  }

  public @NonNull String getCategoryName() {
    return "SECURITY";
  }

  public String getBaseUrl() {
    return get().getRootUrl();
  }

  public boolean isAdmin() {
    return Jenkins.get().getACL().hasPermission(Jenkins.ADMINISTER);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Descriptor<MoGlobalConfigView> getDescriptor() {
    return (Descriptor<MoGlobalConfigView>) Jenkins.get().getDescriptor(getClass());
  }

  public MoGlobalConfig.DescriptorImpl getGlobalConfigDescriptor() {
    return MoGlobalConfig.DESCRIPTOR;
  }

  public MoGlobalConfig getGlobalConfig() {
    return MoGlobalConfig.get();
  }

  @Extension
  public static final class DescriptorImpl extends Descriptor<MoGlobalConfigView> {
    public DescriptorImpl() {}

    @POST
    public FormValidation doCheckSenderEmailAddress(@QueryParameter String senderEmailAddress) {
      String regex = "^(.+)@(.+)$";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(senderEmailAddress);
      if (StringUtils.isEmpty(senderEmailAddress)) {
        return FormValidation.error("Email is required.");
      } else if (!matcher.matches()) {
        return FormValidation.error("Please enter valid email");
      } else {
        return FormValidation.ok();
      }
    }

    public FormValidation doCheckEnableOtpOverEmail(@QueryParameter boolean enableOtpOverEmail) {
      if (enableOtpOverEmail) return FormValidation.ok();
      else return FormValidation.error("Please enable OTP Over Email first");
    }
  }
}
