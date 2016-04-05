package com.silicornio.bulgingeyes.db.drivers.sqlite;

/**
 * Created by javak@silicornio
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JESQLiteOpenHelper extends SQLiteOpenHelper {

    /** Name of the database **/
    private String mDbName;

    /** Version of the database **/
    private int mDbVersion;

    public JESQLiteOpenHelper(Context context, SQLiteDBDriver mysqlDriver, String dbName, int dbVersion){
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    /**
     * Get the cursor
     * @param cursor Cursor from where read value
     * @param index int to read
     * @return Object read
     */
    private static Object getCursorValue(Cursor cursor, int index){
        switch(cursor.getType(index)){
            case Cursor.FIELD_TYPE_INTEGER: return Integer.valueOf(cursor.getInt(index));
            case Cursor.FIELD_TYPE_FLOAT: return Float.valueOf(cursor.getFloat(index));
            case Cursor.FIELD_TYPE_STRING: return cursor.getString(index);
            case Cursor.FIELD_TYPE_BLOB: return cursor.getBlob(index);
            default:
                return null;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
