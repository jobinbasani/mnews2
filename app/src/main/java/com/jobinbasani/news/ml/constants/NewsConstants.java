package com.jobinbasani.news.ml.constants;

import android.os.Environment;

public class NewsConstants {

	public final static String NEWS_FEED_URL = "http://news.google.co.in/news?cf=all&ned=ml_in&hl=ml&output=rss";
	public final static String LOG_TAG = "MalNews";
	public final static int CATEGORY_LOADER_ID = -1;
	public final static int NEWSGROUP_LOADER_ID = -2;
	public final static String CATEGORY_KEY = "categoryid";
	public final static String NEWSID_KEY = "newsid";
	public final static String NEWS_REFRESH_ACTION = "com.jobinbasani.news.ml.newsrefresh";
	public final static String NEWS_URL = "url";
	public final static String PREFS_FILE = "NewsPrefs";
	public final static String LAST_LOADED = "lastLoaded";
	public final static String FIRST_LOAD = "firstLoad";
	public final static String SCR_SHOT_DIR = Environment.getExternalStorageDirectory().toString() + "/MalayalamNewsScreenshots/";
	public final static String SCR_SHOT_PATH_KEY = "scrshotpath";
	public final static String FEED_LOADED = "feedloaded";
	public final static int ONE_MIN_MILLISECONDS = 60000;
	public final static String NEWS_TITLE = "newstitle";
}
