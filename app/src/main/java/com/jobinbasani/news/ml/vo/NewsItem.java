package com.jobinbasani.news.ml.vo;

import java.util.ArrayList;

import com.jobinbasani.news.ml.provider.NewsDataContract.NewsDataEntry;

import android.content.ContentValues;

public class NewsItem {

	private String newsId;
	private String batchId;
	private String newsHeader;
	private String newsDetails;
	private String newsImageUrl;
	private String imageId;
	private String newsSavedImageName;
	private String parentId;
	private String newsLink;
	private String newsProvider;
	private String newsCategory;
	private String categoryId;
	private ArrayList<NewsItem> childNewsItems;
	
	public String getNewsId() {
		return newsId;
	}
	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}
	public String getBatchId() {
		return batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public String getNewsHeader() {
		return newsHeader;
	}
	public void setNewsHeader(String newsHeader) {
		this.newsHeader = newsHeader;
	}
	public String getNewsDetails() {
		return newsDetails;
	}
	public void setNewsDetails(String newsDetails) {
		this.newsDetails = newsDetails;
	}
	public String getNewsImageUrl() {
		return newsImageUrl;
	}
	public void setNewsImageUrl(String newsImageUrl) {
		this.newsImageUrl = newsImageUrl;
	}
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getNewsSavedImageName() {
		return newsSavedImageName;
	}
	public void setNewsSavedImageName(String newsSavedImageName) {
		this.newsSavedImageName = newsSavedImageName;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public ArrayList<NewsItem> getChildNewsItems() {
		return childNewsItems;
	}
	public String getNewsLink() {
		return newsLink;
	}
	public void setNewsLink(String newsLink) {
		this.newsLink = newsLink;
	}
	public String getNewsProvider() {
		return newsProvider;
	}
	public void setNewsProvider(String newsProvider) {
		this.newsProvider = newsProvider;
	}
	public String getNewsCategory() {
		return newsCategory;
	}
	public void setNewsCategory(String newsCategory) {
		this.newsCategory = newsCategory;
	}
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public void setChildNewsItems(ArrayList<NewsItem> childNewsItems) {
		this.childNewsItems = childNewsItems;
	}
	public void setChildNewsItems(ArrayList<NewsItem> childNewsItems, boolean addMainNewsItem) {
		if(addMainNewsItem){
			NewsItem childItem = new NewsItem();
			childItem.setNewsHeader(this.newsHeader);
			childItem.setNewsLink(this.newsLink);
			childItem.setNewsProvider(this.newsProvider);
			childItem.setParentId(this.newsId);
			childItem.setBatchId(this.batchId);
			childItem.setCategoryId(this.categoryId);
			childNewsItems.add(0, childItem);
			setChildNewsItems(childNewsItems);
		}else{
			setChildNewsItems(childNewsItems);
		}
	}
	@Override
	public String toString() {
		return "newsId="+newsId+", parentId="+parentId+", newsCategory="+newsCategory+", newsHeader="+newsHeader+", newsDetails="+newsDetails+", newsLink="+newsLink+", newsProvider="+newsProvider;
	}
	
	public ContentValues[] getContentValueArray(){
		ArrayList<ContentValues> contentList = new ArrayList<ContentValues>();
		
		for(NewsItem mainNewsItem:getChildNewsItems()){
			contentList.add(mainNewsItem.getContentValues());
			for(NewsItem childNewsItem:mainNewsItem.getChildNewsItems()){
				contentList.add(childNewsItem.getContentValues());
			}
		}
		
		return contentList.toArray(new ContentValues[contentList.size()]);
	}
	
	public ContentValues getContentValues(){
		ContentValues values = new ContentValues();
		values.put(NewsDataEntry.COLUMN_NAME_NEWSCATEGORY, getNewsCategory());
		values.put(NewsDataEntry.COLUMN_NAME_CATEGORYID, getCategoryId());
		values.put(NewsDataEntry.COLUMN_NAME_NEWSDETAILS, getNewsDetails());
		values.put(NewsDataEntry.COLUMN_NAME_NEWSHEADER, getNewsHeader());
		values.put(NewsDataEntry.COLUMN_NAME_NEWSID, getNewsId());
		values.put(NewsDataEntry.COLUMN_NAME_BATCHID, getBatchId());
		values.put(NewsDataEntry.COLUMN_NAME_NEWSIMG, getImageId());
		values.put(NewsDataEntry.COLUMN_NAME_NEWSLINK, getNewsLink());
		values.put(NewsDataEntry.COLUMN_NAME_NEWSPROVIDER, getNewsProvider());
		values.put(NewsDataEntry.COLUMN_NAME_PARENTID, getParentId());
		return values;
	}

}
