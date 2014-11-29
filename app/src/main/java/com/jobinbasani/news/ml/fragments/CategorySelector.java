package com.jobinbasani.news.ml.fragments;

import com.jobinbasani.news.ml.R;
import com.jobinbasani.news.ml.constants.NewsConstants;
import com.jobinbasani.news.ml.interfaces.NewsDataHandlers;
import com.jobinbasani.news.ml.provider.NewsDataContract.NewsDataEntry;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Spinner;

public class CategorySelector extends Fragment implements OnItemSelectedListener {
	
	Spinner categorySpinner;
	NewsDataHandlers newsDataHandler;
	SimpleCursorAdapter mAdapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.category_selector, null);
		categorySpinner = (Spinner) rootView.findViewById(R.id.categorySpinner);
		mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null, new String[]{NewsDataEntry.COLUMN_NAME_NEWSCATEGORY}, new int[]{android.R.id.text1}, SimpleCursorAdapter.NO_SELECTION);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAdapter.setViewBinder(new SpinnerViewBinder());
		categorySpinner.setAdapter(mAdapter);
		categorySpinner.setOnItemSelectedListener(this);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(newsDataHandler==null)
			newsDataHandler = (NewsDataHandlers) getActivity();
		newsDataHandler.initLoaderWithId(NewsConstants.CATEGORY_LOADER_ID, null);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i,
			long l) {
		if(view.getTag()!=null && (Integer)view.getTag()>0){
			Bundle args = new Bundle();
			args.putInt(NewsConstants.CATEGORY_KEY, (Integer) view.getTag());
			newsDataHandler.initLoaderWithId(NewsConstants.NEWSGROUP_LOADER_ID, args);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
		
	}
	
	public void changeSpinnerCursor(Cursor c){
		mAdapter.swapCursor(c);
	}
	
	private class SpinnerViewBinder implements ViewBinder{

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(cursor!=null && view.getId() == android.R.id.text1){
				TextView textView = (TextView) view;
				textView.setText(cursor.getString(columnIndex));
				textView.setTag(cursor.getInt(cursor.getColumnIndex(NewsDataEntry._ID)));
				return true;
			}
			return false;
		}
		
	}

}
