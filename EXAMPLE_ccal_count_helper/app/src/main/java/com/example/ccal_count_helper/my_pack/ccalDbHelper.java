package com.example.ccal_count_helper.my_pack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.ccal_count_helper.my_pack.ccalContract.GuestEntry;

public class ccalDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ccalContract.class.getSimpleName();
    private static final String DATABASE_NAME = "ccals.db";
    private static final int DATABASE_VERSION = 1;

    public ccalDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String SQL_CREATE_CALLS_TABLE = "CREATE TABLE " + GuestEntry.TABLE_NAME + " ("
                + ccalContract.GuestEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GuestEntry.COLUMN_CCAL + " TEXT NOT NULL, "
                + GuestEntry.COLUMN_DATE + " TEXT NOT NULL);";

        // Запускаем создание таблицы
        db.execSQL(SQL_CREATE_CALLS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
