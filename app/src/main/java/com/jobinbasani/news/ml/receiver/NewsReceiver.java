package com.jobinbasani.news.ml.receiver;

import com.jobinbasani.news.ml.constants.NewsConstants;
import com.jobinbasani.news.ml.service.NewsService;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class NewsReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(NewsConstants.LOG_TAG, "In broadcast reciever");
		Intent sIntent = new Intent(context, NewsService.class);
		sIntent.putExtra(NewsConstants.FIRST_LOAD, intent.getBooleanExtra(NewsConstants.FIRST_LOAD, false));
		startWakefulService(context, sIntent);
	}

}
