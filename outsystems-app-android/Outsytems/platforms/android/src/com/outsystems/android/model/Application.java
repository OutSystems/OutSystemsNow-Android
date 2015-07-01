/*
 * Outsystems Project
 *
 * Copyright (C) 2014 Outsystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android.model;

import java.io.Serializable;
/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private int imageId;

    private String description;

    private String path;

    private boolean preloader;

    public Application(String name, int imageId, String description) {
        super();
        this.name = name;
        this.imageId = imageId;
        this.description = description;
    }

    public Application(String name, int imageId, String description, String path, boolean preloader) {
        super();
        this.name = name;
        this.imageId = imageId;
        this.description = description;
        this.path = path;
        this.preloader = preloader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasPreloader() {
        return preloader;
    }

    public void setPreloader(boolean preloader) {
        this.preloader = preloader;
    }

}
