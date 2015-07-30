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
    //���������T
	private float mXdpi, mYdpi;
	private int mWidth, mHeight;
	//Camera's variable �۾����ܼ�
    Camera MyCamera;
    private CameraPreview mPreview;
    //Drawing's variable �e�Ϫ��ܼ�
    private ViewGroup myVG;
    private boolean allowDraw = true;
    //��ܤ�V��T
    private DrawInformations InfoCVS;
    private DrawCanvas drawAll;
	//compass sensor's variable ���n�w���ܼ�
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    //�����U�׼ƪ�table
    int RecordTable[][][]; 
    //GPS's variable GPS���ܼ�
    private LocationManager mgr;
    private boolean GPS_State = false;
    private double Lat_present = 121.52224302179211;
    private double Lon_present = 25.067132055373104;
	//���n�w�ثe�����
	private int Nowdirection;
	//The List for Exist Location in the DataBase
	private Construction mAllLocationList;
	//�U���Ϥ����ܼ�
	GetImage getckimgclass;
	int timing = 0;
	Thread th;
	boolean isDownloading = false;
	private boolean isContinue = true;
	private boolean isdownloadComplete = false;
	Bitmap ckBMP;
	//�ˬd����
  	AlertDialog.Builder builderNetck;
  	Dialog alert;
	//���ݭp�⪺�ܼ�
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
	//SD�d���|
	File SDLstr = Environment.getExternalStorageDirectory();
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
	    getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    getActionBar().hide();
		
		setContentView(R.layout.mylayout02);
		
		Log.d("", SDLstr.toString());
		
        //���o����ù���T
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        float xdpi = metrics.xdpi;
        float ydpi = metrics.ydpi;
        
        Log.v("���e", mWidth + "  " + mHeight);
        Log.v("dpi", xdpi + "  " + ydpi);
        
        //�ѵ{������ù���V
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //�q��Ʈw���o���
        getDataFromDB();
        //Initialize Compass Sensor and Camera ��l�ƫ��n�w�M�۾�
        Initialize();
        //�ˬdGPS
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
        //release the camera for other applications ���񱼬۾�
        releaseCamera();
        //�����Ҧ���view
        myVG.removeAllViews();
        //�������U���n�wsensor
        mSensorManager.unregisterListener(this);
        //�������UGPS
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
    	  //Initialize Compass Sensor and Camera ��l�ƫ��n�w�M�۾�
          //Get an instance of the Camera object
    	  Initialize();
    	  //�[�J�۾���view
    	  addAllView();
      }
      //�YGPS�����A���UGPS�A�_�h���s�ˬd
      if(GPS_State)	mgr.requestLocationUpdates("gps", 1000, 1, this);
      else	checkGPS();
      
      //���U���n�wsensor�A�t�׬�NORMAL
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

	//Initialize Compass Sensor and Camera ��l�ƫ��n�w�M�۾�
	private void Initialize(){
		//Initialize compass Sensor ��l�ƫ��n�w
		mSensorManager = (SensorManager)getSystemService(this.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		//Get an instance of the Camera object
		MyCamera = getCameraInstance();
        // get Camera parameters
        Camera.Parameters params = MyCamera.getParameters();
        // set the Camera mode�]�w�۾��Ѽ�
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

	
    //�q��Ʈw���o���
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
        //���oSQLite���O���^�ǭ�:Cursor����
        //���o�Ҧ��ؿv��
        Cursor cursor = myDbHelper.getAll();


        //Obtain the number's of rows
        //���o��ƪ��C��   
		int rows_num = cursor.getCount();
		mAllLocationList = new Construction(rows_num);
		//Obtain all the information from the database
		if(rows_num != 0) {
			//�N���в��ܲĤ@�����
			cursor.moveToFirst();			
			//Ū���X6����쪺��ơA�@��rows_num��
			for(int i=0; i<rows_num; i++) {
				mAllLocationList.add(cursor.getString(1), 
									new String[] {cursor.getString(2), cursor.getString(3)}, 
									cursor.getString(4));
				String name = cursor.getString(0) + " , " + cursor.getString(1) + " , " 
								+ cursor.getString(2) + " , " + cursor.getString(3) + " , "
								+ cursor.getString(4) + " , " + cursor.getString(5);
				//�s�J�ӫؿv�����Ҧ���mark
		        Cursor cursor_mark = myDbHelper.getMarkBy_id(Integer.valueOf(cursor.getString(0)));
		        Log.d("id: ", cursor.getString(0));
		        int mark_rows_num = cursor_mark.getCount();
		        Log.d("id: ", cursor.getString(0) + "  ,�ƶq: " + cursor_mark.getCount());
				if(mark_rows_num != 0) {
					//�N���в��ܲĤ@�����
					cursor_mark.moveToFirst();			
					//Ū���X6����쪺��ơA�@��rows_num��
					for(int j=0; j<mark_rows_num; j++) {
						String tag = "";
						//Log.d(tag , cursor_mark.getString(0) + "  ,  " + cursor_mark.getString(1));
						//Log.d("", "pos: " + Integer.valueOf(cursor.getString(0)));
						mAllLocationList.addMark(Integer.valueOf(cursor_mark.getString(0)), Integer.valueOf(cursor_mark.getString(1)), Integer.valueOf(cursor.getString(0))-1);
						//Log.d("", "2");
						//�N���в��ܤU�@�����
						cursor_mark.moveToNext();
					} 
				}
				//����mark��cursor
				cursor_mark.close();
				//�N���в��ܤU�@�����
				cursor.moveToNext();
			}
		}	
		//����Cursor
		cursor.close();
		//����database
		myDbHelper.close();	
	
		
		//����ˬd
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
		
		//���n�w�@360�Ӥ��
		RecordTable = new int[360][][];
		//�C�Ӥ��@������N�ɫؿv��
		for(int i=0; i<RecordTable.length; i++){
			RecordTable[i] = new int[mAllLocationList.getSize()][];
		}
		//�C�ɫؿv���@��2�Ӯy��
		for(int i=0; i<RecordTable.length; i++){
			for(int j=0; j<RecordTable[i].length; j++){
				RecordTable[i][j] = new int[2];
			}
		}	
    }
    
    
  	private void checkGPS(){
  		Log.v(null, "checkService !!");
          //���o�t�δ��Ѫ��w������A��
          LocationManager status = (LocationManager)(this.getSystemService(Context.LOCATION_SERVICE));
          //�P�_GPS/�����]�w�O�_�}��
          if(status.isProviderEnabled(LocationManager.GPS_PROVIDER)){
          	GPS_State = true;
      		//���o�t�δ��Ѫ��w��A��
      	    mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
      	    //�ϥ�GPS�w��
      	    Location location = mgr.getLastKnownLocation("gps");
          }
          //��ܶ}��GPS����
          else	startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); 
  	}
  	
    //���o�۾�����instance(��w�����@�k)
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
    	getckimgclass = new GetImage(this, "�B�z��...");
		getckimgclass.execute(mURL2);
		th.start();
	}    

	private Construction CalcBearing(Construction mAllLocationList, double Now_lat, double Now_lon) {
		double drg;
		Coordinate_LatLon Src = new Coordinate_LatLon(Now_lon, Now_lat);
		//����ˬd
		for(int i = 0; i<mAllLocationList.getSize(); i++){			
			String tag = null;
			Coordinate_LatLon Target = new Coordinate_LatLon(
					Double.valueOf(mAllLocationList.getelement(i)[2]), 
					Double.valueOf(mAllLocationList.getelement(i)[1]) );
			
			drg = getBearing.getBearing(Src, Target) + 90;
			if(drg >= 360)	drg = drg - 360;
			Log.v(" ", mAllLocationList.getelement(i)[0] + " ����:  " + drg + "  ��");
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
			Lon_present = location.getLatitude();	//���o�n��
			Lat_present = location.getLongitude();	//���o�g��
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
	
	//GPS��m���ܡA���s�U���a��
	private void reLocate(){
		//���N�Ҧ��ؿv���аO���i�ݨ�
		mAllLocationList.setAllVisible();
	      if(!isdownloadComplete){
	    	  DownloadImage();
	      }

	      if(isdownloadComplete){
	    	allowDraw = false;
	    	//mClcProgressDialog.show();
	    	Log.v("str", "�i�J");
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
				Toast.makeText(this, "�b�d�򤧥~����m...", Toast.LENGTH_LONG).show();
			}
		}
	      //mClcProgressDialog.dismiss();
	}	
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    		case 0:
    			Log.d("", "���U�FA");
    			Lat_present = 121.52224302179211;
    			Lon_present = 25.067132055373104;
    			reLocate();
    			break;
    		case 1:
    			Log.d("", "���U�FB");
    			Lat_present = 121.52213223096336;
    			Lon_present = 25.066717154304087;
    			reLocate();
    			break;
    		case 2:
    			Log.d("", "���U�FC");
    			Lat_present = 121.52167636362032;
    			Lon_present = 25.06673681287423;
    			//Lat_present = 121.52165771;
    			//Lon_present = 25.06648138;
    			reLocate();
    			break;
    		case 3:
    			Log.d("", "���U�FD");
    			Lat_present = 121.52132960261407;
    			Lon_present = 25.066774143102016;
    			reLocate();
    			break;
    		case 4:
    			Log.d("", "���U�FE");
    			Lat_present = 121.5210248946178;
    			Lon_present = 25.067380092748834;
    			reLocate();
    			break;
    		case 5:
    			Log.d("", "���U�FF");
    			Lat_present = 121.52127471657093;
    			Lon_present = 25.067857702982767;
    			reLocate();
    			break;
    		case 6:
    			Log.d("", "���U�FG");
    			Lat_present = 121.5218289422863;
    			Lon_present = 25.067657138746583;
    			reLocate();
    			break;
    		case 7:
    			Log.d("", "���U�FH");
    			Lat_present = 121.52237402005792;
    			Lon_present = 25.06763716581416;
    			reLocate();
    			break;
    		case 8:
    			Log.d("", "���U�FI");
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

