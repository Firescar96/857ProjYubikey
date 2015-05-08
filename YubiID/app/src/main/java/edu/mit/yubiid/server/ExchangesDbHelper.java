package edu.mit.yubiid.server;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.mit.yubiid.server.Contract.Exchanges;

public class ExchangesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Exchanges.db";
    
	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
	    "CREATE TABLE " + Exchanges.TABLE_NAME + " (" +
	    		Exchanges._ID + " INTEGER PRIMARY KEY," +
	    		Exchanges.COLUMN_NAME_SENDER_YUBIKEY + TEXT_TYPE + COMMA_SEP +
	    Exchanges.COLUMN_NAME_PUBLIC_YUBIKEY + TEXT_TYPE + COMMA_SEP +
	    Exchanges.COLUMN_NAME_AMOUNT + INT_TYPE + COMMA_SEP +
	    " )";

	private static final String SQL_DELETE_ENTRIES =
	    "DROP TABLE IF EXISTS " + Exchanges.TABLE_NAME;

    public ExchangesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
