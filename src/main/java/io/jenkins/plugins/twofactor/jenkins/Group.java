package io.jenkins.plugins.twofactor.jenkins;

import hudson.model.User;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Group implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(Group.class.getName());
    private static final long serialVersionUID = 1L;

    private String name;
    private Set<String> usernames = new HashSet<>();
    private boolean is2faEnabled = false;


    public Group(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public Group() {
        this.name = null; }

    public String getName() {
        return name;
    }

    public Set<String> getUsernames() {
        return Collections.unmodifiableSet(usernames);
    }

}