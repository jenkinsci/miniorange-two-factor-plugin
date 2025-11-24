package io.jenkins.plugins.twofactor.jenkins;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormApply;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.lang.NonNull;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

import static io.jenkins.plugins.twofactor.constants.MoPluginUrls.Urls.MO_TFA_IP_RESTRICTIONS_CONFIG;

public class MoIPRestrictionsConfig implements Action, Describable<MoIPRestrictionsConfig> {

    private static final Logger LOGGER = Logger.getLogger(MoIPRestrictionsConfig.class.getName());

    @Override
    public String getIconFileName() {
        return "symbol-lock-closed";
    }

    @Override
    public String getDisplayName() {
        return "IP Restrictions Configuration";
    }

    @Override
    public String getUrlName() {
        return MO_TFA_IP_RESTRICTIONS_CONFIG.getUrl();
    }

    @Override
    public Descriptor<MoIPRestrictionsConfig> getDescriptor() {
        Jenkins jenkins = Jenkins.get();
        return (Descriptor<MoIPRestrictionsConfig>) jenkins.getDescriptorOrDie(getClass());
    }

    @SuppressWarnings("unused")
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doSaveWhitelistIp(StaplerRequest request, StaplerResponse response) {
        try {
            FormApply.applyResponse("notificationBar.show('Available in Premium version', notificationBar.WARNING);")
                    .generateResponse(request, response, this);
        } catch (IOException | ServletException e) {
            LOGGER.fine( "Failed to process whitelist save request");
        }
    }

    @SuppressWarnings("unused")
    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doSaveBlacklistIp(StaplerRequest request, StaplerResponse response) throws ServletException, IOException {
        try {
            FormApply.applyResponse("notificationBar.show('Available in Premium version', notificationBar.WARNING);")
                    .generateResponse(request, response, this);
        } catch (IOException | ServletException e) {
            LOGGER.fine("Failed to process blacklist save request");
        }
    }

    @RequirePOST
    public void doShowIp(StaplerRequest req, StaplerResponse rsp) {
        try {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            String ip = "Unable to fetch IP";
            try {
                ip = req.getRemoteAddr();
            } catch (Exception ignored) {
                LOGGER.fine("Failed to fetch remote IP address");
            }

            String js = ""
                    + "const ipSpan=document.getElementById('user-ip'),"
                    + "formBtn=document.querySelector('form[action=\"showIp\"] button');"
                    + "if(ipSpan){ipSpan.textContent='Your IP: " + ip + "'; ipSpan.style.display='inline-block';}"
                    + "if(formBtn) formBtn.style.display='none';"
                    + "setTimeout(()=>{if(ipSpan) ipSpan.style.display='none'; if(formBtn) formBtn.style.display='inline-block';},4000);";

            FormApply.applyResponse(js).generateResponse(req, rsp, this);
        } catch (IOException | ServletException e) {
            LOGGER.fine("Failed to process show IP request");
        }
    }



    @SuppressWarnings("unused")
    @Extension
    public static final class DescriptorImpl extends Descriptor<MoIPRestrictionsConfig> {
        public DescriptorImpl() {
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "IP Restrictions Configuration";
        }

    }


}
