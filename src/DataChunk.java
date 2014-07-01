import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.MersenneTwister;


public class DataChunk {
	
	private final int chunkSize;
	private ArrayList<DataPoint> dataPointArray;
	private int numLabelledPoints;
	
	public DataChunk(int chunkSize, DataProcessor d, MersenneTwister twister){
		this.chunkSize = chunkSize;
		this.numLabelledPoints = 0;
		this.dataPointArray = new ArrayList<DataPoint>(chunkSize);
		DataPoint t;
		for(int i = 0; i < chunkSize; i++){
			t = d.processPoint(twister);
			if(t.isLabeled()){
				numLabelledPoints++;
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
	
}
