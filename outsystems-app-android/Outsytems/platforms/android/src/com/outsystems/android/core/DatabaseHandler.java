/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.outsystems.android.model.HubApplicationModel;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "hubManager";

    // Contacts table name
    private static final String TABLE_HUB_APPLICATION = "hubapplication";

    // Contacts Table Columns names
    private static final String KEY_HOST = "hostname";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_DATE_LAST_LOGIN = "date_last_login";
    private static final String KEY_NAME = "name";
    private static final String KEY_ISJSF = "is_jsf";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_HUB_APPLICATION + "(" + KEY_HOST + " TEXT PRIMARY KEY,"
                + KEY_USER_NAME + " TEXT," + KEY_PASSWORD + " TEXT," + KEY_DATE_LAST_LOGIN + " DATETIME," + KEY_NAME
                + " TEXT," + KEY_ISJSF + " NUMERIC" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HUB_APPLICATION);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addHubApplication(HubApplicationModel hubApplication) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HOST, hubApplication.getHost());
        values.put(KEY_USER_NAME, hubApplication.getUserName());
        values.put(KEY_PASSWORD, hubApplication.getPassword());
        values.put(KEY_DATE_LAST_LOGIN, getDateTime(hubApplication.getDateLastLogin()));
        values.put(KEY_NAME, hubApplication.getName());
        values.put(KEY_ISJSF, hubApplication.isJSF());

        // Inserting Row
        db.insert(TABLE_HUB_APPLICATION, null, values);
        db.close();
    }

    public void addHostHubApplication(String hostname, String name, boolean isJSF) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HOST, hostname);
        values.put(KEY_NAME, name);
        values.put(KEY_ISJSF, isJSF);
        values.put(KEY_DATE_LAST_LOGIN, getDateTime(new Date()));

        // Inserting Row
        db.insert(TABLE_HUB_APPLICATION, null, values);
        db.close();
    }

    // Getting single contact
    public HubApplicationModel getHubApplication(String host) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HUB_APPLICATION, new String[] { KEY_HOST, KEY_USER_NAME, KEY_PASSWORD,
                KEY_DATE_LAST_LOGIN, KEY_NAME, KEY_ISJSF }, KEY_HOST + "=?", new String[] { String.valueOf(host) },
                null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                // boolean value =cursor.getString(5).contains("true");
                HubApplicationModel hubApplication = new HubApplicationModel(cursor.getString(0), cursor.getString(1),
                        cursor.getString(2), convertStringToDate(cursor.getColumnName(3)), cursor.getString(4),
                        cursor.getInt(5) > 0);
                // return contact
                cursor.close();
                db.close();
                return hubApplication;
            }
            cursor.close();
        }
        db.close();
        return null;
    }

    // Getting All Applications
    public List<HubApplicationModel> getAllHubApllications() {
        List<HubApplicationModel> hubApplicationList = new ArrayList<HubApplicationModel>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_HUB_APPLICATION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                HubApplicationModel hubApplication = new HubApplicationModel();
                hubApplication.setHost(cursor.getString(0));
                hubApplication.setUserName(cursor.getString(1));
                hubApplication.setPassword(cursor.getString(2));
                String date = cursor.getString(3);
                hubApplication.setDateLastLogin(convertStringToDate(date));
                hubApplication.setName(cursor.getString(4));
                boolean value = cursor.getInt(5) > 0;
                hubApplication.setJSF(value);

                // Adding Hub Application to list
                hubApplicationList.add(hubApplication);
            } while (cursor.moveToNext());
        }

        // Sorting
        Collections.sort(hubApplicationList, new Comparator<HubApplicationModel>() {
            @Override
            public int compare(HubApplicationModel lhs, HubApplicationModel rhs) {
                if (lhs.getDateLastLogin().getTime() < rhs.getDateLastLogin().getTime())
                    return -1;
                else if (lhs.getDateLastLogin().getTime() == rhs.getDateLastLogin().getTime())
                    return 0;
                else
                    return 1;
            }
        });

        Collections.reverse(hubApplicationList);

        cursor.close();
        db.close();
        // return hub application list
        return hubApplicationList;
    }

    // Updating single hubapplication
    public int updateHubApplication(HubApplicationModel hubApplicationModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, hubApplicationModel.getUserName());
        values.put(KEY_PASSWORD, hubApplicationModel.getPassword());
        values.put(KEY_DATE_LAST_LOGIN, hubApplicationModel.getDateLastLogin().toString());
        values.put(KEY_NAME, hubApplicationModel.getName());
        values.put(KEY_ISJSF, hubApplicationModel.isJSF());

        // updating row
        int result = db.update(TABLE_HUB_APPLICATION, values, KEY_HOST + " = ?",
                new String[] { String.valueOf(hubApplicationModel.getHost()) });
        db.close();
        return result;
    }

    public int updateHubApplicationCredentials(String hostname, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, username);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_DATE_LAST_LOGIN, getDateTime(new Date()));

        // updating row
        int result = db.update(TABLE_HUB_APPLICATION, values, KEY_HOST + " = ?",
                new String[] { String.valueOf(hostname) });
        db.close();
        return result;
    }

    // Convert DateTime to String
    private String getDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    private Date convertStringToDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date convertedDate = new Date();
        try {
            EventLogger.logMessage(getClass(), "Date: " + date);
            convertedDate = dateFormat.parse(date);
            return convertedDate;
        } catch (ParseException e) {
            EventLogger.logError(getClass(), e);
        }

        return null;
    }

}
