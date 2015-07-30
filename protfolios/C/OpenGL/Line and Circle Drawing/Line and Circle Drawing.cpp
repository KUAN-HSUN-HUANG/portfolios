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
	
	//if���U���O����
	if(button == GLUT_LEFT_BUTTON && action == GLUT_DOWN){
		//�O���U���U����Ӧ�m
		sx = xMouse;
		sy = yMouse;
		flag = NULL;//�]�w�O�Ĥ@���e�A���ݭ��e

		glutMotionFunc(mouseLnPlot);//�e�u
	}
	
	//if���U���O�k��
	else if(button == GLUT_RIGHT_BUTTON && action == GLUT_DOWN){
		//�O���U���U�k��Ӧ�m
		sx = xMouse;
		sy = yMouse;
		flag = NULL;//�]�w�O�Ĥ@���e�A���ݭ��e

		glutMotionFunc(mouseRndPlot);//�e��
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



//****************************************************************���e

	if(flag != NULL){	//�Yflag!=NULL���e��(�D�e�Ĥ@��)
		//�]�w���e���_�I�B���I���W�������������I
		initStartX = lbx;
		initStartY = lby;
		initDetecX = lfx;
		initDetecY = lfy;


		//�e�X��u��������
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

		//�e�X��u��������
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


		//x�ܤƲv�By�ܤƲv
		a = initDetecY - initStartY;
		b = initDetecX - initStartX;	

		//|x|�ܤƲv�B|y|�ܤƲv
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



		//���k�W��e
		if((initDetecX > initStartX) && (initDetecY > initStartY)){
			NewD = 2*a + b;


			for(dx = initStartX, dy = initStartY;  (dx <= initDetecX) && (dy <= initDetecY);){
				//0��~45��
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
				//45��~90��
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

		//�����W��e
		if((initDetecX < initStartX) && (initDetecY > initStartY)){
			if(m == 1)	NewD = (-2)*a + b;
			else		NewD = -a + 2*b;


			for(dx = initStartX, dy = initStartY;  (dx >= initDetecX) && (dy <= initDetecY);){
				//135��~180��
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
				//90��~135��
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

		//�����U��e
		if((initDetecX < initStartX) && (initDetecY < initStartY)){
			if(m == 1)	NewD = (-2)*a - b;
			else		NewD = -a + (-2)*b;

			
			for(dx = initStartX, dy = initStartY;  (dx >= initDetecX) && (dy >= initDetecY);){
				//180��~225��
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
				//225��~270��
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

		//���k�U��e
		if((initDetecX > initStartX) && (initDetecY < initStartY)){
			if(m == 1)	NewD = 2*a - b;
			else		NewD = a + (-2)*b;
			
			
			for(dx = initStartX, dy = initStartY;  (dx <= initDetecX) && (dy >= initDetecY);){
				//315��~0��
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
				//270��~315��
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

//****************************************************************���eEND

	//�ϥΰ��������U�ƹ����䤧�y��
	initStartX = sx;
	initStartY = winHeight - sy;
	initDetecX  = xMouse;
	initDetecY  =  winHeight - yMouse;


	//�e�X��u��������
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

	//�e�X��u��������
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


	//x�ܤƲv�By�ܤƲv
	a = initDetecY - initStartY;
	b = initDetecX - initStartX;
	
	//|x|�ܤƲv�B|y|�ܤƲv
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

	//���k�W��e
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




	//�����W��e
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



	//�����U��e
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


	//���k�U��e
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
		//�O���U�e�X���u���_�I�P���I
		lbx = initStartX;
		lby = initStartY;
		lfx = initDetecX;
		lfy = initDetecY;
		flag = 1;//�����w�e�u

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


//****************************************************************���e
	if(flag != NULL){	//�Yflag!=NULL���e��(�D�e�Ĥ@��)
		//�]�w���e���_�I�B���I���W�������������I
		initStartX	= lbx;
		initStartY	= lby;
		initDetecX  = lfx;
		initDetecY  = lfy;

		//|x|�ܤƲv�B|y|�ܤƲv
		rateX = abs(initDetecX - initStartX);
		rateY = abs(initDetecY - initStartY);
		
		//���
		centerX = (int)((initDetecX+initStartX)/2.0);
		centerY = (int)((initDetecY+initStartY)/2.0);


		temp =  rateX*rateX +  rateY*rateY;
		radius = (int)(sqrt(temp)/2.0);//�b�|

		//�q(R,0)�}�l�e
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
			plotPoint(dx+centerX, dy+centerY);//90��~45��
			plotPoint(dy+centerX, dx+centerY);//0��~45��

			plotPoint(-dx+centerX, dy+centerY);//90��~135��
			plotPoint(-dy+centerX, dx+centerY);//180��~135��

			plotPoint(-dx+centerX, -dy+centerY);//270��~225��
			plotPoint(-dy+centerX, -dx+centerY);//180��~225��

			plotPoint(dx+centerX, -dy+centerY);//270�ס�315��
			plotPoint(dy+centerX, -dx+centerY);//0��~315��
		}
		//�ɤW�U�H���]XOR mode�Ӥ֥h��45�סB135�סB225�סB315�ת��I
		plotPoint(dx+centerX, dy+centerY);
		plotPoint(dy+centerX, dx+centerY);

		plotPoint(-dx+centerX, dy+centerY);
		plotPoint(-dy+centerX, dx+centerY);

		plotPoint(-dx+centerX, -dy+centerY);
		plotPoint(-dy+centerX, -dx+centerY);

		plotPoint(dx+centerX, -dy+centerY);
		plotPoint(dy+centerX, -dx+centerY);


		//�ɤW�U�H���]XOR mode�Ӥ֥h�� 0�סB90�סB180�סB270�ת��I
		dx = 0;
		dy = radius;
		plotPoint(centerX, radius+centerY);
		plotPoint(radius+centerX,centerY);
		plotPoint(centerX-radius, centerY);
		plotPoint(centerX, centerY-radius);
	}
//****************************************************************���eEND

	initStartX  = sx;
	initStartY  = winHeight - sy;
	initDetecX  = xMouse;
	initDetecY  =  winHeight - yMouse;


	rateX = abs(initDetecX - initStartX);
	rateY = abs(initDetecY - initStartY);
	
	//���
	centerX = (int)((initDetecX+initStartX)/2.0);
	centerY = (int)((initDetecY+initStartY)/2.0);

	temp =  rateX*rateX +  rateY*rateY;
	radius = (int)(sqrt(temp)/2.0);//�b�|



	//�q(R,0)�}�l�e
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
			//��ߥ�(0,0)������(centerX,centerY)
			plotPoint(dx+centerX, dy+centerY);//90��~45��
			plotPoint(dy+centerX, dx+centerY);//0��~45��

			plotPoint(-dx+centerX, dy+centerY);//90��~135��
			plotPoint(-dy+centerX, dx+centerY);//180��~135��

			plotPoint(-dx+centerX, -dy+centerY);//270��~225��
			plotPoint(-dy+centerX, -dx+centerY);//180��~225��

			plotPoint(dx+centerX, -dy+centerY);//270�ס�315��
			plotPoint(dy+centerX, -dx+centerY);//0��~315��
	}


	//�ɤW�U�H���]XOR mode�Ӥ֥h��45�B135�B225�B315�ת��I
	plotPoint(dx+centerX, dy+centerY);
	plotPoint(dy+centerX, dx+centerY);

	plotPoint(-dx+centerX, dy+centerY);
	plotPoint(-dy+centerX, dx+centerY);

	plotPoint(-dx+centerX, -dy+centerY);
	plotPoint(-dy+centerX, -dx+centerY);

	plotPoint(dx+centerX, -dy+centerY);
	plotPoint(dy+centerX, -dx+centerY);


	//�ɤW�U�H���]XOR mode�Ӥ֥h�� 0�B90�B180�B270�ת��I
	dx = 0;
	dy = radius;
	plotPoint(centerX, radius+centerY);
	plotPoint(radius+centerX,centerY);
	plotPoint(centerX-radius, centerY);
	plotPoint(centerX, centerY-radius);

		
	//�O���U�̫�ƹ���X�BY�y��
	lbx = initStartX;
	lby = initStartY;
	lfx = initDetecX;
	lfy = initDetecY;
	flag = 1;//�����w�e��

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