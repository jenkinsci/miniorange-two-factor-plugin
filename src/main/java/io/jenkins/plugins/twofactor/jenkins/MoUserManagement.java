package io.jenkins.plugins.twofactor.jenkins;

import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormApply;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoOtpOverEmailConfig;
import io.jenkins.plugins.twofactor.jenkins.tfaMethodsConfig.MoSecurityQuestionConfig;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.jenkins.plugins.twofactor.constants.MoPluginUrls.Urls.MO_TFA_USER_MANAGEMENT;

public class MoUserManagement implements Action, Describable<MoUserManagement> {
    private static final Logger LOGGER = Logger.getLogger(MoUserManagement.class.getName());
    @Override
    public String getIconFileName() {
        return "symbol-people";
    }

    @Override
    public String getDisplayName() {
        return "user-management";
    }

    @Override
    public String getUrlName() {
        return MO_TFA_USER_MANAGEMENT.getUrl();
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.sendRedirect(req.getContextPath() + "/manage/tfaGlobalConfig/userManagement/users");
    }
    public Object getAllUsers(){
        return  User.getAll().toArray();
    }

    public Boolean getStatus(String userID){
        return MoGlobalConfig.get().getEnableTfa();
    }


    public void doClearGroupFlags() {
        HttpSession session = Stapler.getCurrentRequest().getSession(false);
        if (session != null) {
            session.removeAttribute("mo.groupCreationSuccess");
            session.removeAttribute("mo.groupCreationError");
            session.removeAttribute("mo.groupDeletionSuccess");
            session.removeAttribute("mo.groupDeletionError");
        }
    }
    @RequirePOST
    public void doCreateGroup(StaplerRequest req, StaplerResponse rsp) throws IOException {
        HttpSession session = req.getSession(true);
        String groupName = req.getParameter("groupName");
        if (groupName != null && !groupName.trim().isEmpty()) {
            try {
                GroupManager.getOrCreateGroup(groupName.trim());
                session.setAttribute("mo.groupCreationSuccess", "true");
            } catch (Exception e) {
                session.setAttribute("mo.groupCreationError", "true");
            }
        }
        rsp.sendRedirect("./groups");
    }

    @RequirePOST
    public void doDeleteGroup(StaplerRequest req, StaplerResponse rsp) throws IOException {
        HttpSession session = req.getSession(true);

        String groupName = req.getParameter("groupName");
        if (groupName != null && !groupName.trim().isEmpty()) {
            try {
                GroupManager.get().deleteGroup(groupName.trim());
                session.setAttribute("mo.groupDeletionSuccess", "true");
            } catch (Exception e) {
                session.setAttribute("mo.groupDeletionError", "true");
            }
        }
        rsp.sendRedirect("./groups");
    }

    public List<String> getAvailableUsersForGroup(String groupName) {
        Group group = GroupManager.getGroup(groupName);
        List<String> groupUsers = group != null ? new ArrayList<>(group.getUsernames()) : new ArrayList<>();

        return ((List<User>) getAllUsers()).stream()
                .map(user -> user.getId())
                .filter(userId -> !groupUsers.contains(userId))
                .collect(Collectors.toList());
    }

    public boolean groupHasUsers(String groupName) {
        Group group = GroupManager.getGroup(groupName);
        return group != null && !group.getUsernames().isEmpty();
    }

    public List<String> getGroupUsers(String groupName) {
        Group group = GroupManager.getGroup(groupName);
        return group != null ? new ArrayList<>(group.getUsernames()) : new ArrayList<>();
    }

    public void doManageGroup(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        String groupName = req.getParameter("groupName");
        if (groupName == null || groupName.trim().isEmpty()) {
            rsp.sendRedirect("./groups");
            return;
        }

        // Set the group name as a request attribute for the Jelly template
        req.setAttribute("groupName", groupName.trim());
        req.getView(this, "manageGroup.jelly").forward(req, rsp);
    }

    public List<Group> getAllGroups() {
        return GroupManager.getAllGroups();
    }
    private boolean hasSessionAttribute(String attributeName) {
        HttpSession session = Stapler.getCurrentRequest().getSession(false);
        return session != null && "true".equals(session.getAttribute(attributeName));
    }


    public boolean hasGroupCreationSuccess() {
        return hasSessionAttribute("mo.groupCreationSuccess");
    }

    public boolean hasGroupCreationError() {
        return hasSessionAttribute("mo.groupCreationError");
    }

    public boolean hasGroupDeletionSuccess() {
        return hasSessionAttribute("mo.groupDeletionSuccess");
    }

    public boolean hasGroupDeletionError() {
        return hasSessionAttribute("mo.groupDeletionError");
    }


    @Override
    public Descriptor<MoUserManagement> getDescriptor() {
        Jenkins jenkins = Jenkins.get();
        return (Descriptor<MoUserManagement>) jenkins.getDescriptorOrDie(getClass());
    }
}
