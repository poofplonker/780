import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import Jama.Matrix;
import cern.jet.random.engine.MersenneTwister;


public class Model {
	private int chunkSize;
	private int index;
	private ArrayList<MacroCluster> macroClusters;
	private ArrayList<MicroCluster> microClusters;
	private ArrayList<Boolean> seenClass;
	private ArrayList<PseudoPoint> pseudoPoints;
	private ArrayList<DataPoint> trainingData;
	private DataChunk dataChunk;
	private int totalPoints;
	private int[] classData;
	private int k;
	private double removedPseudo;
	private int numPseudo;
	private int c;
	
	public Model(MersenneTwister twister, DataChunk chunk, int k, int c, int[] classData, int totalPoints, int index){
		this.dataChunk = chunk;
		this.chunkSize = chunk.getChunkSize();
		this.seenClass = chunk.seenClass(c);
		this.trainingData = chunk.getTrainingData();
		this.k = k;
		this.c = c;
		this.index = index;
		this.classData = classData;
		this.totalPoints = totalPoints;
		clusterData(twister);
		genMicroClusters();
		double nPlof = Loop.NPlof(microClusters);
		createPseudoPoints(nPlof);
		System.out.println("Model gen done");
	}
	
	public int getIndex(){
		return index;
	}
	
	public void incrementClass(){
		c++;
	}
	
	public ArrayList<DataPoint> getTrainingData(){
		return this.trainingData;
	}
	public ArrayList<PseudoPoint> getPseudo(){
		return pseudoPoints;
	}
	
	private void genMicroClusters() {
		//count the number of microclusters that will be required
		//for each cluster, examine its points and place in appropriate microcluster.
		microClusters = new ArrayList<MicroCluster>();


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
						microClusters.add(new MicroCluster(p,true,p.getLabel()));
						microClusters.get(base++).attachPoint(p);
					}
				}else{
					if(classToClusterUnlabelled.containsKey(p.getPredictedLabel())){
						microClusters.get(classToClusterUnlabelled.get(p.getPredictedLabel())).attachPoint(p);
					}else{
						classToClusterUnlabelled.put(p.getPredictedLabel(), base);
						microClusters.add(new MicroCluster(p,false,-1));
						//System.out.println("Label of unlabelled point is:" + p.getLabel());
						microClusters.get(base++).attachPoint(p);
					}
				}
			}
		}
		for(MicroCluster m: microClusters){
			m.recalculateCentroid();
			//System.out.println(m);
		}
		System.out.println("Number of micro-clusters: " + microClusters.size());
	}
	
	//this is supposed to be based on class values in the entire dataset, not for datachunks. TBF
	private void initialiseClusters(MersenneTwister twister){
		int base = 0;
		//precompute the number of centroids per class
		int[] centroidCounter = new int[c];
		ArrayList<DataPoint> dataPoints = dataChunk.getDataPointArray();
		int[] classCounter = dataChunk.getClassCounter(c);
		int totalAssigned = 0;
		for(int j = 0; j < c && dataChunk.getNumLabelledPoints() != 0; j++){
			centroidCounter[j] = Math.round((k*classCounter[j])/totalPoints);
			//System.out.println("We need " + centroidCounter[j] + " centroids to be initialised as class " + j);
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
			
			if(currentPoint.isLabeled()){
				//System.out.println("Adding a point to " + currentPoint.getLabel());
				classPoints.get(currentPoint.getLabel()).add(currentPoint);
			}
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
				//System.out.println("Using farthest first");
				boolean[] contained = new boolean[thisClassPoints.size()];
				int index = Math.abs(twister.nextInt()) % thisClassPoints.size();
				//System.out.println("Initial Index : " + index);
				DataPoint centroid = thisClassPoints.get(index);
				//System.out.println("Initial centroid: " + centroid);
				visitedSet.add(centroid);
				contained[index] = true;
				i++;
				//System.out.println("After 1 addition, size of visitedSet " + visitedSet.size() + " and size of classPoints: " + thisClassPoints.size());
				while(i < representation){
					double maxDistance = -1;
					int maxIndex = -1;
					for(int d = 0; d < thisClassPoints.size(); d++){
						if(contained[d] == true){
							continue;
						}
						double thisDistance = 0;
						for(DataPoint c : visitedSet){
							 thisDistance += c.getDistanceValue(thisClassPoints.get(d));
							 //System.out.println("Distance from point " + c.getAbsoluteIndex() + " to point " + thisClassPoints.get(d).getAbsoluteIndex() + " : " + c.getDistanceValue(thisClassPoints.get(d)));
						}
						//System.out.println("Point: " + thisClassPoints.get(d) + "is " + thisDistance + " distance away");
						if(thisDistance > maxDistance){
							maxDistance = thisDistance;
							maxIndex = d;
						}
					}
					contained[maxIndex] = true;
					visitedSet.add(thisClassPoints.get(maxIndex));
					//System.out.println("Added " + thisClassPoints.get(maxIndex) + " as the furthest away point with distance " + maxDistance);
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
				MacroCluster temp = new MacroCluster(d,i+base,c);
				temp.attachPoint(d);
				d.setCentroid();
				macroClusters.add(temp);
				
				i++;
			}
			base += representation;
		}
		System.out.println("Number of generated macro-clusters: " + base);
	}

	public ArrayList<MacroCluster> clusterData(MersenneTwister twister){
		ArrayList<MacroCluster> clusters = new ArrayList<MacroCluster>(k);
		this.macroClusters = clusters;
		ArrayList<DataPoint> dataPoints = dataChunk.getDataPointArray();
		//Select centroids proportionate to class representation for initialisation.
		
		initialiseClusters(twister);
		/*for(int i = 0; i < k; i++){
			System.out.println("Macrocluster "+ i + " initial centroid: " + macroClusters.get(i).getCentroid());
		}*/
		//initialise all dataPoints to cluster with nearest centroid.
		double totalValue = 0;
		for(DataPoint d: dataPoints){
			//CHECK TO SEE D WAS NOT ALREADY IN THE CLUSTER
			if(!d.isCentroid()){
				totalValue += attachToNearestImp(d);
			}
		}
		/*for(DataPoint d: dataPoints){
			System.out.println("Point " + d.getAbsoluteIndex() + " is in cluster " + d.getClusterIndex());
		}*/
		

		//System.out.println("Total Value for EM: " + totalValue);
		expectationMinimisation(clusters,dataPoints);
//		for(int i = 0; i < clusters.size(); i++){
//			System.out.println("Cluster " + i + " has " + clusters.get(i).totalPoints + " points and " + clusters.get(i).countNumClasses()+ "classes");
////			for(int j = 0; j < clusters.get(i).totalPoints; j++){
////				DataPoint temp = clusters.get(i).getDataPoints().get(j);
////				//System.out.println("\t" + temp.getLabel() + " distance to cent:" + temp.getDistanceValue(clusters.get(i).getCentroid()) + "Averaage dist to others: " + temp.getAverageDist());
////			}
//		}
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
				//System.out.print("New centroid for cluster " + i +": " + newCentroid);
				//System.out.println();
				clusters.get(i).setCentroid(newCentroid);;
			}
			//attach all points to position geometrically nearest to them
			Collections.shuffle(dataPoints);
			double totalValue = 0;
			for(DataPoint d: dataPoints){
				clusters.get(d.getClusterIndex()).removePoint(d);
				int prevSmallIndex = d.getClusterIndex();
				totalValue += attachToNearestImp(d);
				if(d.getClusterIndex() != prevSmallIndex){
					//System.out.println("Point " + d.getAbsoluteIndex() +" with label " + d.getLabel() + " has moved from " + prevSmallIndex + " to " + d.getClusterIndex());
					clusters.get(prevSmallIndex).setChanged();
					clusters.get(d.getClusterIndex()).setChanged();
					changedByRecalcCent++;
					pointsChanged++;
					//System.out.println("Point " + d.getAbsoluteIndex() + " was in " + prevSmallIndex + " and is now in " + d.getClusterIndex());
				}
			}
		
			iterations++;
			//Total value calculated to ensure that expectation minimisation is in fact converging.
			if(iterations > 1000){
				System.out.println("EM broken by iteration limit");
				break;
			}
			//System.out.println("Total Value for EM: " + totalValue);
			//System.out.println("Points changed: " + pointsChanged);
//			System.out.println("Points changed by the change in centroids: " + changedByRecalcCent);
			/*for(DataPoint d: dataPoints){
				System.out.println("Point " + d.getAbsoluteIndex() + " is in cluster " + d.getClusterIndex());
			}*/
			//System.out.println("PrevEm: " + prevEm+", diff:" + (totalValue-prevEm));

			prevEm = totalValue;
			//System.out.println("Iterations: " + iterations);
		}
		for(int i = 0; i < k; i++){
			DataPoint newCentroid = clusters.get(i).recalculateCentroid();
			clusters.get(i).setCentroid(newCentroid);
		}
		System.out.println("EM Done in " + iterations + " iterations.");
		
	}

	private double attachToNearestImp(DataPoint d){
		double minDistance = Double.MAX_VALUE;
		int smallIndex = 0;
		d.resetAverageDist();
		//see which centroid is closest
		for(int i = 0; i < k; i++){
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
		macroClusters.get(smallIndex).attachPoint(d);
		//System.out.println("Attached point " + d.getAbsoluteIndex() + " to cluster " + smallIndex);
		//System.out.println("Distance to cluster: " + minDistance);
		return minDistance;
	}
	
	public void createPseudoPoints(double nPlof){
		double minRating = 1;
		double maxRating = 0;
		pseudoPoints = new ArrayList<PseudoPoint>();
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
	
	public void propagateLabels(double r, double stddev,ArrayList<Model> contig){
		ArrayList<PseudoPoint> workingList  = new ArrayList<PseudoPoint>();
		int vectorLength = pseudoPoints.size();
		for(int i = 0; i < vectorLength; i++){
			workingList.add(pseudoPoints.get(i));
		}
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
//		for(int i = 0; i < vectorLength; i++){
//			System.out.println(pseudoPoints.get(i).toString());
//		}
		//System.out.println();
		double alpha = 0.99;
		double[][] weights = new double[vectorLength][vectorLength];
		double[][] diag = new double[vectorLength][vectorLength];
		double[][] diagOther = new double[vectorLength][vectorLength];
		double[] colCounter = new double[vectorLength];
		//System.out.println("Testing weights:");
		for(int i = 0; i < vectorLength; i++){
			double counter= 0;
			//System.out.println("For point of label " + workingList.get(i).getLabel());
			double distToZero = 0;
			int zeroCount = 0;
			double distToOne = 0;
			int oneCount = 0;
			for(int j = 0; j < vectorLength; j++){
				if(i ==j){	
					weights[i][j] =0;
					//System.out.print(weights[i][j] + " ");
					continue;
				}
				PseudoPoint ith = workingList.get(i);
				PseudoPoint jth = workingList.get(j);
				double distance = ith.getCentroid().getDistanceValue(jth.getCentroid());

				//System.out.println("Distance for  " + i + " "+ j + ": "+ distance );
				weights[i][j] = Math.exp(-1*(distance/(2*stddev*stddev)))*jth.getWeight();
				//sSystem.out.println("Associated weight:" + Math.exp(-1*(distance/(2*stddev*stddev))));
				counter += weights[i][j];
				colCounter[j] += weights[i][j];
				//System.out.print(weights[i][j] + " ");
				if(workingList.get(j).getLabel() == 0){
					distToZero += weights[i][j];
					zeroCount++;
				}else if(workingList.get(j).getLabel() == 1){
					distToOne += weights[i][j];
					oneCount++;
				}

			}
			diag[i][i] = counter;
			//System.out.println("Total distance to zero labelled centroids:" + distToZero/zeroCount );
			//System.out.println("Total distance to one labelled centroids:" + distToOne/oneCount );
			//System.out.println();
		}
		//System.out.println("Diagonal:");
		for(int i = 0; i < vectorLength; i++){
			//System.out.println("Diag new: " + diag[i][i]);
			diag[i][i] = ((double)1)/Math.sqrt(diag[i][i]);
			diagOther[i][i] = ((double)1)/Math.sqrt(colCounter[i]); 
			//System.out.println("Diag after: " + diag[i][i]);
		}
		//System.out.println();
		Matrix dOther = new Matrix(diagOther);
		Matrix d = new Matrix(diag);
		Matrix w = new Matrix(weights);
		//System.out.println("Laplacian normalised:");
		//laplacian normalisation
		Matrix p = d.times(w).times(d);
		double[][] normLaplace = p.getArray();
		for(int i = 0; i < vectorLength; i++){
			double counter = 0;
			double counterother = 0;
			for(int j = 0; j < vectorLength; j++){
				//System.out.println("Actual: " + normLaplace[i][j] + " expected:" + weights[i][j]*diag[i][i]*diag[j][j]);
				counter += normLaplace[j][i];
				counterother += normLaplace[i][j];
			}
//			System.out.println("");
//			System.out.println("Weight of " + i + " is " + workingList.get(i).getWeight());
//			System.out.println("Level of influence exerted by " + i +" is : " + counter );
//			System.out.println("Level of influence exerted on " + i +" is : " + counterother );
		}

		double[][] tZero = new double[vectorLength][c+1];
		for(int i = 0; i < vectorLength; i++){
			int curClass = workingList.get(i).getLabel()+1;
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
		double epsilon = 0.001;
		while(!converge){
			prevT = currentT;
			currentT = p.times(prevT).times(alpha).plus(tZeroMat.times(1-alpha));
			
			//tZero = currentT.getArray();
			converge = true;
			for(int i = 0; i < vectorLength; i++){
				for(int j = 0; j < c+1; j++){
					if(Math.abs(prevT.get(i, j) - currentT.get(i,j)) > epsilon){
						converge = false;
					}
				}
			}
			iterations++;
		}
		/*for(int i = 0; i < vectorLength; i++){
			System.out.print("microCluster " + i +": ");
			for(int j = 0; j < c+1; j++){
				System.out.print( currentT.get(i,j)+" ");
			}
			System.out.println();
		}*/
		System.out.println("Label prop converges after Iterations: " + iterations);
		
		/*HAVE PROBLEM - SINCE INCORPORATING PREVIOUS MICROCLUSTERS, 
		 CANNOT CORRESPOND BETWEEN POINT GENERATED IN LABEL PROP AND 
		 UNLABELLED MICROCLUSTER IN CURRENT
		 */
		for(int i = pseudoPoints.size()-1; i >= 0;i--){
			for(int k = vectorLength-1; k >= 0;k--){
				if(workingList.get(k).getCentroid() != pseudoPoints.get(i).getCentroid()){
					continue;
				}
				int index = -1;
				double maxValue = 0;
				for(int j = 0; j < c+1; j++){
					if(currentT.get(i,j) > maxValue){
						index = j;
						maxValue = currentT.get(i,j);
					}
				}
				if(pseudoPoints.get(i).getLabel() == -1){
					pseudoPoints.get(i).setClass(index-1);
				}
//				}else{
//					if(pseudoPoints.get(i).getLabel() != index-1){
//						System.out.println("Point " + i + " has been mispredicted in label propagation: was" + pseudoPoints.get(i).getLabel() + " but now "+ (index-1));
//					}
//				}
				break;
			}
			
		}
//		for(int i = 0; i < vectorLength; i++){
//			System.out.println("Point "+ i + ": " + pseudoPoints.get(i).getLabel());
//		}
		System.out.println("Number of pseudoPoints generated:" + vectorLength);
	}
	
	public Matrix predictLabel(DataPoint d, double stddev){
		double epsilon = 1e-10;
		Matrix predictMatrix = new Matrix(1, c+1);
		double normalisation = epsilon;
		for(PseudoPoint p: pseudoPoints){
			if(p.getLabel() == -1){
				continue;
			}
			Matrix pointMatrix = new Matrix(1,c+1);
			double distance = d.getDistanceValue(p.getCentroid());
			//System.out.println("Distance from point to pseudo: "+ distance );
			double weight = Math.exp(-1*(distance/(2*stddev*stddev)))*p.getWeight()/**p.getClusterRating()*/;
			//System.out.println("Associated weight:" + Math.exp(-1*(distance/(2*stddev*stddev))));

			
			pointMatrix.set(0, p.getLabel()+1, weight);

			predictMatrix.plusEquals(pointMatrix);
			
			normalisation += weight;
		}
//		System.out.println("Before normalisation:");
//		for(int i = 0; i < c+1; i++){
//			System.out.print(predictMatrix.get(0, i) + " ");
//		}
//		System.out.println();
		predictMatrix.timesEquals(((double)1)/normalisation);
		//System.out.println("Normalisation: "+ normalisation );
//		System.out.println("After:");
//		
//		for(int i = 0; i < c+1; i++){
//			System.out.print(predictMatrix.get(0, i) + " ");
//		}
//		System.out.println();
		return predictMatrix;
	}

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

	public ArrayList<Boolean> getSeenClass() {
		// TODO Auto-generated method stub
		return seenClass;
	}

	public int getNumClass() {
		return c;
	}

	
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
		int zeroCounter = 0;
		int oneCounter = 0;
		for(DataPoint d: data){
			int predicted = predictLabelValue(d, 0.25);
			if(d.getLabel() != predicted){
				//System.out.println("Predicted label: " + currentMod.predictLabelValue(d, 0.25) + " and the actual label is " + d.getLabel());
				misClassify++;
			}
			if(predicted ==1){
				oneCounter++;
			}else if(predicted == 0){
				zeroCounter++;
			}
		}
		//System.out.println("For single model 0 was predicted: " + zeroCounter +" times");
		//System.out.println("For single model 1 was predicted: " + oneCounter +" times");
		double rating = 1-(double)misClassify/data.size();
		//System.out.println("Misclassify:" + misClassify);
		//System.out.println("Number of tests: " + data.size());
		return rating;
	}

	public void setNumClass(int numClasses2) {
		this.c = numClasses2;
	}

	
	
}
