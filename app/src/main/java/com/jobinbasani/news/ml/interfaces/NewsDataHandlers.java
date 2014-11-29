package com.jobinbasani.news.ml.interfaces;

import android.os.Bundle;

public interface NewsDataHandlers {
	void initLoaderWithId(int loaderId, Bundle args);
	void refreshFeed();
}
