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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.twofactor.constants.MoPluginUrls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

@SuppressWarnings("unused")
@Extension
public class MoGlobalConfigView extends ManagementLink implements Describable<MoGlobalConfigView> {

    private static final Logger LOGGER = Logger.getLogger(MoGlobalConfigView.class.getName());
    String GOOGLE_SHEET_SCRIPT = "https://script.google.com/macros/s/AKfycbyTmlBUim5Eaub-gQCHBS6i_jzj803Po_LaAaS03m0O8Oph3Gpb5S2dWsXuH9GEUg__0g/exec";
    String PREMIUM_PLUGIN_URL = "https://miniorange.s3.amazonaws.com/public/plugins/Jenkins/miniorange-two-factor-2.1.0.hpi";

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
        return MoPluginUrls.Urls.MO_TFA_GLOBAL_CONFIG.getUrl();
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

    public MoGlobalAdvancedSettings getAdvancedSettings() {
        return new MoGlobalAdvancedSettings(MoGlobalConfig.get().getAdvancedSettingsDTO());
    }

    public MoUserManagement getUserManagement() {
        return new MoUserManagement();
    }

    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doConfirmationFormSubmit(StaplerRequest request, StaplerResponse response) {
        try {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);

            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String orgName = request.getParameter("orgName");

            if (name == null || name.length() > 50) {
                LOGGER.fine("Invalid name.");
                response.getWriter().write("Not a valid name.");
                return;
            }

            if (orgName == null || orgName.length() > 100) {
                LOGGER.fine("Invalid organization name.");
                response.getWriter().write("Not a valid organization name.");
                return;
            }

            //Email validation
            String regex = "^(.+)@(.+)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                LOGGER.fine("Invalid email address: " + email);
                response.getWriter().write("Not a valid email address.");
                return;
            }


            String url = GOOGLE_SHEET_SCRIPT
                    + "?name=" + URLEncoder.encode(name, "UTF-8")
                    + "&email=" + URLEncoder.encode(email, "UTF-8")
                    + "&query=" + URLEncoder.encode(orgName, "UTF-8");

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoOutput(true);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    LOGGER.fine("Form submitted successfully!");
                    response.getWriter().write("Form submitted successfully!");
                } else {
                    LOGGER.fine("Error submitting form. Response Code: " + responseCode);
                    response.getWriter().write("Error submitting form. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                LOGGER.fine("Exception while submitting form: " + e.getMessage());
                response.getWriter().write("Error submitting form: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            LOGGER.fine("Unexpected error in doConfirmationFormSubmit: " + e.getMessage());
            try {
                response.getWriter().write("Unexpected error: " + e.getMessage());
            } catch (IOException ioException) {
                LOGGER.fine("Failed to write error response: " + ioException.getMessage());
            }
        }
    }


    public void doInstallPlugin(StaplerRequest req, StaplerResponse rsp) {
        LOGGER.fine("Starting plugin installation...");
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) throw new IllegalStateException("Jenkins instance is not available.");

        String pluginName = "miniorange-two-factor";

        File pluginsDir = new File(jenkins.getRootDir(), "plugins");
        if (!pluginsDir.exists() && !pluginsDir.mkdirs()) {
            LOGGER.fine("Failed to create plugins directory: " + pluginsDir.getAbsolutePath());
        }


        File pluginFile = new File(pluginsDir, pluginName + ".jpi");

        try (InputStream in = new URL(PREMIUM_PLUGIN_URL).openStream()) {
            Files.copy(in, pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.fine("Plugin downloaded and installed at: " + pluginFile.getAbsolutePath());

        } catch (IOException e) {
            LOGGER.fine("Error downloading the plugin: " + e.getMessage());
        }

        // Ensures Jenkins picks up the new plugin
        if (!pluginFile.setLastModified(System.currentTimeMillis())) {
            LOGGER.fine("Failed to set last modified time for plugin file: " + pluginFile.getAbsolutePath());
        }


        LOGGER.fine("Plugin installed successfully. Restart Jenkins for changes to take effect.");
        try {
            rsp.sendRedirect(req.getContextPath() + "/manage/RestartPlugin");
        } catch (IOException e) {
            LOGGER.fine("Error redirecting to restart plugin: " + e.getMessage());
        }

        try {
            jenkins.safeRestart();
            LOGGER.fine("Jenkins safe restart triggered successfully.");
        } catch (Exception e) {
            LOGGER.fine("Failed to restart Jenkins: " + e.getMessage());
        }
    }

  @RequirePOST
  public void doSaveGlobalTfaSettings(StaplerRequest req, StaplerResponse rsp)
      throws IOException, ServletException, Descriptor.FormException {
    try {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      JSONObject json = req.getSubmittedForm();
      MoGlobalConfig moGlobalConfig = MoGlobalConfig.get();
      moGlobalConfig.configure(req, json);
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

    public MoGlobalConfig getGlobalConfig() {
        return MoGlobalConfig.get();
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<MoGlobalConfigView> {
        public DescriptorImpl() {
        }

        @POST
        public FormValidation doCheckSenderEmailAddress(@QueryParameter String senderEmailAddress) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
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

        @POST
        @SuppressWarnings("unused")
        public FormValidation doCheckEnableDuoPush(@QueryParameter Boolean enableDuoPush) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return FormValidation.warning("Available in premium version");
        }

        @POST
        @SuppressWarnings("unused")
        public FormValidation doCheckEnableMobileAuthenticator(@QueryParameter Boolean enableMobileAuthenticator) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return FormValidation.warning("Available in premium version");
        }

        @POST
        @SuppressWarnings("unused")
        public FormValidation doCheckEnableOtpOverSms(@QueryParameter Boolean enableOtpOverSms) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return FormValidation.warning("Available in premium version");
        }

    }
}
