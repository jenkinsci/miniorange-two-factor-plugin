package io.jenkins.plugins.twofactor.jenkins;

import hudson.Extension;
import hudson.model.User;
import jenkins.model.GlobalConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Extension
public class GroupManager extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(GroupManager.class.getName());
    private Map<String, Group> groups = new ConcurrentHashMap<>();

    public GroupManager() {
        load();
    }

    public static GroupManager get() {
        return GlobalConfiguration.all().get(GroupManager.class);
    }

    public static Group getOrCreateGroup(String groupName) {
        GroupManager instance = get();
        Group group = instance.groups.get(groupName);
        if (group == null) {
            group = new Group(groupName);
            instance.groups.put(groupName, group);
            LOGGER.fine("Creating new group: " + groupName);
            instance.save();
        }
        return group;
    }


    public static Group getGroup(String groupName) {
        return get().groups.get(groupName);
    }

    public static List<Group> getAllGroups() {
        return new ArrayList<>(get().groups.values());
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    public void deleteGroup(String groupName) {
        Group removed = groups.remove(groupName);
        if (removed != null) {
            // Also remove group from all users
            for (String username : removed.getUsernames()) {
                User user = User.getById(username, false);
                if (user != null) {
                    MoUserProperties props = user.getProperty(MoUserProperties.class);
                    //remove group
                }
            }
            save();
        }
    }

    @Override
    public synchronized void load() {
        super.load();  // Load persisted data
        // After loading, sync users' MoUserProperties to keep group memberships consistent
        for (Group group : groups.values()) {
            for (String username : group.getUsernames()) {
                User jenkinsUser = User.getById(username, false);
                if (jenkinsUser != null) {
                    MoUserProperties props = jenkinsUser.getProperty(MoUserProperties.class);
                    //add group
                }
            }
        }
        save();
    }

}
