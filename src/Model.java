import java.util.ArrayList;
import java.util.Collections;
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
		System.out.println("Counter: " + counter);
		//for each cluster, examine its points and place in appropriate microcluster.
		microClusters = new ArrayList<MicroCluster>(counter);

		int base = 0;
		for(MacroCluster m: macroClusters){
			
			//labelled points will be in cluster base+ 
			HashMap<Integer,Integer> classToClusterLabelled = new HashMap<Integer,Integer>();
			HashMap<Integer,Integer> classToClusterUnlabelled = new HashMap<Integer,Integer>();
			LinkedList<DataPoint> points = m.getDataPoints();
			for(DataPoint p: points){
				if(p.isLabeled()){
					if(classToClusterLabelled.containsKey(p.getLabel())){
						//System.out.println("Cluster for this point: " +classToClusterLabelled.get(p.getLabel()));
						microClusters.get(classToClusterLabelled.get(p.getLabel())).attachPoint(p);
					}else{
						classToClusterLabelled.put(p.getLabel(), base);
						base++;
						microClusters.add(classToClusterLabelled.get(p.getLabel()) ,new MicroCluster(p,true,p.getLabel()));
						microClusters.get(classToClusterLabelled.get(p.getLabel())).attachPoint(p);
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
		for(MicroCluster m: microClusters){
			m.recalculateCentroid();
		}
	}
	
	//this is supposed to be based on class values in the entire dataset, not for datachunks. TBF
	public void initialiseClusters(MersenneTwister twister){
		int base = 0;
		//precompute the number of centroids per class
		int[] centroidCounter = new int[c];
		ArrayList<DataPoint> dataPoints = dataChunk.getDataPointArray();
		int[] classCounter = dataChunk.getClassCounter(c);
		int totalAssigned = 0;
		for(int j = 0; j < c; j++){
			centroidCounter[j] = (int)(k*classCounter[j])/dataChunk.getNumLabelledPoints();
			totalAssigned += centroidCounter[j];
		}
		while(totalAssigned < k){
			centroidCounter[Math.abs(twister.nextInt()) % c]++;
			totalAssigned++;
		}
		ArrayList<LinkedList<DataPoint>> classPoints = new ArrayList<LinkedList<DataPoint>>(k);
		for(int i = 0; i < k; i++){
			classPoints.add(i,new LinkedList<DataPoint>());
		}
		for(int i = 0; i < dataPoints.size(); i++){
			DataPoint currentPoint = dataPoints.get(i);
			classPoints.get(currentPoint.getLabel()).add(currentPoint);
		}
		for(int j = 0; j < c; j++){
			int i = 0; //number of clusters initialised
			
			
			
			//we initialise the number of clusters for a class proportional to the represet
			int representation = centroidCounter[j];
			System.out.println("Class " + j + " has " + representation + " clusters to initiate");
			LinkedList<DataPoint> thisClassPoints = classPoints.get(j);
			
			//get all points of this class

			LinkedList<DataPoint> visitedSet = new LinkedList<DataPoint>();
			if(thisClassPoints.size() > representation){
			//employ farthest first heuristic to select the points from this class
				
				boolean[] contained = new boolean[thisClassPoints.size()];
				int index = Math.abs(twister.nextInt()) % thisClassPoints.size();
				//System.out.println("Initial Index : " + index);
				DataPoint centroid = thisClassPoints.get(index);
				visitedSet.add(centroid);
				contained[index] = true;
				i++;
				//System.out.println("After 1 addition, size of visitedSet " + visitedSet.size() + " and size of classPoints: " + thisClassPoints.size());
				while(i < representation){
					double maxDistance = 0;
					int maxIndex = -1;
					for(int d = 0; d < thisClassPoints.size(); d++){
						if(contained[d] == true){
							System.out.println(d + " broken");
							continue;
						}
						double thisDistance = 0;
						for(DataPoint c : visitedSet){
							 thisDistance += c.getDistanceValue(thisClassPoints.get(d));
							 //System.out.println("Distance from point " + c.getAbsoluteIndex() + " to point " + thisClassPoints.get(d).getAbsoluteIndex() + " : " + c.getDistanceValue(thisClassPoints.get(d)));
						}
						if(thisDistance > maxDistance){
							maxDistance = thisDistance;
							maxIndex = d;
						}
					}
					contained[maxIndex] = true;
					visitedSet.add(thisClassPoints.get(maxIndex));
					i++;
				}
				
			}else{
				i = 0;
				for(DataPoint d: thisClassPoints){
					visitedSet.add(d);
					i++;
				}
				while(i < representation){
					int index = Math.abs(twister.nextInt()) % dataChunk.getChunkSize();
					if(!dataPoints.get(index).isLabeled() && !visitedSet.contains(dataPoints.get(index))){
						visitedSet.add(dataPoints.get(index));
						i++;
					}
				}
				
			}
			i = 0;
			for(DataPoint d: visitedSet){
				macroClusters.add(new MacroCluster(d,i+base,c));
				i++;
			}
			base += representation;
		}
	}

	public ArrayList<MacroCluster> clusterData(MersenneTwister twister){
		ArrayList<MacroCluster> clusters = new ArrayList<MacroCluster>(k);
		this.macroClusters = clusters;
		ArrayList<DataPoint> dataPoints = dataChunk.getDataPointArray();
		//Select centroids proportionate to class representation for initialisation.
		
		initialiseClusters(twister);
		for(int i = 0; i < k; i++){
			System.out.println("Macrocluster "+ i + " initial centroid: " + macroClusters.get(i).getCentroid());
		}
		//initialise all dataPoints to cluster with nearest centroid.
		double totalValue = 0;
		for(DataPoint d: dataPoints){
			totalValue += attachToNearestImp(d);
		}
		for(DataPoint d: dataPoints){
			System.out.println("Point " + d.getAbsoluteIndex() + " is in cluster " + d.getClusterIndex());
		}
		

		System.out.println("Total Value for EM: " + totalValue);
		expectationMinimisation(clusters,dataPoints);
		return clusters;
		
	}

	private void expectationMinimisation(ArrayList<MacroCluster> clusters, ArrayList<DataPoint> dataPoints) {
		double prevEm = 0;
		int pointsChanged = 1;
		int iterations = 0;
		while(pointsChanged > 0){
			pointsChanged = 0;
			int changedByRecalcCent = 0;
			//recalculate the cluster centroids based on the datapoints in the clusters
			for(int i = 0; i < k; i++){
				DataPoint newCentroid = clusters.get(i).recalculateCentroid();
				System.out.print("New centroid for cluster " + i +": " + newCentroid);
				System.out.println();
				clusters.set(i, new MacroCluster(newCentroid,i,c));
			}
			//attach all points to position geometrically nearest to them
			Collections.shuffle(dataPoints);
			double totalValue = 0;
			for(DataPoint d: dataPoints){
				int prevSmallIndex = d.getClusterIndex();
				totalValue += attachToNearestImp(d);
				if(d.getClusterIndex() != prevSmallIndex){
					changedByRecalcCent++;
					pointsChanged++;
					System.out.println("Point " + d.getAbsoluteIndex() + " was in " + prevSmallIndex + " and is now in " + d.getClusterIndex());
				}
			}
		
			iterations++;
			//Total value calculated to ensure that expectation minimisation is in fact converging.
			
			System.out.println("Total Value for EM: " + totalValue);
			System.out.println("Points changed: " + pointsChanged);
			//System.out.println("Points changed by the change in centroids: " + changedByRecalcCent);
			for(DataPoint d: dataPoints){
				System.out.println("Point " + d.getAbsoluteIndex() + " is in cluster " + d.getClusterIndex());
			}
			System.out.println("PrevEm: " + prevEm+", diff:" + (totalValue-prevEm));
			prevEm = totalValue;
			//System.out.println("Iterations: " + iterations);
		}
		
	}

	private double attachToNearestImp(DataPoint d){
		double minDistance = Double.MAX_VALUE;
		int smallIndex = 0;
		//see which centroid is closest
		for(int i = 0; i < k; i++){
			MacroCluster current = macroClusters.get(i);
			double currentDistance = current.calcEMScore(d);
			//System.out.println("Current EMS:" + currentDistance);
			if(currentDistance < minDistance){
				minDistance = currentDistance;
				smallIndex = i;
			}
		}
		//add to cluster nearest
		macroClusters.get(smallIndex).attachPoint(d);
		//System.out.println("Attached point " + d.getAbsoluteIndex() + " to cluster " + smallIndex);
		//System.out.println("Distance to cluster: " + minDistance);
		return minDistance;
	}
	private void attachToNearest(DataPoint d) {
		double minDistance = Double.MAX_VALUE;
		int smallIndex = 0;
		//see which centroid is closest
		for(int i = 0; i < k; i++){
			double currentDistance = macroClusters.get(i).getCentroid().getDistanceValue(d);
			//System.out.println("Current Distance:" + currentDistance);
			if(currentDistance < minDistance){
				minDistance = currentDistance;
				smallIndex = i;
			}
		}
		//add to cluster nearest
		macroClusters.get(smallIndex).attachPoint(d);
		//System.out.println("Attached point " + d.getAbsoluteIndex() + " to cluster " + smallIndex);
		//System.out.println("Distance to cluster: " + minDistance);
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
