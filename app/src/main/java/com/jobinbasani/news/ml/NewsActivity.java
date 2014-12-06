package com.jobinbasani.news.ml;

import com.google.analytics.tracking.android.EasyTracker;
import com.jobinbasani.news.ml.constants.NewsConstants;
import com.jobinbasani.news.ml.util.NewsUtil;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class NewsActivity extends ActionBarActivity implements OnKeyListener{
	
	private String url;
	private String newsTitle;
	private ProgressBar progressBar;
	private WebView webView;
	private ShareActionProvider mShareActionProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		setupActionBar();
		this.url = getIntent().getStringExtra(NewsConstants.NEWS_URL);
		this.newsTitle = getIntent().getStringExtra(NewsConstants.NEWS_TITLE);
		setShareIntent();
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.getProgressDrawable().setColorFilter(Color.RED, Mode.SRC_IN);
		webView = (WebView) findViewById(R.id.webView);
		webView.setOnKeyListener(this);
		loadPage();
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.news_rate_app).setVisible(NewsUtil.showRateApp(this));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news, menu);
		MenuItem shareItem = menu.findItem(R.id.newsShareAction);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
		setShareIntent();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.news_feedback:
			startActivity(NewsUtil.getFeedbackIntent(this));
			break;
		case R.id.action_news_open_browser:
			startActivity(NewsUtil.getBrowserIntent(this.url));
			break;
		case R.id.news_rate_app:
			startActivity(NewsUtil.getPlaystoreListing(getPackageName()));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setShareIntent(){
		if(mShareActionProvider!=null)
		{
			String[] shareUrlArray = url.split("url=");
			mShareActionProvider.setShareIntent(NewsUtil.getShareDataIntent(newsTitle+" - "+shareUrlArray[shareUrlArray.length-1]));
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void loadPage(){

		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebChromeClient(new WebChromeClient(){

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				try{
					progressBar.setProgress(newProgress);
				}catch(Exception e){

				}
			}

		});
		webView.setWebViewClient(new WebViewClient(){

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Toast.makeText(NewsActivity.this, description, Toast.LENGTH_SHORT).show();
			}

		}); 
		webView.loadUrl(this.url);
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch(keyCode){
			case KeyEvent.KEYCODE_BACK:
				if(webView.canGoBack()){
					webView.goBack();
					return true;
				}
				break;
			}
		}
		return false;
	}

}
