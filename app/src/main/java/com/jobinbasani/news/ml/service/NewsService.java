package com.jobinbasani.news.ml.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.jobinbasani.news.ml.R;
import com.jobinbasani.news.ml.constants.NewsConstants;
import com.jobinbasani.news.ml.provider.NewsDataContract;
import com.jobinbasani.news.ml.provider.NewsDataContract.NewsDataEntry;
import com.jobinbasani.news.ml.receiver.NewsReceiver;
import com.jobinbasani.news.ml.vo.NewsItem;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NewsService extends IntentService {
	
	private ArrayList<NewsItem> newsCollection;
	private ArrayList<String> idList;
	private long batchId;

	public NewsService() {
		super("NewsService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(NewsConstants.LOG_TAG, "In intent service");
		boolean isFirstLoad = intent.getBooleanExtra(NewsConstants.FIRST_LOAD, false);
		long lastLoaded = getSharedPreferences(NewsConstants.PREFS_FILE, MODE_PRIVATE).getLong(NewsConstants.LAST_LOADED, 0);
		batchId = System.currentTimeMillis();
		String[] topics = getResources().getStringArray(R.array.topics);
		newsCollection = new ArrayList<NewsItem>();
		idList = new ArrayList<String>();
		Intent newsIntent = new Intent(NewsConstants.NEWS_REFRESH_ACTION);
		newsIntent.putExtra(NewsConstants.FIRST_LOAD, isFirstLoad);
		if((batchId-lastLoaded)>(NewsConstants.ONE_MIN_MILLISECONDS * 4)){
			CountDownLatch countdownLatch = new CountDownLatch(topics.length);
			int categoryId = 0;
			for(String topic:topics){
				new Thread(new FeedLoader(countdownLatch, topic, ++categoryId+"")).start();
			}
			try{
				countdownLatch.await();
				
				if(newsCollection.size()>0){
					clearOldImages(batchId-(NewsConstants.ONE_MIN_MILLISECONDS * 10));
					NewsItem newsValues = new NewsItem();
					newsValues.setChildNewsItems(newsCollection);
					int insertCount = getContentResolver().bulkInsert(NewsDataContract.CONTENT_URI, newsValues.getContentValueArray());
					if(insertCount>0){
						Log.d(NewsConstants.LOG_TAG, "Inserted = "+insertCount);
						getContentResolver().delete(NewsDataContract.CONTENT_URI, NewsDataEntry.COLUMN_NAME_BATCHID+"<?", new String[]{batchId+""});
						newsIntent.putExtra(NewsConstants.FEED_LOADED, true);
					}
					
				}
				
				finishTasks(newsIntent, intent);
			}catch(Exception e){
				
			}
		}else{
			newsIntent.putExtra(NewsConstants.FEED_LOADED, false);
			finishTasks(newsIntent, intent);
		}
		
	}
	
	private void finishTasks(Intent newsIntent, Intent wakefulIntent){
		LocalBroadcastManager.getInstance(this).sendBroadcast(newsIntent);
		newsCollection = null;
		idList = null;
		NewsReceiver.completeWakefulIntent(wakefulIntent);
	}
	
	private void getTopicNews(String topic, String categoryId){
		ArrayList<NewsItem> newsList = new ArrayList<NewsItem>();
		try{
			String feedUrl = NewsConstants.NEWS_FEED_URL;
			if(!topic.equals("0"))
				feedUrl = feedUrl+"&topic="+topic;
			feedUrl = feedUrl+"&num=40&rand="+new Random().nextLong();
			URL url = new URL(feedUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setAllowUserInteraction(false);
            urlConnection.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			   try {
			     BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
			     XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			     XmlPullParser xpp = factory.newPullParser();
			     xpp.setInput(in, "UTF-8");
			     int eventType = xpp.getEventType();
			     String newsCategory = null;
			     while(eventType != XmlPullParser.END_DOCUMENT){
			    	 if( xpp.getName()!=null && xpp.getName().equals("category") && eventType==XmlPullParser.START_TAG){
			    		 eventType = xpp.next();
			    		 newsCategory = xpp.getText();
			    		 eventType = xpp.next();
			    		 continue;
			    	 }
			    	 if(xpp.getName()!=null && xpp.getName().equals("description") && eventType==XmlPullParser.START_TAG){
			    		 eventType = xpp.next();
			    		 String details = xpp.getText().replaceAll("&nbsp;", " ").replaceAll("&raquo;", ">>");
			    		 if(details.startsWith("<table")){
			    			 String newsId = getNewsId();
			    			 NewsItem mainNews = new NewsItem();
			    			 NewsItem childNews = new NewsItem();
			    			 ArrayList<NewsItem> childNewsList = new ArrayList<NewsItem>();
				    		 XmlPullParser detailsParser = factory.newPullParser();
				    		 detailsParser.setInput(new StringReader(details));
				    		 int detailsEventType = detailsParser.getEventType();
				    		 boolean upcomingMainNewsHeader = false;
				    		 boolean upcomingMainNewsDetails = false;
				    		 boolean upcomingMainNewsProvider = false;
				    		 boolean childNewsStart = true;
				    		 int fontCounter = 0;
				    		 int linkCounter = 0;
				    		 boolean mainHeaderSection = true;
				    		 mainNews.setNewsId(newsId);
				    		 mainNews.setBatchId(batchId+"");
				    		 mainNews.setCategoryId(categoryId);
				    		 if(newsCategory!=null && newsCategory.length()>0){
				    			 mainNews.setNewsCategory(newsCategory);
				    		 }
				    		 while(detailsEventType != XmlPullParser.END_DOCUMENT){
				    			 if(mainHeaderSection){
				    				 if(mainNews.getNewsImageUrl()==null && detailsEventType == XmlPullParser.START_TAG && detailsParser.getName()!=null && detailsParser.getName().equals("img")){
					    				 mainNews.setNewsImageUrl(detailsParser.getAttributeValue(null, "src"));
					    				 if(mainNews.getNewsImageUrl()!=null && mainNews.getNewsImageUrl().startsWith("//")){
					    					 mainNews.setNewsImageUrl("http:"+mainNews.getNewsImageUrl());
					    				 }
					    				 if(mainNews.getNewsImageUrl()!=null){
					    					 mainNews.setImageId("nimg"+mainNews.getNewsId()+".png");
					    				 }
					    				 if(mainNews.getImageId()!=null){
					    					 downloadImage(mainNews.getNewsImageUrl(), mainNews.getImageId());
					    				 }
					    				 detailsEventType = detailsParser.next();
					    				 continue;
					    			 }
					    			 if(mainNews.getNewsLink()==null && detailsEventType==XmlPullParser.START_TAG && detailsParser.getName()!=null && detailsParser.getName().equals("a")){
					    				 linkCounter++;
					    				 if(linkCounter == 2){
					    					 mainNews.setNewsLink(detailsParser.getAttributeValue(null, "href"));
						    				 upcomingMainNewsHeader = true;
					    				 }
					    				 detailsEventType = detailsParser.next();
					    				 continue;
					    			 }
					    			 if(upcomingMainNewsHeader && mainNews.getNewsHeader()==null && detailsEventType==XmlPullParser.START_TAG && detailsParser.getName()!=null && detailsParser.getName().equals("b")){
					    				 upcomingMainNewsHeader = false;
					    				 upcomingMainNewsProvider = true;
					    				 
					    				 detailsEventType = detailsParser.next();
					    				 mainNews.setNewsHeader(detailsParser.getText());
					    				 detailsEventType = detailsParser.next();
					    				 continue;
					    			 }
				    				 if(upcomingMainNewsProvider && detailsEventType==XmlPullParser.START_TAG && detailsParser.getName()!=null && detailsParser.getName().equals("font")){
				    					 fontCounter++;
				    					 if(fontCounter == 2){
				    						 detailsEventType = detailsParser.next();
				    						 mainNews.setNewsProvider(detailsParser.getText());
				    						 upcomingMainNewsProvider = false;
				    						 upcomingMainNewsDetails = true;
					    				 }
				    					 detailsEventType = detailsParser.next();
					    				 continue;
				    				 }
				    				 
				    				 if(upcomingMainNewsDetails && detailsEventType==XmlPullParser.START_TAG && detailsParser.getName()!=null && detailsParser.getName().equals("font")){
				    					 detailsEventType = detailsParser.next();
				    					 mainNews.setNewsDetails(detailsParser.getText());
			    						 upcomingMainNewsProvider = false;
			    						 mainHeaderSection = false;
			    						 detailsEventType = detailsParser.next();
					    				 continue;
				    				 }
				    			 }else{
				    				 if(childNewsStart && detailsEventType==XmlPullParser.START_TAG && detailsParser.getName()!=null && detailsParser.getName().equals("a")){
				    					 childNewsStart = false;
				    					 childNews = new NewsItem();
				    					 childNews.setParentId(newsId);
				    					 childNews.setNewsLink(detailsParser.getAttributeValue(null, "href"));
				    					 detailsEventType = detailsParser.next();
				    					 childNews.setNewsHeader(detailsParser.getText());
				    					 if(childNews.getNewsHeader()==null){
				    						 childNews.setNewsHeader(mainNews.getNewsHeader());
				    					 }
				    				 }else if(!childNewsStart && detailsEventType==XmlPullParser.START_TAG && detailsParser.getName()!=null && detailsParser.getName().equals("nobr")){
				    					 childNewsStart = true;
				    					 detailsEventType = detailsParser.next();
				    					 childNews.setNewsProvider(detailsParser.getText());
				    					 if(childNews.getNewsHeader()!=null && childNews.getNewsLink()!=null && childNews.getNewsProvider()!=null){
				    						 childNews.setBatchId(batchId+"");
				    						 childNews.setCategoryId(categoryId);
				    						 if(!childNews.getNewsLink().contains("veekshanam")){
				    							 childNewsList.add(childNews);
				    						 }
				    					 }
				    				 }
				    			 } 
				    			  try{
                                      detailsEventType = detailsParser.next();
                                  }catch (Exception e){

                                  }

				    		 }
				    		 if(childNewsList.size()>1){
				    			 mainNews.setChildNewsItems(childNewsList,true);
					    		 newsList.add(mainNews);
				    		 }
			    		 }
			    	 }
			    	 
			    	 eventType = xpp.next();
			     }
			   }
			    finally {
			     urlConnection.disconnect();
			   }
		}catch(Exception e){
			e.printStackTrace();
		}
		
		synchronized (this) {
			newsCollection.addAll(newsList);
		}
	}
	
	private synchronized String getNewsId(){
		long cTime = System.currentTimeMillis();
		while(idList.contains(cTime+"")){
			cTime++;
		}
		idList.add(cTime+"");
		return cTime+"";
	}
	
	private boolean downloadImage(String imgUrl, String imgId){
		HttpURLConnection conn = null;
		boolean status = false;
		try {
			URL url = new URL(imgUrl);
			int responseCode = -1;
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			responseCode = conn.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK){
				BufferedInputStream input = new BufferedInputStream(conn.getInputStream());
				Bitmap newsImg = BitmapFactory.decodeStream(input);
				FileOutputStream outStream = openFileOutput(imgId, Context.MODE_PRIVATE);
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				newsImg.compress(Bitmap.CompressFormat.PNG, 100, bytes);
				outStream.write(bytes.toByteArray());
				outStream.close();
				status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(conn!=null){
				conn.disconnect();
			}
		}
		return status;
	}
	
	private void clearOldImages(long referenceTimeStamp){
		FilenameFilter newsImgFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if(filename.startsWith("nimg") && filename.endsWith(".png")){
					return true;
				}
				return false;
			}
		};
		File[] newsImages = getFilesDir().listFiles(newsImgFilter);
		for(File newsImage:newsImages){
			if(newsImage.lastModified()<referenceTimeStamp)
				newsImage.delete();
		}
	}
	
	private class FeedLoader implements Runnable{
		
		CountDownLatch latch;
		String topic;
		String categoryId;
		
		public FeedLoader(CountDownLatch cLatch, String topic, String categoryId){
			this.latch = cLatch;
			this.topic = topic;
			this.categoryId = categoryId;
		}

		@Override
		public void run() {
			getTopicNews(this.topic, this.categoryId);
			latch.countDown();
		}
		
	}
}
