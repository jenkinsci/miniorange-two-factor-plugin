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

import static com.miniorange.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA;
import static com.miniorange.twofactor.jenkins.MoFilter.userAuthenticationStatus;
import static jenkins.model.Jenkins.get;

import com.miniorange.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import hudson.Extension;
import hudson.model.*;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.lang.NonNull;

@SuppressWarnings("unused")
@Extension
public class MoUserConfig extends UserProperty implements Action {
  private static final Logger LOGGER = Logger.getLogger(MoUserConfig.class.getName());

  @DataBoundConstructor
  public MoUserConfig() {}

  @Override
  public String getIconFileName() {
    try {
      User user = User.current();
      if (user == null || !userAuthenticationStatus.getOrDefault(ENABLE_2FA.getSetting(), false)) {
        return null;
      }
      StaplerRequest request = Stapler.getCurrentRequest();
      String username = request.getRequestURI().split("/")[2];
      if (user.getId().equals(username)) {
        return "/plugin/miniorange-two-factor/images/tfaIcon.png";
      }

    } catch (Exception e) {
      LOGGER.fine("Exception in showing user security question, exception is " + e.getMessage());
    }
    return null;
  }

  @Override
  public String getDisplayName() {
    return "2fa configuration";
  }

  @Override
  public String getUrlName() {
    return "tfaConfiguration";
  }

  public boolean getLoggedIn() {
    MoSecurityQuestionConfig userTfaData = user.getProperty(MoSecurityQuestionConfig.class);
    return userTfaData.getFirstSecurityQuestion().equals("");
  }

  public String getUserId() {
    User user = User.current();
    if (user == null) {
      return null;
    }
    return user.getId();
  }

  @SuppressWarnings("unused")
  public MoSecurityQuestionConfig getSecurityQuestions() {
    return new MoSecurityQuestionConfig();
  }

  @SuppressWarnings("unused")
  public String getBaseUrl() {
    return get().getRootUrl();
  }

  @Override
  public UserPropertyDescriptor getDescriptor() {
    return new MoUserConfig.DescriptorImpl();
  }

  @Extension
  public static class DescriptorImpl extends UserPropertyDescriptor {
    public DescriptorImpl() {
      super(MoUserConfig.class);
    }

    @Override
    public UserProperty newInstance(User user) {
      return new MoUserConfig();
    }

    @NonNull
    @Override
    public String getDisplayName() {
      if (!userAuthenticationStatus.getOrDefault(ENABLE_2FA.getSetting(), false)) {
        return "";
      }
      return "2fa configuration";
    }
  }
}
