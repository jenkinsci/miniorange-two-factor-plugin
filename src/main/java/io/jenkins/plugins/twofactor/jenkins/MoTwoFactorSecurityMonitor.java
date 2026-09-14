package io.jenkins.plugins.twofactor.jenkins;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import jenkins.model.Jenkins;

@SuppressWarnings("unused")
@Extension
public class MoTwoFactorSecurityMonitor extends AdministrativeMonitor {

  @Override
  public boolean isActivated() {
    if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
      return false;
    }
    if (!isEnabled()) {
      return false;
    }
    return Boolean.TRUE.equals(MoGlobalConfig.get().getEnableTfa());
  }

  @Override
  public boolean isSecurity() {
    return true;
  }
}
