#include "glut.h"
#include<stdio.h>
#include<math.h>

GLsizei winWidth = 800, winHeight = 600;
//***********************************************
int sx, sy;
int lbx,lby,lfx,lfy;
int flag;
//***********************************************

void init();
void displayFcn();
void winReshapeFcn(GLint newWidth, GLint newHeight);
void plotPoint(GLint x, GLint y);
void plotLnPoint();
void mousePtPlot(GLint button, GLint action, GLint xMouse, GLint yMouse);
void mouseLnPlot(GLint xMouse, GLint yMouse);
void mouseRndPlot(GLint xMouse, GLint yMouse);
void Checkmouse(GLint button, GLint action, GLint xMouse, GLint yMouse);


void init(){
	glClearColor(1.0,  1.0,  1.0,  1.0);
	glMatrixMode(GL_PROJECTION);
	gluOrtho2D(0.0,  200.0,  0.0,  150.0);
}




void displayFcn(){
	glClear(GL_COLOR_BUFFER_BIT);



//*******************************XOR mode
	glEnable(GL_COLOR_LOGIC_OP);
    glLogicOp(GL_XOR);
	glColor3f(1.0,  1.0,  1.0);
//*******************************
	

	glPointSize(1.0);
}




void winReshapeFcn(GLint newWidth, GLint newHeight){
	glViewport(0, 0, newWidth, newHeight);
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluOrtho2D(0.0,  GLdouble(newWidth),  0.0,  newHeight);

	winWidth = newWidth;
	winHeight = newHeight;
}




void plotPoint(GLint x, GLint y){
	glBegin(GL_POINTS);
		glVertex2i(x, y);
	glEnd();
}




void Checkmouse(GLint button, GLint action, GLint xMouse, GLint yMouse){
	
	//if按下的是左鍵
	if(button == GLUT_LEFT_BUTTON && action == GLUT_DOWN){
		//記錄下按下左鍵該位置
		sx = xMouse;
		sy = yMouse;
		flag = NULL;//設定是第一次畫，不需重畫

		glutMotionFunc(mouseLnPlot);//畫線
	}
	
	//if按下的是右鍵
	else if(button == GLUT_RIGHT_BUTTON && action == GLUT_DOWN){
		//記錄下按下右鍵該位置
		sx = xMouse;
		sy = yMouse;
		flag = NULL;//設定是第一次畫，不需重畫

		glutMotionFunc(mouseRndPlot);//畫圓
	}
}




void mousePtPlot(GLint button, GLint action, GLint xMouse, GLint yMouse){
	plotPoint(sx,  winHeight - sy);
	glFlush();
}




void mouseLnPlot(GLint xMouse, GLint yMouse){
	int dx, dy;
	int initDetecX, initDetecY;
	int initStartX, initStartY;
	int NewD;
	int a, b;
	int cX, cY;
	int m, rateX, rateY;



//****************************************************************重畫

	if(flag != NULL){	//若flag!=NULL重畫之(非畫第一次)
		//設定重畫之起點、終點為上次結束紀錄之點
		initStartX = lbx;
		initStartY = lby;
		initDetecX = lfx;
		initDetecY = lfy;


		//畫出當線為垂直時
		if(initDetecX == initStartX){
			if(initDetecY > initStartY){
				for(dx = lbx, dy = lby; dy <= initDetecY; dy++){
					plotPoint(dx,  dy);
				}
			}
			else
				for(dx = lbx, dy = lby; dy >= initDetecY; dy--){
					plotPoint(dx,  dy);
				}
		}

		//畫出當線為水平時
		if(initDetecY == initStartY){
			if(initDetecX > initStartX){
				for(dx = lbx, dy = lby; dx <= initDetecX; dx++){
					plotPoint(dx,  dy);
				}
			}
			else
				for(dx = lbx, dy = lby; dx >= initDetecX; dx--){
					plotPoint(dx,  dy);
				}
		}


		//x變化率、y變化率
		a = initDetecY - initStartY;
		b = initDetecX - initStartX;	

		//|x|變化率、|y|變化率
		rateX = abs(initDetecX - initStartX);
		rateY = abs(initDetecY - initStartY);
		if(rateX < rateY){	// |m| > 1
			a = -a;
			m = 2;
		}
		else{
			b = -b;
			m = 1;	// |m| <= 1
		}



		//往右上方畫
		if((initDetecX > initStartX) && (initDetecY > initStartY)){
			NewD = 2*a + b;


			for(dx = initStartX, dy = initStartY;  (dx <= initDetecX) && (dy <= initDetecY);){
				//0度~45度
				if(m == 1){
					if(NewD > 0){
						dx = dx++;
						dy = dy++;
						NewD = NewD + a + b;
					}
					else{
						dx = dx++;
						dy = dy;
						NewD = NewD + a;
					}
				}
				//45度~90度
				else{
					if(NewD > 0){
						dx = dx++;
						dy = dy++;
						NewD = NewD + a + b;
					}
					else{
						dx = dx;
						dy = dy++;
						NewD = NewD + b;
					}
				}
				plotPoint(dx, dy);
			}
		}

		//往左上方畫
		if((initDetecX < initStartX) && (initDetecY > initStartY)){
			if(m == 1)	NewD = (-2)*a + b;
			else		NewD = -a + 2*b;


			for(dx = initStartX, dy = initStartY;  (dx >= initDetecX) && (dy <= initDetecY);){
				//135度~180度
				if(m == 1){
					if(NewD < 0){
						dx = dx--;
						dy = dy++;
						NewD = NewD -a + b;
					}
					else{
						dx = dx--;
						dy = dy;
						NewD = NewD - a;
					}
				}
				//90度~135度
				else{
					if(NewD < 0){
						dx = dx--;
						dy = dy++;
						NewD = NewD -a + b;
					}
					else{
						dx = dx;
						dy = dy++;
						NewD = NewD + b;
					}
				}
				plotPoint(dx, dy);
			}

		}

		//往左下方畫
		if((initDetecX < initStartX) && (initDetecY < initStartY)){
			if(m == 1)	NewD = (-2)*a - b;
			else		NewD = -a + (-2)*b;

			
			for(dx = initStartX, dy = initStartY;  (dx >= initDetecX) && (dy >= initDetecY);){
				//180度~225度
				if(m == 1){
					if(NewD < 0){
						dx = dx--;
						dy = dy;
						NewD = NewD - a;
					}
					else{
						dx = dx--;
						dy = dy--;
						NewD = NewD - a - b;
					}
				}
				//225度~270度
				else{
					if(NewD < 0){
						dx = dx;
						dy = dy--;
						NewD = NewD - b;
					}
					else{
						dx = dx--;
						dy = dy--;					
						NewD = NewD -a - b;
					}
				}
				plotPoint(dx, dy);
			}
		}

		//往右下方畫
		if((initDetecX > initStartX) && (initDetecY < initStartY)){
			if(m == 1)	NewD = 2*a - b;
			else		NewD = a + (-2)*b;
			
			
			for(dx = initStartX, dy = initStartY;  (dx <= initDetecX) && (dy >= initDetecY);){
				//315度~0度
				if(m == 1){
					if(NewD < 0){
						dx = dx++;
						dy = dy--;
						NewD = NewD + a - b;
					}
					else{
						dx = dx++;
						dy = dy;
						NewD = NewD + a;
					}
				}
				//270度~315度
				else{
					if(NewD < 0){
						dx = dx++;
						dy = dy--;					
						NewD = NewD + a - b;
					}
					else{
						dx = dx;
						dy = dy--;
						NewD = NewD - b;
					}
				}
				plotPoint(dx, dy);
			}
		}


	}

//****************************************************************重畫END

	//使用偵測到壓下滑鼠左鍵之座標
	initStartX = sx;
	initStartY = winHeight - sy;
	initDetecX  = xMouse;
	initDetecY  =  winHeight - yMouse;


	//畫出當線為垂直時
	if(initDetecX == initStartX){
		if(initDetecY > initStartY){
			for(dx = sx, dy = sy; dy >= yMouse; dy--){
				plotPoint(dx,  winHeight - dy);
			}
		}
		else
			for(dx = sx, dy = sy; dy <= yMouse; dy++){
				plotPoint(dx,  winHeight - dy);
			}
	}

	//畫出當線為水平時
	if(initDetecY == initStartY){
		if(initDetecX > initStartX){
			for(dx = sx, dy = sy; dx <= xMouse; dx++){
				plotPoint(dx,  winHeight - dy);
			}
		}
		else
			for(dx = sx, dy = sy; dx >= xMouse; dx--){
				plotPoint(dx,  winHeight - dy);
			}
	}


	//x變化率、y變化率
	a = initDetecY - initStartY;
	b = initDetecX - initStartX;
	
	//|x|變化率、|y|變化率
	rateX = abs(initDetecX - initStartX);
	rateY = abs(initDetecY - initStartY);
	if(rateX < rateY){
		a = -a;
		m = 2;
	}
	else{
		b = -b;
		m = 1;
	}

	//往右上方畫
	if((initDetecX > initStartX) && (initDetecY > initStartY)){
		NewD = 2*a + b;


		for(dx = initStartX, dy = initStartY;  (dx <= initDetecX) && (dy <= initDetecY);){
			if(m == 1){
				if(NewD > 0){
					dx = dx++;
					dy = dy++;
					NewD = NewD + a + b;
				}
				else{
					dx = dx++;
					dy = dy;
					NewD = NewD + a;
				}
			}
			else{
				if(NewD > 0){
					dx = dx++;
					dy = dy++;
					NewD = NewD + a + b;
				}
				else{
					dx = dx;
					dy = dy++;
					NewD = NewD + b;
				}
			}
			plotPoint(dx, dy);
		}
	}




	//往左上方畫
	if((initDetecX < initStartX) && (initDetecY > initStartY)){
		if(m == 1)	NewD = (-2)*a + b;
		else		NewD = -a + 2*b;


		for(dx = initStartX, dy = initStartY;  (dx >= initDetecX) && (dy <= initDetecY);){
			if(m == 1){
				if(NewD < 0){
					dx = dx--;
					dy = dy++;
					NewD = NewD -a + b;
				}
				else{
					dx = dx--;
					dy = dy;
					NewD = NewD - a;
				}
			}
			
			else{
				if(NewD < 0){
					dx = dx--;
					dy = dy++;
					NewD = NewD -a + b;
				}
				else{
					dx = dx;
					dy = dy++;
					NewD = NewD + b;
				}
			}
			plotPoint(dx, dy);
		}
	}



	//往左下方畫
	if((initDetecX < initStartX) && (initDetecY < initStartY)){
		if(m == 1)	NewD = (-2)*a - b;
		else		NewD = -a + (-2)*b;

		
		for(dx = initStartX, dy = initStartY;  (dx >= initDetecX) && (dy >= initDetecY);){
			if(m == 1){
				if(NewD < 0){
					dx = dx--;
					dy = dy;
					NewD = NewD - a;
				}
				else{
					dx = dx--;
					dy = dy--;
					NewD = NewD - a - b;
				}
			}
			else{
				if(NewD < 0){
					dx = dx;
					dy = dy--;
					NewD = NewD - b;
				}
				else{
					dx = dx--;
					dy = dy--;					
					NewD = NewD -a - b;
				}
			}
			plotPoint(dx, dy);
		}
	}


	//往右下方畫
	if((initDetecX > initStartX) && (initDetecY < initStartY)){
		if(m == 1)	NewD = 2*a - b;
		else		NewD = a + (-2)*b;
		
		
		for(dx = initStartX, dy = initStartY;  (dx <= initDetecX) && (dy >= initDetecY);){
			if(m == 1){
				if(NewD < 0){
					dx = dx++;
					dy = dy--;
					NewD = NewD + a - b;
				}
				else{
					dx = dx++;
					dy = dy;
					NewD = NewD + a;
				}
			}
			else{
				if(NewD < 0){
					dx = dx++;
					dy = dy--;					
					NewD = NewD + a - b;
				}
				else{
					dx = dx;
					dy = dy--;
					NewD = NewD - b;
				}
			}
			plotPoint(dx, dy);
		}

	}
		//記錄下畫出此線之起點與終點
		lbx = initStartX;
		lby = initStartY;
		lfx = initDetecX;
		lfy = initDetecY;
		flag = 1;//紀錄已畫線

	glFlush();
}//mouseLnPlot() END




void mouseRndPlot(GLint xMouse, GLint yMouse){
	int dx, dy;
	int initDetecX, initDetecY;
	int initStartX, initStartY;
	int NewD;
	double temp;
	int radius;
	int centerX, centerY;
	int rateX, rateY;
	int computeX, computeY;


//****************************************************************重畫
	if(flag != NULL){	//若flag!=NULL重畫之(非畫第一次)
		//設定重畫之起點、終點為上次結束紀錄之點
		initStartX	= lbx;
		initStartY	= lby;
		initDetecX  = lfx;
		initDetecY  = lfy;

		//|x|變化率、|y|變化率
		rateX = abs(initDetecX - initStartX);
		rateY = abs(initDetecY - initStartY);
		
		//圓心
		centerX = (int)((initDetecX+initStartX)/2.0);
		centerY = (int)((initDetecY+initStartY)/2.0);


		temp =  rateX*rateX +  rateY*rateY;
		radius = (int)(sqrt(temp)/2.0);//半徑

		//從(R,0)開始畫
		computeX = 0;
		computeY = radius;
		NewD = 8*computeX  - 4*computeY + 5;


		for(dx = 0, dy = radius; dx <= dy; ){
			if(NewD > 0){
				dx++;
				dy--;
				NewD = NewD + 2*dx - 2*dy + 5;
			}
			else{
				dx++;
				NewD = NewD + 2*dx + 3;
			}
			plotPoint(dx+centerX, dy+centerY);//90度~45度
			plotPoint(dy+centerX, dx+centerY);//0度~45度

			plotPoint(-dx+centerX, dy+centerY);//90度~135度
			plotPoint(-dy+centerX, dx+centerY);//180度~135度

			plotPoint(-dx+centerX, -dy+centerY);//270度~225度
			plotPoint(-dy+centerX, -dx+centerY);//180度~225度

			plotPoint(dx+centerX, -dy+centerY);//270度～315度
			plotPoint(dy+centerX, -dx+centerY);//0度~315度
		}
		//補上各象限因XOR mode而少去的45度、135度、225度、315度的點
		plotPoint(dx+centerX, dy+centerY);
		plotPoint(dy+centerX, dx+centerY);

		plotPoint(-dx+centerX, dy+centerY);
		plotPoint(-dy+centerX, dx+centerY);

		plotPoint(-dx+centerX, -dy+centerY);
		plotPoint(-dy+centerX, -dx+centerY);

		plotPoint(dx+centerX, -dy+centerY);
		plotPoint(dy+centerX, -dx+centerY);


		//補上各象限因XOR mode而少去的 0度、90度、180度、270度的點
		dx = 0;
		dy = radius;
		plotPoint(centerX, radius+centerY);
		plotPoint(radius+centerX,centerY);
		plotPoint(centerX-radius, centerY);
		plotPoint(centerX, centerY-radius);
	}
//****************************************************************重畫END

	initStartX  = sx;
	initStartY  = winHeight - sy;
	initDetecX  = xMouse;
	initDetecY  =  winHeight - yMouse;


	rateX = abs(initDetecX - initStartX);
	rateY = abs(initDetecY - initStartY);
	
	//圓心
	centerX = (int)((initDetecX+initStartX)/2.0);
	centerY = (int)((initDetecY+initStartY)/2.0);

	temp =  rateX*rateX +  rateY*rateY;
	radius = (int)(sqrt(temp)/2.0);//半徑



	//從(R,0)開始畫
	computeX = 0;
	computeY = radius;
	NewD = 8*computeX  - 4*computeY + 5;

			
	for(dx = 0, dy = radius; dx <= dy; ){
		if(NewD > 0){
			dx++;
			dy--;
			NewD = NewD + 2*dx - 2*dy + 5;
		}
		else{
			dx++;
			NewD = NewD + 2*dx + 3;
		}
			//圓心由(0,0)平移至(centerX,centerY)
			plotPoint(dx+centerX, dy+centerY);//90度~45度
			plotPoint(dy+centerX, dx+centerY);//0度~45度

			plotPoint(-dx+centerX, dy+centerY);//90度~135度
			plotPoint(-dy+centerX, dx+centerY);//180度~135度

			plotPoint(-dx+centerX, -dy+centerY);//270度~225度
			plotPoint(-dy+centerX, -dx+centerY);//180度~225度

			plotPoint(dx+centerX, -dy+centerY);//270度～315度
			plotPoint(dy+centerX, -dx+centerY);//0度~315度
	}


	//補上各象限因XOR mode而少去的45、135、225、315度的點
	plotPoint(dx+centerX, dy+centerY);
	plotPoint(dy+centerX, dx+centerY);

	plotPoint(-dx+centerX, dy+centerY);
	plotPoint(-dy+centerX, dx+centerY);

	plotPoint(-dx+centerX, -dy+centerY);
	plotPoint(-dy+centerX, -dx+centerY);

	plotPoint(dx+centerX, -dy+centerY);
	plotPoint(dy+centerX, -dx+centerY);


	//補上各象限因XOR mode而少去的 0、90、180、270度的點
	dx = 0;
	dy = radius;
	plotPoint(centerX, radius+centerY);
	plotPoint(radius+centerX,centerY);
	plotPoint(centerX-radius, centerY);
	plotPoint(centerX, centerY-radius);

		
	//記錄下最後滑鼠之X、Y座標
	lbx = initStartX;
	lby = initStartY;
	lfx = initDetecX;
	lfy = initDetecY;
	flag = 1;//紀錄已畫圓

	glFlush();
}//mouseRndPlot END




void main(int argc, char **argv){
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_SINGLE | GLUT_RGB);
	glutInitWindowPosition(100, 100);
	glutInitWindowSize(winWidth, winHeight);
	glutCreateWindow("The anime of line and a circle");

	init();
	glutDisplayFunc(displayFcn);
	glutReshapeFunc(winReshapeFcn);
	glutMouseFunc(Checkmouse);
	glutMainLoop();
}