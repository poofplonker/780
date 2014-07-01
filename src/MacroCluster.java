import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class MacroCluster extends Cluster{
	private DataPoint labelledCentroid;
	private int labelledPointCount;
	private ArrayList<DataPoint> labelledPoints;
	private ArrayList<DataPoint> unLabelledPoints;
	private int[] classCounter;
	private int[] predictedClassCounter;
	private int c;
	private double geometricScore;
	private double impurityScore;
	
	
	public MacroCluster(DataPoint centroid, int c){
		super(centroid);
		this.centroid = centroid;
		labelledPoints = new ArrayList<DataPoint>();
		unLabelledPoints = new ArrayList<DataPoint>();
		classCounter = new int[c];
		predictedClassCounter = new int[c];
		totalPoints = 0;
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
		}else{
			if(!unLabelledPoints.remove(d)){
				return false;
			}
			predictedClassCounter[d.getPredictedLabel()]--;
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
		}else{
			unLabelledPoints.add(d);
			predictedClassCounter[d.getPredictedLabel()]++;
		}
		totalPoints++;
		points.add(d);
		return;
	}
	
	/*recalculating the centroid  only takes into account the geometric position of the 
	 * points, not any of the other weights related to the purity of the cluster, etc.*/
	
	public double calcImpurity(){
		//calculate adc
		int adc = 0;
		for(DataPoint d: labelledPoints){
			adc += (labelledPointCount - classCounter[d.getLabel()]);
		}
		double entropy = 0;
		for(int i = 0; i < c; i++){
			double prior = (double)classCounter[i]/labelledPointCount;
			entropy += (-prior*Math.log(prior)/Math.log(2));
		}
		entropy *= adc;
		this.impurityScore = entropy;
		return entropy;
	}
	
	public double unLabelledDispersion(){
		double dispersion = 0;
		for(DataPoint d: unLabelledPoints){
			dispersion += centroid.getDistanceValue(d);
		}
		return dispersion;
	}
	
	public double labelledDispersion(){
		double dispersion = 0;
		for(DataPoint d: labelledPoints){
			dispersion += centroid.getDistanceValue(d);
		}
		return dispersion;
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
