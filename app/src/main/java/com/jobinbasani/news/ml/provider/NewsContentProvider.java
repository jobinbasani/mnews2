package com.jobinbasani.news.ml.provider;

import com.jobinbasani.news.ml.provider.NewsDataContract.NewsDataEntry;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class NewsContentProvider extends ContentProvider {
	
	private NewsDbHelper dbHelper = null;
	private static UriMatcher URI_MATCHER;
	private static final int NEWS_CATEGORIES = 1;
	private static final int NEWS_MAIN = 2;
	private static final int NEWS_CHILD = 3;
	
	static{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(NewsDataContract.AUTHORITY, NewsDataContract.CATEGORIES, NEWS_CATEGORIES);
		URI_MATCHER.addURI(NewsDataContract.AUTHORITY, NewsDataContract.MAINNEWS, NEWS_MAIN);
		URI_MATCHER.addURI(NewsDataContract.AUTHORITY, NewsDataContract.CHILDNEWS, NEWS_CHILD);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		return db.delete(NewsDataEntry.TABLE_NAME, selection, selectionArgs);
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int rowsAdded = 0;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		
		try{
			for(ContentValues value:values){
				db.insert(NewsDataEntry.TABLE_NAME, null, value);
				rowsAdded++;
			}
			
			db.setTransactionSuccessful();
		}catch(Exception e){
			
		}finally{
			db.endTransaction();
		}
		return rowsAdded;
	}

	@Override
	public boolean onCreate() {
		dbHelper = new NewsDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		
		int uriType = URI_MATCHER.match(uri);
		switch(uriType){
		case NEWS_CATEGORIES:
			String categorySelection = "SELECT distinct "+NewsDataEntry.COLUMN_NAME_CATEGORYID+" as "+NewsDataEntry._ID+", "+NewsDataEntry.COLUMN_NAME_NEWSCATEGORY+" from "+NewsDataEntry.TABLE_NAME
					+" where "+NewsDataEntry.COLUMN_NAME_NEWSCATEGORY+" is not null and "
					+NewsDataEntry.COLUMN_NAME_BATCHID
					+"=(select max("+NewsDataEntry.COLUMN_NAME_BATCHID+") from "+NewsDataEntry.TABLE_NAME+") order by "+NewsDataEntry.COLUMN_NAME_CATEGORYID;
			return dbHelper.getReadableDatabase().rawQuery(categorySelection, null);
		case NEWS_MAIN:
			return dbHelper.getReadableDatabase().query(NewsDataEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, NewsDataEntry.COLUMN_NAME_NEWSID);
		case NEWS_CHILD:
			return dbHelper.getReadableDatabase().query(NewsDataEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, NewsDataEntry._ID);
		}
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}

}
