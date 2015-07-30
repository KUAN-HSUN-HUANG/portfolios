/*
 * This Class is a Structure List for Exist Location in the DataBase
 */
package com.example.campusguidv5;

import java.util.LinkedList;

public class Construction{
	private LinkedList<String> LocationName;
	private LinkedList<String[]> Coordinates;
	private Mark[] markList;
	private LinkedList<String> Contains;
	private LinkedList<Float> BearList;
	private LinkedList<Boolean> Construction_Visible;
	

	public Construction(int Mark_size){
		LocationName = new LinkedList<String>();
		Coordinates = new LinkedList<String[]>();
		markList = new Mark[Mark_size];
		Contains = new LinkedList<String>();
		BearList = new LinkedList<Float>();
		Construction_Visible = new LinkedList<Boolean>();
		
		
	}
	
	//Add new Location to List
	public void add(String Name, String[] Coordinates, String Contains) {
		this.Contains.add(Contains);
		this.Coordinates.add(Coordinates);
		this.LocationName.add(Name);
		this.BearList.addLast(null);
		this.Construction_Visible.add(true);
		/*
		for(int i=0; i<Obs.length; i++){
			Obstacle_X.add(i, Obs[i][0]);
			Obstacle_Y.add(i, Obs[i][1]);
		}
		*/
	}

	public void addMark(int x, int y, int pos){
		if(markList[pos] == null)	markList[pos] = new Mark();
		this.markList[pos].add(x, y);
	}
	
	public int getMarkSize(int pos){
		return this.markList[pos].getLength();
	}
	
	public int getMark_X(int id, int pos){
		return this.markList[id].getX(pos);
	}	
	public int getMark_Y(int id, int pos){
		return this.markList[id].getY(pos);
	}		
	
	/*
	 * Get String[] = { LocationName，
	 * 					Lat，
	 * 					Lon，
	 * 					Contains}
	 */
	public String[] getelement(int pos){
		String[] result = {	LocationName.get(pos),
							Coordinates.get(pos)[0],
							Coordinates.get(pos)[1],
							Contains.get(pos)
							};
		return result;
	}
	
	// Get List's Size
	public int getSize(){
		return LocationName.size();
	}
	
	// Clear all List
	public void removeall(){
		LocationName.clear();
		Coordinates.clear();
		Contains.clear();
	}
	
	//該建築物是否可看到
	public boolean isConstruction_Visible(int pos) {
		return Construction_Visible.get(pos);
	}
	
	//重新紀錄目前位置和建築物之相對角度
	public void setRelativeBearing(int pos, float b){
		BearList.set(pos, b);
	}
	//將紀錄還原
	public void setAllVisible(){
		for(int i=0; i<Construction_Visible.size(); i++){
			Construction_Visible.set(i, true);
		}
	}
	
	
	public Float getRelativeBearing(int pos){
		return BearList.get(pos);
	}
	

	public void setConstruction_Visible(int pos, boolean o) {
		Construction_Visible.set(pos, o);
	}	

}


//存放建築物的邊緣點List
class Mark{
    private LinkedList<Integer> linkedList_X;
    private LinkedList<Integer> linkedList_Y;
    
    
    public Mark() {
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