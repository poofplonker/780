import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;


public class MacroCluster extends Cluster{
	private int clusterIndex;
	private DataPoint labelledCentroid;
	private int labelledPointCount;
	private LinkedList<DataPoint> labelledPoints;
	private LinkedList<DataPoint> unLabelledPoints;
	private int[] classCounter;
	private int[] predictedClassCounter;
	private int c;
	private double unlabelledDispersion;
	private double labelledDispersion;
	private double geometricScore;
	private double impurityScore;
	
	
	public MacroCluster(DataPoint centroid, int index, int c){
		super(centroid);
		this.clusterIndex = index;
		this.centroid = centroid;
		labelledPoints = new LinkedList<DataPoint>();
		unLabelledPoints = new LinkedList<DataPoint>();
		classCounter = new int[c];
		predictedClassCounter = new int[c];
		totalPoints = 0;
		labelledDispersion = 0;
		unlabelledDispersion = 0;
		this.c = c;
	}
	public DataPoint getCentroid() {
		return centroid;
	}

	public int countNumClasses(){
		int counter = 0;
		for(int i = 0; i < c; i++){
			if(classCounter[i] > 0){
				counter++;
			}
		}
		return counter;
	}
	public void setCentroid(DataPoint centroid) {
		this.centroid = centroid;
	}
	
	public boolean removePoint(DataPoint d){
		if (d.isLabeled()){
			if(!labelledPoints.remove(d)){
				return false;
			}
			labelledPointCount--;
			classCounter[d.getLabel()]--;
			labelledDispersion -= centroid.getDistanceValue(d);
		}else{
			if(!unLabelledPoints.remove(d)){
				return false;
			}
			predictedClassCounter[d.getPredictedLabel()]--;
			unlabelledDispersion -= centroid.getDistanceValue(d);
		}
		if(!points.remove(d)){
			return false;
		}
		totalPoints--;
		return true;
	}
	
	public void attachPoint(DataPoint d){
		if (d.isLabeled()){
			labelledPointCount++;
			labelledPoints.add(d);
			classCounter[d.getLabel()]++;
			labelledDispersion += centroid.getDistanceValue(d);
		}else{
			unLabelledPoints.add(d);
			predictedClassCounter[d.getPredictedLabel()]++;
			unlabelledDispersion += centroid.getDistanceValue(d);
		}
		totalPoints++;
		d.setClusterIndex(clusterIndex);
		points.add(d);
		return;
	}
	
	/*recalculating the centroid  only takes into account the geometric position of the 
	 * points, not any of the other weights related to the purity of the cluster, etc.*/
	
	public double calcImpurity(){
		//calculate adc
		int adc = 0;
		for(int i = 0; i < c; i++){
			adc += (labelledPointCount - classCounter[i]);
		}
		//System.out.println("In impurity calc - adc after calc:" + adc);
		double entropy = 0;
		for(int i = 0; i < c; i++){
			double prior;
			if(labelledPointCount == 0){
				prior = 0;
			}else{
				prior = (double)classCounter[i]/labelledPointCount;
				entropy += (-1*prior*(Math.log(prior)/Math.log(2)));
			}
			//System.out.println("Prior: " + prior);
			
		}
		entropy *= adc;
		//System.out.println("Entropy: " + entropy);
		this.impurityScore = entropy;
		return entropy;
	}
	
	public double getImpurity(){
		return impurityScore;
	}
	
	public double unLabelledDispersion(){
		return unlabelledDispersion;
	}
	
	public double labelledDispersion(){
		return labelledDispersion;
	}
	
	
	public int countPredictedClasses() {
		int counter = 0;
		for(int i = 0; i < predictedClassCounter.length; i++){
			if(predictedClassCounter[i] >0){
				counter++;
			}
		}
		return counter;
	}
	
	public int countMicroClusters(){
		return countPredictedClasses() + countNumClasses();
	}
	
	
}
