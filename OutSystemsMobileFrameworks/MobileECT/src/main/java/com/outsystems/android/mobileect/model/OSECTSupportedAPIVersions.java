package com.outsystems.android.mobileect.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lrs on 21-11-2014.
 */
public class OSECTSupportedAPIVersions {

    private List<OSECTApi> supportedAPIVersions;

    public OSECTSupportedAPIVersions(){
        this.setSupportedAPIVersions(new ArrayList<OSECTApi>());
    }

    /**
     * Getters & Setters
     */

    public List<OSECTApi> getSupportedAPIVersions() {
        return supportedAPIVersions;
    }

    public void setSupportedAPIVersions(List<OSECTApi> supportedAPIVersions) {
        this.supportedAPIVersions = supportedAPIVersions;

    }


    /**
     * Versions management
     */
    public void addVersion(OSECTApi version){
        // Add new API version
        this.supportedAPIVersions.add(version);

        // Sort list
        Collections.sort(this.supportedAPIVersions);

    }

    public void removeAllVersions(){
        this.supportedAPIVersions.clear();
    }

    /**
     * Supported Version
     */
    public boolean checkCompatibilityWithVersion(String version){
        if(version == null || version.isEmpty())
            return false;

        ArrayList<OSECTApi> compatibleVersions = new ArrayList<OSECTApi>();

        Iterator<OSECTApi> iterator = this.getSupportedAPIVersions().iterator();

        while(iterator.hasNext()){
            OSECTApi api = iterator.next();
            boolean compatible = api.isCompatibleWithVersion(version);
            if(compatible){
                compatibleVersions.add(api);
                break;
            }
        }

        this.supportedAPIVersions.clear();
        this.supportedAPIVersions = compatibleVersions;

        return this.supportedAPIVersions.size() > 0;
    }

    public String getAPIVersionURL(){
        String url = null;

        if(this.supportedAPIVersions.size() > 0){
            OSECTApi api = this.getSupportedAPIVersions().get(0);
            url = api.getUrl();
        }

        return url;
    }


    public boolean hasSupportedAPIVersion(){
        return this.supportedAPIVersions.size() > 0;
    }

}
