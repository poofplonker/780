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
	private double percentUnlabelled;
	
	
	public DataProcessor(int vectorLength, double percentUnlabelled, BufferedReader br){
		this.vectorLength = vectorLength;
		this.percentUnlabelled = percentUnlabelled;
		this.br = br;
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
			
			/*Both all Integers share min and max: Fix */
			if (values[i].matches("-?\\d+")){
				int result = Integer.parseInt(values[i]);
				IntegerData data =  new IntegerData(result,i,vectorLength);
				dataValues.add(i,data);
				if(result > data.getMax()){
					data.setMax(result);
				}
			
			//values is a float
			}else if (values[i].matches("([0-9]*)\\.([0-9]*)")){
				double result = Double.parseDouble(values[i]);
				DoubleData data = new DoubleData(result,i,vectorLength);
				dataValues.add(i, data);
				if(result > data.getMax()){
					data.setMax(result);
				}
			}else{
				dataValues.add(i, new CategoricalData(values[i],i,vectorLength));
			}
		}
		DataPoint d;
		//simulation of unlabelled data
		if(twister.nextDouble() < percentUnlabelled){
			d = new DataPoint(dataValues, ((CategoricalData)dataValues.get(vectorLength-1)).numerValue(), false);
		}else{
			d = new DataPoint(dataValues,((CategoricalData)dataValues.get(vectorLength-1)).numerValue(),true);
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
	
}
