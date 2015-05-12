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

import com.outsystems.android.model.Application;
import com.outsystems.android.model.HubApplicationModel;
import com.outsystems.android.model.MobileECT;

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
    private static final int DATABASE_VERSION = 4;

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


    // Mobile ECT Database

    // Mobile ECT Table name
    private static final String TABLE_MOBILE_ECT = "mobileECT";

    //  Mobile ECT Table Columns names
    private static final String KEY_MOBILE_ECT_FIRST_LOAD = "firstLoad";


    // Offline Support Database

    // Applications Table name
    private static final String TABLE_LOGIN_APPLICATIONS = "login_applications";

    // Applications Table Columns names
    private static final String KEY_APPLICATION_HOST = "hostname";
    private static final String KEY_APPLICATION_USER_NAME = "user_name";
    private static final String KEY_APPLICATION_NAME = "name";
    private static final String KEY_APPLICATION_DESCRIPTION = "description";
    private static final String KEY_APPLICATION_IMAGE = "image";
    private static final String KEY_APPLICATION_PATH = "path";
    private static final String KEY_APPLICATION_PRELOADER = "preloader";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_HUB_APPLICATION + "(" + KEY_HOST + " TEXT PRIMARY KEY,"
                + KEY_USER_NAME + " TEXT," + KEY_PASSWORD + " TEXT," + KEY_DATE_LAST_LOGIN + " DATETIME," + KEY_NAME
                + " TEXT," + KEY_ISJSF + " NUMERIC" + ")";
        try {
            db.execSQL(CREATE_CONTACTS_TABLE);
        }catch(Exception e){
            e.printStackTrace();
        }

        // Mobile ECT
        String createMobileECTTable = "CREATE TABLE " + TABLE_MOBILE_ECT + "(" + KEY_MOBILE_ECT_FIRST_LOAD + " NUMERIC PRIMARY KEY" + ")";
        try {
            db.execSQL(createMobileECTTable);
        }catch(Exception e){
            e.printStackTrace();
        }

        // Offline support
        String createApplicationsTable = "CREATE TABLE " + TABLE_LOGIN_APPLICATIONS +
                "(" + KEY_APPLICATION_HOST          + " TEXT,"
                    + KEY_APPLICATION_USER_NAME     + " TEXT,"
                    + KEY_APPLICATION_NAME          + " TEXT,"
                    + KEY_APPLICATION_DESCRIPTION   + " TEXT,"
                    + KEY_APPLICATION_IMAGE         + " NUMERIC,"
                    + KEY_APPLICATION_PATH          + " TEXT,"
                    + KEY_APPLICATION_PRELOADER     + " NUMERIC,"
                    + " PRIMARY KEY ("+KEY_APPLICATION_HOST+", "+KEY_APPLICATION_USER_NAME+", "+KEY_APPLICATION_NAME+")"
                +")";

        try {
            db.execSQL(createApplicationsTable);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HUB_APPLICATION);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_ECT);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN_APPLICATIONS);
        */

        try {

            // Update Applications table
            db.execSQL("ALTER TABLE "+TABLE_LOGIN_APPLICATIONS+" ADD "+ KEY_APPLICATION_PRELOADER  + " NUMERIC");

            // Create tables again
            onCreate(db);

        }catch(Exception e){
            e.printStackTrace();
        }
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
                        cursor.getString(2), convertStringToDate(cursor.getString(3)), cursor.getString(4),
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

    public HubApplicationModel getLastLoginHubApplicationModel(){
        HubApplicationModel result = null;

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_HUB_APPLICATION +
                             " ORDER BY datetime("+KEY_DATE_LAST_LOGIN + ") DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            result = new HubApplicationModel();
            result.setHost(cursor.getString(0));
            result.setUserName(cursor.getString(1));
            result.setPassword(cursor.getString(2));
            String date = cursor.getString(3);
            result.setDateLastLogin(convertStringToDate(date));
            result.setName(cursor.getString(4));
            boolean value = cursor.getInt(5) > 0;
            result.setJSF(value);
        }

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


    // Mobile ECT

    public void addMobileECT(boolean firstLoad) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MOBILE_ECT_FIRST_LOAD, firstLoad ? 1 : 0);

        // Delete previous rows
        db.delete(TABLE_MOBILE_ECT,null,null);

        // Inserting Row
        db.insert(TABLE_MOBILE_ECT, null, values);
        db.close();
    }

    // Getting single contact
    public MobileECT getMobileECT() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MOBILE_ECT, new String[] { KEY_MOBILE_ECT_FIRST_LOAD }, null, null,
                null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                MobileECT mobileECT = new MobileECT();
                mobileECT.setFirstLoad(cursor.getInt(0) > 0);

                cursor.close();
                db.close();

                return mobileECT;
            }
            cursor.close();
        }
        db.close();
        return null;
    }


    public void addLoginApplications(String hostname, String username, List<Application> applications){
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete previous rows
        db.delete(TABLE_LOGIN_APPLICATIONS,null,null);

        if(applications != null) {
            for(Application app : applications) {
                ContentValues values = new ContentValues();
                values.put(KEY_APPLICATION_HOST, hostname);
                values.put(KEY_APPLICATION_USER_NAME, username);

                values.put(KEY_APPLICATION_NAME, app.getName());
                values.put(KEY_APPLICATION_DESCRIPTION, app.getDescription());
                values.put(KEY_APPLICATION_IMAGE, app.getImageId());
                values.put(KEY_APPLICATION_PATH, app.getPath());
                values.put(KEY_APPLICATION_PRELOADER, app.hasPreloader() ? 1 : 0);

                // Inserting Row
                db.insert(TABLE_LOGIN_APPLICATIONS, null, values);
            }
        }
        db.close();
    }

    public List<Application> getLoginApplications(String hostname, String username){
        List<Application> result = new ArrayList<Application>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_LOGIN_APPLICATIONS +
                             " WHERE "+KEY_APPLICATION_HOST+"= ?" +
                             " AND "+KEY_APPLICATION_USER_NAME +"= ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{hostname,username});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                String host = cursor.getString(0);
                String user = cursor.getString(1);
                String name = cursor.getString(2);
                String desc = cursor.getString(3);
                int image = cursor.getInt(4);
                String path = cursor.getString(5);
                Boolean preloader = cursor.getInt(6) > 0;

                Application app = new Application(name, image, desc, path, preloader);

                result.add(app);

            } while (cursor.moveToNext());
        }

        db.close();

        return result;
    }
}
