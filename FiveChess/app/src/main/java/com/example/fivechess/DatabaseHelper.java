package com.example.fivechess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "treasure.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_NAME = "time";
    public static final String start_time = "start_time";
    public static final String end_time = "end_time";
    public static final String game_duration = "game_duration";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE time (" +
                "start_time INTEGER, " +
                "end_time INTEGER, " +
                "game_duration INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + game_duration + " INTEGER");
        }
    }
}
