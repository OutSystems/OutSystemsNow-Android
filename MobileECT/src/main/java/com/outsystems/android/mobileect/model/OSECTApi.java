package com.outsystems.android.mobileect.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by lrs on 21-11-2014.
 */
public class OSECTApi implements Comparable<OSECTApi>  {
    private final int MAJOR_VERSION = 0;
    private final int MINOR_VERSION = 1;
    private final int MAINTENANCE_VERSION = 2;

    private String version;
    private boolean currentVersion;
    private String url;

    public OSECTApi(String version, boolean currentVersion, String url){
        this.setVersion(version);
        this.setCurrentVersion(currentVersion);
        this.setUrl(url);
    }

    /**
     * Getters & Setters
     */

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(boolean currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString(){
        return "ECT Api - Version: "+this.version+ ", URL: "+this.url+", IsCurrentVersion: "+this.currentVersion;
    }


    /**
     * Compare
     */

    @Override
    public int compareTo(OSECTApi other) {
        if(other == null)
            return 1;

        // Current Version
        int currentVersionComp = this.compareByCurrentVersion(other);
        if(currentVersionComp != 0)
            return currentVersionComp;

        ArrayList<String> thisVersions = getReleaseVersions(this.version);
        ArrayList<String> otherVersions = getReleaseVersions(other.getVersion());

        // Major release version
        int majorReleaseComp = this.compareByReleaseVersion(thisVersions,otherVersions,MAJOR_VERSION);
        if(majorReleaseComp != 0)
            return majorReleaseComp;

        // Minor release version
        int minorReleaseComp = this.compareByReleaseVersion(thisVersions,otherVersions,MINOR_VERSION);
        if(minorReleaseComp != 0)
            return minorReleaseComp;

        // Maintenance release version
        int maintenanceReleaseComp = this.compareByReleaseVersion(thisVersions,otherVersions,MAINTENANCE_VERSION);
        if(maintenanceReleaseComp != 0)
            return maintenanceReleaseComp;

        return 0;
    }

    private int compareByCurrentVersion(OSECTApi other){
        if(!this.isCurrentVersion() && other.isCurrentVersion())
            return -1;
        else{
            if(this.isCurrentVersion() && !other.isCurrentVersion()){
                return 1;
            }
        }
        return 0;
    }

    private ArrayList<String> getReleaseVersions(String version){
        StringTokenizer tokenizer = new StringTokenizer(version,".");
        ArrayList<String> releases = new ArrayList<String>();

        while(tokenizer.hasMoreTokens()){
          releases.add(tokenizer.nextToken());
        }

        return releases;
    }

    private int compareByReleaseVersion(List<String> thisRelease,List<String> otherRelease, int version){
        if(thisRelease.size() > version && otherRelease.size() > version){
            try {
                int thisVersion = Integer.valueOf(thisRelease.get(version));
                int otherVersion = Integer.valueOf(otherRelease.get(version));

                if(thisVersion < otherVersion)
                    return -1;
                else{
                    if(thisVersion > otherVersion)
                        return 1;
                }

            }
            catch(Exception e){
                return 0;
            }
        }

        return 0;
    }


    /**
     * Compatibility
     */
    public boolean isCompatibleWithVersion(String version){

        ArrayList<String> thisReleases = getReleaseVersions(this.version);
        ArrayList<String> otherReleases = getReleaseVersions(version);

        if (thisReleases.size() != otherReleases.size() ||
                (thisReleases.size() < MINOR_VERSION || otherReleases.size() < MINOR_VERSION ))
            return false;

        boolean majorVersion = thisReleases.get(MAJOR_VERSION).equals(otherReleases.get(MAJOR_VERSION));

        boolean minorVersion = thisReleases.get(MINOR_VERSION).equals(otherReleases.get(MINOR_VERSION));;

        return majorVersion && minorVersion;
    }
}
