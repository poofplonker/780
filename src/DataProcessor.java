import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import cern.jet.random.engine.MersenneTwister;


public class DataProcessor {
	//array of dictionaries, for any nominal value which needs to be converted to an int
	//array of integer values counting the number of different nominal values for any string attribute
	private int vectorLength;
	private BufferedReader br;
	private int recordsProcessed = 0;
	private int seenClasses = 0;
	private HashMap<String, Integer> classMap;
	private double percentUnlabelled;
	
	
	public DataProcessor(int vectorLength, double percentUnlabelled, BufferedReader br){
		this.vectorLength = vectorLength;
		this.percentUnlabelled = percentUnlabelled;
		this.br = br;
		this.classMap = new HashMap<String,Integer>();
		
	}
	
	public int getSeenClasses(){
		return seenClasses;
	}
	
	public DataPoint processPoint(MersenneTwister twister){
		
		//handle this properly ffs
		String[] values = new String[vectorLength];
		try {
			if(br.ready()){
			  values = br.readLine().split(",");
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		ArrayList<DataType> dataValues = new ArrayList<DataType>(vectorLength);
		//assumes that no data values are missing - potentially dodgy
		for (int i = 0; i < values.length; i++){
			
			//value is an integer
			dataValues.add(i,processField(values[i],i));
			/*Both all Integers share min and max: Fix */

		}
		DataPoint d;
		//remove class label
		DataType classLabel = dataValues.remove(vectorLength-1);
		if(!classMap.containsKey(((CategoricalData) classLabel).getRaw())){
			classMap.put(((CategoricalData) classLabel).getRaw(),1);
			seenClasses++;
		}
		//simulation of unlabelled data
		if(twister.nextDouble() < percentUnlabelled){
			d = new DataPoint(dataValues, classLabel, ((CategoricalData)classLabel).numerValue(), false);
		}else{
			d = new DataPoint(dataValues, classLabel,((CategoricalData)classLabel).numerValue(),true);
			
		}
		recordsProcessed++;
		return d;
	}

	public double getPercentUnlabelled() {
		return percentUnlabelled;
	}

	public void setPercentUnlabelled(double percentUnlabelled) {
		this.percentUnlabelled = percentUnlabelled;
	}
	
	public DataType processField(String value, int i){
		if (value.matches("-?\\d+")){
			int result = Integer.parseInt(value);
			DoubleData data =  new DoubleData(result,i,vectorLength);
			
			if(result > data.getMax()){
				data.setMax(result);
			}
			return data;
		
		//values is a float
		}else if (value.matches("([0-9]*)\\.([0-9]*)")){
			double result = Double.parseDouble(value);
			DoubleData data = new DoubleData(result,i,vectorLength);
			if(result > data.getMax()){
				data.setMax(result);
			}
			return data;
		}else{
			return new CategoricalData(value,i,vectorLength);
		}
	}
}
