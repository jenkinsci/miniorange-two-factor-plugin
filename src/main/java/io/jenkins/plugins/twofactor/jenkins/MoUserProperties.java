package io.jenkins.plugins.twofactor.jenkins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class MoUserProperties extends UserProperty implements Action {
    private static final Logger LOGGER = Logger.getLogger(MoUserProperties.class.getName());
    private boolean is2faEnabled;
    private Set<String> groupNames = new HashSet<>();

    @DataBoundConstructor
    public MoUserProperties(Boolean is2faEnabled) {
        this.is2faEnabled = is2faEnabled;
    }

    public void enable2FA() throws IOException {
        this.is2faEnabled = true;
        user.save();
    }

    public void disable2FA() throws IOException {
        this.is2faEnabled = false;
        user.save();
    }


    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    public Set<String> getGroupNames() {
        return Collections.unmodifiableSet(groupNames);
    }


    public UserPropertyDescriptor getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends UserPropertyDescriptor {
        public DescriptorImpl() {
            super(MoUserProperties.class);
        }

        @Override
        public UserProperty newInstance(User user) {
            return new MoUserProperties(false);
        }

        @Override
        @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION", justification = "Intentionally returning null to hide from UI")
        public String getDisplayName() {
            return null;
        }
    }
}