package com.miniorange.twofactor.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.security.SecurityListener;

import java.util.logging.Logger;

import static com.miniorange.twofactor.jenkins.MoFilter.userAuthenticationStatus;
import static com.miniorange.twofactor.jenkins.tfaMethodsAuth.MoSecurityQuestionAuth.showWrongCredentialWarning;

@SuppressWarnings("unused")
@Extension
public class MoSecurityListener extends SecurityListener {

    private static final Logger LOGGER = Logger.getLogger(MoSecurityListener.class.getName());
    @Override
    public void loggedOut(@NonNull String username) {

        userAuthenticationStatus.put(username, false);
        showWrongCredentialWarning.remove(username);
        LOGGER.fine("Executing logged out event for username " + username);
    }


}
