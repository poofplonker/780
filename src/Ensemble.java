import java.util.ArrayList;

import Jama.Matrix;


public class Ensemble {
	private ArrayList<Model> ensemble;
	private int numModels;
	private int maxModels;
	private int k; 
	private int success;
	private int classifications;
	
	public Ensemble(int l, int k){
		maxModels = l;
		numModels = 0;
		this.k = k;
		ensemble = new ArrayList<Model>();
		success = 0;
		classifications = 0;
	}
	
	public double getAccuracy(){
		if(classifications == 0){
			return 0;
		}
		return (double)success / classifications;
	}
	public void addModel(Model m){
		refineEnsemble(m);
		if(numModels < maxModels){
			ensemble.add(numModels++,m);
		}
		else{
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
						if(x.getPseudo().size() > k){
							int record = x.getPseudo().size();
							mergePoints(x);
							x.getPseudo().add(p);
							//System.out.println("After merge and insert size has gone from "+ record +" to " + x.getPseudo().size());
						}
					}
				}
			}
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
			int misClassify = 0;
			Model currentMod = ensemble.get(i);
			for(DataPoint d: trainingData){
				if(d.getLabel() != currentMod.predictLabelValue(d, 0.25)){
					//System.out.println("Predicted label: " + currentMod.predictLabelValue(d, 0.25) + " and the actual label is " + d.getLabel());
					misClassify++;
				}
			}
			double rating = 1-(double)misClassify/numTraining;
			if(rating < minAccuracy){
				minAccuracy = rating;
				worstEnsemble = i;
			}
			//System.out.println("Model " + i + "  has an accuracy of " + rating);
		}
		System.out.println("Model " + worstEnsemble + " sucks hard with accuracy " + minAccuracy);
		ensemble.remove(worstEnsemble);
	}

	public int predictPoint(DataPoint d, int numClasses){
		int predictedClass = -1;
		Matrix classVector = new Matrix(1,numClasses+1);
		Matrix predictor  = null;
		for(Model m: ensemble){
			predictor = m.predictLabel(d, 0.25);
			
			classVector.plusEquals(predictor);
			
		}
		//System.out.println("Dimensions of predictor: " + predictor.getRowDimension() + " " +predictor.getColumnDimension());
		//System.out.println("Dimensions of class vector: " + classVector.getRowDimension() + " " + classVector.getColumnDimension());
		double maxValue = 0;
		for(int i = 0; i < numClasses+1; i++){
			if(maxValue < classVector.get(0, i)){
				maxValue = classVector.get(0,i);
				predictedClass = i-1;
			}
		}
		//System.out.println("Predicted class for " + d + " is" + predictedClass);
		if(predictedClass == d.getActualLabel()){
			success++;
		}
		classifications++;
		return predictedClass;
		
	}
	
	public void predictChunk(DataChunk d){
		for(DataPoint x: d.getDataPointArray()){
			if(!x.isLabeled()){
				x.setPredictedLabel(predictPoint(x,ensemble.get(0).getNumClass()));
			}
		}
		
	}
}
