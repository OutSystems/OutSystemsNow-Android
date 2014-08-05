/*
 * Outsystems Project
 *
 * Copyright (C) 2014 Outsystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android.model;

import java.util.List;
/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class Login {

    private boolean success;

    private String errorMessage;

    private List<Application> applications;

    public Login(boolean success, String errorMessage, List<Application> applications) {
        super();
        this.success = success;
        this.errorMessage = errorMessage;
        this.applications = applications;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

}
