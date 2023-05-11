package com.miniorange.twofactor.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.model.User;
import hudson.util.FormApply;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

import static com.miniorange.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA;
import static com.miniorange.twofactor.jenkins.MoFilter.userAuthenticationStatus;
import static jenkins.model.Jenkins.get;

@SuppressWarnings("unused")
@Extension
public class MoGlobalConfigView extends ManagementLink implements Describable<MoGlobalConfigView> {

    private static final Logger LOGGER = Logger.getLogger(MoGlobalConfigView.class.getName());

    public MoGlobalConfigView() {
    }

    public boolean getEnableTfa() {
        return MoGlobalConfig.get().getEnableTfa();
    }

    public boolean doNotShowHeader(){
        try{
            User user = User.current();
            assert user != null;
            return (userAuthenticationStatus.get(ENABLE_2FA.getSetting()) && !userAuthenticationStatus.getOrDefault(user.getId(), false));

        }
        catch (Exception e) {
            LOGGER.fine("Error in doNotShowHeader calculation " + e.getMessage());
            return false;
        }

    }

    @Override
    public String getIconFileName() {
        if (isAdmin())
            return "/plugin/miniorange-two-factor/images/tfaIcon.png";
        return null;
    }

    @Override
    public String getDisplayName() {
        return "2FA Global Configurations";
    }

    @Override
    public String getUrlName() {
        return "tfaGlobalConfig";
    }

    @Override
    public String getDescription() {
        return "Configure two factor settings for your jenkins instance";
    }

    @RequirePOST
    public void doSaveGlobalTfaSettings(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
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

    @Extension
    public static final class DescriptorImpl extends Descriptor<MoGlobalConfigView> {
        public DescriptorImpl() {
        }
    }
}
