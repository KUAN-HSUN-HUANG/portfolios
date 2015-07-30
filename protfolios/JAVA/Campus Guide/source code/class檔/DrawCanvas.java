package com.example.campusguidv5;

import android.R.integer;
import android.content.Context;
import android.graphics.Paint.Align;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class DrawCanvas extends View{
	private Construction drawList;
	private int RcdTb[][][];
	private int Bearing;
	private int TextSize = 40;
	private int TextColor = Color.YELLOW;
	private float cX, cY;
	private Paint drawPoint = new Paint();
	private Paint drawRect = new Paint();
	private Paint drawContRect = new Paint();
	private Paint drawCont = new Paint();


	
    public DrawCanvas(Context context) {
        super(context);

    }


  
    public void redraw(Construction src, int[][][] table, int brg){
 		drawList = src;
 		RcdTb = table;
 		Bearing = brg;
    	invalidate();
    }

    public void setTextSize(int size){ TextSize = size; }
    public void setTextColor(int color){ TextColor = color; }
     
	 @Override
     protected void onDraw(Canvas canvas) {
         
         //字體顏色
         drawPoint.setColor(TextColor);
         drawCont.setColor(Color.BLACK);
         //字體大小
         drawPoint.setTextSize(TextSize);
         drawCont.setTextSize(TextSize);
         //文字位置
         drawPoint.setTextAlign(Align.CENTER);
         drawCont.setTextAlign(Align.CENTER);
         
         //矩形顏色
         drawRect.setColor(Color.BLUE);
         drawContRect.setColor(Color.WHITE);

         
         if(drawList != null){
        	 for(int i=0; i < drawList.getSize(); i++){
        		 if(drawList.isConstruction_Visible(i)){
        			 if(RcdTb[Bearing][i][0] > 0){
        				 
        				 //Log.v("位置: ", "drawList.getelement(i)[0]"+);
        		/*		 
	        		 String LocationName = drawList.getelement(i)[0];
		        		 canvas.drawText(LocationName, 
		        				 	RcdTb[Bearing][i][0], RcdTb[Bearing][i][1],
		        				 	drawPoint);
	        		 
        			 }
        		*/
        			
        			String dstr = drawList.getelement(i)[0];
        			
        			 Rect bounds = new Rect();
        			 drawPoint.getTextBounds("字", 0, 1, bounds);
        			 
        			 
        			 
        			 int strLength = dstr.length();
        			 
        			 //canvas.drawRect(RcdTb[Bearing][i][0] - bounds.width()/2, RcdTb[Bearing][i][1] - (dstr.length()*bounds.height())/2 - bounds.height()/2, RcdTb[Bearing][i][0] + bounds.width()/2, RcdTb[Bearing][i][1] + (dstr.length()*bounds.height())/2, drawRect);
    				 int halfH_odd = (int)((dstr.length()/2.0)*bounds.height() +0.5);
    				 int halfH_even = (int)((dstr.length()*bounds.height())/2.0 +0.5);
    				 
    				 int halfW = (int)(bounds.width()/2.0 + 0.5);
        			 if(dstr.length()%2 != 0){
        				 canvas.drawRect(RcdTb[Bearing][i][0] - halfW, RcdTb[Bearing][i][1] - halfH_odd - 20, RcdTb[Bearing][i][0] + halfW, RcdTb[Bearing][i][1] + halfH_odd-10, drawRect);
        			 }
        			 else{
        				 canvas.drawRect(RcdTb[Bearing][i][0] - halfW, RcdTb[Bearing][i][1] - halfH_odd - bounds.height(), RcdTb[Bearing][i][0] + halfW, RcdTb[Bearing][i][1] + halfH_even - bounds.height() + 10, drawRect);
        			 }
        			 

    				 int halfH = (int)((drawList.getelement(i)[3].length()*bounds.height())/2.0 +0.5);        			 
    				 halfW = (int)(bounds.width()/2.0 + 0.5);
        			 if(drawList.getelement(i)[3].length()%2 != 0){
        				 canvas.drawRect(RcdTb[Bearing][i][0] - halfW + bounds.width(), RcdTb[Bearing][i][1] - halfH - 20, RcdTb[Bearing][i][0] + halfW + bounds.width(), RcdTb[Bearing][i][1] + halfH - 10, drawContRect);
        			 }
        			 else{
        				 canvas.drawRect(RcdTb[Bearing][i][0] - halfW + bounds.width(), RcdTb[Bearing][i][1] - halfH - bounds.height(), RcdTb[Bearing][i][0] + halfW + bounds.width(), RcdTb[Bearing][i][1] + halfH - bounds.height() + 10, drawContRect);
        			 }
/*
        			 for(int j=strLength/2-1, k = 1; j>=0; j--, k++)	canvas.drawText(dstr.substring(j,j+1), RcdTb[Bearing][i][0], RcdTb[Bearing][i][1] - k * bounds.height(), drawPoint);
        			 for(int j=strLength/2+1, k = 1; j<strLength; j++, k++)	canvas.drawText(dstr.substring(j,j+1), RcdTb[Bearing][i][0], RcdTb[Bearing][i][1] + k * bounds.height(), drawPoint);
canvas.drawText(dstr.substring(dstr.length()/2,dstr.length()/2+1), RcdTb[Bearing][i][0], RcdTb[Bearing][i][1], drawPoint);
*/
        			 for(int j=strLength/2-1, k = 1; j>=0; j--, k++)	canvas.drawText(dstr.substring(j,j+1), RcdTb[Bearing][i][0], 360 - k * bounds.height(), drawPoint);
        			 for(int j=strLength/2+1, k = 1; j<strLength; j++, k++)	canvas.drawText(dstr.substring(j,j+1), RcdTb[Bearing][i][0], 360 + k * bounds.height(), drawPoint);
        			 canvas.drawText(dstr.substring(dstr.length()/2,dstr.length()/2+1), RcdTb[Bearing][i][0], 360, drawPoint);

        			 String dstrContext = drawList.getelement(i)[3];
        			 int Context_length = dstrContext.length();
        			 for(int j=Context_length/2-1, k = 1; j>=0; j--, k++)	canvas.drawText(dstrContext.substring(j,j+1), RcdTb[Bearing][i][0] + bounds.width(), 360 - k * bounds.height(), drawCont);
        			 for(int j=Context_length/2+1, k = 1; j<Context_length; j++, k++)	canvas.drawText(dstrContext.substring(j,j+1), RcdTb[Bearing][i][0] + bounds.width(), 360 + k * bounds.height(), drawCont);
        			 canvas.drawText(dstrContext.substring(dstrContext.length()/2,dstrContext.length()/2+1), RcdTb[Bearing][i][0] + bounds.width(), 360, drawCont);
        		 }
        			 
	    	}
/*        		 
        	 String srrr1 = "看阿好一顆大西瓜";
        	 String srrr2 = "一隻好大蚊子";
			 Rect bounds = new Rect();
			 drawPoint.getTextBounds("字", 0, 1, bounds);
			 //Log.v("srrr1.length()", ""+srrr1.length());

			 //if(srrr1.length() %2 !=0){
			 int strLength = srrr1.length();
			 for(int j=strLength/2-1, k = 1; j>=0; j--, k++)	canvas.drawText(srrr1.substring(j,j+1), 300, 300 - k * bounds.height(), drawPoint);
			 for(int j=strLength/2+1, k = 1; j<strLength; j++, k++)	canvas.drawText(srrr1.substring(j,j+1), 300, 300 + k * bounds.height(), drawPoint);
			 canvas.drawText(srrr1.substring(srrr1.length()/2,srrr1.length()/2+1), 300, 300, drawPoint);
 */
        	 }
	 }
         
         else	canvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR); 
	 }
}

class DrawInformations extends View{
	private int drawX, drawY;
	private String drawText1 = " ";
	private String drawText2 = " ";
	private String drawText3 = " ";
	private int TextSize = 20;
	private int TextColor = Color.BLUE;
	private Paint drawPoint = new Paint();


	
    public DrawInformations(Context context) {
    	
        super(context);
    }

    public void setDrawPos(int Width, int Height){
    	Log.v("befor", Width + "  " + Height);
    	drawX = (int)(Width/10.0 + 0.5);
    	drawY = Height/2;
    	Log.v("after", drawX + "  " + drawY);
    }
    
  
    public void redraw(String text1, String text2, String text3){
    		drawText1 = text1;
    		drawText2 = text2;
    		drawText3 = text3;
    		invalidate();
    }

    public void setTextSize(int size){ TextSize = size; }
    public void setTextColor(int color){ TextColor = color; }
     
	 @Override
     protected void onDraw(Canvas canvas) {
         
         //字體顏色
         drawPoint.setColor(TextColor);
         //字體大小
         drawPoint.setTextSize(TextSize);
         //文字位置
         drawPoint.setTextAlign(Align.CENTER);
         //Log.v("check", drawX + "  " + drawY);
		 canvas.drawText(drawText2 + "  ,  " + drawText3, 
				 //600,530,
				 drawY , drawX*6,
				 //300,300,
				 drawPoint);
		 
        canvas.drawText(drawText1, 
        			drawY,drawX*7,
        			//600,580,
        			drawPoint);
        }
}