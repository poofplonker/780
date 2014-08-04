import java.util.ArrayList;

import Jama.Matrix;


public class Ensemble {
	private ArrayList<Model> ensemble;
	private int numModels;
	private int maxModels;
	
	public Ensemble(int l){
		maxModels = l;
		numModels = 0;
		ensemble = new ArrayList<Model>();
	}
	
	public void addModel(Model m){
		if(numModels < maxModels){
			ensemble.add(numModels++,m);
		}
		else{
			updateEnsemble(m);
		}
	}
	
	private void updateEnsemble(Model m) {
		ArrayList<DataPoint> trainingData = m.getTrainingData();
		ensemble.add(m);
		int worstEnsemble = -1;
		int numTraining = trainingData.size();
		double minAccuracy = 1.5;
		for(int i = 0; i < ensemble.size(); i++){
			int misClassify = 0;
			for(DataPoint d: trainingData){
				if(d.getActualLabel() != m.predictLabelValue(d, 0.25)){
					misClassify++;
				}
			}
			double rating = (double)misClassify/numTraining;
			if(rating < minAccuracy){
				minAccuracy = rating;
				worstEnsemble = i;
			}
			System.out.println("Model " + i + "  has an accuracy of " + rating);
		}
		System.out.println("Ensemble " + worstEnsemble + " sucks hard");
		ensemble.remove(worstEnsemble);
	}

	public int predictPoint(DataPoint d, int numClasses){
		int predictedClass = -1;
		Matrix classVector = new Matrix(1,numClasses+1);
		for(Model m: ensemble){
			classVector.plusEquals(m.predictLabel(d, 0.25));
		}
		double maxValue = 0;
		for(int i = 0; i < numClasses+1; i++){
			if(maxValue < classVector.get(0, i)){
				maxValue = classVector.get(0,i);
				predictedClass = i-1;
			}
		}
		System.out.println("Predicted class for " + d + " is" + predictedClass);
		return predictedClass;
		
	}
}
