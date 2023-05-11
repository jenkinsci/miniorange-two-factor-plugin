package com.miniorange.twofactor.jenkins;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;

import static com.miniorange.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA;
import static com.miniorange.twofactor.jenkins.MoFilter.userAuthenticationStatus;

@Extension
public class MoGlobalConfig extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(MoGlobalConfig.class.getName());
    private boolean enableTfa;

    public MoGlobalConfig() {
        load();
    }

    public boolean getEnableTfa() {
        return enableTfa;
    }

    @SuppressWarnings("unused")
    @DataBoundSetter
    public void setEnableTfa(boolean unableTfa) {
        enableTfa = unableTfa;
        save();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        enableTfa = formData.getBoolean("enableTfa");
        userAuthenticationStatus.put(ENABLE_2FA.getSetting(), enableTfa);
        this.save();
        LOGGER.fine("Saving global configuration as " + formData);
        return super.configure(req, formData);
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
