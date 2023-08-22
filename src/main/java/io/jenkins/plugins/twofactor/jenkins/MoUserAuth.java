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

import static jenkins.model.Jenkins.get;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.RootAction;
import hudson.model.User;
import io.jenkins.plugins.twofactor.constants.MoPluginUrls;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsAuth.MoOtpOverEmailAuth;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsAuth.MoSecurityQuestionAuth;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoOtpOverEmailConfig;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

@SuppressWarnings("unused")
@Extension
public class MoUserAuth implements RootAction, Describable<MoUserAuth> {
  private static final Logger LOGGER = Logger.getLogger(MoUserAuth.class.getName());
  Map<String, MoSecurityQuestionAuth> moSecurityQuestionAuthMap = new HashMap<>();
  Map<String, MoOtpOverEmailAuth> moOtpOverEmailAuthMap = new HashMap<>();

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return MoPluginUrls.Urls.MO_USER_AUTH.getUrl();
  }

  @Override
  public String getUrlName() {
    return MoPluginUrls.Urls.MO_USER_AUTH.getUrl();
  }

  @SuppressWarnings("unused")
  public String getBaseUrl() {
    return get().getRootUrl();
  }

  public String getUserId() {
    User currentUser = User.current();
    if (currentUser == null) {
      return "";
    }
    return currentUser.getId();
  }

  @SuppressWarnings("unused")
  public MoSecurityQuestionAuth getSecurityQuestionAuth() {
    if (moSecurityQuestionAuthMap.get(getUserId()) == null) {
      moSecurityQuestionAuthMap.put(getUserId(), new MoSecurityQuestionAuth());
    }
    return moSecurityQuestionAuthMap.get(getUserId());
  }

  @SuppressWarnings("unused")
  public MoOtpOverEmailAuth getOtpOverEmailAuth() {
    if (moOtpOverEmailAuthMap.get(getUserId()) == null) {
      moOtpOverEmailAuthMap.put(getUserId(), new MoOtpOverEmailAuth());
    }
    return moOtpOverEmailAuthMap.get(getUserId());
  }

  public void cleanUserAuthResource(String userId) {
    moSecurityQuestionAuthMap.remove(userId);
    moOtpOverEmailAuthMap.remove(userId);
  }

  public boolean showSecurityQuestionForConfiguration() {
    User user = User.current();
    assert user != null;
    MoSecurityQuestionConfig securityQuestionConfig =
        user.getProperty(MoSecurityQuestionConfig.class);
    boolean isConfigured = securityQuestionConfig != null && securityQuestionConfig.isConfigured();
    boolean isEnabled = MoGlobalConfig.get().isEnableSecurityQuestionsAuthentication();
    return isConfigured && isEnabled;
  }

  @SuppressWarnings("unused")
  public boolean showOtpOverEmailForConfiguration() {
    User user = User.current();
    assert user != null;
    MoOtpOverEmailConfig otpOverEmailConfig = user.getProperty(MoOtpOverEmailConfig.class);
    boolean isConfigured = otpOverEmailConfig != null && otpOverEmailConfig.isConfigured();
    boolean isEnabled = MoGlobalConfig.get().isEnableOtpOverEmailAuthentication();
    return isConfigured && isEnabled;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Descriptor<MoUserAuth> getDescriptor() {
    return Jenkins.get().getDescriptorOrDie(getClass());
  }
}