package com.example.campusguidv5;

import android.R.integer;
import android.util.Log;

public class BearingSuitibitlyCheck {

	
	static int[][][] generateBearingTable(Construction mAllLocationList, 
										int Range, int[][][] RecordTable,
										int widthPixels, int HeightPixels
										){
		int mL, mM, mR, mRng;
		int count = 0;
		int condition = 0;
		float RecordBearing = 0;
		int posX=0;
		int half_HeightPixels = HeightPixels/2;
		int one_cell = widthPixels/2;
		int[] cellArray;
		int posY=widthPixels/2;
		double differ, differAbs;
		boolean CKstate = false;
		
		float cell = widthPixels/90;
		cellArray = new int[Range];
		for(int i=0; i<Range; i++){
		cellArray[i] = (int)(cell*i + 0.5);
		}
		
		for(int nowBaring = 0; nowBaring < 360; nowBaring++){
			mM = nowBaring;
			mRng = Range;
			mL = mM - mRng;
			mR = mM + mRng;
			if(mR >= 360) mR = mR - 360;
			if(mL < 0) mL = mL + 360;
			
			if(mR > mM && mM > mL)	condition = 1;
			if(mL > mR && mR > mM)	condition = 2;
			if(mM > mL && mL > mR)	condition = 3;
			
			
			for(int i = 0; i<mAllLocationList.getSize(); i++){
				if(mAllLocationList.isConstruction_Visible(i)){
					CKstate = false;
					//System.out.println( mAllLocationList.getelement(i)[0] + "和你夾" + mAllLocationList.getRelativeBearing(i) + "度");
					//取得以紀錄之角度
					RecordBearing = mAllLocationList.getRelativeBearing(i);
					//夾角差
					differAbs = RecordBearing - mM;
					if(RecordBearing - mM < 0)	differAbs = differAbs + 360;
					
					//differAbs = Math.abs(RecordBearing - mM);
					int C = (int)(66.44 * (differAbs/10));
					
					switch(condition) {
				        case 1: 
				        	if(differAbs <= 1){
				        		posX = half_HeightPixels;
				        		//System.out.println("1    中");
				        	}
				        	//可畫在螢幕上，畫在右側
				        	else if(mM < RecordBearing && RecordBearing < mR){
				        		//posX = half_HeightPixels + C ;
				        		posX = half_HeightPixels + cellArray[(int)(RecordBearing - mM + 0.5 - 1)];
				        		//System.out.println("1    右");
				        	}
				        	//可畫在螢幕上，畫在左側
				            else if(mL < RecordBearing && RecordBearing < mM){
				            	//posX = half_HeightPixels - C ;
				            	posX = half_HeightPixels - cellArray[(int)(mM - RecordBearing + 0.5 - 1)];
				            	//System.out.println("1    左");
				            }
				            else posX = -1;
				        	CKstate = true;
				            break; 
				        case 2: 
				        	if(differAbs <= 1){
				        		posX = half_HeightPixels;
				        		//System.out.println("2    中");
				        	}
				        	//可畫在螢幕上，畫在右側
				        	else if(mM < RecordBearing && RecordBearing < mR){
				        		//posX = half_HeightPixels + C ;
				        		posX = half_HeightPixels + cellArray[(int)(RecordBearing - mM + 0.5 - 1)];
				        		//System.out.println("2    右");
				        	}
				        	//可畫在螢幕上，畫在左側
				        	else if(mL < RecordBearing && RecordBearing <= 359){
				        		posX = half_HeightPixels - cellArray[(int)(mM + 360.5 - RecordBearing - 1)];
				        	}
				            else if(1 <= RecordBearing && RecordBearing < mM){
				            	posX = half_HeightPixels - cellArray[(int)(mM - RecordBearing + 0.5 - 1)];
				            }
				            else posX = -1;
				        	CKstate = true;
				            break; 
				        case 3: 
				        	if(differAbs <= 1){
				        		posX = half_HeightPixels;
				        		//System.out.println("3    中");
				        	}
				        	//可畫在螢幕上，畫在右側
				        	else if(mM < RecordBearing && RecordBearing <= 359){
				        		posX = half_HeightPixels + cellArray[(int)(RecordBearing - mM + 0.5 - 1)];
				        	}
				        	else if(1 <= RecordBearing && RecordBearing < mR){
				        		posX = half_HeightPixels + cellArray[(int)(360.5 + RecordBearing - mM - 1)];
				        	}
				        	//可畫在螢幕上，畫在左側
				            else if(mL < RecordBearing && RecordBearing < mM){
				            	//posX = half_HeightPixels - C ;
				            	posX = half_HeightPixels - cellArray[(int)(mM - RecordBearing + 0.5 - 1)];
				            }
				            else posX = -1;
				        	CKstate = true;
				            break; 
						}
					if(!CKstate) posX = -1;
					Log.v("CCCC", nowBaring + ":  " + i + " , " + posX + " , " + posY);
					RecordTable[nowBaring][i][0] = posX;
					RecordTable[nowBaring][i][1] = posY;
				}
			}
		}
		return RecordTable;
	}
}
