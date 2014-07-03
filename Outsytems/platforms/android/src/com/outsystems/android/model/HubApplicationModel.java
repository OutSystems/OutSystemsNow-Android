/*
 * Outsystems Project
 *
 * Copyright (C) 2014 Outsystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android.model;

import java.util.Date;
/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class HubApplicationModel {

    private String host;
    private String userName;
    private String password;
    private Date dateLastLogin;
    private String name;
    private boolean isJSF;

    public HubApplicationModel() {

    }

    public HubApplicationModel(String host, String userName, String password, Date dateLastLogin, String name, boolean isJSF) {
        super();
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.dateLastLogin = dateLastLogin;
        this.name = name;
        this.isJSF = isJSF;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDateLastLogin() {
        return dateLastLogin;
    }

    public void setDateLastLogin(Date dateLastLogin) {
        this.dateLastLogin = dateLastLogin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isJSF() {
        return isJSF;
    }

    public void setJSF(boolean isJSF) {
        this.isJSF = isJSF;
    }
}
