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
		
		//�O�_�N�Ϥ��s�ܥ���
		boolean SaveImage = saveImage;
        int picw = tmpImage.getWidth();
        int pich = tmpImage.getHeight();
        //Log.d("pic", "picw, pich " + picw + " , " + pich);
        //�e�u�Ϋؿv����class
        BuildingList mBuildingList[];
        
        //�e�B�z�A�N�a�϶¥շ��ȤơA�î������T
        int[] ckImgpix = new int[picw * pich];
        //���ե�
        int[] tmpImgpix = new int[picw * pich];
        int[] TESTtmpImgpix = new int[picw * pich];
        int[] pureMappixel = new int[picw * pich];
        tmpImage.getPixels(ckImgpix, 0, picw, 0, 0, picw, pich);
        tmpImage.getPixels(TESTtmpImgpix, 0, picw, 0, 0, picw, pich);
        tmpImage.getPixels(TESTtmpImgpix, 0, picw, 0, 0, picw, pich);
        
        
        int Now_X = 0, Now_Y = 0;
        int R,G,B;
        int test_I = 0;
        
        //�H�U��j��Ngoogle map�W����r������
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
        
        //�a�ϤG�Ȥ�
        for(int i=0, index; i<pich; i++){
        	for(int j=0; j<picw; j++){
        		index = i * picw + j;
        		//bitwise shifting
                R = (ckImgpix[index] >> 16) & 0xff;     
                G = (ckImgpix[index] >> 8) & 0xff;
                B = ckImgpix[index] & 0xff;

                //�Ϥ��������I�Y���۾��Ҧb���I
                if(R == 255 && G == 0 && B == 0){
                	//Now_X = j;
                	//Now_Y = i;
                }
                else if(R == 0 && G == 255 && B == 0){
                	//Log.d("", j + "," + i + "  :  " + "���");
                }
                else if(R == 0 && G == 0 && B == 255){
                	//Log.d("", j + "," + i + "  :  " + "�Ŧ�");
                }
                //�Ǧ�B�¦�B�z���¦�(�ؿv)
                else if(R == G && R == B && R < 250){
                	ckImgpix[index] = Color.argb(255, 0, 0, 0);
                	tmpImgpix[index] = Color.argb(255, 0, 0, 0);
                	TESTtmpImgpix[index] = Color.argb(255, 0, 0, 0);
                	pureMappixel[index] = Color.argb(255, 0, 0, 0);
                }
             
                //��L���B�z���զ�
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
         * �YState=1�A�h�ϥΦ۩w�q�y���ഫpixel
         */
        if(State == 1){
        	Now_X = pixelAry[0];
        	Now_Y = pixelAry[1];
        }
        
        
        //�C�ӫؿv�������U�۪��@���u
        mBuildingList = new BuildingList[mAllLocationList.getSize()];
    
        //�ѤU���U�Ӫ�satic google map ��X�ثe�Ҧb��m���I�ܸ�Ʈw���Ҧ��ؿv����mark�I�������Ҧ��s�u
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
        
		
		//�v�@�ˬd�ܫؿv�����C���u�A�Y�u�ܩ��ê���W�h�ۤW�¦�A�_�h�ۤW���
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
		//�X�ɫؿv��
        for(int i=0; i<mAllLocationList.getSize(); i++){
        	Log.d("", "��" + i + "��" );
        	visible_count = false;
        	for(int j=0; j<mBuildingList[i].getMarkNumbers(); j++){
        		Log.d("", "��" + j + "���u" );
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
        		Log.d("", "count: " + Redcount + "��");
        		if(Redcount < 10){
        			visible_count = true;
        			break;
        		}
        	}
        	Log.d("", mAllLocationList.getelement(i)[0]);
        	if(visible_count == false){
        		Log.d("", "�ݤ���");
        		mAllLocationList.setConstruction_Visible(i, false);
        	}
        	else	Log.d("", "�ݪ���");
        }        

        
        
        
 //�s���Ϥ�
 if(SaveImage){
////////////////////////////////////////���եΡA�C�ӫؿv�����s���@�i��///////////////////////////////////// //              
        for(int i=0; i<mAllLocationList.getSize(); i++){
        	int[] tmpAry = new int[picw * pich];
	        //�_��
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
	    
	    
	    
/////////���եΡA�U�ؿv����mark�s���@�i��//////////////////////////////////////////////////
    	int[] tmpAry = new int[picw * pich];
        //�_��
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
			output = new FileOutputStream (new File(SDLstr.toString() + "/"+  "mark��"  +  test_I++ + StoreName));
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



//���ͤ@���u�W���Ҧ��I��class
class GenerateLinePoint{
	static LinePointsList getAllPoint(LinePointsList mlist, 
										int sX, int sY, int eX, int eY){
		int dx, dy;
		int NewD;
		int a, b;
		int cX, cY;
		int m, rateX, rateY;
		
		//�e�X��u��������
		if(sX == eX){
			if(eY > sY){
				//Log.d("", " �@  ");
				for(dx = sX, dy = sY; dy <= eY; dy++){
					//Log.d("", "�[�I" + dx + "," + dy);
					//�_�I�M���I���į�
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					else	mlist.add(dx, dy);
				}
			}
			else{
				//Log.d("", " �G  ");
				for(dx = sX, dy = sY; dy >= eY; dy--){
					//Log.d("", "�[�I" + dx + "," + dy);
					//�_�I�M���I���į�
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					
					else	mlist.add(dx, dy);
					
				}
			}
		}
		//�e�X��u��������
		else if(eY == sY){
			if(eX > sX){
				for(dx = sX, dy = sY; dx <= eX; dx++){
					//�_�I�M���I���į�
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					
					else	mlist.add(dx,  dy);
				}
			}
			else
				for(dx = sX, dy = sY; dx >= eX; dx--){
					//�_�I�M���I���į�
					if(dx == sX && dy == sY || dx == eX && dy == eY);
					else	mlist.add(dx,  dy);
				}
		}		
		else{
		//x�ܤƲv�By�ܤƲv
		a = eY - sY;
		b = eX - sX;	

		//|x|�ܤƲv�B|y|�ܤƲv
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

		//���k�W��e
		if((eX > sX) && (eY > sY)){
			NewD = 2*a + b;
			
			//Log.d("", "���k�W��e");
			for(dx = sX, dy = sY;  (dx < eX) && (dy < eY);){
				//0��~45��
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
				//45��~90��
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

		//�����W��e
		if((eX < sX) && (eY > sY)){
			if(m == 1)	NewD = (-2)*a + b;
			else		NewD = -a + 2*b;

			//Log.d("", "�����W��e");
			for(dx = sX, dy = sY;  (dx > eX) && (dy < eY);){
				//135��~180��
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
				//90��~135��
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

		//�����U��e
		if((eX < sX) && (eY < sY)){
			if(m == 1)	NewD = (-2)*a - b;
			else		NewD = -a + (-2)*b;

			//Log.d("", "�����U��e");
			for(dx = sX, dy = sY;  (dx > eX) && (dy > eY);){
				
				//180��~225��
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
				//225��~270��
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

		//���k�U��e
		if((eX > sX) && (eY < sY)){
			if(m == 1)	NewD = 2*a - b;
			else		NewD = a + (-2)*b;
			
			//Log.d("", "���k�U��e");
			for(dx = sX, dy = sY;  (dx < eX) && (dy > eY);){
				//315��~0��
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
				//270��~315��
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
		//Log.d("", "mlist�}�C�j�p:  " + mlist.getLength());
		return mlist;
		
	}
}

//�x�s���u�W�Ҧ��I��class
class LinePointsList{
	//�C���I��x,y�y��
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


//�C�@�ɫؿv����class
class BuildingList{
	//�C�ɫؿv�����h�����u
	private LinePointsList[] mList;
	//�C�ɫؿv���аO�ƥ�
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
