package com.example.radek.mapsproject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "routes_db";
    //private static DatabaseHelper sInstance;


    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
/*
    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }
*/

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(Route.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Route.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    long insertRoute(String route, String json, int time, String coordinates) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Route.COLUMN_ROUTE, route);
        values.put(Route.COLUMN_JSON, json);
        values.put(Route.COLUMN_TIME, time);
        values.put(Route.COLUMN_COORDINATES, coordinates);

        long id = sqLiteDatabase.insert(Route.TABLE_NAME, null, values);
        sqLiteDatabase.close();

        return id;
    }

    Route getRoute(long id) {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(Route.TABLE_NAME,
                new String[]{Route.COLUMN_ID, Route.COLUMN_ROUTE, Route.COLUMN_JSON, Route.COLUMN_TIME, Route.COLUMN_COORDINATES},
                Route.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        assert cursor != null;
        Route route = new Route(cursor.getInt(cursor.getColumnIndex(Route.COLUMN_ID)),
                                cursor.getString(cursor.getColumnIndex(Route.COLUMN_ROUTE)),
                                cursor.getString(cursor.getColumnIndex(Route.COLUMN_JSON)),
                                cursor.getString(cursor.getColumnIndex(Route.COLUMN_TIME)),
                                cursor.getString(cursor.getColumnIndex(Route.COLUMN_COORDINATES)));

        sqLiteDatabase.close();
        cursor.close();

        return route;
    }

    String getJsonfromDB(String routeName) {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(Route.TABLE_NAME,
                new String[]{Route.COLUMN_ID, Route.COLUMN_ROUTE, Route.COLUMN_JSON, Route.COLUMN_TIME, Route.COLUMN_COORDINATES},
                Route.COLUMN_ROUTE + "=?",
                new String[]{routeName}, null, null, null, null);

        if(cursor !=null) {
            cursor.moveToFirst();
        }

        assert cursor != null;
        Route route = new Route(cursor.getInt(cursor.getColumnIndex(Route.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_ROUTE)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_JSON)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_TIME)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_COORDINATES)));

        sqLiteDatabase.close();
        cursor.close();
        return route.getJson();
    }

    String getTimeFromDB(String routeName) {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(Route.TABLE_NAME,
                new String[]{Route.COLUMN_ID, Route.COLUMN_ROUTE, Route.COLUMN_JSON, Route.COLUMN_TIME, Route.COLUMN_COORDINATES},
                Route.COLUMN_ROUTE + "=?",
                new String[]{routeName}, null, null, null, null);

        if(cursor !=null) {
            cursor.moveToFirst();
        }

        assert cursor != null;
        Route route = new Route(cursor.getInt(cursor.getColumnIndex(Route.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_ROUTE)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_JSON)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_TIME)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_COORDINATES)));

        sqLiteDatabase.close();
        cursor.close();
        return route.getTime();

    }

    String getCoordinatesFromDB(String routeName) {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(Route.TABLE_NAME,
                new String[]{Route.COLUMN_ID, Route.COLUMN_ROUTE, Route.COLUMN_JSON, Route.COLUMN_TIME, Route.COLUMN_COORDINATES},
                Route.COLUMN_ROUTE + "=?",
                new String[]{routeName}, null, null, null, null);

        if(cursor !=null) {
            cursor.moveToFirst();
        }

        assert cursor != null;
        Route route = new Route(cursor.getInt(cursor.getColumnIndex(Route.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_ROUTE)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_JSON)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_TIME)),
                cursor.getString(cursor.getColumnIndex(Route.COLUMN_COORDINATES)));

        sqLiteDatabase.close();
        cursor.close();
        return route.getCoordinates();

    }

     List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();

        String query = "SELECT * FROM " + Route.TABLE_NAME + " ORDER BY " + Route.COLUMN_ID + " ASC";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Route route = new Route();
                route.setId(cursor.getInt(cursor.getColumnIndex(Route.COLUMN_ID)));
                route.setRoute(cursor.getString(cursor.getColumnIndex(Route.COLUMN_ROUTE)));
                route.setJson(cursor.getString(cursor.getColumnIndex(Route.COLUMN_JSON)));
                route.setTime(cursor.getString(cursor.getColumnIndex(Route.COLUMN_TIME)));
                route.setCoordinates(cursor.getString(cursor.getColumnIndex(Route.COLUMN_COORDINATES)));


                routes.add(route);
            } while (cursor.moveToNext());
        }

        // close db connection
        sqLiteDatabase.close();

        // return notes list
        return routes;
    }

    void updateRoute(Route route) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Route.COLUMN_ROUTE, route.getRoute());
        sqLiteDatabase.update(Route.TABLE_NAME, values, Route.COLUMN_ID + " = ?", new String[] {String.valueOf(route.getId())});
        sqLiteDatabase.close();
    }

    void updateTime(String routeName, String time) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Route.COLUMN_TIME, time);
        sqLiteDatabase.update(Route.TABLE_NAME, values, Route.COLUMN_ROUTE + " = ?", new String[]{routeName});
        sqLiteDatabase.close();

    }

    void updateJson(String routeName, String jsonarray) {

        /*
        String Query = "UPDATE " + Route.TABLE_NAME
                + " SET " + Route.COLUMN_JSON
                + " = " + "'" + jsonarray + "'"
                + " WHERE "
                + Route.COLUMN_ROUTE +" = " + "'" + routeName + "'";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL(Query);
      */

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Route.COLUMN_JSON, jsonarray);
        sqLiteDatabase.update(Route.TABLE_NAME, values, Route.COLUMN_ROUTE + " = ?", new String[]{routeName});
        sqLiteDatabase.close();

    }
    void updateCoordinates(String routeName, String coordinatesarray) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Route.COLUMN_COORDINATES, coordinatesarray);
        sqLiteDatabase.update(Route.TABLE_NAME, values, Route.COLUMN_ROUTE + " = ?", new String[]{routeName});
        sqLiteDatabase.close();

    }

    void deleteRoute(Route route) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(Route.TABLE_NAME, Route.COLUMN_ID + " = ?", new String[] {String.valueOf(route.getId())});
        sqLiteDatabase.close();
    }

    int getRoutesCount() {

        String query = "SELECT * FROM " + Route.TABLE_NAME;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        int count = cursor.getCount();
        sqLiteDatabase.close();
        cursor.close();

        return count;
    }
}
