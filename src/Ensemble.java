import java.util.ArrayList;

import Jama.Matrix;


public class Ensemble {
	private ArrayList<Model> ensemble;
	private ArrayList<Model> contig;
	private ArrayList<Integer> numOfClass;
	private int numModels;
	private int totalPoints;
	private int maxModels;
	private int k; 
	private int success;
	private int classifications;
	
	public Ensemble(int l, int k, int c){
		maxModels = l;
		numModels = 0;
		this.numOfClass = new ArrayList<Integer>();
		this.k = k;
		ensemble = new ArrayList<Model>();
		contig = new ArrayList<Model>();
		success = 0;
		classifications = 0;
		totalPoints = 0;
	}
	
	public void countNewDataPoints(ArrayList<DataPoint> data){
		for(DataPoint d: data){
			while(d.getLabel() >= numOfClass.size()){
				numOfClass.add(0);
			}
			if(d.isLabeled()){
				numOfClass.set(d.getLabel(), numOfClass.get(d.getLabel())+1);
				totalPoints++;
			}
		}
	}
	
	public int getTotalPoints(){
		return totalPoints;
	}
	public int[] getClassCounter(){
		int[] result = new int[numOfClass.size()];
		for(int i = 0; i < numOfClass.size(); i++){
			result[i] = numOfClass.get(i);
		}
		return result;
	}
	
	
	
	public double getAccuracy(){
		if(classifications == 0){
			return 1;
		}
		return (double)success / classifications;
	}
	
	private void updateContig(Model m){
		if(contig.size() == 3){
			contig.remove(0);
		}
		contig.add(m);
		
	}
	
	public void addModel(Model m){
		updateContig(m);
		
		
		if(numModels < maxModels){
			ensemble.add(numModels++,m);
//			for(Model x: ensemble){
//				double rating = x.classificationScore(m.getTrainingData());
//				//System.out.println("Model " + x + "  has an accuracy of " + rating + " and size of " + x.getPseudo().size());
//			}
		}
		else{
			refineEnsemble(m);
			//accuracyCheck(m);
			updateEnsemble(m);
		}
	}
	

	public void expandClasses(int c){
		for(Model m : ensemble){
			while(m.getSeenClass().size() < c){
				m.getSeenClass().add(false);
				m.incrementClass();
			}
		}
	}
	
	private void refineEnsemble(Model m) {
		ArrayList<Integer> unseenClasses = new ArrayList<Integer>();
		ArrayList<Boolean> mseen = m.getSeenClass();
		System.out.println("In refine ensemble the num of classes is " + m.getNumClass() + " and the length of the seen vector is " + mseen.size() );
		for(int i = 0; i < m.getNumClass(); i++){
			if(!mseen.get(i)){
				continue;
			}
			boolean evolved = true; 
			for(Model x : ensemble){
				while(x.getSeenClass().size() < i){
					x.getSeenClass().add(false);
					x.incrementClass();
				}
				if(x.getSeenClass().get(i)){
					evolved = false;
					break;
				}
			}
			if(evolved){
				unseenClasses.add(i);
			}
		}
		for(Integer unseen : unseenClasses){
			for(PseudoPoint p : m.getPseudo()){
				if(p.getLabel() == unseen){
					for(Model x : ensemble){
						int record = x.getPseudo().size();
						if(x.getPseudo().size() > k){
							
							mergePoints(x);
							

						}
						x.getPseudo().add(p);
						System.out.println("After merge and insert size has gone from "+ record +" to " + x.getPseudo().size());
					}
					break;
				}
			}
		}
		
	}
	
	private void accuracyCheck(Model m){
		ArrayList<DataPoint> trainingData = m.getTrainingData();
		for(Model x: ensemble){
			x.accuracyCheck(trainingData);
		}
		
	}

	private void mergePoints(Model x) {
		ArrayList<PseudoPoint> p = x.getPseudo();
		int length = p.size();
		PseudoPoint one;
		PseudoPoint two;
		int y = 0,z =0;
		double minDistance = Double.MAX_VALUE;
		for(int i = 0; i < length; i++){
			for(int j = i+1; j < length; j++){
				one = p.get(i);
				two = p.get(j);
				if(one.getLabel() != two.getLabel()){
					continue;
				}
				double distance = one.getCentroid().getDistanceValue(two.getCentroid());
				if(minDistance > distance){
					distance = minDistance;
					y = i;
					z = j;
				}
			}
		}
		//merge points i and j	
		one = p.get(y);
		two = p.get(z);
		one.merge(two);
		p.remove(z);
		p.remove(y-1);
		p.add(one);
		
	}

	private void updateEnsemble(Model m) {
		ArrayList<DataPoint> trainingData = m.getTrainingData();
		ensemble.add(m);
		int worstEnsemble = -1;
		int numTraining = trainingData.size();
		double minAccuracy = 1.5;
		for(int i = 0; i < ensemble.size(); i++){
			Model currentMod = ensemble.get(i);
			double rating = currentMod.classificationScore(trainingData);
			if(rating < minAccuracy){
				minAccuracy = rating;
				worstEnsemble = i;
			}
			System.out.println("Model " + currentMod.getIndex() + "  has an accuracy of " + rating);
		}
		System.out.println("Model " + ensemble.get(worstEnsemble).getIndex() + " sucks hard with accuracy " + minAccuracy);
		ensemble.remove(worstEnsemble);
	}

	
	public int predictPoint(DataPoint d, int numClasses,boolean accuracy){
		int predictedClass = -1;
		Matrix classVector = new Matrix(1,numClasses+1);
		Matrix predictor  = null;
		//System.out.println("Class vector dimensions:"  + 1 + " "+(numClasses+1));
		int counter = 0;
		double[][] record = new double[6][];
		for(Model m: ensemble){
			if(numClasses > m.getNumClass()){
				m.setNumClass(numClasses);
			}
			
			predictor = m.predictLabel(d, 0.25);
			record[counter] = predictor.getArray()[0];
//			System.out.print("Predictor for model:" + (counter++) + " ");
//			for(int i = 0; i < classVector.getColumnDimension(); i++){
//				System.out.print(predictor.get(0, i) + " ");
//			}
//			System.out.println();
			counter++;
			classVector.plusEquals(predictor);

		}
//		System.out.println("For final prediction:");
//		for(int i = 0; i < classVector.getColumnDimension(); i++){
//			System.out.print(classVector.get(0, i) + " ");
//		}
//		System.out.println("\n");
		//System.out.println("Dimensions of predictor: " + predictor.getRowDimension() + " " +predictor.getColumnDimension());
		//System.out.println("Dimensions of class vector: " + classVector.getRowDimension() + " " + classVector.getColumnDimension());
		double maxValue = 0;
		for(int i = 0; i < numClasses+1; i++){
			if(maxValue < classVector.get(0, i)){
				maxValue = classVector.get(0,i);
				predictedClass = i-1;
			}
		}
		//System.out.println("Predicted class for this point is" + predictedClass);
//		if(predictedClass != d.getActualLabel()){
//			System.out.println("Incorrect Point -- Label:" + d.getActualLabel() + " Predicted Label: " + predictedClass);
//		}
		if(accuracy){
			if(predictedClass == d.getActualLabel()){
				success++;
			}else{
//				System.out.println("Incorrect Point -- Label:" + d.getActualLabel() + " Predicted Label: " + predictedClass);
//				for(int i = 0; i < numModels; i++){
//					System.out.print("Predictor for model "+ i + ": ");
//					for(int j = 0; j < record[i].length; j++){
//						System.out.print(record[i][j] + " ");
//					}
//					System.out.println();
//				}
			}
			classifications++;
		}
		return predictedClass;
		
	}
	
	public void predictChunkForClustering(DataChunk d){
		for(DataPoint x: d.getDataPointArray()){
			if(!x.isLabeled()){
				x.setPredictedLabel(predictPoint(x,ensemble.get(0).getNumClass(),true));
			}
		}
		
	}
	
	public void predictChunk(DataChunk d){
		int zeroCounter = 0;
		int oneCounter = 0;
		int firstSuccess = success;
		int firstClassifications = classifications;
		for(DataPoint x: d.getTestData()){
			int prediction = predictPoint(x,ensemble.get(0).getNumClass(),true);
			//if(!x.isLabeled()){
				if(prediction == 1){
					oneCounter++;
				}else if(prediction == 0){
					zeroCounter++;
				}else{
					System.out.println(prediction + " was predicted");
				}
				
//			}else{
//				System.out.println("This is broken");
//			}
		}
		System.out.println("Zero was predicted: " + zeroCounter + " times");
		System.out.println("One was predicted: " + oneCounter + " times");
		System.out.println("Accuracy on this chunk:" + ((double)(success-firstSuccess))/(classifications-firstClassifications));
		
	}

	public ArrayList<Model> getContig() {
		// TODO Auto-generated method stub
		return contig;
	}
}
