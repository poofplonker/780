import java.util.ArrayList;
import java.util.LinkedList;

public class MicroCluster extends Cluster{
	
	private boolean labelled;
	private int microClusterLabel;
	private double loopRating;
	private double plofDenom;
	private boolean plofDenomSet;
	private int plofCounter;
	

	public MicroCluster(DataPoint centroid, boolean labelled, int label){
		super(centroid);
		points = new LinkedList<DataPoint>();
		this.labelled = labelled;
		totalPoints = 0;
		microClusterLabel = label;
		loopRating = 0;
		plofDenomSet = false;
		//points.add(centroid);	//done at creation outside
	}
	
	public void setPlofDenom(double denom, int counter){
		plofDenom = denom;
		plofCounter = counter;
		plofDenomSet = true;
	}
	
	public double getPlofDenom(){
		return plofDenom;
	}
	public boolean isPlofDenomSet(){
		return plofDenomSet;
	}
	public int getPlofCounter(){
		return plofCounter;
	}
	
	public int getLabel(){
		return microClusterLabel;
	}
	public void attachPoint(DataPoint d){
		totalPoints++;
		points.add(d);
		return;
	}
	
	public double genLoopRating(double nPlof){
		//for every point, generate its loop value
		double loopSum = 0;
		for(DataPoint d: points){
			loopSum = Math.max(loopSum, Loop.loop(d, this, nPlof));
			//counter++;
		}
		loopSum = 1 - loopSum;
		loopRating = loopSum;
		return loopSum;
		
	}
	
	public String toString(){
		String thing = "";
		thing += " Label: " + microClusterLabel;
		thing += " Labelled: " + labelled;
		thing += " Number of points: " + totalPoints;
		thing += " Rating: " + loopRating;
		return thing;
	}
}
