import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cern.jet.random.engine.MersenneTwister;


public class DataChunk {
	
	private final int chunkSize;
	private ArrayList<DataPoint> dataPointArray;
	private ArrayList<DataPoint> trainingData;
	private HashMap<Integer,Boolean> seenClass;
	private ArrayList<DataPoint> testData;
	private int maxLabel = 0;
	private int numLabelledPoints;
	
	public DataChunk(int chunkSize, DataProcessor d, MersenneTwister twister){
		this.chunkSize = chunkSize;
		this.numLabelledPoints = 0;
		this.dataPointArray = new ArrayList<DataPoint>(chunkSize);
		this.trainingData = new ArrayList<DataPoint>();
		this.testData = new ArrayList<DataPoint>();
		this.seenClass = new HashMap<Integer,Boolean>();
		DataPoint t;
		int i = 0;
		while(i < chunkSize){
			t = d.processPoint(twister,true);
			if(t == null){
				return;
			}
			if(t.isLabeled()){
				numLabelledPoints++;
				this.trainingData.add(t);
				seenClass.put(t.getLabel(),true);
				if(t.getLabel() > maxLabel){
					maxLabel = t.getLabel();
				}
				
			}
			this.dataPointArray.add(t);	
			t = d.processPoint(twister,false);
			if(t == null){
				return;
			}
			this.testData.add(t);
			i++;
		}
	}
	
	public ArrayList<DataPoint> getDataPointArray(){
		return this.dataPointArray;
	}
	public int getChunkSize(){
		return this.chunkSize;
	}
	
	public ArrayList<DataPoint> getTrainingData(){
		return this.trainingData;
	}
	public int getNumLabelledPoints(){
		return numLabelledPoints;
	}
	
	public int[] getClassCounter(int c){
		int[] classCounter = new int[c];
		for(DataPoint d : dataPointArray){
			if(d.isLabeled()){
				classCounter[d.getLabel()]++;
			}
		}
		return classCounter;
	}

	public ArrayList<Boolean> seenClass(int c) {
		ArrayList<Boolean> seen = new ArrayList<Boolean>(c);
		for(int i = 0; i < c; i++){
			if(seenClass.containsKey(i)){
				seen.add(true);
			}else{
				seen.add(false);
			}
		}
		return seen;
	}

	public ArrayList<DataPoint> getTestData() {
		// TODO Auto-generated method stub
		return testData;
	}
	
}
