import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import cern.jet.random.engine.MersenneTwister;


public class Model {
	private int chunkSize;
	private ArrayList<Integer> numClasses;
	private ArrayList<MacroCluster> macroClusters;
	private ArrayList<MicroCluster> microClusters;
	private ArrayList<PseudoPoint> pseudoPoints;
	private DataChunk dataChunk;
	private int k;
	private int c;
	
	public Model(MersenneTwister twister, DataChunk chunk, int k, int c){
		this.dataChunk = chunk;
		this.chunkSize = chunk.getChunkSize();
		this.numClasses = new ArrayList<Integer>();
		this.k = k;
		this.c = c;
		clusterData(twister);
		genMicroClusters();
		createPseudoPoints();
	}
	
	private void genMicroClusters() {
		//count the number of microclusters that will be required
		int counter = 0;
		for(MacroCluster m: macroClusters){
			counter += m.countMicroClusters();
		}
		//for each cluster, examine its points and place in appropriate microcluster.
		microClusters = new ArrayList<MicroCluster>(counter);
		int base = 0;
		for(MacroCluster m: macroClusters){
			
			//labelled points will be in cluster base+ 
			HashMap<Integer,Integer> classToClusterLabelled = new HashMap<Integer,Integer>();
			HashMap<Integer,Integer> classToClusterUnlabelled = new HashMap<Integer,Integer>();
			ArrayList<DataPoint> points = m.getDataPoints();
			for(DataPoint p: points){
				if(p.isLabeled()){
					if(classToClusterLabelled.containsKey(p.getLabel())){
						microClusters.get(classToClusterLabelled.get(p.getLabel())).attachPoint(p);
					}else{
						classToClusterLabelled.put(p.getLabel(), base);
						base++;
					}
				}else{
					if(classToClusterUnlabelled.containsKey(p.getPredictedLabel())){
						microClusters.get(classToClusterLabelled.get(p.getPredictedLabel())).attachPoint(p);
					}else{
						classToClusterLabelled.put(p.getPredictedLabel(), base);
						base++;
					}
				}
			}
			
			
		}
	}

	public ArrayList<MacroCluster> clusterData(MersenneTwister twister){
		ArrayList<MacroCluster> clusters = new ArrayList<MacroCluster>(k);
		this.macroClusters = clusters;
		ArrayList<DataPoint> dataPoints = dataChunk.getDataPointArray();
		//randomly select points to be the initial centroids of the k clusters.
		for(int i = 0; i < k; i++){
			DataPoint centroid = dataPoints.get((int)twister.nextDouble()*k);
			clusters.set(i, new MacroCluster(centroid,c));
		}
		
		//initialise all dataPoints to cluster with nearest centroid.
		for(DataPoint d: dataPoints){
			attachToNearest(d);
		}
		expectationMinimisation(clusters,dataPoints);
		return clusters;
		
	}

	private void expectationMinimisation(ArrayList<MacroCluster> clusters, ArrayList<DataPoint> dataPoints) {
		
		int pointsChanged = 1;
		int iterations = 0;
		while(pointsChanged > 0){
			pointsChanged = 0;
			//recalculate the cluster centroids based on the datapoints in the clusters
			for(int i = 0; i < k; i++){
				DataPoint newCentroid = clusters.get(i).recalculateCentroid();
				clusters.set(i, new MacroCluster(newCentroid,c));
			}
			//attach all points to position geometrically nearest to them
			for(DataPoint d: dataPoints){
				int prevSmallIndex = d.getClusterIndex();
				attachToNearest(d);
				if(!d.isLabeled() && d.getClusterIndex() != prevSmallIndex){
					pointsChanged++;
				}
			}
			//for every labelled datapoint, try it out in every other cluster. Put the point in the cluster which 
			//most reduces the impurity and distance.
			for(DataPoint d: dataPoints){
				int bestCluster = d.getClusterIndex();
				double bestImprove = 0;
				if(d.isLabeled()){
					MacroCluster current = clusters.get(d.getClusterIndex());
					for(int i = 0; i < k; i++){
						
						if(d.getClusterIndex() != i){
							MacroCluster prospective = clusters.get(i);
							
							double oldImpurity = prospective.calcImpurity();
							double oldLabelledDispersion = prospective.labelledDispersion();
							double oldScore = oldImpurity*oldLabelledDispersion + current.calcImpurity()*current.labelledDispersion(); ;
							prospective.attachPoint(d);
							if(!current.removePoint(d)){
								System.out.println("Could not remove point requested.");
							}
							double newScore = prospective.calcImpurity()*prospective.labelledDispersion() + 
									current.calcImpurity()*current.labelledDispersion();
							if(oldScore - newScore > bestImprove){
								bestCluster = i;
							}
							current.attachPoint(d);
							if(!prospective.removePoint(d)){
								System.out.println("Could not remove point requested");
							}
						}
					}
					if(bestImprove > 0){
						pointsChanged++;
						if(!current.removePoint(d)){
							System.out.println("Could not remove point requested");
						}
						clusters.get(bestCluster).attachPoint(d);
					}
				}
			}
			iterations++;
			//Total value calculated to ensure that expectation minimisation is in fact converging.
			double totalValue = 0;
			for(int i = 0; i < k; i++){
				MacroCluster clust = clusters.get(i);
				totalValue += clust.calcImpurity()*clust.labelledDispersion() + clust.unLabelledDispersion();
			}
		}
		
	}

	private void attachToNearest(DataPoint d) {
		double minDistance = Double.MAX_VALUE;
		int smallIndex = 0;
		//see which centroid is closest
		for(int i = 0; i < k; i++){
			double currentDistance = macroClusters.get(i).getCentroid().getDistanceValue(d) ;
			if(currentDistance < minDistance){
				minDistance = currentDistance;
				smallIndex = i;
			}
		}
		//add to cluster nearest
		macroClusters.get(smallIndex).attachPoint(d);
		return;
	}
	
	public void createPseudoPoints(){
		pseudoPoints = new ArrayList<PseudoPoint>(microClusters.size());
		for(int i = 0; i < microClusters.size();i++){
			MicroCluster m = microClusters.get(i);
			DataPoint centroid = m.recalculateCentroid();
			int label = m.getLabel();
			int weight = m.getDataPoints().size();
			pseudoPoints.add(i, new PseudoPoint(centroid,weight,label));
		}
	}
	
	
}
