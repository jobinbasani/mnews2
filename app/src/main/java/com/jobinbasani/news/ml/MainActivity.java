package com.jobinbasani.news.ml;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.jobinbasani.news.ml.constants.NewsConstants;
import com.jobinbasani.news.ml.fragments.CategorySelector;
import com.jobinbasani.news.ml.fragments.NewsWidget;
import com.jobinbasani.news.ml.interfaces.NewsDataHandlers;
import com.jobinbasani.news.ml.provider.NewsDataContract;
import com.jobinbasani.news.ml.provider.NewsDataContract.NewsDataEntry;
import com.jobinbasani.news.ml.receiver.NewsReceiver;
import com.jobinbasani.news.ml.util.NewsUtil;

public class MainActivity extends ActionBarActivity implements LoaderCallbacks<Cursor>, NewsDataHandlers {
	
	CategorySelector categorySelector;
	NewsWidget newsWidget;
	SharedPreferences prefs;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			afterRefresh(intent.getBooleanExtra(NewsConstants.FIRST_LOAD, false),intent.getBooleanExtra(NewsConstants.FEED_LOADED, false));
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = getSharedPreferences(NewsConstants.PREFS_FILE, MODE_PRIVATE);
		long lastLoaded = prefs.getLong(NewsConstants.LAST_LOADED, 0);
		if(lastLoaded == 0){
			setContentView(R.layout.splashscreen_layout);
			getSupportActionBar().hide();
		}else{
			setContentView(R.layout.activity_main);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(NewsConstants.NEWS_REFRESH_ACTION));
		long lastLoaded = prefs.getLong(NewsConstants.LAST_LOADED, 0);
		if(lastLoaded == 0){
			refreshNews(true);
		}else if(System.currentTimeMillis()-lastLoaded>(NewsConstants.ONE_MIN_MILLISECONDS * 5)){
			refreshNews(false);
		}
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
		if(newsWidget!=null)
			newsWidget.setRefreshing(false);
		EasyTracker.getInstance(this).activityStop(this);
		super.onStop();
		
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_main_rate_app).setVisible(NewsUtil.showRateApp(this));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case R.id.action_screenshot:
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					String scrShotPath = NewsUtil.takeScreenshot(getWindow().getDecorView().getRootView());
					Intent scrShotIntent = new Intent(MainActivity.this, ScreenshotActivity.class);
					scrShotIntent.putExtra(NewsConstants.SCR_SHOT_PATH_KEY, scrShotPath);
					startActivity(scrShotIntent);
				}
			});
			
			break;
		case R.id.action_main_feedback:
			startActivity(NewsUtil.getFeedbackIntent(this));
			break;
		case R.id.action_main_rate_app:
			startActivity(NewsUtil.getPlaystoreListing(getPackageName()));
			break;
		}
		
		return true;
	}

	private void refreshNews(boolean firstLoad){
		if(!firstLoad)
			newsWidget.setRefreshing(true);
		Intent newsIntent = new Intent(this, NewsReceiver.class);
		newsIntent.putExtra(NewsConstants.FIRST_LOAD, firstLoad);
		sendBroadcast(newsIntent);
	}
	private void afterRefresh(boolean firstLoad, boolean feedLoaded){
		if(firstLoad){
			try{
				setContentView(R.layout.activity_main);
				getSupportActionBar().show();
			}catch(Exception e){
				
			}
		}else{
			newsWidget.setRefreshing(false);
			if(feedLoaded)
				NewsUtil.showToast(this, "Refresh complete!");
			else
				NewsUtil.showToast(this, "No new items!");
			resetAndLoadList();
		}
		if(feedLoaded){
			SharedPreferences.Editor editor = prefs.edit();
			editor.putLong(NewsConstants.LAST_LOADED, System.currentTimeMillis());
			editor.commit();
		}
	}
	
	private void resetAndLoadList(){
		newsWidget.swapMainCursor(null);
		categorySelector.changeSpinnerCursor(null);
		initLoaderWithId(NewsConstants.CATEGORY_LOADER_ID, null);
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(categorySelector==null){
			categorySelector = (CategorySelector) getFragmentManager().findFragmentByTag(getResources().getString(R.string.categorySelectorTag));
		}
		if(newsWidget==null){
			newsWidget = (NewsWidget) getFragmentManager().findFragmentByTag(getResources().getString(R.string.newsWidgetTag));
		}
		switch(id){
		case NewsConstants.CATEGORY_LOADER_ID:
			return new CursorLoader(MainActivity.this, NewsDataContract.CONTENT_URI_CATEGORIES, null, null, null, null);
		case NewsConstants.NEWSGROUP_LOADER_ID:
			String categoryId = args.getInt(NewsConstants.CATEGORY_KEY, 0)+"";
			return new CursorLoader(MainActivity.this, NewsDataContract.CONTENT_URI_MAINNEWS, new String[]{NewsDataEntry._ID,NewsDataEntry.COLUMN_NAME_NEWSHEADER,NewsDataEntry.COLUMN_NAME_NEWSDETAILS,NewsDataEntry.COLUMN_NAME_NEWSIMG,NewsDataEntry.COLUMN_NAME_NEWSID}, NewsDataEntry.COLUMN_NAME_NEWSID+" is not null and "+NewsDataEntry.COLUMN_NAME_CATEGORYID+"=?", new String[]{categoryId}, null);
		default:
			String newsId = args.getString(NewsConstants.NEWSID_KEY, "0");
			return new CursorLoader(MainActivity.this, NewsDataContract.CONTENT_URI_CHILDNEWS, new String[]{NewsDataEntry._ID,NewsDataEntry.COLUMN_NAME_NEWSHEADER,NewsDataEntry.COLUMN_NAME_NEWSPROVIDER,NewsDataEntry.COLUMN_NAME_NEWSLINK}, NewsDataEntry.COLUMN_NAME_PARENTID+"=?", new String[]{newsId}, null);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch(loader.getId()){
		case NewsConstants.CATEGORY_LOADER_ID:
			categorySelector.changeSpinnerCursor(cursor);
			break;
		case NewsConstants.NEWSGROUP_LOADER_ID:
			newsWidget.swapMainCursor(cursor);
			break;
		default:
			newsWidget.swapChildCursor(loader.getId(), cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch(loader.getId()){
		case NewsConstants.CATEGORY_LOADER_ID:
			categorySelector.changeSpinnerCursor(null);
			break;
		case NewsConstants.NEWSGROUP_LOADER_ID:
			newsWidget.swapMainCursor(null);
			break;
		default:
			newsWidget.swapChildCursor(loader.getId(), null);
		}
	}

	@Override
	public void initLoaderWithId(int loaderId, Bundle args) {
		if(getLoaderManager().getLoader(loaderId)!=null){
			getLoaderManager().restartLoader(loaderId, args, this);
		}else{
			getLoaderManager().initLoader(loaderId, args, this);
		}
	}

	@Override
	public void refreshFeed() {
		refreshNews(false);
	}

}
