package com.miniorange.twofactor.jenkins.tfaMethodsAuth;

import com.miniorange.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormApply;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

import static com.miniorange.twofactor.jenkins.MoFilter.userAuthenticationStatus;
import static jenkins.model.Jenkins.get;

@Extension
public class MoSecurityQuestionAuth implements Action, Describable<MoSecurityQuestionAuth> {
    private static final Logger LOGGER = Logger.getLogger(MoSecurityQuestionAuth.class.getName());
    int firstRandomSecurityQuestionIndex = 0;
    int secondRandomSecurityQuestionIndex = 1;
    private String[] securityQuestionArray;
    private String[] securityAnswerArray;
    public static Map<String, Boolean> showWrongCredentialWarning = new HashMap<>();


    public MoSecurityQuestionAuth() {
        try {
            User user = User.current();
            if (user != null) {

                MoSecurityQuestionConfig securityQuestion = user.getProperty(MoSecurityQuestionConfig.class);
                this.securityQuestionArray = new String[]{securityQuestion.getFirstSecurityQuestion(), securityQuestion.getSecondSecurityQuestion(), securityQuestion.getCustomSecurityQuestion()};
                this.securityAnswerArray = new String[]{securityQuestion.getFirstSecurityQuestionAnswer(), securityQuestion.getSecondSecurityQuestionAnswer(), securityQuestion.getCustomSecurityQuestionAnswer()};
            }
        } catch (Exception e) {
            LOGGER.fine("Error in getting security questions and answers for user authentication " + e.getMessage());
        }
    }

    @Override
    public String getIconFileName() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "securityQuestionAuth";
    }

    @Override
    public String getUrlName() {
        return "securityQuestionAuth";
    }

    @SuppressWarnings("unused")
    public String getUserId() {
        return User.current() != null ? Objects.requireNonNull(User.current()).getId() : "";
    }

    private void initializeRandomTwoIndex() {
        Random rand = new Random();
        firstRandomSecurityQuestionIndex = rand.nextInt(3);
        secondRandomSecurityQuestionIndex = rand.nextInt(2);
        if (secondRandomSecurityQuestionIndex >= firstRandomSecurityQuestionIndex) {
            secondRandomSecurityQuestionIndex++;
        }
    }

    @SuppressWarnings("unused")
    public String getFirstRandomSecurityQuestion() {
        initializeRandomTwoIndex();
        return securityQuestionArray[firstRandomSecurityQuestionIndex];
    }

    @SuppressWarnings("unused")
    public String getSecondRandomSecurityQuestion() {
        return securityQuestionArray[secondRandomSecurityQuestionIndex];
    }

    private String getFirstRandomSecurityQuestionAnswer() {
        return securityAnswerArray[firstRandomSecurityQuestionIndex];
    }

    private String getSecondRandomSecurityQuestionAnswer() {
        return securityAnswerArray[secondRandomSecurityQuestionIndex];
    }

    @SuppressWarnings("unused")
    public boolean getShowWrongCredentialWarning() {
        User user = User.current();
        assert user != null;
        return showWrongCredentialWarning.get(user.getId()) != null;
    }


    private boolean validateUserAnswers(net.sf.json.JSONObject formData) {
        return formData.get("userFirstAuthenticationAnswer").toString().equals(getFirstRandomSecurityQuestionAnswer()) &&
                formData.get("userSecondAuthenticationAnswer").toString().equals(getSecondRandomSecurityQuestionAnswer());
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public void doSecurityQuestionAuthenticate(StaplerRequest staplerRequest, StaplerResponse staplerResponse) throws Exception {
        net.sf.json.JSONObject formData = staplerRequest.getSubmittedForm();
        User user = User.current();
        HttpSession session = staplerRequest.getSession(false);
        String redirectUrl = get().getRootUrl();
        LOGGER.fine("Authenticating user tfa security answers");
        try {
            if (user == null)
                return;
            if (validateUserAnswers(formData)) {
                LOGGER.fine(user.getId() + " user is authentic");
                userAuthenticationStatus.put(user.getId(), true);
                if (session != null) {
                    redirectUrl = (String) session.getAttribute("tfaRelayState");
                    session.removeAttribute("tfaRelayState");
                }
                showWrongCredentialWarning.put(user.getId(), false);
            } else {
                LOGGER.fine("User is not authentic");
                redirectUrl = "./";
                showWrongCredentialWarning.put(user.getId(), true);
            }
            LOGGER.fine("Redirecting user to " + redirectUrl);

            if(redirectUrl == null)
                redirectUrl = Jenkins.get().getRootUrl();

            FormApply.success(redirectUrl).generateResponse(staplerRequest, staplerResponse, null);
        } catch (Exception e) {
            LOGGER.fine("Exception while authenticating/Logging out the user " + e.getMessage());
        }
    }


    @Override
    public MoSecurityQuestionAuth.DescriptorImpl getDescriptor() {
        return (MoSecurityQuestionAuth.DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
    }

    @SuppressWarnings("unused")
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Extension
    public static class DescriptorImpl extends Descriptor<MoSecurityQuestionAuth> {

    }
}

