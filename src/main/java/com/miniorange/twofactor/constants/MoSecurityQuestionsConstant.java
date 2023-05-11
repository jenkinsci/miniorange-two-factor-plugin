package com.miniorange.twofactor.constants;

public class MoSecurityQuestionsConstant {

    public enum SecurityQuestions {

        SELECT_SECURITY_QUESTION("Select Security Question"),
        FIRST_COMPANY_NAME("What is your first company name ?"),
        CHILDHOOD_NICKNAME("What is your childhood nickname ?"),
        MEET_SPOUSE_CITY("In what city did you meet your spouse/significant other ?"),
        FAVORITE_CHILDHOOD_FRIEND("What is the name favorite childhood friend ?"),
        SIXTH_GRADE_SCHOOL("What school did you attend for sixth grade ?"),
        FIRST_JOB_CITY("In what city or town was your first job?"),
        FAVORITE_SPORT("What is the your favourite sport ?"),
        FAVORITE_SPORT_PLAYER("Who is your favourite sport player?"),
        GRANDMOTHER_MAIDEN_NAME("What is your grandmother's maiden name ?"),
        FIRST_VEHICLE_REGISTRATION_NUMBER("What is your first vehicle's registration number ?");

        private final String question;

        SecurityQuestions(String question) {
            this.question = question;
        }

        public String getQuestion() {
            return question;
        }

    }

    public enum UserSecurityQuestionKey {
        USER_FIRST_SECURITY_QUESTION("firstSecurityQuestion"),
        USER_SECOND_SECURITY_QUESTION("secondSecurityQuestion"),
        USER_CUSTOM_SECURITY_QUESTION("customSecurityQuestion"),
        USER_FIRST_SECURITY_QUESTION_ANSWER("firstSecurityQuestionAnswer"),
        USER_SECOND_SECURITY_QUESTION_ANSWER("secondSecurityQuestionAnswer"),
        USER_CUSTOM_SECURITY_QUESTION_ANSWER("customSecurityQuestionAnswer");

        private final String key;

        UserSecurityQuestionKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

}



