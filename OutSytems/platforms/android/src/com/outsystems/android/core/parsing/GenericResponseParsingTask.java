/*
 * Outsystems Project
 *
 * Copyright (C) 2014 Outsystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android.core.parsing;

import android.os.AsyncTask;
/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public abstract class GenericResponseParsingTask extends AsyncTask<Object, Void, Object> implements ParsingTaskMethods {

    public abstract Object parsingMethod();

    public abstract void parsingFinishMethod(Object result);

    @Override
    protected Object doInBackground(Object... params) {
        return parsingMethod();
    }

    @Override
    protected void onPostExecute(Object objects) {
        parsingFinishMethod(objects);
    }
}
