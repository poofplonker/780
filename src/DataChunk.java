import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.MersenneTwister;


public class DataChunk {
	
	private final int chunkSize;
	private ArrayList<DataPoint> dataPointArray;
	private ArrayList<DataPoint> trainingData;
	private int numLabelledPoints;
	
	public DataChunk(int chunkSize, DataProcessor d, MersenneTwister twister){
		this.chunkSize = chunkSize;
		this.numLabelledPoints = 0;
		this.dataPointArray = new ArrayList<DataPoint>(chunkSize);
		this.trainingData = new ArrayList<DataPoint>();
		DataPoint t;
		for(int i = 0; i < chunkSize; i++){
			t = d.processPoint(twister);
			if(t.isLabeled()){
				numLabelledPoints++;
				this.trainingData.add(t);
			}
			this.dataPointArray.add(t);	
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
	
}
