package com.crdir.iMemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "memo.db";
    private static int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS memo_label (" +
                "_id TEXT PRIMARY KEY," +
                "label VARCHAR(30) )";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS memo_item (" +
                "label_id TEXT," +
                "content TEXT," +
                "type SMALLINT, " +
                "item_id TEXT, " +
                "FOREIGN KEY (label_id) REFERENCES memo_label(_id) ON DELETE CASCADE ON UPDATE CASCADE)";
        db.execSQL(sql);

        sql = "PRAGMA foreign_keys = ON";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
