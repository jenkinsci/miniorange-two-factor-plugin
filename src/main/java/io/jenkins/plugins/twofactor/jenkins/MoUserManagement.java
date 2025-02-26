package io.jenkins.plugins.twofactor.jenkins;

import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormApply;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoOtpOverEmailConfig;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.logging.Logger;

import static io.jenkins.plugins.twofactor.constants.MoPluginUrls.Urls.MO_TFA_USER_MANAGEMENT;

public class MoUserManagement implements Action, Describable<MoUserManagement> {
    private static final Logger LOGGER = Logger.getLogger(MoUserManagement.class.getName());
    @Override
    public String getIconFileName() {
        return "symbol-people";
    }

    @Override
    public String getDisplayName() {
        return "user-management";
    }

    @Override
    public String getUrlName() {
        return MO_TFA_USER_MANAGEMENT.getUrl();
    }

    public Object getAllUsers(){
        return  User.getAll().toArray();
    }

    public Boolean getStatus(String userID){
        return MoGlobalConfig.get().getEnableTfa();
    }

    @Override
    public Descriptor<MoUserManagement> getDescriptor() {
        Jenkins jenkins = Jenkins.get();
        return (Descriptor<MoUserManagement>) jenkins.getDescriptorOrDie(getClass());
    }
}
