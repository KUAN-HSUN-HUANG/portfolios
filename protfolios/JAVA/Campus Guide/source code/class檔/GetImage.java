package com.example.campusguidv5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorEventListener;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.util.Log;

public class GetImage extends AsyncTask<String, Integer, Bitmap>{
	Bitmap mBitmapFile;
	boolean complete = false;
	URL murl;
	ProgressDialog mProgressDialog;
	Context context;
	String str_mesg;
	
	public GetImage(Context context, String str_mesg){
		  this.context=context;
		  this.str_mesg=str_mesg;
		  complete = false;
	}
	
    @Override
    protected void onPreExecute(){
    	super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(str_mesg);
		mProgressDialog.setIndeterminate(true);
		
		mProgressDialog.show();
    }	
	
    @Override
	protected Bitmap doInBackground(String... params) {
    	
		try {
			murl = new URL(params[0]);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		//設定條件製造出mutable的Bitmap
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inMutable = true;
        
    	try {
			mBitmapFile = BitmapFactory.decodeStream(murl.openStream(), null, o);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return mBitmapFile;
	}

    @Override
    protected void onProgressUpdate(Integer... mProgressDialog) {
        super.onProgressUpdate(mProgressDialog);
    }    
    @Override
	protected void onPostExecute(Bitmap mBitmapFile){
    	if(mBitmapFile != null){
    		complete = true;
	    	if(mProgressDialog != null){
	    		if(mProgressDialog.isShowing()){
	    			mProgressDialog.setMessage("Complete");
	    			mProgressDialog.dismiss();
	    		}
	    	}
	    	
	    	//儲存圖片
	    	String SDLstr = "/storage/sdcard0";
		    InputStream input = null;
		    OutputStream output = null;
			try {
				output = new FileOutputStream (new File(SDLstr + "/Img00" + ".png"));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			mBitmapFile.compress(Bitmap.CompressFormat.PNG, 100, output);
		    try {
				output.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		    try {
				output.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		    
    	}
	}
    
    public boolean downloadState(){
    	return complete;
    }
    
    public Bitmap getresult(){
    	return mBitmapFile;
    }
    
    public void dismissDlg(){
    	mProgressDialog.dismiss();
    }

}
