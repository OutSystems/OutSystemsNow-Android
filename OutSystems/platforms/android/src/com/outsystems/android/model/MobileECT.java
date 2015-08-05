package com.outsystems.android.model;

import java.io.Serializable;

/**
 * Created by lrs on 27-11-2014.
 */
public class MobileECT implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean isFirstLoad;

    public MobileECT(){
        super();
        this.isFirstLoad = true;
    }

    public boolean isFirstLoad() {
        return isFirstLoad;
    }

    public void setFirstLoad(boolean isFirstLoad) {
        this.isFirstLoad = isFirstLoad;
    }
}
