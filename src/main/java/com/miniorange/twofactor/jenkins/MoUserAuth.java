package com.miniorange.twofactor.jenkins;

import com.miniorange.twofactor.jenkins.tfaMethodsAuth.MoSecurityQuestionAuth;
import hudson.Extension;

import hudson.model.*;
import jenkins.model.Jenkins;

import java.util.Objects;
import java.util.logging.Logger;
@SuppressWarnings("unused")
@Extension
public class MoUserAuth implements RootAction, Describable<MoUserAuth> {

    private static final Logger LOGGER = Logger.getLogger(MoUserAuth.class.getName());

    public MoUserAuth(){

    }
    @Override
    public String getIconFileName() {
        return null; }

    @Override
    public String getDisplayName() {
        return "tfaUserAuth";
    }

    @Override
    public String getUrlName() {
        return "tfaUserAuth";
    }

    public String getUserId() {
        return User.current() != null ? Objects.requireNonNull(User.current()).getId() : "";
    }
    @SuppressWarnings("unused")
    public MoSecurityQuestionAuth getSecurityQuestionAuth() {
        return new MoSecurityQuestionAuth();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<MoUserAuth> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }

    @SuppressWarnings("unused")
    public static final MoUserAuth.DescriptorImpl DESCRIPTOR = new MoUserAuth.DescriptorImpl();

    @Extension
    public static class DescriptorImpl extends Descriptor<MoUserAuth> {

    }
}

