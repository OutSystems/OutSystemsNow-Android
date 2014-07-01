package com.outsystems.android.model;

import com.google.gson.annotations.SerializedName;

public class Infrastructure {

    @SerializedName("Name")
    private String name;

    public Infrastructure(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
