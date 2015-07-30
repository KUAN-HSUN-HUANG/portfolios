package com.example.campusguidv5;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener, LocationListener, Runnable{
    //手機相關資訊
	private float mXdpi, mYdpi;
	private int mWidth, mHeight;
	//Camera's variable 相機的變數
    Camera MyCamera;
    private CameraPreview mPreview;
    //Drawing's variable 畫圖的變數
    private ViewGroup myVG;
    private boolean allowDraw = true;
    //顯示方向資訊
    private DrawInformations InfoCVS;
    private DrawCanvas drawAll;
	//compass sensor's variable 指南針的變數
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    //紀錄各度數的table
    int RecordTable[][][]; 
    //GPS's variable GPS的變數
    private LocationManager mgr;
    private boolean GPS_State = false;
    private double Lat_present = 121.52224302179211;
    private double Lon_present = 25.067132055373104;
	//指南針目前的方位
	private int Nowdirection;
	//The List for Exist Location in the DataBase
	private Construction mAllLocationList;
	//下載圖片的變數
	GetImage getckimgclass;
	int timing = 0;
	Thread th;
	boolean isDownloading = false;
	private boolean isContinue = true;
	private boolean isdownloadComplete = false;
	Bitmap ckBMP;
	//檢查網路
  	AlertDialog.Builder builderNetck;
  	Dialog alert;
	//等待計算的變數
	ProgressDialog mClcProgressDialog;
	
	
	boolean fortest = true;
	int testCount = 0;
/*	
	String MAP_Defult = "http://maps.google.com/maps/api/staticmap?&center=25.067153385598463,121.52131138377358&zoom=18&size=500x550&maptype=roadmap&sensor=false&style=feature:all|element:geometry|hue:0xffffff|saturation:-100|lightness:100&style=feature:landscape.man_made|element:geometry|hue:0x0000000|lightness:-100";
	String MAP_MarkersURL = "http://dl.dropbox.com/u/23077112/Rd.png";
	String MAP_Markers = "&markers=icon:" + MAP_MarkersURL + "|size:small|shadow:false";
	String MAP_PresentLocation = "|25.067132055373104,121.52224302179211";
	String MAP_PresentLocation2 = "|" + Lon_present + "," + Lat_present;
	String MAP_Present_URL = MAP_Defult + MAP_Markers + MAP_PresentLocation2;
*/
	private int SaveCount = 0;
	//SD卡路徑
	File SDLstr = Environment.getExternalStorageDirectory();
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
	    getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    getActionBar().hide();
		
		setContentView(R.layout.mylayout02);
		
		Log.d("", SDLstr.toString());
		
        //取得手機螢幕資訊
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        float xdpi = metrics.xdpi;
        float ydpi = metrics.ydpi;
        
        Log.v("長寬", mWidth + "  " + mHeight);
        Log.v("dpi", xdpi + "  " + ydpi);
        
        //由程式控制螢幕方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //從資料庫取得資料
        getDataFromDB();
        //Initialize Compass Sensor and Camera 初始化指南針和相機
        Initialize();
        //檢查GPS
        checkGPS();
        
        
        InfoCVS = new DrawInformations(this);
        InfoCVS.setDrawPos(mWidth, mHeight);
        drawAll = new DrawCanvas(this);
        
        myVG = (ViewGroup) findViewById(R.id.FrameLayout1);
        addAllView();
        
        
	}

    @Override
    protected void onPause() {
        super.onPause();
        //release the camera for other applications 釋放掉相機
        releaseCamera();
        //移除所有的view
        myVG.removeAllViews();
        //取消註冊指南針sensor
        mSensorManager.unregisterListener(this);
        //取消註冊GPS
		if(GPS_State){
			mgr.removeUpdates(this);
			GPS_State = false;
		}
		//if(ckBMP != null)	ckBMP.recycle();
		//ckBMP = null;
		if(alert.isShowing())	alert.dismiss();
		if(getckimgclass != null)	getckimgclass.dismissDlg();
    }    
    @Override
    protected void onResume() {
      super.onResume();
   
      
      if(MyCamera == null){
    	  //Initialize Compass Sensor and Camera 初始化指南針和相機
          //Get an instance of the Camera object
    	  Initialize();
    	  //加入相機的view
    	  addAllView();
      }
      //若GPS有抓到，註冊GPS，否則重新檢查
      if(GPS_State)	mgr.requestLocationUpdates("gps", 1000, 1, this);
      else	checkGPS();
      
      //註冊指南針sensor，速度為NORMAL
      mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

      if(!isdownloadComplete){
    	  if( isOnline() )	DownloadImage();
    	  else	alert.show();  
      }
    }
    
    
    private void addAllView(){
        myVG.addView(mPreview);
        //myVG.addView(InfoCVS);
        myVG.addView(drawAll);
        
    }

	//Initialize Compass Sensor and Camera 初始化指南針和相機
	private void Initialize(){
		//Initialize compass Sensor 初始化指南針
		mSensorManager = (SensorManager)getSystemService(this.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		//Get an instance of the Camera object
		MyCamera = getCameraInstance();
        // get Camera parameters
        Camera.Parameters params = MyCamera.getParameters();
        // set the Camera mode設定相機參數
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        List<Camera.Size> PicSizsList = params.getSupportedPictureSizes();
        List<Camera.Size> PrvSizsList = params.getSupportedPreviewSizes();
        params.setPictureSize(PicSizsList.get(0).width, PicSizsList.get(0).height);
        params.setPreviewSize(PrvSizsList.get(0).width, PrvSizsList.get(0).height);
        //MyCamera.setParameters(params);
		mPreview = new CameraPreview(this, MyCamera);
		

		builderNetck  = new AlertDialog.Builder(this);
		builderNetck.setTitle("Notification");
		//builder.setIcon(R.drawable.alert_dialog_icon);
		builderNetck.setMessage("Can't access the net , please check the network !!");
		alert = builderNetck.create();
	}

	
    //從資料庫取得資料
    private void getDataFromDB(){
        //Open the DataBase from exist file
        myDB myDbHelper = new myDB(this);
        try {
        	myDbHelper.createDataBase();
        } catch (IOException ioe) {
 		throw new Error("Unable to create database");
        }
 
        try {
        	myDbHelper.openDataBase();
        }catch(SQLException sqle){
        	throw sqle;
        }        
        //Obtain SQlite Class's return value: The Cursor's Object 
        //取得SQLite類別的回傳值:Cursor物件
        //取得所有建築物
        Cursor cursor = myDbHelper.getAll();


        //Obtain the number's of rows
        //取得資料表的列數   
		int rows_num = cursor.getCount();
		mAllLocationList = new Construction(rows_num);
		//Obtain all the information from the database
		if(rows_num != 0) {
			//將指標移至第一筆資料
			cursor.moveToFirst();			
			//讀取出6個欄位的資料，共有rows_num筆
			for(int i=0; i<rows_num; i++) {
				mAllLocationList.add(cursor.getString(1), 
									new String[] {cursor.getString(2), cursor.getString(3)}, 
									cursor.getString(4));
				String name = cursor.getString(0) + " , " + cursor.getString(1) + " , " 
								+ cursor.getString(2) + " , " + cursor.getString(3) + " , "
								+ cursor.getString(4) + " , " + cursor.getString(5);
				//存入該建築物中所有的mark
		        Cursor cursor_mark = myDbHelper.getMarkBy_id(Integer.valueOf(cursor.getString(0)));
		        Log.d("id: ", cursor.getString(0));
		        int mark_rows_num = cursor_mark.getCount();
		        Log.d("id: ", cursor.getString(0) + "  ,數量: " + cursor_mark.getCount());
				if(mark_rows_num != 0) {
					//將指標移至第一筆資料
					cursor_mark.moveToFirst();			
					//讀取出6個欄位的資料，共有rows_num筆
					for(int j=0; j<mark_rows_num; j++) {
						String tag = "";
						//Log.d(tag , cursor_mark.getString(0) + "  ,  " + cursor_mark.getString(1));
						//Log.d("", "pos: " + Integer.valueOf(cursor.getString(0)));
						mAllLocationList.addMark(Integer.valueOf(cursor_mark.getString(0)), Integer.valueOf(cursor_mark.getString(1)), Integer.valueOf(cursor.getString(0))-1);
						//Log.d("", "2");
						//將指標移至下一筆資料
						cursor_mark.moveToNext();
					} 
				}
				//關閉mark的cursor
				cursor_mark.close();
				//將指標移至下一筆資料
				cursor.moveToNext();
			}
		}	
		//關閉Cursor
		cursor.close();
		//關閉database
		myDbHelper.close();	
	
		
		//資料檢查
		for(int i = 0; i<mAllLocationList.getSize(); i++){			
			String tag = null;
			Log.d(tag,
					mAllLocationList.getelement(i)[0] + " , " +
					mAllLocationList.getelement(i)[1] + " , " +
					mAllLocationList.getelement(i)[2] + " , " +
					mAllLocationList.getelement(i)[3]);
			for(int j=0; j<mAllLocationList.getMarkSize(i); j++){
				Log.d(tag , mAllLocationList.getMark_X(i ,j) + "  ,  " + mAllLocationList.getMark_Y(i, j));
			}
		}
		
		//指南針共360個方位
		RecordTable = new int[360][][];
		//每個方位共對應到N棟建築物
		for(int i=0; i<RecordTable.length; i++){
			RecordTable[i] = new int[mAllLocationList.getSize()][];
		}
		//每棟建築物共有2個座標
		for(int i=0; i<RecordTable.length; i++){
			for(int j=0; j<RecordTable[i].length; j++){
				RecordTable[i][j] = new int[2];
			}
		}	
    }
    
    
  	private void checkGPS(){
  		Log.v(null, "checkService !!");
          //取得系統提供的定位相關服務
          LocationManager status = (LocationManager)(this.getSystemService(Context.LOCATION_SERVICE));
          //判斷GPS/網路設定是否開啟
          if(status.isProviderEnabled(LocationManager.GPS_PROVIDER)){
          	GPS_State = true;
      		//取得系統提供的定位服務
      	    mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
      	    //使用GPS定位
      	    Location location = mgr.getLastKnownLocation("gps");
          }
          //轉至開啟GPS頁面
          else	startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); 
  	}
  	
    //取得相機物件的instance(更安全的作法)
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
      
    private void releaseCamera(){
        if (MyCamera != null){
        	MyCamera.stopPreview();
        	MyCamera.release();        
        	MyCamera = null;
        }
    }      

    
    private boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
    
    
	private void DownloadImage(){
		timing = 0;
		isContinue = true;
		th = new Thread(this);
	
    	String mURL = "http://maps.google.com/maps/api/staticmap?&center=25.067153385598463,121.52131138377358&zoom=18&size=500x550&maptype=roadmap&sensor=false&style=feature:all|element:geometry|hue:0xffffff|saturation:-100|lightness:100&style=feature:landscape.man_made|element:geometry|hue:0x0000000|lightness:-100&markers=icon:http://dl.dropbox.com/u/23077112/Rd_1_pixel.png|size:small|shadow:false";
		String mURL2 = "http://maps.google.com/maps/api/staticmap?&center=25.067153385598463,121.52131138377358&zoom=18&size=500x550&maptype=roadmap&sensor=false&style=feature:all|element:labels|visibility:off&style=feature:all|element:geometry|hue:0xffffff|saturation:-100|lightness:100&style=feature:landscape.man_made|element:geometry|hue:0x0000000|lightness:-100";
    	getckimgclass = new GetImage(this, "處理中...");
		getckimgclass.execute(mURL2);
		th.start();
	}    

	private Construction CalcBearing(Construction mAllLocationList, double Now_lat, double Now_lon) {
		double drg;
		Coordinate_LatLon Src = new Coordinate_LatLon(Now_lon, Now_lat);
		//資料檢查
		for(int i = 0; i<mAllLocationList.getSize(); i++){			
			String tag = null;
			Coordinate_LatLon Target = new Coordinate_LatLon(
					Double.valueOf(mAllLocationList.getelement(i)[2]), 
					Double.valueOf(mAllLocationList.getelement(i)[1]) );
			
			drg = getBearing.getBearing(Src, Target) + 90;
			if(drg >= 360)	drg = drg - 360;
			Log.v(" ", mAllLocationList.getelement(i)[0] + " 角度:  " + drg + "  度");
			mAllLocationList.setRelativeBearing(i, (float)drg);
		}
		return mAllLocationList;
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main, menu);
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.add(0, 0, 0, "A");
        menu.add(0, 1, 1, "B");
        menu.add(0, 2, 2, "C");
        menu.add(0, 3, 3, "D");
        menu.add(0, 4, 4, "E");
        menu.add(0, 5, 5, "F");
        menu.add(0, 6, 6, "G");
        menu.add(0, 7, 7, "H");
        menu.add(0, 8, 8, "I");
        return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		if(location != null){
			Lon_present = location.getLatitude();	//取得緯度
			Lat_present = location.getLongitude();	//取得經度
			Log.v("loctag", Lat_present + ", " + Lon_present);
			InfoCVS.redraw(String.valueOf(Nowdirection), new java.text.DecimalFormat("#.00000000").format(Lon_present), new java.text.DecimalFormat("#.00000000").format(Lat_present));
			//InfoCVS.redraw(String.valueOf(Nowdirection), Lon_present, Lat_present);

			reLocate();
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.v("",  "" + event.values[0]);
		float mX = 360 -(event.values[0] + 90);	
		Nowdirection = (int)event.values[0] + 90;
		if(Nowdirection < 0) Nowdirection = Nowdirection + 360;
		if(Nowdirection >= 360) Nowdirection = Nowdirection - 360;
		InfoCVS.redraw(String.valueOf(Nowdirection), new java.text.DecimalFormat("#.00000000").format(Lon_present), new java.text.DecimalFormat("#.00000000").format(Lat_present));
		
		if(allowDraw)	drawAll.redraw(mAllLocationList, RecordTable, Nowdirection);
	}
	
	//GPS位置改變，重新下載地圖
	private void reLocate(){
		//先將所有建築物標記為可看見
		mAllLocationList.setAllVisible();
	      if(!isdownloadComplete){
	    	  DownloadImage();
	      }

	      if(isdownloadComplete){
	    	allowDraw = false;
	    	//mClcProgressDialog.show();
	    	Log.v("str", "進入");
	    	String Lat_decimal = new java.text.DecimalFormat("#.00000").format(Lat_present).substring(String.valueOf(Lat_present).indexOf(".") + 2);
			String Lon_decimal = new java.text.DecimalFormat("#.00000").format(Lon_present).substring(String.valueOf(Lon_present).indexOf(".") + 2);
			int Hpix = (int) Math.round((((Integer.valueOf(Lat_decimal) - 1998 ) / 266.0) * 499 ));
			int Wpix = (int) Math.round((((6850 - Integer.valueOf(Lon_decimal)) / 269.0) * 549));	
			Log.v("WuuuuuH", "in "  + Hpix + " , " + Wpix+ " , " + Lat_present + " , " + Lon_present);
			if(Hpix >= 0 && Hpix <= 499 && Wpix >= 0 && Wpix <= 549){
				mAllLocationList = CalcBearing(mAllLocationList, Lat_present, Lon_present);
				mAllLocationList = CheckImage.check(ckBMP, mAllLocationList, SDLstr, + SaveCount++ +".png", 1 , new int[] {Hpix, Wpix}, true);
				RecordTable = BearingSuitibitlyCheck.generateBearingTable(mAllLocationList, 50, RecordTable, mWidth, mHeight);
				allowDraw = true;
			}
			else{
				Toast.makeText(this, "在範圍之外的位置...", Toast.LENGTH_LONG).show();
			}
		}
	      //mClcProgressDialog.dismiss();
	}	
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    		case 0:
    			Log.d("", "按下了A");
    			Lat_present = 121.52224302179211;
    			Lon_present = 25.067132055373104;
    			reLocate();
    			break;
    		case 1:
    			Log.d("", "按下了B");
    			Lat_present = 121.52213223096336;
    			Lon_present = 25.066717154304087;
    			reLocate();
    			break;
    		case 2:
    			Log.d("", "按下了C");
    			Lat_present = 121.52167636362032;
    			Lon_present = 25.06673681287423;
    			//Lat_present = 121.52165771;
    			//Lon_present = 25.06648138;
    			reLocate();
    			break;
    		case 3:
    			Log.d("", "按下了D");
    			Lat_present = 121.52132960261407;
    			Lon_present = 25.066774143102016;
    			reLocate();
    			break;
    		case 4:
    			Log.d("", "按下了E");
    			Lat_present = 121.5210248946178;
    			Lon_present = 25.067380092748834;
    			reLocate();
    			break;
    		case 5:
    			Log.d("", "按下了F");
    			Lat_present = 121.52127471657093;
    			Lon_present = 25.067857702982767;
    			reLocate();
    			break;
    		case 6:
    			Log.d("", "按下了G");
    			Lat_present = 121.5218289422863;
    			Lon_present = 25.067657138746583;
    			reLocate();
    			break;
    		case 7:
    			Log.d("", "按下了H");
    			Lat_present = 121.52237402005792;
    			Lon_present = 25.06763716581416;
    			reLocate();
    			break;
    		case 8:
    			Log.d("", "按下了I");
    			Lat_present = 121.522171;
    			Lon_present = 25.06775;
    			reLocate();
    			break;    	    			
    	}
    	return true;
    }

    
	@Override
	public void run() {
		while(isContinue){
			if(getckimgclass.downloadState()){
				isdownloadComplete = true;
				ckBMP = getckimgclass.getresult();
				isContinue = false;
				break;
			}
			try {
				Log.v( "Thread Checking..." ,"Checking...");
				th.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timing++;
		}
	}

}

