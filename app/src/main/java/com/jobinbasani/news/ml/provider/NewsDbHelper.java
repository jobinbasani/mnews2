package com.jobinbasani.news.ml.provider;

import com.jobinbasani.news.ml.provider.NewsDataContract.NewsDataEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NewsDbHelper extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "News.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NewsDataEntry.TABLE_NAME + " (" +
            		NewsDataEntry._ID +INTEGER_TYPE+" PRIMARY KEY AUTOINCREMENT," +
            		NewsDataEntry.COLUMN_NAME_NEWSID + INTEGER_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_BATCHID + INTEGER_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_PARENTID + INTEGER_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_NEWSHEADER + TEXT_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_NEWSCATEGORY + TEXT_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_CATEGORYID + INTEGER_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_NEWSDETAILS + TEXT_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_NEWSPROVIDER + TEXT_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_NEWSIMG + TEXT_TYPE + COMMA_SEP +
            		NewsDataEntry.COLUMN_NAME_NEWSLINK + TEXT_TYPE +
            " )";
        private static final String SQL_DELETE_ENTRIES =
        	    "DROP TABLE IF EXISTS " + NewsDataEntry.TABLE_NAME;

	public NewsDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

}
