import java.util.ArrayList;

import sun.tools.tree.ThisExpression;

/* Class which holds a single instance of data, along with information regarding the classification of that data.*/

public class DataPoint {
	
	private int predictedLabel = -1;	//-1 means no prediction has been made.
	private final int actualLabel;		
	private ArrayList<DataType> data;	//Data with category and such
	private int vectorLength;
	private int clusterIndex;			//Which cluster the dataPoint is in. 
	
	public DataPoint(ArrayList<DataType> data, int i){
		this.actualLabel = i;
		this.data = data;
		this.vectorLength = data.size();
		this.clusterIndex = -1;
	}
	
	public void setClusterIndex(int clusterIndex){
		this.clusterIndex = clusterIndex;
	}
	
	public int getClusterIndex(){
		return clusterIndex;
	}
	
	public int getPredictedLabel(){
		return this.predictedLabel;
	}
	public int getLabel(){
		return actualLabel;
	}
	public boolean isLabeled(){
		return actualLabel != 0;
	}
	
	public void setPredictedLabel(int label){
		this.predictedLabel = label;
	}
	
	public int getActualLabel(){
		return this.actualLabel;
	}
	
	public ArrayList<DataType> getData(){
		return this.data;
	}
	
	public int getVectorLength(){
		return this.vectorLength;
	}
	
	//Distance between two dataPoints. 
	public double getDistanceValue(DataPoint other){
		double[] difference = getDistanceVector(other.getData());
		double value = 0;
		for (int i = 0; i < this.vectorLength; i++){
			if(difference[i] < 0){
				System.out.println("An error has occurred in the distance measurement.");
			}
			value += difference[i]*difference[i];
		}
		return Math.sqrt(value);
	}
	
	//returns vector of distances between points in each dimension of two dataPoints.  
	public double[] getDistanceVector(ArrayList<DataType> arrayList){
		double[] distanceVector = new double[this.vectorLength];
		for(int i = 0; i < this.vectorLength; i++){
			distanceVector[i] = this.data.get(i).distance(arrayList.get(i));
		}
		return distanceVector;
	}
}
