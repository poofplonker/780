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
	private int adc;
	private double unlabelledDispersion;
	private double labelledDispersion;
	private double dispersion;
	private double geometricScore;
	private double impurityScore;
	private boolean changed;
	
	
	public MacroCluster(DataPoint centroid, int index, int c){
		super(centroid);
		this.clusterIndex = index;
		this.centroid = centroid;
		labelledPoints = new LinkedList<DataPoint>();
		unLabelledPoints = new LinkedList<DataPoint>();
		classCounter = new int[c+1];
		predictedClassCounter = new int[c+1];
		totalPoints = 0;
		labelledDispersion = 0;
		unlabelledDispersion = 0;
		dispersion = 0;
		adc = 0;
		changed = false;
		this.c = c;
	}
	
	public void setChanged(){
		changed = true;
	}
	
	public void resetChanged(){
		changed = false;
	}
	
	public DataPoint getCentroid() {
		return centroid;
	}

	public int countNumClasses(){
		int counter = 0;
		for(int i = 0; i < c+1; i++){
			if(classCounter[i] > 0){
				counter++;
			}
		}
		//System.out.println("Labelled Class Counter: " + counter);
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

			//calcADC();
		}else{
			if(!unLabelledPoints.remove(d)){
				return false;
			}
			predictedClassCounter[d.getPredictedLabel()+1]--;
		}
		classCounter[d.getLabel()+1]--;
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
		}else{
			unLabelledPoints.add(d);
			predictedClassCounter[d.getPredictedLabel()+1]++;
			//calcADC();
		}
		classCounter[d.getLabel()+1]++;
		totalPoints++;
		d.setClusterIndex(clusterIndex);
		points.add(d);

		return;
	}
	
	/*recalculating the centroid  only takes into account the geometric position of the 
	 * points, not any of the other weights related to the purity of the cluster, etc.*/

	public double calcEMScore(DataPoint d){
		double instance = centroid.getDistanceValue(d)*centroid.getDistanceValue(d)*(1+calcImpurity(d.getLabel()+1,d.isLabeled()));
		//System.out.println("Score for cluster: " + score);
		d.incrementAverageDist(centroid.getDistanceValue(d));
		return instance;
	}
	
	
	private double calcImpurity(int label, boolean isLabelled){
		//calculate adc
		double entropy = 0;
		double prior;
		for(int i = 1; i < c+1; i++){
			if(classCounter[i]!= 0){
				prior = ((double)classCounter[i])/totalPoints;
				//System.out.println("Prior: " + prior);
				entropy += (-1*prior*((Math.log(prior)/Math.log(2))));
			}
		}
		int dc = totalPoints - classCounter[label];
		
		//System.out.println("Label: " + label+ " -- dc:  "+ dc );
		//System.out.println("In impurity calc - adc after calc:" + adc);
		//System.out.println("ADC: " + dc + " - Ent: " + entropy );
		entropy *= dc;
		//System.out.println("Entropy: " + entropy);
		this.impurityScore = entropy;
		return entropy;
	}
	
	private void calcADC(){
		int temp = 0;
		for(int i = 0; i < c; i++){
			temp += (labelledPointCount - classCounter[i])*classCounter[i];
		}
		adc = temp;
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
		//System.out.println("Unlabelled Class Counter: " + counter);
		return counter;
	}
	
	public int countMicroClusters(){
		
		return countPredictedClasses() + countNumClasses();
	}

	public int getNumPoints() {
		// TODO Auto-generated method stub
		return totalPoints;
	}
	
	
}
