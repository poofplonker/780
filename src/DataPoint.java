import java.util.ArrayList;

/* Class which holds a single instance of data, along with information regarding the classification of that data.*/

public class DataPoint {
	private static int num_dataPoints = 0;
	private int absoluteIndex;
	private int predictedLabel = -1;	//-1 means no prediction has been made.
	private final int actualLabel;		
	private boolean isLabeled;
	private DataType classLabel;
	private ArrayList<DataType> data;	//Data with category and such
	private int vectorLength;
	private int clusterIndex;			//Which cluster the dataPoint is in. 
	private double averageDist;
	private int avCounter = 0;
	private double plof;
	private boolean plofSet;
	private boolean isCentroid;
	
	public DataPoint(ArrayList<DataType> data, DataType classLabel, int i,boolean isLabeled){
		this.actualLabel = i;
		this.data = data;
		this.vectorLength = data.size();
		this.clusterIndex = -1;
		this.isLabeled = isLabeled;
		this.classLabel = classLabel;
		this.absoluteIndex = num_dataPoints++;
		this.averageDist = 0;
		this.isCentroid = false;
		this.plof = 0;
		plofSet = false;
	}
	
	public void setPlof(double d){
		this.plof = d;
		plofSet = true;
	}
	
	public boolean isPlofSet(){
		return plofSet;
	}
	public double getPlof(){
		return this.plof;
	}
	public void setCentroid(){
		isCentroid = true;
	}
	
	public boolean isCentroid(){
		return isCentroid;
	}
	public void resetAverageDist(){
		this.averageDist = 0;
		avCounter = 0;
	}
	
	public void incrementAverageDist(double distance){
		this.averageDist += distance; 
		avCounter++;
	}
	
	public double getAverageDist(){
		return averageDist/avCounter;
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
		if(isLabeled){
			return actualLabel;
		}
		return -1;
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
//			if(difference[i] < 0){
//				System.out.println("An error has occurred in the distance measurement.");
//			}
			value += difference[i]*difference[i];
		}
		//System.out.println("Value in getDistanceValue:" + value);
		//System.out.println("Distance between " + this + " and " +other + " is " + Math.sqrt(value));
		return Math.sqrt(value);
	}
	
	//returns vector of distances between points in each dimension of two dataPoints.  
	public double[] getDistanceVector(ArrayList<DataType> arrayList){
		double[] distanceVector = new double[this.vectorLength];
		//System.out.println("The vectorLength set in dataPoint is:" + this.vectorLength);
		//System.out.println("The length of the arraylist being passed in is however:"+ this.data.size());
		for(int i = 0; i < arrayList.size(); i++){
			
			distanceVector[i] = this.data.get(i).normDistance(arrayList.get(i));
			if(distanceVector[i] < 0){
				System.out.println(arrayList.get(i) + " " + ((DoubleData)this.data.get(i)).getRaw());
			}
			//System.out.println("Distance in dimension "+ i + ": " + distanceVector[i]);
		}
		return distanceVector;
	}
	
	public String toString(){
		String printer = "";
		if(isLabeled()){
			printer += Integer.toString(actualLabel) + " ";
		}else{
			printer += Integer.toString(-1) + " ";
		}
		for(DataType d: data){
			if(d instanceof IntegerData){
				printer += Integer.toString(((IntegerData)d).getRaw());
				printer += " ";
			}
			else if(d instanceof DoubleData){
				printer += Double.toString(((DoubleData)d).getRaw());
				printer += " ";
			}else{
				printer += ((CategoricalData)d).getRaw();
				printer += " ";
			}
			
		}
		return printer;
		
	}

	public DataType getClassLabel() {
		// TODO Auto-generated method stub
		return classLabel;
	}

	public void setToLabelled() {
		this.isLabeled = true;
		
	}
}
