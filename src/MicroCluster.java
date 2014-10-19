import java.util.ArrayList;
import java.util.Arrays;
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
		double[] scoreList = new double[points.size()];
		for(int i = 0; i < points.size(); i++){
			scoreList[i] = Loop.loop(points.get(i), this, nPlof);
			//counter++;
		}
		Arrays.sort(scoreList);
		int topPointNum = points.size()/20 + 1;
		for(int i = points.size()-1; i >= points.size()-topPointNum; i--){
			loopSum += scoreList[i];
		}
		loopSum /= topPointNum;
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

	public void setPoints(LinkedList<DataPoint> appendedPoints) {
		points = appendedPoints;
		
	}
}
