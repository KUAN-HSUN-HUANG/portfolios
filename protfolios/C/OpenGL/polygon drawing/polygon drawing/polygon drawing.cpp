
#include<stdio.h>
#include<stdlib.h>
#include <memory.h>
#include "glut.h"
#include<math.h>
GLsizei winWidth = 800, winHeight = 600;


typedef struct point{
        int x;
        int y;
}POINT ;


typedef struct edge{
		POINT p1;
		POINT p2;
		int m[2];
		char c;
        int state;
        struct edge *next ;
}EDGE ;


typedef struct list{
	int count;
	EDGE *head;
	EDGE *rear;
}LIST;


typedef struct polygon{
	int count;
	LIST *list;
	polygon *next;
}PLG;


typedef struct triangle{
		EDGE e1;
		EDGE e2;
		EDGE e3;
        struct triangle *next ;
}TRG;


void init();
void displayFcn();
void winReshapeFcn(GLint newWidth, GLint newHeight);
void plotPoint(GLint x, GLint y);
void mousePtPlot(GLint button, GLint action, GLint xMouse, GLint yMouse);
void mouseLnPlot(GLint xMouse, GLint yMouse);
LIST *listLink(LIST *listhead, EDGE **current, int x1, int y1, int x2, int y2, int s);
int listDelete(LIST *listhead, int x1, int y1, int x2, int y2);
int lineInsert(LIST *listhead, int x1, int y1, int x2, int y2);
void find_beginning_to_end(LIST *listhead, EDGE **begin, EDGE **end, int x1, int y1, int x2, int y2);
void list_link_orphan(LIST *listhead);
PLG *polygon_link(PLG *polygon_head, LIST *listhead,  PLG **prvious);
TRG *TRGlink(TRG *head, TRG **prvious, TRG **current, POINT *p1, POINT *p2, POINT *p3, int s1, int s2, int s3);
PLG *division(PLG *polygon_head, LIST *listhead);
void PrintList(LIST *head);
void Printpolygon(LIST *head);
void PrintTRG(TRG *head);
int check_edge_same(EDGE *e1, LIST *head);
int check_triangle(int x1, int y1, int x2, int y2, int x3, int y3, EDGE *checkedhead);
int triangulation(LIST *srchead, TRG *trghead);
void sort(EDGE array[], int size);
LIST *sortpoint(LIST *list, int flag);
void fillcollor(TRG *trghead);
double findArea(int x1, int y1, int x2, int y2, int x3, int y3);
int cross(int x1, int y1, int x2, int y2, int x3, int y3);
PLG * clipping(LIST *listhead, PLG *polygonlist, int minY, int maxY, int minX, int maxX);
///////////////////////
PLG *polygonlist = NULL;
PLG *PLGcurrent = NULL;
EDGE *sourcehead = NULL;
TRG *trianglehead = NULL;
LIST *listhead = NULL;
EDGE *current = NULL;
EDGE *prvious = NULL;
TRG *TRGcurrent = NULL;
TRG *TRGprvious = NULL;
int sx, sy;
//int x1, y1;
int startX, startY;
int previousX, previousY;
int flag = 0;
int doneflag = 0;
int counti=0;
EDGE *loghead;




void init(){
	glClearColor(0.0,  0.0,  0.0,  1.0);

	glMatrixMode(GL_PROJECTION);
	gluOrtho2D(0.0,  200.0,  0.0,  150.0);
}




void displayFcn(){
	glClear(GL_COLOR_BUFFER_BIT);
	glColor3f(1.0,  1.0,  1.0);
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


PLG *polygon_link(PLG *polygon_head, LIST *listhead,  PLG **prvious){
	PLG *ptr;
	LIST *newlist;

	newlist = (LIST*)malloc(sizeof(LIST));
	newlist = listhead;
	memcpy(newlist, listhead, sizeof(listhead));
	if(polygon_head == NULL){
		polygon_head = (PLG*)malloc(sizeof(PLG));		
		polygon_head->list = newlist;
		polygon_head->next = NULL;
		polygon_head->count = 1;
		*prvious = polygon_head;
		return polygon_head;
	}
	else{
		ptr = (PLG*)malloc(sizeof(PLG));
		ptr->list = newlist;
		ptr->next = NULL;
		ptr->count++;
		(*prvious)->next = ptr;
		*prvious = ptr; 
	}
	return polygon_head;
}




LIST *listLink(LIST *listhead, EDGE **current, int x1, int y1, int x2, int y2, int s){
	EDGE *ptr;
	int dx, dy;
	if(listhead == NULL ){	//目前link-list沒有node 
		listhead = (LIST*)malloc(sizeof(LIST));
		ptr = (EDGE*)malloc(sizeof(EDGE));
		ptr->p1.x = x1;
		ptr->p1.y = y1;
		ptr->p2.x = x2;
		ptr->p2.y = y2;
		ptr->m[0] = abs(x1 - x2);
		ptr->m[1] = abs(y1 - y2);
		ptr->state = s;
		//判斷斜率是正或負
		if((x1 - x2 >0 && y1 - y2 >0) || (x1 - x2 <0 && y1 - y2 <0))	ptr->c = '+';
		else ptr->c = '-';		
		ptr->next = NULL;
		*current = ptr;
		listhead->head = ptr;
		listhead->rear = ptr;
		listhead->count = 1;
		return listhead;
	}
	else{
		ptr = (EDGE*)malloc(sizeof(EDGE));
		ptr->p1.x = x1;
		ptr->p1.y = y1;
		ptr->p2.x = x2;
		ptr->p2.y = y2;
		ptr->m[0] = abs(x1 - x2);
		ptr->m[1] = abs(y1 - y2);
		ptr->state = s;
		//判斷斜率是正或負
		if((x1 - x2 >0 && y1 - y2 >0) || (x1 - x2 <0 && y1 - y2 <0))	ptr->c = '+';
		else ptr->c = '-';
		ptr->next = listhead->head;
		listhead->rear = ptr;
		(*current)->next = ptr;
		*current = ptr;
		listhead->count++;
		return listhead;
	}
}



void find_beginning_to_end(LIST *listhead, EDGE **begin, EDGE **end, int x1, int y1, int x2, int y2){
	EDGE *ptr;
	EDGE *line;
	int i;
	double m1, m2, m3, m4;

	line = (EDGE*)malloc(sizeof(EDGE));
	ptr = listhead->head;
	prvious = NULL;
	
	for(i = 1; i <= listhead->count; i++){
		m1 = (double)(y1 - ptr->p1.y) / (double)(x1 - ptr->p1.x);
		m2 = (double)(y1 - ptr->p2.y) / (double)(x1 - ptr->p2.x);
		m3 = (double)(y2 - ptr->p2.y) / (double)(x2 - ptr->p2.x);
		m4 = (double)(y2 - ptr->p2.y) / (double)(x2 - ptr->p2.x);

		if((int)m1 == (int)m2)	*begin = ptr;
		if((int)m3 == (int)m4)	*end = ptr;

		ptr = ptr->next;
	}
}




int lineInsert(LIST *listhead, int x1, int y1, int x2, int y2){
	EDGE *ptr;
	EDGE *prvious;
	EDGE *current;
	EDGE *connect1, *connect2;
	EDGE *line;
	int i;
	int flag = 0;
	int find;

	line = (EDGE*)malloc(sizeof(EDGE));
	ptr = listhead->head;
	prvious = NULL;
	
	for(i = 1; i <= listhead->count; i++){
		if(ptr->p1.x == x1 && ptr->p1.y == y1){
			connect1 = ptr;
			flag = 1;
		}
		if(ptr->p2.x == x2 && ptr->p2.y == y2){
			connect2 = ptr;
		}
		if(ptr->p2.x == x1 && ptr->p2.y == y1){
			connect1 = ptr;
			flag = 2;
		}
		if(ptr->p1.x == x2 && ptr->p1.y == y2){
			connect2 = ptr;
		}
			prvious = ptr;
			ptr = ptr->next;
	}
	if(flag == 1){
		current = listhead->rear;
		listLink(listhead, &current, connect2->p2.x, connect2->p2.y, connect1->p1.x, connect1->p1.y, 3);
		PrintList(listhead);
		return 1;
	}
	else if(flag == 2){
		connect2->next = connect1;
		return 1;
	}
	else return 0;


}




int listDelete(LIST *listhead, int x1, int y1, int x2, int y2){
	EDGE *ptr;
	EDGE *prvious;
	int i;

	ptr = listhead->head;
	prvious = NULL;
	
	for(i = 1; i <= listhead->count; i++){
		if(ptr->p1.x == x1 && ptr->p1.y == y1 && ptr->p2.x == x2 && ptr->p2.y == y2){
			if(prvious == NULL){
				listhead->head = ptr->next;
				listhead->rear->next = listhead->head;
			}
			else if(ptr == listhead->rear){
				prvious->next = listhead->head;
				listhead->rear = prvious;
			}
			else{
				prvious->next = ptr->next;
			}
			free(ptr);
			listhead->count--;
			return 1;
		}
			prvious = ptr;
			ptr = ptr->next;
	}
	return 0;
}



void list_link_orphan(LIST *listhead){
	EDGE *ptr;
	EDGE *prvious;
	int i;

	ptr = listhead->head;
	prvious = NULL;
	for(i = 1; i <= listhead->count; i++){
		if(prvious != NULL){
			if((prvious->p2.x != ptr->p1.x) || (prvious->p2.y != ptr->p1.y)){
				prvious->p2.x = ptr->p1.x;
				prvious->p2.y = ptr->p1.y;
			}
		}
		prvious = ptr;
		ptr = ptr->next;
	}
}


EDGE *link(EDGE *head, EDGE **prvious, EDGE **current, int x1, int y1, int x2, int y2, int state){
	EDGE *temp ;

    if( head == NULL ){//目前link-list沒有node 
		head = (EDGE*)malloc( sizeof( EDGE ) );
		head ->p1.x = x1;
		head ->p1.y = y1;
		head ->p2.x = x2;
		head ->p2.y = y2;
		head -> state = 1;
		head -> next = NULL ;
		*current = head;
		*prvious = head;
		return head;
	}
	
	else{
		temp =	(EDGE*)malloc( sizeof( EDGE ) );
		temp ->p1.x = x1;
		temp ->p1.y = y1;
		temp ->p2.x = x2;
		temp ->p2.y = y2;
		temp -> state = 1;
		temp -> next = NULL ;
        (*current) -> next = temp;
        *prvious = *current;
        *current = temp;
		return head;
	}
}




TRG *TRGlink(TRG *head, TRG **prvious, TRG **current, POINT *p1, POINT *p2, POINT *p3, int s1, int s2, int s3){
	TRG *temp ;

    if( head == NULL ){//目前link-list沒有node 
		head = (TRG*)malloc( sizeof( TRG ) );
		head->e1.p1.x=p1->x;
		head->e1.p1.y=p1->y;
		head->e1.p2.x=p2->x;
		head->e1.p2.y=p2->y;
		head->e1.m[0] = abs(p1->x - p2->x);
		head->e1.m[1] = abs(p1->y - p2->y);
		head->e1.state=s1;
		//判斷斜率是正或負
		if(((p1->y - p2->y) >0 &&(p1->x - p2->x) >0) || ((p1->y - p2->y) <0 && (p1->x - p2->x) <0))	head->e1.c = '+';
		else head->e1.c = '-';

		head->e2.p1.x=p2->x;
		head->e2.p1.y=p2->y;
		head->e2.p2.x=p3->x;
		head->e2.p2.y=p3->y;
		head->e2.m[0] = abs(p2->x - p3->x);
		head->e2.m[1] = abs(p2->y - p3->y);
		head->e2.state=s2;
		//判斷斜率是正或負
		if(((p2->y - p3->y) >0 &&(p2->x - p3->x) >0) || ((p2->y - p3->y) <0 && (p2->x - p3->x) <0))	head->e2.c = '+';
		else head->e2.c = '-';

		head->e3.p1.x=p3->x;
		head->e3.p1.y=p3->y;
		head->e3.p2.x=p1->x;
		head->e3.p2.y=p1->y;
		head->e3.m[0] = abs(p3->x - p1->x);
		head->e3.m[1] = abs(p3->y - p1->y);
		head->e3.state=s3;
		//判斷斜率是正或負
		if(((p3->y - p1->y) >0 &&(p3->x - p1->x) >0) || ((p3->y - p1->y) <0 && (p3->x - p1->x) <0))	head->e3.c = '+';
		else head->e3.c = '-';
		

		head -> next = NULL ;
		*current = head;
		*prvious = head;
		return head;
	}
	else{
		temp =	(TRG*)malloc( sizeof( TRG ) );
		temp->e1.p1.x=p1->x;
		temp->e1.p1.y=p1->y;
		temp->e1.p2.x=p2->x;
		temp->e1.p2.y=p2->y;
		temp->e1.m[0] = abs(p1->x - p2->x);
		temp->e1.m[1] = abs(p1->y - p2->y);
		temp->e1.state=s1;
		//判斷斜率是正或負
		if(((p1->y - p2->y) >0 &&(p1->x - p2->x) >0) || ((p1->y - p2->y) <0 && (p1->x - p2->x) <0))	temp->e1.c = '+';
		else temp->e1.c = '-';

		temp->e2.p1.x=p2->x;
		temp->e2.p1.y=p2->y;
		temp->e2.p2.x=p3->x;
		temp->e2.p2.y=p3->y;
		temp->e2.m[0] = abs(p2->x - p3->x);
		temp->e2.m[1] = abs(p2->y - p3->y);
		temp->e2.state=s2;
		//判斷斜率是正或負
		if(((p2->y - p3->y) >0 &&(p2->x - p3->x) >0) || ((p2->y - p3->y) <0 && (p2->x - p3->x) <0))	temp->e2.c = '+';
		else temp->e2.c = '-';
		
		temp->e3.p1.x=p3->x;
		temp->e3.p1.y=p3->y;
		temp->e3.p2.x=p1->x;
		temp->e3.p2.y=p1->y;
		temp->e3.m[0] = abs(p3->x - p1->x);
		temp->e3.m[1] = abs(p3->y - p1->y);
		temp->e3.state=s3;
		//判斷斜率是正或負
		if(((p3->y - p1->y) >0 &&(p3->x - p1->x) >0) || ((p3->y - p1->y) <0 && (p3->x - p1->x) <0))	temp->e3.c = '+';
		else temp->e3.c = '-';
		

		temp -> next = NULL ;
        (*current) -> next = temp;
        *prvious = *current;
        *current = temp;
		return head;
	}
}




void PrintList(LIST *head){
	int i ;
	EDGE *ptr ;

	if(head != NULL){
		ptr = head->head;
		for(i=1;; i++){
			if(ptr == head->rear)	break;
			ptr = ptr->next;
		}
	}

}

void Printpolygon(PLG *listhead){
	int i, j;
	LIST *listptr;
	EDGE *ptr;
	PLG *PLGptr;
	
	printf("Printpolygon start\n");
	if(listhead){
		listptr = listhead->list;
		PLGptr = listhead;
		for(j=1;;j++){
			ptr = PLGptr->list->head;
			for(i=1; i <= PLGptr->list->count; i++){
				if(ptr->state == 1)	glColor3f(0.0,  1.0,  0.0);
					

				ptr = ptr->next;
			}
			PLGptr = PLGptr->next;
			if(PLGptr == NULL)	break;
		}
		printf("Printpolygon finish\n");
	}
}


void PrintTRG(TRG *head){
	int i ;
	TRG *ptr ;

	ptr = head;
	for(i = 1; ptr != NULL; i++){		
			if(ptr->e1.state == 0){
				glColor3f(1.0,  0.0,  0.0);
				glBegin(GL_LINES);
					glVertex2i(ptr->e1.p1.x, ptr->e1.p1.y);
					glVertex2i(ptr->e1.p2.x, ptr->e1.p2.y);
				glEnd();			
			}
		if(ptr->e2.state == 0){
			glColor3f(1.0,  0.0,  0.0);
			glBegin(GL_LINES);
				glVertex2i(ptr->e2.p1.x, ptr->e2.p1.y);
				glVertex2i(ptr->e2.p2.x, ptr->e2.p2.y);
			glEnd();
		}
		if(ptr->e3.state == 0){
			glColor3f(1.0,  0.0,  0.0);
			glBegin(GL_LINES);
				glVertex2i(ptr->e3.p1.x, ptr->e3.p1.y);
				glVertex2i(ptr->e3.p2.x, ptr->e3.p2.y);
			glEnd();
		}
		ptr = ptr-> next;
	}
}




int cross(int x1, int y1, int x2, int y2, int x3, int y3){
	int v1x = x2 - x1;
	int v1y = y2 - y1;
	int v2x = x3 - x2;
	int v2y = y3 - y2;


	if( (v1x * v2y - v2x * v1y) >0)	return 1;
	else return 0;
}




double findArea(int x1, int y1, int x2, int y2, int x3, int y3){
       double e1, e2, e3;
       double S;

        e1 = sqrt((double)((x1-x2)*(x1-x2) + (y2-y1)*(y2-y1)));
        e2 = sqrt((double)((x3-x2)*(x3-x2) + (y3-y2)*(y3-y2)));
        e3 = sqrt((double)((x3-x1)*(x3-x1) + (y3-y1)*(y3-y1)));
        S = (e1+e2+e3)/2.0;
        
    return sqrt(S*(S-e1)*(S-e2)*(S-e3));
}



//邊重複回傳1，否則回傳0
int check_edge_same(EDGE *e1, LIST *head){
	EDGE *ptr ;

	ptr = head->head;
	for(;;){
		if((e1->p1.x == ptr->p1.x) && (e1->p1.y == ptr->p1.y) && (e1->p2.x == ptr->p2.x) && (e1->p2.y == ptr->p2.y))	return 1;
		ptr = ptr-> next;
		if(ptr == head->head)	break;
	}
	return 0;
}




int check_triangle(int x1, int y1, int x2, int y2, int x3, int y3, LIST *checkedhead){
EDGE *ptr;
int i;
int prex=NULL, prey=NULL;
double a1, a2, a3, suma;
int dx, dy;
char m;


ptr = checkedhead->head;

if(cross(x1, y1, x2, y2, x3, y3) == 0){
	return 0;
}
for(i=1;;i++){
	if((ptr->p1.x != x1 && ptr->p1.y != y1) && (ptr->p1.x != x2 && ptr->p1.y != y2) && (ptr->p1.x != x3 && ptr->p1.y != y3)){
		prex = ptr->p1.x;
		prey = ptr->p1.y;
		suma = findArea(x1, y1, x2, y2, x3, y3);
		a1 = findArea(ptr->p1.x, ptr->p1.y, x2, y2, x3, y3);
		a2 = findArea(x1, y1, ptr->p1.x, ptr->p1.y, x3, y3);
		a3 = findArea(x1, y1, x2, y2, ptr->p1.x, ptr->p1.y);
		if((int)(a1+a2+a3) == (int)suma || (int)(a1+a2+a3) == (int)suma+1 || (int)(a1+a2+a3) == (int)suma-1){
			return 0;
		}
	}
	ptr = ptr->next;
	if(ptr == checkedhead->head)	break;
}
return 1;
}


int triangulation(LIST *srchead, TRG *trghead){
	int s1, s2, s3;
	POINT *TRGp1, *TRGp2, *TRGp3 ,*TRGtemp;
	EDGE *ptr ;
	EDGE *check_edge_ptr;
	LIST *check_point_list;
	LIST *originallist;
	EDGE *cpptr;


	TRGp1 = NULL;
	TRGp2 = NULL;
	TRGp3 = NULL;
	originallist = (LIST*)malloc(sizeof(LIST));
	memcpy(originallist, srchead, sizeof(srchead));
	check_edge_ptr = (EDGE*)malloc(sizeof(EDGE));
	check_point_list = NULL;
	cpptr = srchead->head;
	
	for(;;){
		check_point_list = listLink(check_point_list, &current, cpptr->p1.x, cpptr->p1.y, cpptr->p2.x, cpptr->p2.y, 1);
		cpptr = cpptr->next;
		if(cpptr == srchead->head)	break;
	}
	ptr = srchead->head;


	while(1){
		if(TRGp1 != NULL){
			while(1){
				TRGp2->x= TRGp3->x;
				TRGp2->y= TRGp3->y;
				ptr = ptr-> next;	
				TRGp3->x= ptr->p1.x;
				TRGp3->y= ptr->p1.y;

				while(check_triangle(TRGp1->x, TRGp1->y, TRGp2->x, TRGp2->y, TRGp3->x, TRGp3->y, check_point_list) == 0){
					if(srchead->count == 3){
						
						TRGp1->x = srchead->head->p1.x;
						TRGp1->y = srchead->head->p1.y;
						TRGp2->x = srchead->head->next->p1.x;
						TRGp2->y = srchead->head->next->p1.y;
						TRGp3->x = srchead->rear->p1.x;
						TRGp3->y = srchead->rear->p1.y;
						if(check_triangle(TRGp1->x, TRGp1->y, TRGp2->x, TRGp2->y, TRGp3->x, TRGp3->y, check_point_list) != 0){
							//檢查三角形第一條邊
							check_edge_ptr->p1.x = srchead->head->p1.x;
							check_edge_ptr->p1.y = srchead->head->p1.y;
							check_edge_ptr->p2.x = srchead->head->next->p1.x;
							check_edge_ptr->p2.y = srchead->head->next->p1.y;
							s1 = check_edge_same(check_edge_ptr, srchead);
							//檢查三角形第二條邊
							check_edge_ptr->p1.x = srchead->head->next->p1.x;
							check_edge_ptr->p1.y = srchead->head->next->p1.y;
							check_edge_ptr->p2.x = srchead->rear->p1.x;
							check_edge_ptr->p2.y = srchead->rear->p1.y;
							s2 = check_edge_same(check_edge_ptr, srchead);

							//檢查三角形第三條邊
							check_edge_ptr->p1.x = srchead->rear->p1.x;
							check_edge_ptr->p1.y = srchead->rear->p1.y;
							check_edge_ptr->p2.x = srchead->head->p1.x;
							check_edge_ptr->p2.y = srchead->head->p1.y;
							s3 = check_edge_same(check_edge_ptr, srchead);
							trianglehead = TRGlink(trianglehead, &TRGprvious, &TRGcurrent, TRGp1, TRGp2, TRGp3, s1, s2, s3);
						}
						listDelete(listhead, TRGp2->x, TRGp2->y, TRGp3->x, TRGp3->y);
						return 0;	
					}
					else if(srchead->count < 3)	return 0;
					else{
						PrintList(srchead);
						TRGp1->x = TRGp2->x;
						TRGp1->y = TRGp2->y;
						TRGp2->x = TRGp3->x;
						TRGp2->y = TRGp3->y;
						ptr = ptr-> next;
						TRGp3->x = ptr->p1.x;
						TRGp3->y = ptr->p1.y;
					}
				}
				break;
			}
		}
		else{
			TRGp1 = (POINT*)malloc(sizeof(POINT));
			TRGp2 = (POINT*)malloc(sizeof(POINT));
			TRGp3 = (POINT*)malloc(sizeof(POINT));
			TRGp1->x = ptr->p1.x;
			TRGp1->y = ptr->p1.y;
			ptr = ptr-> next;
			TRGp2->x = ptr->p1.x;
			TRGp2->y = ptr->p1.y;
			ptr = ptr-> next;
			TRGp3->x = ptr->p1.x;
			TRGp3->y = ptr->p1.y;
			if(srchead->count == 3){
				trianglehead = TRGlink(trianglehead, &TRGprvious, &TRGcurrent, TRGp1, TRGp2, TRGp3, 1, 1, 1);
				listDelete(srchead, TRGp2->x, TRGp2->y, TRGp3->x, TRGp3->y);
				return 0;
			}
			while(check_triangle(TRGp1->x, TRGp1->y, TRGp2->x, TRGp2->y, TRGp3->x, TRGp3->y, check_point_list) == 0){
				TRGp1->x = TRGp2->x;
				TRGp1->y = TRGp2->y;
				TRGp2->x = TRGp3->x;
				TRGp2->y = TRGp3->y;
				ptr = ptr-> next;
				TRGp3->x = ptr->p1.x;
				TRGp3->y = ptr->p1.y;
				//找下一個三角形，直到沒有點在此三角形內部為止
			}
		}

		//檢查三角形第一條邊(此邊是外框或內框)
		check_edge_ptr->p1.x = TRGp1->x;
		check_edge_ptr->p1.y = TRGp1->y;
		check_edge_ptr->p2.x = TRGp2->x;
		check_edge_ptr->p2.y = TRGp2->y;
		s1 = check_edge_same(check_edge_ptr, srchead);

		//檢查三角形第二條邊(此邊是外框或內框)
		check_edge_ptr->p1.x = TRGp2->x;
		check_edge_ptr->p1.y = TRGp2->y;
		check_edge_ptr->p2.x = TRGp3->x;	
		check_edge_ptr->p2.y = TRGp3->y;
		s2 = check_edge_same(check_edge_ptr, srchead);

		//檢查三角形第三條邊(此邊是外框或內框)
		check_edge_ptr->p1.x = TRGp3->x;
		check_edge_ptr->p1.y = TRGp3->y;
		check_edge_ptr->p2.x = TRGp1->x;
		check_edge_ptr->p2.y = TRGp1->y;
		s3 = check_edge_same(check_edge_ptr, srchead);
		
		//加入三角形linked-list內
		trianglehead = TRGlink(trianglehead, &TRGprvious, &TRGcurrent, TRGp1, TRGp2, TRGp3, s1, s2, s3);
		listDelete(srchead, TRGp2->x, TRGp2->y, TRGp3->x, TRGp3->y);
		list_link_orphan(srchead);
	}

	free(check_point_list);
	return 1;
}




LIST *sortpoint(LIST *list, char flag){
	EDGE* ptr;
	EDGE* preptr;
	EDGE* temp;
	int i, j;


	if(flag == 'x'){
		for(j = 0; j <= list->count; j++){
			ptr = list->head;
			preptr = NULL;
			for(i = 0; i <= list->count; i++){
				if(preptr != NULL){
					if(preptr->p1.x >= ptr->p1.x){
						temp = (EDGE*)malloc(sizeof(EDGE));
						temp->p1.x = preptr->p1.x;
						temp->p1.y = preptr->p1.y;
						preptr->p1.x = ptr->p1.x;
						preptr->p1.y = ptr->p1.y;
						ptr->p1.x = temp->p1.x;
						ptr->p1.y = temp->p1.y;
					}
				}
				preptr = ptr;
				ptr = ptr->next;
				PrintList(list);
				if(ptr == list->head)	break;
			}
		}
	}
printf("sortpoint finish\n");
return list;
}

void sort(EDGE array[], int size, int flag){
	EDGE temp;

	if(flag == 0){
		for(int i = 0; i<size-1; i++){
			if(array[i].p1.x > array[i+1].p1.x){
				temp = array[i];
				array[i] = array[i+1];
				array[i+1] = temp;
			}
			if(array[size-1-i].p1.x < array[size-2-i].p1.x){
				temp = array[size-1-i];
				array[size-1-i] = array[size-2-i];
				array[size-2-i] = temp;
			}
		}
	}
	else{
		for(int i = 0; i<size-1; i++){
			if(array[i].p1.y > array[i+1].p1.y){
				temp = array[i];
				array[i] = array[i+1];
				array[i+1] = temp;
			}
			if(array[size-1-i].p1.y < array[size-2-i].p1.y){
				temp = array[size-1-i];
				array[size-1-i] = array[size-2-i];
				array[size-2-i] = temp;
			}
		}

	}

}


PLG * clipping(LIST *listhead, PLG *polygonlist, int minY, int maxY, int minX, int maxX){
	int i;
	int dx, dy;
	double x1, y1, x2, y2, X1, X2, Y;
	int flag1 = 0, flag2 = 0;
	EDGE *pair_ptr1, *pair_preptr1;
	EDGE *pair_ptr2, *pair_preptr2;
	EDGE *preptr;
	EDGE *ptr;
	EDGE *beginptr, *endptr, *deleteptr, *predeleteptr;
	LIST *begin_end_list;
	EDGE *begin_end_current;


	LIST *connectlist1;
	EDGE *connectcurrent1;
	LIST *connectlist2;
	EDGE *connectcurrent2;
	LIST *datalist;
	EDGE *datacurrent;
	

	ptr = listhead->head;
	datalist = NULL;
	connectlist1 = NULL;
	connectcurrent1 = NULL;
	connectlist2 = NULL;
	connectcurrent2 = NULL;

	begin_end_list = NULL;
	begin_end_current = NULL;


	for(i=1; i<= listhead->count; i++){
		if((ptr->p1.y < maxY && ptr->p2.y > maxY) || (ptr->p1.y > maxY && ptr->p2.y < maxY)){
			Y = maxY;
			x1 = (double)(ptr->p1.x);
			y1 = (double)(ptr->p1.y);
			x2 = (double)(ptr->p2.x);
			y2 = (double)(ptr->p2.y);
			X1 = (x2*Y - x1*Y - x2*y1 + x1*y2) / (y2 - y1);
			connectlist1 = listLink(connectlist1, &connectcurrent1, (int)X1, maxY, -1, -1, 3);
			flag1 = 1;
		}
		if((ptr->p1.y < minY && ptr->p2.y > minY) || (ptr->p1.y > minY && ptr->p2.y < minY)){
			Y = minY;
			x1 = (double)(ptr->p1.x);
			y1 = (double)(ptr->p1.y);
			x2 = (double)(ptr->p2.x);
			y2 = (double)(ptr->p2.y);
			X2 = (x2*Y - x1*Y - x2*y1 + x1*y2) / (y2 - y1);
			connectlist2 = listLink(connectlist2, &connectcurrent2, (int)X2, minY, -1, -1, 3);
			flag2 = 1;
		}
			ptr = ptr->next;
			flag1 = 0;
			flag2 = 0;
	}

		printf("before sorting\n");
		PrintList(connectlist1);
		PrintList(connectlist2);
		if(connectlist1)		connectlist1 = sortpoint(connectlist1,'x');
		if(connectlist2)		connectlist2 = sortpoint(connectlist2,'x');
		printf("after sorting\n");
		PrintList(connectlist1);
		PrintList(connectlist2);

		glClear(GL_COLOR_BUFFER_BIT);
		glFlush();


		if(!connectlist1 && !connectlist2){
				polygonlist = polygon_link(polygonlist, listhead, &PLGcurrent);
				Printpolygon(polygonlist);
				glFlush();

				return polygonlist;
		}

		if(connectlist1->count == connectlist2->count){
			pair_preptr1 = connectlist1->head;
			pair_ptr1 = pair_preptr1->next;
			pair_preptr2 = connectlist2->head;
			pair_ptr2 = pair_preptr2->next;
			for(i=2;i <= connectlist1->count; i = i*2){
				datalist = listLink(datalist, &datacurrent, pair_preptr1->p1.x, pair_preptr1->p1.y, pair_preptr2->p1.x, pair_preptr2->p1.y, 1);
				datalist = listLink(datalist, &datacurrent, pair_preptr2->p1.x, pair_preptr2->p1.y, pair_ptr2->p1.x, pair_ptr2->p1.y, 3);
				datalist = listLink(datalist, &datacurrent, pair_ptr2->p1.x, pair_ptr2->p1.y, pair_ptr1->p1.x, pair_ptr1->p1.y, 1);
				datalist = listLink(datalist, &datacurrent, pair_ptr1->p1.x, pair_ptr1->p1.y, pair_preptr1->p1.x, pair_preptr1->p1.y, 3);

				PrintList(datalist);
				polygonlist = polygon_link(polygonlist, datalist, &PLGcurrent);
				Printpolygon(polygonlist);
				glFlush();
				system("pause");
				datalist = NULL;
				datacurrent = NULL;
				if(pair_ptr1->next == NULL)	break;
				pair_preptr1 = pair_ptr1->next;
				pair_ptr1 = pair_ptr1->next->next;
				pair_preptr2 = pair_ptr2->next;
				pair_ptr2 = pair_ptr2->next->next;

			}
}


return polygonlist;
}



void fillcollor(TRG *trghead){
	int dy;
	int i, j;
	int cs;
	TRG *ptr;
	EDGE arrayX[3];
	EDGE arrayY[3];
	int leftX[5];
	int rightX[5];
	int lfstate, rtstate;
	
	int drawrightX, drawleftX;


	ptr = trghead;
	for(i=1;ptr!= NULL;i++){
		arrayX[0] = ptr->e1;
		arrayX[1] = ptr->e2;
		arrayX[2] = ptr->e3;
		arrayY[0] = ptr->e1;
		arrayY[1] = ptr->e2;
		arrayY[2] = ptr->e3;
		sort(arrayX, 3, 0);
		sort(arrayY, 3, 1);

		cs = cross(arrayY[0].p1.x, arrayY[0].p1.y, arrayY[1].p1.x, arrayY[1].p1.y, arrayY[2].p1.x, arrayY[2].p1.y);
		//printf("cross = %d\n", cs);

		if(arrayY[0].p1.y == arrayY[1].p1.y){
			if(arrayY[0].p1.x > arrayY[2].p1.x){
				rightX[0] = arrayY[0].p1.x;
				rightX[1] = 0;
				rightX[2] = arrayY[0].m[0];
				rightX[3] = arrayY[0].m[1];
				if(arrayY[0].state == 1) rtstate = 1;
				else	rtstate = 0;

				if(arrayY[0].c == '+')	rightX[4]  = 1;
				else rightX[4]  = -1;

				leftX[0] = arrayY[1].p1.x;
				leftX[1] = 0;
				leftX[2] = arrayY[2].m[0];
				leftX[3] = arrayY[2].m[1];
				if(arrayY[2].state == 1) lfstate = 1;
				else	lfstate = 0;

				if(arrayY[2].c == '+')	leftX[4]  = 1;
				else leftX[4]  = -1;	
			}
			else{
				rightX[0] = arrayY[1].p1.x;
				rightX[1] = 0;
				rightX[2] = arrayY[1].m[0];
				rightX[3] = arrayY[1].m[1];
				if(arrayY[1].state == 1) rtstate = 1;
				else	rtstate = 0;

				if(arrayY[1].c == '+')	rightX[4]  = 1;
				else rightX[4]  = -1;

				leftX[0] = arrayY[0].p1.x;
				leftX[1] = 0;
				leftX[2] = arrayY[2].m[0];
				leftX[3] = arrayY[2].m[1];
				if(arrayY[2].state == 1) lfstate = 1;
				else	lfstate = 0;

				if(arrayY[2].c == '+')	leftX[4]  = 1;
				else leftX[4]  = -1;	
			}
			for(dy = arrayY[0].p1.y + 1; dy < arrayY[2].p1.y; dy++){
				if(rtstate == 1){
					drawrightX = rightX[0]-1;
				}
				else{
					drawrightX = rightX[0];
				}
				if(lfstate == 1){ 
					drawleftX = leftX[0]+1;
				}
				else{
					drawleftX = leftX[0];
				}
				glColor3f(1.0,  1.0,  1.0);
				glBegin(GL_LINES);
					glVertex2i(leftX[0],  dy);
					glVertex2i(rightX[0], dy);
				glEnd();

				leftX[1] = leftX[1] + leftX[2];
				while(1){
					if(leftX[1] >= leftX[3]){
						leftX[1] = leftX[1] - leftX[3];
						leftX[0] = leftX[0] + leftX[4];
					}
					else break;
				}
				rightX[1] = rightX[1] + rightX[2];
				while(1){
					if(rightX[1] >= rightX[3]){
						rightX[1] = rightX[1] - rightX[3];
						rightX[0] = rightX[0] + rightX[4];
					}
					else break;
				}
			}
		}
		else if(arrayY[2].p1.y == arrayY[1].p1.y){
			if(arrayY[1].p1.x > arrayY[2].p1.x){
				rightX[0] = arrayY[0].p1.x;
				rightX[1] = 0;
				rightX[2] = arrayY[0].m[0];
				rightX[3] = arrayY[0].m[1];
				if(arrayY[0].state == 1) rtstate = 1;
				else	rtstate = 0;

				if(arrayY[0].c == '+')	rightX[4]  = 1;
				else rightX[4]  = -1;

				leftX[0] = arrayY[0].p1.x;
				leftX[1] = 0;
				leftX[2] = arrayY[2].m[0];
				leftX[3] = arrayY[2].m[1];
				if(arrayY[2].state == 1) lfstate = 1;
				else	lfstate = 0;

				if(arrayY[2].c == '+')	leftX[4]  = 1;
				else leftX[4]  = -1;	
			}
			else{
				rightX[0] = arrayY[0].p1.x;
				rightX[1] = 0;
				rightX[2] = arrayY[0].m[0];
				rightX[3] = arrayY[0].m[1];
				if(arrayY[0].state == 1) rtstate = 1;
				else	rtstate = 0;

				if(arrayY[0].c == '+')	rightX[4]  = 1;
				else rightX[4]  = -1;

				leftX[0] = arrayY[0].p1.x;
				leftX[1] = 0;
				leftX[2] = arrayY[1].m[0];
				leftX[3] = arrayY[1].m[1];
				if(arrayY[1].state == 1) lfstate = 1;
				else	lfstate = 0;

				if(arrayY[1].c == '+')	leftX[4]  = 1;
				else leftX[4]  = -1;	
			}
			leftX[1] = leftX[1] + leftX[2];
			while(1){
				if(leftX[1] >= leftX[3]){
					leftX[1] = leftX[1] - leftX[3];
					leftX[0] = leftX[0] + leftX[4];
				}
				else break;
			}
			rightX[1] = rightX[1] + rightX[2];
			while(1){
				if(rightX[1] >= rightX[3]){
					rightX[1] = rightX[1] - rightX[3];
					rightX[0] = rightX[0] + rightX[4];
				}
				else break;
			}
			for(dy = arrayY[0].p1.y + 1; dy < arrayY[2].p1.y; dy++){
				if(rtstate == 1){
					drawrightX = rightX[0]-1;
				}
				else{
					drawrightX = rightX[0];
				}
				if(lfstate == 1){ 
					drawleftX = leftX[0]+1;
				}
				else{
					drawleftX = leftX[0];
				}
				glColor3f(1.0,  1.0,  1.0);
				glBegin(GL_LINES);
					glVertex2i(leftX[0],  dy);
					glVertex2i(rightX[0], dy);
				glEnd();

				leftX[1] = leftX[1] + leftX[2];
				while(1){
					if(leftX[1] >= leftX[3]){
						leftX[1] = leftX[1] - leftX[3];
						leftX[0] = leftX[0] + leftX[4];
					}
					else break;
				}
				rightX[1] = rightX[1] + rightX[2];
				while(1){
					if(rightX[1] >= rightX[3]){
						rightX[1] = rightX[1] - rightX[3];
						rightX[0] = rightX[0] + rightX[4];
					}
					else break;
				}
			}
		}
		else{
		if((arrayY[0].p1.x > arrayY[1].p1.x && arrayY[0].p1.x > arrayY[2].p1.x  && cs == 1) || 
			(arrayY[1].p1.x > arrayY[0].p1.x && arrayY[1].p1.x > arrayY[2].p1.x  && cs == 1)||
			(arrayY[2].p1.x > arrayY[0].p1.x && arrayY[2].p1.x > arrayY[1].p1.x  && cs == 1)){
				rightX[0] = arrayY[0].p1.x;
				rightX[1] = 0;
				rightX[2] = arrayY[0].m[0];
				rightX[3] = arrayY[0].m[1];
				if(arrayY[0].state == 1) rtstate = 1;
				else	rtstate = 0;

				if(arrayY[0].c == '+')	rightX[4]  = 1;
				else rightX[4]  = -1;

				leftX[0] = arrayY[0].p1.x;
				leftX[1] = 0;
				leftX[2] = arrayY[2].m[0];
				leftX[3] = arrayY[2].m[1];
				if(arrayY[2].state == 1) lfstate = 1;
				else	lfstate = 0;

				if(arrayY[2].c == '+')	leftX[4]  = 1;
				else leftX[4]  = -1;			
		}
		else{
				rightX[0] = arrayY[0].p1.x;
				rightX[1] = 0;
				rightX[2] = arrayY[0].m[0];
				rightX[3] = arrayY[0].m[1];
				if(arrayY[0].state == 1) rtstate = 1;
				else	rtstate = 0;
				if(arrayY[0].c == '+')	rightX[4]  = 1;
				else rightX[4]  = -1;
				leftX[0] = arrayY[0].p1.x;
				leftX[1] = 0;
				leftX[2] = arrayY[1].m[0];
				leftX[3] = arrayY[1].m[1];
				if(arrayY[1].state == 1) lfstate = 1;
				else	lfstate = 0;
				if(arrayY[1].c == '+')	leftX[4]  = 1;
				else leftX[4]  = -1;	
		}
		
			leftX[1] = leftX[1] + leftX[2];
			while(1){
				if(leftX[1] >= leftX[3]){
					leftX[1] = leftX[1] - leftX[3];
					leftX[0] = leftX[0] + leftX[4];
				}
				else break;
			}
			rightX[1] = rightX[1] + rightX[2];
			while(1){
				if(rightX[1] >= rightX[3]){
					rightX[1] = rightX[1] - rightX[3];
					rightX[0] = rightX[0] + rightX[4];
				}
				else break;
			}

		for(dy = arrayY[0].p1.y; dy < arrayY[1].p1.y; dy++){
			if(rtstate == 1){
				drawrightX = rightX[0]-1;
			}
			else{
				drawrightX = rightX[0];
			}
			if(lfstate == 1){ 
				drawleftX = leftX[0]+1;
			}
			else{
				drawleftX = leftX[0];
			}
			glColor3f(1.0,  1.0,  1.0);
			glBegin(GL_LINES);
				glVertex2i(leftX[0],  dy);
				glVertex2i(rightX[0], dy);
			glEnd();

			leftX[1] = leftX[1] + leftX[2];
			while(1){
				if(leftX[1] >= leftX[3]){
					leftX[1] = leftX[1] - leftX[3];
					leftX[0] = leftX[0] + leftX[4];
				}
				else break;
			}
			rightX[1] = rightX[1] + rightX[2];
			while(1){
				if(rightX[1] >= rightX[3]){
					rightX[1] = rightX[1] - rightX[3];
					rightX[0] = rightX[0] + rightX[4];
				}
				else break;
			}
			for(j=0; j<2; j++)	glFlush();

		}


		if((arrayY[0].p1.x > arrayY[1].p1.x && arrayY[0].p1.x > arrayY[2].p1.x  && cs == 1) || 
			(arrayY[1].p1.x > arrayY[0].p1.x && arrayY[1].p1.x > arrayY[2].p1.x  && cs == 1)||
			(arrayY[2].p1.x > arrayY[0].p1.x && arrayY[2].p1.x > arrayY[1].p1.x  && cs == 1)){
				rightX[0] = arrayY[1].p1.x;
				rightX[1] = 0;
				rightX[2] = arrayY[1].m[0];
				rightX[3] = arrayY[1].m[1];
				if(arrayY[1].state == 1) rtstate = 1;
				else	rtstate = 0;
				if(arrayY[1].c == '+')	rightX[4]  = 1;
				else rightX[4]  = -1;
		}
		else{
				leftX[0] = arrayY[1].p1.x;
				leftX[1] = 0;
				leftX[2] = arrayY[2].m[0];
				leftX[3] = arrayY[2].m[1];
				if(arrayY[2].state == 1) lfstate = 1;
				else	lfstate = 0;
				if(arrayY[2].c == '+')	leftX[4]  = 1;
				else leftX[4]  = -1;
		}

		
		for(dy = arrayY[1].p1.y; dy < arrayY[2].p1.y; dy++){
			if(rtstate == 1){
				drawrightX = rightX[0]-1;
			}
			else{
				drawrightX = rightX[0];
			}
			if(lfstate == 1){ 
				drawleftX = leftX[0]+1;
			}
			else{
				drawleftX = leftX[0];
			}
			glColor3f(1.0,  1.0,  1.0);
			glBegin(GL_LINES);
				glVertex2i(leftX[0],  dy);
				glVertex2i(rightX[0], dy);
			glEnd();

			leftX[1] = leftX[1] + leftX[2];
			while(1){
				if(leftX[1] > leftX[3]){
					leftX[1] = leftX[1] - leftX[3];
					leftX[0] = leftX[0] + leftX[4];
				}
				else break;
			}
			rightX[1] = rightX[1] + rightX[2];
			while(1){
				if(rightX[1] > rightX[3]){
					rightX[1] = rightX[1] - rightX[3];
					rightX[0] = rightX[0] + rightX[4];
				}
				else break;
			}
			for(j=0; j<2; j++)	glFlush();
		}	
		}
		ptr = ptr-> next;
	}
	for(j=0; j<10; j++)	glFlush();
}




void mousePtPlot(GLint button, GLint action, GLint xMouse, GLint yMouse){
	EDGE *ptr;
	int j=0;
	PLG *PLGptr;

	if(flag == 9) return;

	//按下左鍵
	if(button == GLUT_LEFT_BUTTON && action == GLUT_DOWN){
		//第二個點
		if(flag == 1){
			//存入邊
			listhead = listLink(listhead, &current, previousX, previousY, xMouse, winHeight - yMouse, 1);
			counti++;
			//畫出外框
			
			glColor3f(0.0,  1.0,  0.0);
			glBegin(GL_LINES);
				glVertex2i(previousX,  previousY);
				glVertex2i(xMouse,	winHeight - yMouse);
			glEnd();
		}
		//第一個點
		if(flag == 0){
			//紀錄起始點
			startX = xMouse;
			startY = winHeight - yMouse;
			flag = 1;
		}
		//紀錄上一個點

		else if(flag == 4){

			printf("triangle start\n");
			PLGptr = polygonlist;
			
			for(j=1;;j++){
				triangulation(PLGptr->list, trianglehead);
				PLGptr = PLGptr->next;
				if(PLGptr == NULL)	break;
			}
			PrintTRG(trianglehead);
			glFlush();

			printf("triangle finish\n");
			flag = 5;

		}
		else if(flag == 5){
			printf("drawing start\n");
			fillcollor(trianglehead);
			printf("drawing finish\n");
			flag = 9;
		}
		if(flag != 5 || flag != 9){
			previousX = xMouse;
			previousY = winHeight - yMouse;
			plotPoint(xMouse,  winHeight - yMouse);
		}
		if(flag == 3)	flag = 4;
	}
	//按下右鍵
	if(button == GLUT_RIGHT_BUTTON && action == GLUT_DOWN && doneflag == 0){
		//把最後一個點和起始點連接
		listhead = listLink(listhead, &current, previousX, previousY,startX, startY, 1);
		polygonlist = polygon_link(polygonlist, listhead, &PLGcurrent);
			glColor3f(0.0,  1.0,  0.0);
			glBegin(GL_LINES);
				glVertex2i(previousX,  previousY);
				glVertex2i(startX,	startY);
			glEnd();		
		flag = 4;
		//畫出外框
		glFlush();
		printf("draw contour finish\n");
		doneflag = 1;
	}
	glFlush();
}






void mouseLnPlot(GLint xMouse, GLint yMouse){
	plotPoint(xMouse,  winHeight - yMouse);
	glFlush();
}



void main(int argc, char **argv){
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_SINGLE | GLUT_RGB);
	glutInitWindowPosition(100, 100);
	glutInitWindowSize(winWidth, winHeight);
	glutCreateWindow("Mouse Plot Points");

	init();
	glutDisplayFunc(displayFcn);
	glutReshapeFunc(winReshapeFcn);
	glutMouseFunc(mousePtPlot);

	glutMainLoop();
}