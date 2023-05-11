package com.miniorange.twofactor.jenkins.tfaMethodsConfig;

import hudson.Extension;

import hudson.model.*;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpSession;
import java.util.Objects;
import java.util.logging.Logger;

import static com.miniorange.twofactor.constants.MoGlobalConfigConstant.AdminConfiguration.ENABLE_2FA;
import static com.miniorange.twofactor.constants.MoSecurityQuestionsConstant.SecurityQuestions;
import static com.miniorange.twofactor.constants.MoSecurityQuestionsConstant.SecurityQuestions.SELECT_SECURITY_QUESTION;
import static com.miniorange.twofactor.constants.MoSecurityQuestionsConstant.UserSecurityQuestionKey.*;
import static com.miniorange.twofactor.jenkins.MoFilter.userAuthenticationStatus;

public class MoSecurityQuestionConfig extends UserProperty implements Action {
    private static final Logger LOGGER = Logger.getLogger(MoSecurityQuestionConfig.class.getName());
    private Secret firstSecurityQuestion;
    private Secret secondSecurityQuestion;
    private Secret customSecurityQuestion;
    private Secret firstSecurityQuestionAnswer;
    private Secret secondSecurityQuestionAnswer;
    private Secret customSecurityQuestionAnswer;

    @DataBoundConstructor
    public MoSecurityQuestionConfig(Secret firstSecurityQuestion, Secret secondSecurityQuestion, Secret customSecurityQuestion, Secret firstSecurityQuestionAnswer, Secret secondSecurityQuestionAnswer, Secret customSecurityQuestionAnswer) {
        this.firstSecurityQuestion = firstSecurityQuestion;
        this.secondSecurityQuestion = secondSecurityQuestion;
        this.customSecurityQuestion = customSecurityQuestion;
        this.firstSecurityQuestionAnswer = firstSecurityQuestionAnswer;
        this.secondSecurityQuestionAnswer = secondSecurityQuestionAnswer;
        this.customSecurityQuestionAnswer = customSecurityQuestionAnswer;
    }

    public boolean getLoggedIn(){
        MoSecurityQuestionConfig userTfaData = user.getProperty(MoSecurityQuestionConfig.class);
        return userTfaData.getFirstSecurityQuestion().equals("");
    }

    public MoSecurityQuestionConfig() {
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Security question";
    }

    @Override
    public String getUrlName() {
        return "securityQuestion";
    }

    @SuppressWarnings("unused")
    public boolean isUserAuthenticatedFromTfa() {
        User user = User.current();
        assert user != null;
        return userAuthenticationStatus.getOrDefault(user.getId(), false);
    }

    public String getFirstSecurityQuestion() {
        return Objects.requireNonNull(User.current()).getProperty(MoSecurityQuestionConfig.class).firstSecurityQuestion.getPlainText();
    }

    public String getSecondSecurityQuestion() {
        return Objects.requireNonNull(User.current()).getProperty(MoSecurityQuestionConfig.class).secondSecurityQuestion.getPlainText();
    }

    public String getCustomSecurityQuestion() {
        return Objects.requireNonNull(User.current()).getProperty(MoSecurityQuestionConfig.class).customSecurityQuestion.getPlainText();
    }


    public String getFirstSecurityQuestionAnswer() {
        return Objects.requireNonNull(User.current()).getProperty(MoSecurityQuestionConfig.class).firstSecurityQuestionAnswer.getPlainText();
    }

    public String getSecondSecurityQuestionAnswer() {
        return Objects.requireNonNull(User.current()).getProperty(MoSecurityQuestionConfig.class).secondSecurityQuestionAnswer.getPlainText();
    }

    public String getCustomSecurityQuestionAnswer() {
        return Objects.requireNonNull(User.current()).getProperty(MoSecurityQuestionConfig.class).customSecurityQuestionAnswer.getPlainText();
    }


    public void setFirstSecurityQuestion(Secret firstSecurityQuestion) {
        this.firstSecurityQuestion = firstSecurityQuestion;
    }


    public void setSecondSecurityQuestion(Secret secondSecurityQuestion) {
        this.secondSecurityQuestion = secondSecurityQuestion;
    }

    @SuppressWarnings("unused")
    public void setCustomSecurityQuestion(Secret customSecurityQuestion) {
        this.customSecurityQuestion = customSecurityQuestion;
    }

    public void setFirstSecurityQuestionAnswer(Secret firstSecurityQuestionAnswer) {
        this.firstSecurityQuestionAnswer = firstSecurityQuestionAnswer;
    }


    public void setSecondSecurityQuestionAnswer(Secret secondSecurityQuestionAnswer) {
        this.secondSecurityQuestionAnswer = secondSecurityQuestionAnswer;
    }

    public void setCustomSecurityQuestionAnswer(Secret customSecurityQuestionAnswer) {
        this.customSecurityQuestionAnswer = customSecurityQuestionAnswer;
    }

    private boolean isFormFilledCorrectly(net.sf.json.JSONObject json) {
        return !json.getString(USER_FIRST_SECURITY_QUESTION.getKey()).equals(json.getString(USER_SECOND_SECURITY_QUESTION.getKey())) &&
                !json.getString(USER_FIRST_SECURITY_QUESTION.getKey()).equals(SELECT_SECURITY_QUESTION.getQuestion()) &&
                !json.getString(USER_SECOND_SECURITY_QUESTION.getKey()).equals(SELECT_SECURITY_QUESTION.getQuestion()) &&
                StringUtils.isNotBlank(json.getString(USER_CUSTOM_SECURITY_QUESTION.getKey())) &&
                StringUtils.isNotBlank(json.getString(USER_FIRST_SECURITY_QUESTION_ANSWER.getKey())) &&
                StringUtils.isNotBlank(json.getString(USER_SECOND_SECURITY_QUESTION_ANSWER.getKey())) &&
                StringUtils.isNotBlank(json.getString(USER_CUSTOM_SECURITY_QUESTION_ANSWER.getKey()));
    }

    @SuppressWarnings("unused")
    @RequirePOST
    public void doSaveSecurityQuestion(StaplerRequest req, StaplerResponse rsp) throws Exception {
        LOGGER.fine("Saving user security questions");
        net.sf.json.JSONObject json = req.getSubmittedForm();
        String redirectUrl = req.getContextPath() + "./";
        boolean firstTimeUserLogIn = false;
        User user = User.current();
        try {
            if (isFormFilledCorrectly(json)) {
                if (user != null) {
                    MoSecurityQuestionConfig userSecurityQuestion = user.getProperty(MoSecurityQuestionConfig.class);
                    userSecurityQuestion.setFirstSecurityQuestion(Secret.fromString(json.getString(USER_FIRST_SECURITY_QUESTION.getKey())));
                    userSecurityQuestion.setSecondSecurityQuestion(Secret.fromString(json.getString(USER_SECOND_SECURITY_QUESTION.getKey())));
                    userSecurityQuestion.setCustomSecurityQuestion(Secret.fromString(json.getString(USER_CUSTOM_SECURITY_QUESTION.getKey())));
                    userSecurityQuestion.setFirstSecurityQuestionAnswer(Secret.fromString(json.getString(USER_FIRST_SECURITY_QUESTION_ANSWER.getKey())));
                    userSecurityQuestion.setSecondSecurityQuestionAnswer(Secret.fromString(json.getString(USER_SECOND_SECURITY_QUESTION_ANSWER.getKey())));
                    userSecurityQuestion.setCustomSecurityQuestionAnswer(Secret.fromString(json.getString(USER_CUSTOM_SECURITY_QUESTION_ANSWER.getKey())));
                    user.save();
                }
                HttpSession session = req.getSession(false);
                assert user != null;
                userAuthenticationStatus.put(user.getId(), true);
                if (session != null) {
                    redirectUrl = (String) session.getAttribute("tfaRelayState");
                    session.removeAttribute("tfaRelayState");
                }

                if (redirectUrl != null) {
                    LOGGER.fine("Saved security questions, redirecting user to " + redirectUrl);
                    FormApply.success(redirectUrl).generateResponse(req, rsp, null);
                }
            }
        } catch (Exception e) {
            LOGGER.fine("Error in saving security questions, Form is not filled correctly ");
            throw new Exception("Cannot save security questions, exception is " + e.getMessage());
        }
        if(redirectUrl == null) {
            redirectUrl = Jenkins.get().getRootUrl();
        }
        LOGGER.fine("Redirecting user to " + redirectUrl);
        FormApply.success(redirectUrl).generateResponse(req, rsp, null);
    }

    @Override
    public UserPropertyDescriptor getDescriptor() {
        return new DescriptorImpl();
    }

    @SuppressWarnings("unused")
    public static final MoSecurityQuestionConfig.DescriptorImpl DESCRIPTOR = new MoSecurityQuestionConfig.DescriptorImpl();

    @Extension
    public static class DescriptorImpl extends UserPropertyDescriptor {
        public DescriptorImpl() {
            super(MoSecurityQuestionConfig.class);
        }

        @Override
        public UserProperty newInstance(User user) {
            return new MoSecurityQuestionConfig(Secret.fromString(""), Secret.fromString(""), Secret.fromString(""), Secret.fromString(""), Secret.fromString(""), Secret.fromString(""));
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "";
        }

        private ListBoxModel fillSecurityQuestion() {
            ListBoxModel securityQuestionsDropDown = new ListBoxModel();
            for (SecurityQuestions securityQuestion : SecurityQuestions.values()) {
                securityQuestionsDropDown.add(new ListBoxModel.Option(securityQuestion.getQuestion()));
            }
            return securityQuestionsDropDown;
        }

        @SuppressWarnings("unused")
        @RequirePOST
        public ListBoxModel doFillFirstSecurityQuestionItems() {
            return fillSecurityQuestion();
        }

        @SuppressWarnings("unused")
        @RequirePOST
        public ListBoxModel doFillSecondSecurityQuestionItems() {
            return fillSecurityQuestion();
        }

        private FormValidation validateForm(boolean condition, String errorMessage) {
            if (condition) {
                return FormValidation.error(errorMessage);
            }
            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        @RequirePOST
        public FormValidation doCheckFirstSecurityQuestion(@QueryParameter String firstSecurityQuestion, @QueryParameter String secondSecurityQuestion) {
            return validateForm(firstSecurityQuestion.equals(SELECT_SECURITY_QUESTION.getQuestion()) || firstSecurityQuestion.equals(secondSecurityQuestion),
                    "Please select a valid security question");
        }


        @SuppressWarnings("unused")
        @RequirePOST
        public FormValidation doCheckFirstSecurityQuestionAnswer(@QueryParameter String firstSecurityQuestionAnswer, @QueryParameter String secondSecurityQuestion) {
            return validateForm(StringUtils.isBlank(firstSecurityQuestionAnswer), "Please Enter valid answer");
        }

        @SuppressWarnings("unused")
        @RequirePOST
        public FormValidation doCheckSecondSecurityQuestion(@QueryParameter String secondSecurityQuestion, @QueryParameter String firstSecurityQuestion) {
            return validateForm(secondSecurityQuestion.equals(SELECT_SECURITY_QUESTION.getQuestion()) || secondSecurityQuestion.equals(firstSecurityQuestion),
                    "Please select a valid security question");
        }

        @SuppressWarnings("unused")
        @RequirePOST
        public FormValidation doCheckSecondSecurityQuestionAnswer(@QueryParameter String secondSecurityQuestionAnswer) {
            return validateForm(StringUtils.isBlank(secondSecurityQuestionAnswer), "Please Enter valid answer");
        }

        @SuppressWarnings("unused")
        @RequirePOST
        public FormValidation doCheckCustomSecurityQuestion(@QueryParameter String customSecurityQuestion) {
            return validateForm(StringUtils.isBlank(customSecurityQuestion), "Please select a valid security question");
        }

        @SuppressWarnings("unused")
        @RequirePOST
        public FormValidation doCheckCustomSecurityQuestionAnswer(@QueryParameter String customSecurityQuestionAnswer) {
            return validateForm(StringUtils.isBlank(customSecurityQuestionAnswer), "Please Enter valid answer");
        }

    }
}

