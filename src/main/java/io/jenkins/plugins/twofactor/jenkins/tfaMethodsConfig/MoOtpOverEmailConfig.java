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
package io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig;

import static io.jenkins.plugins.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA_FOR_ALL_USERS;
import static io.jenkins.plugins.twofactor.constants.MoPluginUrls.Urls.MO_USER_CONFIG;
import static io.jenkins.plugins.twofactor.jenkins.MoFilter.moPluginSettings;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.util.FormApply;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;

import io.jenkins.plugins.twofactor.jenkins.MoGlobalConfig;
import io.jenkins.plugins.twofactor.jenkins.MoUserAuth;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class MoOtpOverEmailConfig extends UserProperty implements Action {
  private static final Logger LOGGER = Logger.getLogger(MoOtpOverEmailConfig.class.getName());
  public Boolean isConfigured;

  @DataBoundConstructor
  public MoOtpOverEmailConfig(Boolean isConfigured) {
    this.isConfigured = isConfigured;
  }

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "OTP over email";
  }

  @Override
  public String getUrlName() {
    return "otpOverEmailConfig";
  }


  @SuppressWarnings("unused")
  @RequirePOST
  public void doReset(StaplerRequest req, StaplerResponse rsp)
      throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.READ);
    try {
      MoOtpOverEmailConfig otpOverEmailConfig = user.getProperty(MoOtpOverEmailConfig.class);
      otpOverEmailConfig.setConfigured(false);
      LOGGER.fine("Resetting the OTP over email authentication method");
      user.save();
    } catch (Exception e) {
      LOGGER.fine("Error in resetting the OTP over email config");
    }
    FormApply.success(req.getReferer())
            .generateResponse(req, rsp, null);
  }

  public Boolean isConfigured() {
    return isConfigured;
  }

  public void setConfigured(Boolean Configured) {
    isConfigured = Configured;
  }

  @SuppressWarnings("unused")
  @Extension
  public static class DescriptorImpl extends UserPropertyDescriptor {
    public DescriptorImpl() {
      super(MoOtpOverEmailConfig.class);
    }

    @Override
    public UserProperty newInstance(User user) {
      return new MoOtpOverEmailConfig(false);
    }

    @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Intentionally returning null to hide from UI")
    @Override
    public String getDisplayName() {
      return null;
    }

    @SuppressWarnings("unused")
    public Boolean showInUserProfile() {
      return moPluginSettings.getOrDefault(ENABLE_2FA_FOR_ALL_USERS.getKey(), false) && MoGlobalConfig.get().isEnableOtpOverEmailAuthentication();
    }

    @SuppressWarnings("unused")
    public String getUserId() {
      User currentUser = User.current();
      if (currentUser == null) {
        return "";
      }
      return currentUser.getId();
    }

    public String getContextPath() {
      return MoUserAuth.getContextPath();
    }
  }
}
