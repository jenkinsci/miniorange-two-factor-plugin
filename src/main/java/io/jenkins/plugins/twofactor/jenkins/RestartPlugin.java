package io.jenkins.plugins.twofactor.jenkins;

import hudson.Extension;
import hudson.model.ManagementLink;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class RestartPlugin  extends ManagementLink {

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Restart Jenkins after plugin installation.";
    }

    @Override
    public String getUrlName() {
        return "RestartPlugin";
    }
}
