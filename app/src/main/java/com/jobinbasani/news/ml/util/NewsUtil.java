package com.jobinbasani.news.ml.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import com.jobinbasani.news.ml.R;
import com.jobinbasani.news.ml.constants.NewsConstants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class NewsUtil {

	public static void showToast(Activity activity, String message){
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout,
		                               (ViewGroup) activity.findViewById(R.id.toastLayoutRoot));

		TextView text = (TextView) layout.findViewById(R.id.toastText);
		text.setText(message);

		Toast toast = new Toast(activity.getApplicationContext());
		toast.setGravity(Gravity.BOTTOM, 0, 60);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
	
	public static Intent getBrowserIntent(String url){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW);
		browserIntent.setData(Uri.parse(url));
		return browserIntent;
	}
	
	public static Intent getShareDataIntent(String data){
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, data);
		shareIntent.setType("text/plain");
		return shareIntent;
	}
	
	public static String takeScreenshot(View v){
		String filePath = NewsConstants.SCR_SHOT_DIR+"news"+System.currentTimeMillis()+".png";
		
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			File imgDir = new File(NewsConstants.SCR_SHOT_DIR);
			if(!imgDir.exists()){
				imgDir.mkdirs();
			}
			Bitmap bitmap;
			v.setDrawingCacheEnabled(true);
			bitmap = Bitmap.createBitmap(v.getDrawingCache());
			v.setDrawingCacheEnabled(false);
			
			OutputStream fout = null;
			File imgFile = new File(filePath);
			try{
				fout = new FileOutputStream(imgFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
				fout.flush();
				fout.close();
			}catch(Exception e){
				
			}
		}
		
		
		return filePath;
	}
	
	public static void debug(String msg){
		Log.d(NewsConstants.LOG_TAG, msg);
	}
	
	public static Intent getFeedbackIntent(Context context){
		Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+context.getResources().getString(R.string.feedbackEmail)+"?subject="+Uri.encode(context.getResources().getString(R.string.feedbackSubject))));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.feedbackSubject));

		List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(emailIntent, 0);
		//To prevent Receiver leak bug when only application is available for Intent
		if (activities.size() > 1) {
		    // Create and start the chooser
		    return Intent.createChooser(emailIntent, context.getResources().getString(R.string.feedbackIntentTitle));

		  } else {
		    return emailIntent;
		}
	}
	
	public static boolean showRateApp(Context context){
		String installedBy = context.getPackageManager().getInstallerPackageName(context.getPackageName());
		if(installedBy == null || !installedBy.equals(context.getResources().getString(R.string.playStore))){
			return false;
		}
		return true;
	}
	
	public static Intent getPlaystoreListing(String packageName){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id="+packageName));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		return intent;
	}

}
