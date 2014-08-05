/*
 * OutSystems Project
 *
 * Copyright (C) 2014 OutSystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android.core;
/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public abstract class WSRequestHandler {

    public abstract void requestFinish(Object result, boolean error, int statusCode);

}