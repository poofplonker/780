import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import Jama.Matrix;
import cern.jet.random.engine.MersenneTwister;


public class Model {
	private static final double ALPHA = 0.99;		//learning rate for label propagation
	private static final double OUTLIERTHRESHOLD = 0.5;
	private int index;								//Index to distinguish different models. Indexing done sequentially
	private ArrayList<MacroCluster> macroClusters;
	private ArrayList<MicroCluster> microClusters;
	private ArrayList<Boolean> seenClass;			//Whether or not certain classes have been seen in this data chunk.
	private ArrayList<PseudoPoint> pseudoPoints;
	private ArrayList<DataPoint> trainingData;		
	private ArrayList<Double> loopSummary;			//Summary of cluster scores
	private DataChunk dataChunk;
	private int totalPoints;
	private boolean removal;						//Whether we are removing outliers
	private int k;	
	private double removedPseudo;					//Percentage of pseudopoints removed from model due to inaccuracy
	private int numPseudo;
	private int c;									//Number of classes	
	private boolean ratingClusters; 				//Whether we are scoring clusters and weighting classification by that scoring
	private double stddev;							//STddev for label propagation
	
	//Model constructor 
	public Model(MersenneTwister twister, DataChunk chunk, int k, int c, int[] classData, int totalPoints, int index,boolean removal, boolean ratingClusters){
		
		//initialisation
		this.dataChunk = chunk;
		this.seenClass = chunk.seenClass(c);
		this.trainingData = chunk.getTrainingData();
		this.k = k;
		this.c = c;
		this.index = index;
		this.totalPoints = totalPoints;
		this.ratingClusters = ratingClusters;
		this.removal = removal;
		
		
		clusterData(twister);
		
		genMicroClusters();
		
		double nPlof = Loop.NPlof(microClusters);
		createPseudoPoints(nPlof);
		
		//After full model is built
		//Create a summary of the cluster scores. We do this by returning the min, max, quartile scores, and median cluster score. 
		double[] loopScores = new double[pseudoPoints.size()];
		
		//get loop scores for all pseudopoints (summary of microcluster) and sort them
		for(int i = 0; i <pseudoPoints.size(); i++ ){
			loopScores[i] = pseudoPoints.get(i).getClusterRating();
		}
		Arrays.sort(loopScores);
		
		loopSummary = new ArrayList<Double>();
		
		//this loop will record the min, max, median, and interquartile cluster scores and return it to where the data is collated.
		for(int i = 0; i < 5; i++){
			loopSummary.add(loopScores[i*(pseudoPoints.size()-1)/4]);
		}
		
		//printout for console 
		System.out.println("Loop Summary:");
		for(int i = 0; i < loopSummary.size(); i++){
			System.out.print(loopSummary.get(i) + " ");
		}
		System.out.println();
		System.out.println("Model gen done");
	}
	
	
	public int getIndex(){
		return index;
	}
	
	
	public ArrayList<Double> getLoopSummary(){
		return loopSummary;
	}
	
	//For when we do pseudo-point injection, we need to increment the number of classes.
	public void incrementClass(){
		c++;
	}
	
	public ArrayList<DataPoint> getTrainingData(){
		return this.trainingData;
	}
	
	public ArrayList<PseudoPoint> getPseudo(){
		return pseudoPoints;
	}
	
	public ArrayList<Boolean> getSeenClass() {
		// TODO Auto-generated method stub
		return seenClass;
	}

	public int getNumClass() {
		return c;
	}
	
	/*
	 Function which splits macroclusters based on the class distribution into microclusters.	
	 */
	private void genMicroClusters() {
		
		//count the number of microclusters that will be required
		//for each cluster, examine its points and place in appropriate microcluster.
		microClusters = new ArrayList<MicroCluster>();

		//base is an indexing variable used to assign a number (macrocluster,class) combination
		//so that it can be found later.
		int base = 0;
		
		for(MacroCluster m: macroClusters){
			
			//Hash maps to see whether a microclsuter already exists for that class, or
			//predicted class for unlabelled points, and which index it corresponds to.
			HashMap<Integer,Integer> classToClusterLabelled = new HashMap<Integer,Integer>();
			HashMap<Integer,Integer> classToClusterUnlabelled = new HashMap<Integer,Integer>();
			LinkedList<DataPoint> points = m.getDataPoints();
			
			for(DataPoint p: points){
				if(p.isLabeled()){
					if(classToClusterLabelled.containsKey(p.getLabel())){
						//if there already exists a microcluster for this class, add it to that microcluster
						microClusters.get(classToClusterLabelled.get(p.getLabel())).attachPoint(p);
					}else{
						//if not, add class to  hash map, create new microcluster, and add point.
						classToClusterLabelled.put(p.getLabel(), base);
						microClusters.add(new MicroCluster(p,true,p.getLabel()));
						microClusters.get(base++).attachPoint(p);
					}
				}else{
					if(classToClusterUnlabelled.containsKey(p.getPredictedLabel())){			
						//if there already exists a microcluster for this predicted class, add it to that microcluster
						microClusters.get(classToClusterUnlabelled.get(p.getPredictedLabel())).attachPoint(p);
					}else{
						//if not, add predicted to hash map, create new microcluster, and add point.
						classToClusterUnlabelled.put(p.getPredictedLabel(), base);
						microClusters.add(new MicroCluster(p,false,-1));
						microClusters.get(base++).attachPoint(p);
					}
				}
			}
		}
		
		//calculate centroids for all the microclusters.
		for(MicroCluster m: microClusters){
			m.recalculateCentroid();
		}
		System.out.println("Number of micro-clusters: " + microClusters.size());
	}
	
	/*
	 * Framework function for the entire clustering process.
	 */
	public ArrayList<MacroCluster> clusterData(MersenneTwister twister){
		ArrayList<MacroCluster> clusters = new ArrayList<MacroCluster>(k);
		this.macroClusters = clusters;
		ArrayList<DataPoint> dataPoints = dataChunk.getDataPointArray();

		//Select centroids proportionate to class representation for initialisation.
		initialiseClusters(twister);

		//initialise all dataPoints to cluster with nearest centroid.
		double totalValue = 0;
		for(DataPoint d: dataPoints){
			//CHECK TO SEE D WAS NOT ALREADY IN THE CLUSTER
			if(!d.isCentroid()){
				totalValue += attachToNearestImp(d);
			}
		}
		
		//Perform Expectation Minimisation
		expectationMinimisation(clusters,dataPoints);
		return clusters;
		
	}
	
	/*
	 * This is a function which initialises the clusters for the clustering phase. This is done by seeding the k
	 * macro-clusters with a single labelled point, based on the distribution of labelled points in the data chunk.
	 */
	private void initialiseClusters(MersenneTwister twister)
	{
		//base is once again an index for the macroclusters.
		int base = 0;
		int visitedSum = 0;
		
		//precompute the number of centroids per class
		
		//cluster counter contains the number of macroclusters initialised from points of that class
		int[] clusterCounter = new int[c];
		ArrayList<DataPoint> dataPoints = dataChunk.getDataPointArray();
		
		//class counter contains how many points of each class are labelled in this datachunk.
		int[] classCounter = dataChunk.getClassCounter(c);
		int numLabelledPoints = dataChunk.getNumLabelledPoints();
		
		int totalAssigned = 0;
		for(int j = 0; j < c && dataChunk.getNumLabelledPoints() != 0; j++){
			clusterCounter[j] = Math.round((k*classCounter[j])/numLabelledPoints);
			totalAssigned += clusterCounter[j];
		}
		
		//if due to rounding we have some leftover clusters, select a random class. 
		while(totalAssigned < k){
			clusterCounter[Math.abs(twister.nextInt()) % c]++;
			totalAssigned++;
		}
		//class points is list of points of a certain class
		ArrayList<LinkedList<DataPoint>> classPoints = new ArrayList<LinkedList<DataPoint>>(c);
		for(int i = 0; i < c; i++){
			classPoints.add(i,new LinkedList<DataPoint>());
		}
		
		//populate list of points for each class
		for(int i = 0; i < dataPoints.size(); i++){
			
			DataPoint currentPoint = dataPoints.get(i);
			if(currentPoint.isLabeled()){
				classPoints.get(currentPoint.getLabel()).add(currentPoint);
			}
		}
		
		//for every class
		for(int j = 0; j < c; j++){
			int i = 0; //number of clusters initialised of this class
			
			//we initialise the number of clusters for a class proportional to the representation
			int representation = clusterCounter[j];
			
			//get all points of this class
			LinkedList<DataPoint> thisClassPoints = classPoints.get(j);
			
			//Visited set is the points which are the centroid for some cluster
			LinkedList<DataPoint> visitedSet = new LinkedList<DataPoint>();
			
			if(thisClassPoints.size() > representation){
				
				//employ farthest first heuristic to select the points from this class
				boolean[] contained = new boolean[thisClassPoints.size()];
				
				//select a random point to begin with
				int index = Math.abs(twister.nextInt()) % thisClassPoints.size();
				DataPoint centroid = thisClassPoints.get(index);

				visitedSet.add(centroid);
				contained[index] = true;
				i++;
				
				//while you have not fulfilled your quota from this class
				while(i < representation){
					
					double maxDistance = -1;
					int maxIndex = -1;
					
					//for each point of that class
					for(int d = 0; d < thisClassPoints.size(); d++){
						
						//if the point is already a centroid, ignore it
						if(contained[d] == true){
							continue;
						}
						
						//calculate the distance between a point and all other points in the centroid
						double thisDistance = 0;
						for(DataPoint c : visitedSet){
							 thisDistance += c.getDistanceValue(thisClassPoints.get(d));
							 //System.out.println("Distance from point " + c.getAbsoluteIndex() + " to point " + thisClassPoints.get(d).getAbsoluteIndex() + " : " + c.getDistanceValue(thisClassPoints.get(d)));
						}
						
						//if it is the largest, record it
						if(thisDistance > maxDistance){
							maxDistance = thisDistance;
							maxIndex = d;
						}
					}
					
					//add furthest away point to set of points to be centroid
					contained[maxIndex] = true;
					visitedSet.add(thisClassPoints.get(maxIndex));
					i++;
				}
				
			}else{
				
				//if we are out of point for that class, randomly select unlabelled points.
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
			//for each point to be centroid, create a microcluster with it as the centroid
			visitedSum += visitedSet.size();
			for(DataPoint d: visitedSet){
				MacroCluster temp = new MacroCluster(d,macroClusters.size(),c);
				temp.attachPoint(d);
				d.setCentroid();
				macroClusters.add(temp);
				i++;
			}
			base += representation;
		}
		System.out.println("Visited set size:" + visitedSum);
		System.out.println("Number of generated macro-clusters: " + base);
		System.out.println("Read no of microClusters:" + macroClusters.size());
	}

	
	/*
	 * Expectation minimisation function to find a good clustering.
	 */

	private void expectationMinimisation(ArrayList<MacroCluster> clusters, ArrayList<DataPoint> dataPoints) {

		int pointsChanged = 1;
		int iterations = 0;
		
		//termination condition: no points change cluster
		while(pointsChanged > 0){
			pointsChanged = 0;
			
			//recalculate the cluster centroids based on the datapoints in the clusters
			for(int i = 0; i < k; i++){
				DataPoint newCentroid = clusters.get(i).recalculateCentroid();
				clusters.get(i).setCentroid(newCentroid);
			}
			//shuffle points
			Collections.shuffle(dataPoints);
			
			//total value is the score of the objective function.
			double totalValue = 0;
			//for every point
			for(DataPoint d: dataPoints){
				//if a cluster only has a single point, do not try to reassign it: it will be closest to itself
				if(clusters.get(d.getClusterIndex()).getNumPoints() < 2){
					continue;
				}
				clusters.get(d.getClusterIndex()).removePoint(d);
				
				//remove point, record where it came from, attach to nearest point
				int prevSmallIndex = d.getClusterIndex();
				totalValue += attachToNearestImp(d);
				
				//if the cluster the point is attached to changes
				if(d.getClusterIndex() != prevSmallIndex){

					clusters.get(prevSmallIndex).setChanged();
					clusters.get(d.getClusterIndex()).setChanged();
					pointsChanged++;

				}
			}
		
			iterations++;
			//Total value calculated to ensure that expectation minimisation is in fact converging.
			if(iterations > 100){
				System.out.println("EM broken by iteration limit");
				break;
			}

		}
		for(int i = 0; i < k; i++){
			DataPoint newCentroid = clusters.get(i).recalculateCentroid();
			clusters.get(i).setCentroid(newCentroid);
		}
		System.out.println("EM Done in " + iterations + " iterations.");
		System.out.println("Data point size:" + dataPoints.size());
	}

	/*
	 * This function attaches a given point to the nearest 
	 * cluster, with cluster impurity taken into account.
	 */
	private double attachToNearestImp(DataPoint d){
		double minDistance = Double.MAX_VALUE;
		int smallIndex = 0;
		d.resetAverageDist();
		
		//see which centroid is closest
		for(int i = 0; i < macroClusters.size(); i++){
			MacroCluster current = macroClusters.get(i);
			current.attachPoint(d);
			double currentDistance = current.calcEMScore(d);
			current.removePoint(d);
			//System.out.println("Current EMS:" + currentDistance);
			if(currentDistance < minDistance || (currentDistance == minDistance && macroClusters.get(smallIndex).getDataPoints().size() != 0)){
				minDistance = currentDistance;
				smallIndex = i;
			}
		}
		
		//add to cluster nearest
		macroClusters.get(smallIndex).attachPoint(d);;
		return minDistance;
	}
	
	/*
	 * Function which turns microclusters into pseudopoints (the summary of microclusters).
	 */
	public void createPseudoPoints(double nPlof){
		double minRating = 1;
		double maxRating = 0;
		pseudoPoints = new ArrayList<PseudoPoint>();
		if(removal){
			removeOutliers(nPlof);
		}
		for(int i = 0; i < microClusters.size();i++){
			MicroCluster m = microClusters.get(i);

			DataPoint centroid = m.recalculateCentroid();
			int label = m.getLabel();
			int weight = m.getDataPoints().size();
			double clusterRating = m.genLoopRating(nPlof);
			if(clusterRating < minRating){
				minRating = clusterRating;
			}else if(clusterRating > maxRating){
				maxRating = clusterRating;
			}
			//System.out.println(m);
			pseudoPoints.add(new PseudoPoint(centroid,weight,label, clusterRating));
		}
		System.out.println("Min Rating: " + minRating);
		System.out.println("Max Rating: " + maxRating);
		numPseudo = pseudoPoints.size();
	}
	
	private void removeOutliers(double nPlof){
		for(MicroCluster m: microClusters){
			LinkedList<DataPoint> appendedPoints = new LinkedList<DataPoint>();
			for(DataPoint d: m.getDataPoints()){
				double loop = Loop.loop(d, m, nPlof);
				if(loop <= OUTLIERTHRESHOLD){
					appendedPoints.add(d);
				}
			}
			m.setPoints(appendedPoints);
			m.recalculateCentroid();
		}
	}
	
	/*
	 * Label propagation algorithm:
	 * Uses label propagation to assign a label to unlabelled pseudopoints.
	 */
	public void propagateLabels(double r, double stddev,ArrayList<Model> contig){
		this.stddev = stddev;
		//working list contains the pseudopoints from this model, but also labelled pseudopoints
		//from previous clusters.
		ArrayList<PseudoPoint> workingList  = new ArrayList<PseudoPoint>();
		int vectorLength = pseudoPoints.size();
		for(int i = 0; i < vectorLength; i++){
			workingList.add(pseudoPoints.get(i));
		}
		
		//add these labelled microcluster from previous models to the current set.
		for(Model m: contig){
			ArrayList<PseudoPoint> temp = m.getPseudo();
			for(int i = 0; i < temp.size(); i++){
				if(temp.get(i).isLabelled()){
					workingList.add(temp.get(i));
					vectorLength++;
					//System.out.println("Is labelled:" + temp.get(i));
				}
			}
		}
		
		Collections.sort(workingList);
		Collections.sort(pseudoPoints);
		//weight 2d array
		double[][] weights = new double[vectorLength][vectorLength];
		
		//the diagonal 2d contains the sum of a ith row at entry [i][i]
		double[][] diag = new double[vectorLength][vectorLength];
		//constructing the weight matrix
		for(int i = 0; i < vectorLength; i++){
			double counter= 0;
			for(int j = 0; j < vectorLength; j++){
				if(i ==j){	
					weights[i][j] =0;
					continue;
				}
				PseudoPoint ith = workingList.get(i);
				PseudoPoint jth = workingList.get(j);
				weights[i][j] = weightFunction(ith.getCentroid(), jth, stddev);
				if(ratingClusters){
					weights[i][j] *= jth.getClusterRating();
				}
				counter += weights[i][j];
			}
			diag[i][i] = counter;
		}
		
		for(int i = 0; i < vectorLength; i++){
			diag[i][i] = ((double)1)/Math.sqrt(diag[i][i]);
		}
		
		//convert 2d arrays to matrices
		Matrix d = new Matrix(diag);
		Matrix w = new Matrix(weights);

		//laplacian normalisation
		Matrix p = d.times(w).times(d);
		double[][] normLaplace = p.getArray();
		
		double[][] tZero = new double[vectorLength][c+1];
		
		//tracks predicted class for each pseudopoint
		int[] classTracker = new int[vectorLength];
		
		//create the vectors for each pseudopoint based on their classes. 
		//if labelled x, the xth value in vector should be set and the rest should be 0.
		//if unlabelled, all values should be set to 0.
		for(int i = 0; i < vectorLength; i++){
			//increment by one in previous value takes the -1 class of unlabelled points to 0.
			//hence indexable. So in this calculation, stuff indexed by 0 is unlabelled and class 0 is indexed by one,etc.
			int curClass = workingList.get(i).getLabel()+1;
			classTracker[i] = curClass;
			for(int j = 0; j < c+1; j++){
				if(j == curClass && curClass != 0){
					tZero[i][j] = 1;
				}else{
					tZero[i][j] = 0;
				}
				//System.out.print( tZero[i][j]+" ");
			}
			//System.out.println();
		}
		Matrix tZeroMat = new Matrix(tZero);
		Matrix currentT = tZeroMat;
		Matrix prevT = tZeroMat;
		boolean converge = false;
		int iterations = 0;
		//iteratively apply the label propagation until point changes classes.
		while(!converge){
			prevT = currentT;
			currentT = p.times(prevT).times(ALPHA).plus(tZeroMat.times(1-ALPHA));
			
			//tZero = currentT.getArray();
			converge = true;
			for(int i = 0; i < vectorLength; i++){
				//find current predicted class of I by finding max value
				int maxIndex = 0;
				double maxValue = 0;
				for(int j = 0; j < c+1; j++){
					if(currentT.get(i,j) > maxValue){
						maxValue = currentT.get(i,j);
						maxIndex = j;
					}
				}
				if(maxIndex != classTracker[i]){
					converge = false;
				}
				classTracker[i] = maxIndex;
			}
			iterations++;
		}
		
		System.out.println("Label prop converges after Iterations: " + iterations);
		//match the points in the working set to the original pseudo-points by comparing centroids
		for(int i = pseudoPoints.size()-1; i >= 0;i--){
			for(int k = vectorLength-1; k >= 0;k--){
				if(workingList.get(k).getCentroid() != pseudoPoints.get(i).getCentroid()){
					continue;
				}
				int index = -1;
				double maxValue = 0;
				//find the class predicted
				for(int j = 0; j < c+1; j++){
					if(currentT.get(i,j) > maxValue){
						index = j;
						maxValue = currentT.get(i,j);
					}
				}
				//label point if point is unlabelled already. Remember to decrement 
				//to get the correct corresponding class, since class 0 is unlabelled for
				//the purposes of label propagation.
				if(pseudoPoints.get(i).getLabel() == -1){
					pseudoPoints.get(i).setClass(index-1);
				}

				break;
			}
			
		}
	}
	/*
	 * Weight function for the label propagation.
	 */
	private double weightFunction(DataPoint d, PseudoPoint p, double stddev){
		double distance = d.getDistanceValue(p.getCentroid());
		return  Math.exp(-1*(distance/(2*stddev*stddev)))*p.getWeight();
	}
	
	/*
	 This function predicts the label of a given point against 
	 the complete model. Returns a matrix which will be combined with other models
	 when making predictions against the ensemble.
	 */
	
	public Matrix predictLabel(DataPoint d, double stddev){
		double epsilon = 1e-10;
		Matrix predictMatrix = new Matrix(1, c+1);
		double normalisation = epsilon;
		
		// determine similarity of point relative to each 
		for(PseudoPoint p: pseudoPoints){
			if(p.getLabel() == -1){
				continue;
			}
			Matrix pointMatrix = new Matrix(1,c+1);
			double weight = weightFunction(d,p,stddev);
			if(ratingClusters){
				weight *= p.getClusterRating();
			}
			pointMatrix.set(0, p.getLabel()+1, weight);

			predictMatrix.plusEquals(pointMatrix);
			
			normalisation += weight;
		}

		predictMatrix.timesEquals(((double)1)/normalisation);
		return predictMatrix;
	}
	/*
	 * This function makes a definitive prediction for the class of a point
	 * against a single model, rather than returning the vector. It does this by
	 * labelling the class as the largest of the values in the vector.
	 */
	public int predictLabelValue(DataPoint d, double e) {
		Matrix p = predictLabel(d,e);
		double maxValue = 0;
		int predictedClass = -1;
		for(int i = 0; i < c+1; i++){
			if(maxValue < p.get(0, i)){
				maxValue = p.get(0,i);
				predictedClass = i-1;
			}
		}
		return predictedClass;
	}



	/*
	 * This function is used when pruning pseudopoints based on accuracy. It assigns each point to its nearest pseudopoint,
	 * so that other functions can calculate teh accuracy of this pseudopoint and delete it if necessary.
	 */
	private boolean attachToNearest(ArrayList<LinkedList<DataPoint>> neighbours, DataPoint currentPoint){
		PseudoPoint currentPseudo;
		double minDistance = Double.MAX_VALUE;
		int minIndex = -1;
		for(int j  = 0; j < pseudoPoints.size(); j++){
			if(currentPoint.getDistanceValue(pseudoPoints.get(j).getCentroid()) < minDistance){
				minDistance = currentPoint.getDistanceValue(pseudoPoints.get(j).getCentroid());
				minIndex = j;
			}
		}
		if(minIndex != -1){
			neighbours.get(minIndex).add(currentPoint);
			currentPseudo = pseudoPoints.get(minIndex);
			if(currentPoint.getLabel() == currentPseudo.getLabel()){
				currentPseudo.accurateNeighbour();
			}else{
				currentPseudo.inAccurateNeighbour();
			}
		}else{
			//because no minimal distance was chosen, there are no pseudopoints in the model, and it will classify
			// with 0 accuracy
			return false;
		}
		return true;
	}
	
	private boolean filterPseudos(ArrayList<LinkedList<DataPoint>> neighbours){
		PseudoPoint currentPseudo;
		boolean restart = false;
		double removalCounter = 0;
		double boundary = 0.7;
		Collections.sort(pseudoPoints, new Comparator<PseudoPoint>(){
		    public int compare(PseudoPoint s1, PseudoPoint s2) {
		        if(s1.getAccuracy() < s2.getAccuracy()){
		        	return -1;
		        }else if(s1.getAccuracy() == s2.getAccuracy()){
		        	return 0;
		        }
		        return 1;
		    }
		});
		int startingPseudo = pseudoPoints.size();
		for(int i = 0; i < pseudoPoints.size(); i++){
			currentPseudo = pseudoPoints.get(i);
			//System.out.println("Removal chcek: " + removedPseudo/numPseudo);
			if(currentPseudo.getAccuracy() < boundary && removalCounter/startingPseudo < 0.5 && removedPseudo/numPseudo < 0.8){
				pseudoPoints.remove(i);
				LinkedList<DataPoint> temp = neighbours.get(i);
				neighbours.remove(i);
//				for(DataPoint d: temp){
//					attachToNearest(neighbours,d);
//				}
				i--;
				removedPseudo++;
				removalCounter++;
				
			}
			
		}
		//System.out.println("Removed "+ removalCounter + " pseudoPoints from model " + getIndex());
		return true;
	}
	
	public void accuracyCheck(ArrayList<DataPoint> trainingData2) {
		//System.out.println("Model " + getIndex() + " had " + pseudoPoints.size() + " pseudoPoints");
		ArrayList<LinkedList<DataPoint>> neighbours = new ArrayList<LinkedList<DataPoint>>(pseudoPoints.size());
		for(int i = 0; i < pseudoPoints.size(); i++){
			neighbours.add(new LinkedList<DataPoint>());
			pseudoPoints.get(i).resetAccuracy();
		}
		PseudoPoint currentPseudo;
		//for every datapoint in the training set, find the pseudopoint which is closest to it. 
		for(int i = 0; i < trainingData2.size(); i++){
			DataPoint currentPoint = trainingData2.get(i);
			attachToNearest(neighbours,currentPoint);
		}
		int removalCounter = 0;
		filterPseudos(neighbours);

		//System.out.println("Model " + getIndex() + " now has " + pseudoPoints.size() + " pseudoPoints");
		
	}
	
	public double classificationScore(ArrayList<DataPoint> data){
		double misClassify = 0;

		for(DataPoint d: data){
			int predicted = predictLabelValue(d, stddev);
			if(d.getLabel() != predicted){
				//System.out.println("Predicted label: " + currentMod.predictLabelValue(d, 0.25) + " and the actual label is " + d.getLabel());
				misClassify++;
			}

		}

		double rating = 1-(double)misClassify/data.size();

		return rating;
	}

	public void setNumClass(int numClasses2) {
		this.c = numClasses2;
	}

	
	
}
