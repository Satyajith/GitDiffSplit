package com.procore.prdiffs.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/* Model class for User items */
public class User {
    @SerializedName("login")
    @Expose
    private String login;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
