# Two-Factor Authentication for Jenkins

**Two-Factor Authentication for Jenkins** Plugin adds a layer of security to Jenkins authentication by requiring users
to provide a second factor of authentication along with their username and password.
It enhances the overall security of your Jenkins environment. Additionally, this plugin does not require you to extend the security realm, making it easier to implement and use.

**Supported Authentication method**

* Security Questions
* OTP over email

# Installation Instructions

* Login to your Jenkins.
* Go to the **Manage Jenkins** option from the left panel, and open the **Manage Plugins** tab.
* Go to the **advanced** tab and upload the hpi file.
* You can get the hpi file by running ```mvn clean package``` command on this code. Contact info@xecurify.com if you are
  facing any issues.

![image_1](docs/images/configuration/upload_plugin.png)

![image_2](docs/images/configuration/installation_success.png)
*  **⚠️   Make sure you restart the Jenkins**

# Step 2: Setup Your 2FA

* Open **Manage Jenkins** => **2FA Global Configurations** and check the **Enable 2FA for all users** checkbox.

![image_3](docs/images/configuration/configure_gobal_section.png)

* Check the checkbox **Enable for all users** to enable 2FA  in Jenkins for all users and save the configuration.
 
![image_5](docs/images/configuration/enable_2fa.png)

* Check the available two-factor authentication methods that users can use for authentication.

![image_6](docs/images/configuration/enable_security_methods.png)

[//]: # (todo Give link to the setup guide for OTP over email in **this**.&#41;)
* Follow this guide to set up and configure authentication methods in Jenkins

* Now, whenever a user signs in with Jenkins credentials, a 2FA configuration display will appear in Jenkins.

![image_7](docs/images/configuration/inline2FA.png)

# Reset/Configure authentication methods

* Go to your user profile and click on the 2FA configuration button in the side panel. 

![image_8](docs/images/configuration/reset_2FA.png)

* Choose the "Reset" or "Configuration" option to either reset the currently configured authentication method or set up a new method if it is currently not configured.
![image_8](docs/images/configuration/reset_configure.png)

# Troubleshooting and Logging

Refer below steps to generate error Logs and send it to info@xecurify.com. We will help you
resolve your issue in no time.

* Sign in to Jenkins as an admin and select Manage Jenkins from the left panel of the dashboard.
* Scroll down to find the System Log option.
* Click Add new Log Recorder button and add the log recorder name as 2FaLogs.
* Add **io.jenkins.plugins.twofactor.jenkins** as a Logger and select fine as a log level.
  ![image_9](docs/images/troubleshooting/logger_record.png)
* Save the settings. 
* Perform 2FA on another browser/private window to record logs. 
* Visit the System Log option again and copy the recorded logs from the 2FaLogs logger. 
* Paste logs in a notepad/word file and send it to us. 
* You can even reach us at info@xecurify.com or raise a ticket using this [link](https://miniorange.atlassian.net/servicedesk/customer/portal/2%22).
