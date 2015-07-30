package com.example.campusguidv5;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class CheckImage{
	static Construction check(Bitmap srcImage, Construction mAllLocationList,
						File SDLstr, String StoreName, int State, int[] pixelAry,
						boolean saveImage){
		Bitmap tmpImage = srcImage.copy(srcImage.getConfig(), true);
		
		//是否將圖片存至本機
		boolean SaveImage = saveImage;
        int picw = tmpImage.getWidth();
        int pich = tmpImage.getHeight();
        //Log.d("pic", "picw, pich " + picw + " , " + pich);
        //畫線用建築物的class
        BuildingList mBuildingList[];
        
        //前處理，將地圖黑白極值化，並消除雜訊
        int[] ckImgpix = new int[picw * pich];
        //測試用
        int[] tmpImgpix = new int[picw * pich];
        int[] TESTtmpImgpix = new int[picw * pich];
        int[] pureMappixel = new int[picw * pich];
        tmpImage.getPixels(ckImgpix, 0, picw, 0, 0, picw, pich);
        tmpImage.getPixels(TESTtmpImgpix, 0, picw, 0, 0, picw, pich);
        tmpImage.getPixels(TESTtmpImgpix, 0, picw, 0, 0, picw, pich);
        
        
        int Now_X = 0, Now_Y = 0;
        int R,G,B;
        int test_I = 0;
        
        //以下兩迴圈將google map上的文字給消掉
        for(int i=372, index; i<=415; i++){
        	for(int j=314; j<=350; j++){
        		index = j * picw + i;
        		ckImgpix[index] = Color.argb(255, 255, 255, 255);
        	}
        }
        for(int i=345, index; i<=371; i++){
        	for(int j=326; j<=349; j++){
        		index = j * picw + i;
        		ckImgpix[index] = Color.argb(255, 255, 255, 255);
        	}
        }        
        
        //地圖二值化
        for(int i=0, index; i<pich; i++){
        	for(int j=0; j<picw; j++){
        		index = i * picw + j;
        		//bitwise shifting
                R = (ckImgpix[index] >> 16) & 0xff;     
                G = (ckImgpix[index] >> 8) & 0xff;
                B = ckImgpix[index] & 0xff;

                //圖片中紅色點即為相機所在的點
                if(R == 255 && G == 0 && B == 0){
                	//Now_X = j;
                	//Now_Y = i;
                }
                else if(R == 0 && G == 255 && B == 0){
                	//Log.d("", j + "," + i + "  :  " + "綠色");
                }
                else if(R == 0 && G == 0 && B == 255){
                	//Log.d("", j + "," + i + "  :  " + "藍色");
                }
                //灰色、黑色處理成黑色(建築)
                else if(R == G && R == B && R < 250){
                	ckImgpix[index] = Color.argb(255, 0, 0, 0);
                	tmpImgpix[index] = Color.argb(255, 0, 0, 0);
                	TESTtmpImgpix[index] = Color.argb(255, 0, 0, 0);
                	pureMappixel[index] = Color.argb(255, 0, 0, 0);
                }
             
                //其他均處理成白色
                else{
                	ckImgpix[index] = Color.argb(255, 255, 255, 255);
                	tmpImgpix[index] = Color.argb(255, 255, 255, 255);
                	TESTtmpImgpix[index] = Color.argb(255, 255, 255, 255);
                	pureMappixel[index] = Color.argb(255, 255, 255, 255);
                }                

        	}
        }
        pureMappixel = ckImgpix.clone();
        
        /*
         * 若State=1，則使用自定義座標轉換pixel
         */
        if(State == 1){
        	Now_X = pixelAry[0];
        	Now_Y = pixelAry[1];
        }
        
        
        //每個建築物都有各自的一條線
        mBuildingList = new BuildingList[mAllLocationList.getSize()];
    
        //由下載下來的satic google map 找出目前所在位置的點至資料庫內所有建築物的mark點之間的所有連線
		for(int i = 0; i<mAllLocationList.getSize(); i++){			
			Log.d(null,
					mAllLocationList.getelement(i)[0] + " , " +
					mAllLocationList.getelement(i)[1] + " , " +
					mAllLocationList.getelement(i)[2] + " , " +
					mAllLocationList.getelement(i)[3]);
			mBuildingList[i] = new BuildingList(mAllLocationList.getMarkSize(i));
			for(int j=0; j<mAllLocationList.getMarkSize(i); j++){
				Log.d("" , mAllLocationList.getMark_X(i ,j) + "  ,  " + mAllLocationList.getMark_Y(i, j));
				mBuildingList[i].setMarkList(j , GenerateLinePoint.getAllPoint( mBuildingList[i].getMarkList(j), 
																mAllLocationList.getMark_X(i ,j), mAllLocationList.getMark_Y(i, j), 
																Now_Y, Now_X) );
				}
		}
        
		
		//逐一檢查至建築物的每條線，若線至於障礙物上則著上黑色，否則著上綠色
        for(int i=0; i<mAllLocationList.getSize(); i++){
        	for(int j=0; j<mBuildingList[i].getMarkNumbers(); j++){
        		LinePointsList tmpList = mBuildingList[i].getMarkList(j);
        		for(int k=0; k<tmpList.getLength(); k++){
        			//Log.v("ary", "j , X , y , picw :  " + j + " " + tmpList.getX(k) + " "  +  tmpList.getY(k) + " " + picw);
		          	int index = tmpList.getX(k) * picw + tmpList.getY(k);
		              R = (tmpImgpix[index] >> 16) & 0xff;     
		              G = (tmpImgpix[index] >> 8) & 0xff;
		              B = tmpImgpix[index] & 0xff;
		          	
		              if(R == 0 && G == 0 && B == 0){
		              	ckImgpix[index] = Color.argb(255, 255, 0, 0);
		              }
		              else{
		              	ckImgpix[index] = Color.argb(255, 0, 225, 0);
		              }
        		}
        	}        
        }

        
       
		
		
		int Redcount = 0;
		boolean visible_count = false;
		//幾棟建築物
        for(int i=0; i<mAllLocationList.getSize(); i++){
        	Log.d("", "第" + i + "棟" );
        	visible_count = false;
        	for(int j=0; j<mBuildingList[i].getMarkNumbers(); j++){
        		Log.d("", "第" + j + "條線" );
        		Redcount = 0;
        		LinePointsList tmpList = mBuildingList[i].getMarkList(j);
        		for(int k=0; k<tmpList.getLength(); k++){
		          	int index = tmpList.getX(k) * picw + tmpList.getY(k);
		              R = (tmpImgpix[index] >> 16) & 0xff;     
		              G = (tmpImgpix[index] >> 8) & 0xff;
		              B = tmpImgpix[index] & 0xff;
		          	
		              if(R == 0 && G == 0 && B == 0){
		              	//ckImgpix[index] = Color.argb(255, 255, 0, 0);
		              	Redcount++;
		              }
		              else{
		              	//ckImgpix[index] = Color.argb(255, 0, 225, 0);
		              }
        		}
        		Log.d("", "count: " + Redcount + "個");
        		if(Redcount < 10){
        			visible_count = true;
        			break;
        		}
        	}
        	Log.d("", mAllLocationList.getelement(i)[0]);
        	if(visible_count == false){
        		Log.d("", "看不到");
        		mAllLocationList.setConstruction_Visible(i, false);
        	}
        	else	Log.d("", "看的到");
        }        

        
        
        
 //存成圖片
 if(SaveImage){
////////////////////////////////////////測試用，每個建築物均存成一張圖///////////////////////////////////// //              
        for(int i=0; i<mAllLocationList.getSize(); i++){
        	int[] tmpAry = new int[picw * pich];
	        //復原
        	tmpAry = pureMappixel.clone();
	        tmpImage.setPixels(pureMappixel, 0, picw, 0, 0, picw, pich);
	        //ckImage.getPixels(tmpAry, 0, picw, 0, 0, picw, pich);
        	for(int j=0; j<mBuildingList[i].getMarkNumbers(); j++){
        		LinePointsList tmpList = mBuildingList[i].getMarkList(j);
        		for(int k=0; k<tmpList.getLength(); k++){
		          	int index = tmpList.getX(k) * picw + tmpList.getY(k);
		              R = (pureMappixel[index] >> 16) & 0xff;     
		              G = (pureMappixel[index] >> 8) & 0xff;
		              B = pureMappixel[index] & 0xff;
		          	
		              if(R == 0 && G == 0 && B == 0){
		            	  tmpAry[index] = Color.argb(255, 255, 0, 0);
		              }
		              else{
		            	  tmpAry[index] = Color.argb(255, 0, 225, 0);
		              }
        		}
        	}
	        tmpImage.setPixels(tmpAry, 0, picw, 0, 0, picw, pich);
	    	InputStream input = null;
	    	OutputStream output = null;
			try {
				output = new FileOutputStream (new File(SDLstr.toString() + "/"+  "Each_"  +  test_I++ + StoreName));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			tmpImage.compress(Bitmap.CompressFormat.PNG, 100, output);
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

	    tmpImage.setPixels(ckImgpix, 0, picw, 0, 0, picw, pich);
	    InputStream input = null;
	    OutputStream output = null;
		try {
			output = new FileOutputStream (new File(SDLstr.toString() + "/"+ "All_" + StoreName));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		tmpImage.compress(Bitmap.CompressFormat.PNG, 100, output);
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
	    
	    
	    
/////////測試用，各建築物的mark存成一張圖//////////////////////////////////////////////////
    	int[] tmpAry = new int[picw * pich];
        //復原
    	tmpAry = pureMappixel.clone();
        tmpImage.setPixels(pureMappixel, 0, picw, 0, 0, picw, pich);
        //ckImage.getPixels(tmpAry, 0, picw, 0, 0, picw, pich);
        
		for(int i=0; i<mAllLocationList.getSize(); i++){
			for(int j=0; j<mAllLocationList.getMarkSize(i); j++){
				//Log.d("" , mAllLocationList.getMark_X(i ,j) + "  ,  " + mAllLocationList.getMark_Y(i, j));
	          	int index = mAllLocationList.getMark_X(i ,j) * picw + mAllLocationList.getMark_Y(i ,j);
	              tmpAry[index] = Color.argb(255, 255, 0, 0);
			}
			
		} 	

        tmpImage.setPixels(tmpAry, 0, picw, 0, 0, picw, pich);
    	InputStream tinput = null;
    	OutputStream toutput = null;
		try {
			output = new FileOutputStream (new File(SDLstr.toString() + "/"+  "mark圖"  +  test_I++ + StoreName));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		tmpImage.compress(Bitmap.CompressFormat.PNG, 100, output);
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
        return mAllLocationList;
        
	}
	
	
	

	

	
}
//////////////////////////////////////////////////////////////////////////////////////



//產生一直線上的所有點的class
class GenerateLinePoint{
	static LinePointsList getAllPoint(LinePointsList mlist, 
										int sX, int sY, int eX, int eY){
		int dx, dy;
		int NewD;
		int a, b;
		int cX, cY;
		int m, rateX, rateY;
		
		//畫出當線為垂直時
		if(sX == eX){
			if(eY > sY){
				//Log.d("", " 一  ");
				for(dx = sX, dy = sY; dy <= eY; dy++){
					//Log.d("", "加點" + dx + "," + dy);
					//起點和終點不採納
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					else	mlist.add(dx, dy);
				}
			}
			else{
				//Log.d("", " 二  ");
				for(dx = sX, dy = sY; dy >= eY; dy--){
					//Log.d("", "加點" + dx + "," + dy);
					//起點和終點不採納
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					
					else	mlist.add(dx, dy);
					
				}
			}
		}
		//畫出當線為水平時
		else if(eY == sY){
			if(eX > sX){
				for(dx = sX, dy = sY; dx <= eX; dx++){
					//起點和終點不採納
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					
					else	mlist.add(dx,  dy);
				}
			}
			else
				for(dx = sX, dy = sY; dx >= eX; dx--){
					//起點和終點不採納
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					else	mlist.add(dx,  dy);
				}
		}		
		else{
		//x變化率、y變化率
		a = eY - sY;
		b = eX - sX;	

		//|x|變化率、|y|變化率
		rateX = Math.abs(eX - sX);
		rateY = Math.abs(eY - sY);
		if(rateX < rateY){	// |m| > 1
			a = -a;
			m = 2;
		}
		else{
			b = -b;
			m = 1;	// |m| <= 1
		}

		//往右上方畫
		if((eX > sX) && (eY > sY)){
			NewD = 2*a + b;
			
			//Log.d("", "往右上方畫");
			for(dx = sX, dy = sY;  (dx < eX) && (dy < eY);){
				//0度~45度
				if(m == 1){
					//Log.d("", "m == 1");
					//Log.d("", "dx: " + dx + "  dy: " + dy);
					if(NewD > 0){
						dx++;
						dy++;
						NewD = NewD + a + b;
					}
					else{
						dx++;
						NewD = NewD + a;
					}
				}
				//45度~90度
				else{
					//Log.d("", "else");
					if(NewD > 0){
						dx++;
						dy++;
						NewD = NewD + a + b;
					}
					else{
						dy++;
						NewD = NewD + b;
					}
				}
				if(dx == sX && dy == sY || dx == eX && dy == eY);
				else mlist.add(dx, dy);
			}
		}

		//往左上方畫
		if((eX < sX) && (eY > sY)){
			if(m == 1)	NewD = (-2)*a + b;
			else		NewD = -a + 2*b;

			//Log.d("", "往左上方畫");
			for(dx = sX, dy = sY;  (dx > eX) && (dy < eY);){
				//135度~180度
				if(m == 1){
					if(NewD < 0){
						dx--;
						dy++;
						NewD = NewD -a + b;
					}
					else{
						dx--;
						NewD = NewD - a;
					}
				}
				//90度~135度
				else{
					if(NewD < 0){
						dx--;
						dy++;
						NewD = NewD -a + b;
					}
					else{
						dy++;
						NewD = NewD + b;
					}
				}
				if(dx == sX && dy == sY || dx == eX && dy == eY);
				else mlist.add(dx, dy);
			}

		}

		//往左下方畫
		if((eX < sX) && (eY < sY)){
			if(m == 1)	NewD = (-2)*a - b;
			else		NewD = -a + (-2)*b;

			//Log.d("", "往左下方畫");
			for(dx = sX, dy = sY;  (dx > eX) && (dy > eY);){
				
				//180度~225度
				if(m == 1){
					if(NewD < 0){
						dx--;
						NewD = NewD - a;
					}
					else{
						dx--;
						dy--;
						NewD = NewD - a - b;
					}
				} 
				//225度~270度
				else{
					if(NewD < 0){
						dy--;
						NewD = NewD - b;
					}
					else{
						dx--;
						dy--;					
						NewD = NewD -a - b;
					}
				}
				//Log.d("", "dx: " + dx + "  dy: " + dy);
				if(dx == sX && dy == sY || dx == eX && dy == eY);
				else mlist.add(dx, dy);
			}
		}

		//往右下方畫
		if((eX > sX) && (eY < sY)){
			if(m == 1)	NewD = 2*a - b;
			else		NewD = a + (-2)*b;
			
			//Log.d("", "往右下方畫");
			for(dx = sX, dy = sY;  (dx < eX) && (dy > eY);){
				//315度~0度
				if(m == 1){
					if(NewD < 0){
						dx++;
						dy--;
						NewD = NewD + a - b;
					}
					else{
						dx++;
						NewD = NewD + a;
					}
				}
				//270度~315度
				else{
					if(NewD < 0){
						dx++;
						dy--;					
						NewD = NewD + a - b;
					}
					else{
						dy--;
						NewD = NewD - b;
					}
				}
				if(dx == sX && dy == sY || dx == eX && dy == eY);
				else	mlist.add(dx, dy);
			}
		}		
		}
		//Log.d("", "mlist陣列大小:  " + mlist.getLength());
		return mlist;
		
	}
}

//儲存直線上所有點的class
class LinePointsList{
	//每個點有x,y座標
    private LinkedList<Integer> linkedList_X;
    private LinkedList<Integer> linkedList_Y;
    
    
    public LinePointsList() {
    	linkedList_X = new LinkedList<Integer>();
    	linkedList_Y = new LinkedList<Integer>();
    }


	public int getX(int pos) {
		return linkedList_X.get(pos) ;
	}


	public int getY(int pos) {
		return linkedList_Y.get(pos) ;
	}
	
	
	public void add(int x, int y) {
		linkedList_X.addLast(x);
		linkedList_Y.addLast(y);
	}
    
	public int getLength(){
		return linkedList_X.size();
	}
 
}


//每一棟建築物的class
class BuildingList{
	//每棟建築物有多條直線
	private LinePointsList[] mList;
	//每棟建築物標記數目
	int mark_numbers = 0;
  
  
	public BuildingList(int marksize) {
		mList = new LinePointsList[marksize];
		mark_numbers = marksize;
		for(int i=0; i<marksize; i++){
		  mList[i] = new LinePointsList();
		}
	}
  
  	

	public LinePointsList getMarkList(int pos) {
		return mList[pos];
	}
	
	public void setMarkList(int pos, LinePointsList newMarkList) {
		mList[pos] = newMarkList;
	}
  
	public int getMarkNumbers(){
		return mark_numbers;
	}

}
