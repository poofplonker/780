import java.util.ArrayList;

import sun.tools.tree.ThisExpression;

/* Class which holds a single instance of data, along with information regarding the classification of that data.*/

public class DataPoint {
	private static int num_dataPoints = 0;
	private int absoluteIndex;
	private int predictedLabel = -1;	//-1 means no prediction has been made.
	private final int actualLabel;		
	private boolean isLabeled;
	private ArrayList<DataType> data;	//Data with category and such
	private int vectorLength;
	private int clusterIndex;			//Which cluster the dataPoint is in. 
	
	public DataPoint(ArrayList<DataType> data, int i,boolean isLabeled){
		this.actualLabel = i;
		this.data = data;
		this.vectorLength = data.size();
		this.clusterIndex = -1;
		this.isLabeled = isLabeled;
		this.absoluteIndex = num_dataPoints++;
	}
	
	public void setClusterIndex(int clusterIndex){
		this.clusterIndex = clusterIndex;
	}
	
	public int getAbsoluteIndex(){
		return this.absoluteIndex;
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
		return isLabeled;
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
		//System.out.println("Value in getDistanceValue:" + value);
		return Math.sqrt(value);
	}
	
	//returns vector of distances between points in each dimension of two dataPoints.  
	public double[] getDistanceVector(ArrayList<DataType> arrayList){
		double[] distanceVector = new double[this.vectorLength];
		for(int i = 0; i < this.vectorLength; i++){
			
			distanceVector[i] = this.data.get(i).distance(arrayList.get(i));
			//System.out.println("Distance in dimension "+ i + ": " + distanceVector[i]);
		}
		return distanceVector;
	}
}
