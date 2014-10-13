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
	private MersenneTwister twister;
	private int recordsProcessed = 0;
	private int seenClasses = 0;
	private HashMap<String, Integer> classMap;
	private HashMap<Integer, Integer> intClassMap;
	private double percentUnlabelled;
	private boolean synthetic;
	private SYNDGen syndgen;
	
	
	public DataProcessor(int vectorLength, double percentUnlabelled, BufferedReader br, boolean synthetic, MersenneTwister twister){
		this.vectorLength = vectorLength;
		this.percentUnlabelled = percentUnlabelled;
		this.br = br;
		this.classMap = new HashMap<String,Integer>();
		this.intClassMap = new HashMap<Integer, Integer>();
		this.synthetic = synthetic;
		this.twister = twister;
		if(synthetic){
			System.out.println("Kicking off the synthetic data");
			syndgen = new SYNDGen(twister);
		}
	}
	
	public int getSeenClasses(){
		return seenClasses;
	}
	
	public DataPoint processPoint(boolean training){
		
		//handle this properly ffs
		String[] values = new String[vectorLength];
		if(!synthetic){
			try {
				if(br.ready()){
					values = br.readLine().split(",");
				}else{
					return null;
				}
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}else{
			if(syndgen.hasMore()){
				values = syndgen.getPoint();
				seenClasses = 2;
			}else{
				return null;
			}
		}
		ArrayList<DataType> dataValues = new ArrayList<DataType>(vectorLength);
		//assumes that no data values are missing - potentially dodgy
		int valueCounter = 0;
		for (int i = 0; i < values.length; i++){
			if(values[i].matches("-?\\d+") || values[i].matches("([0-9]*)\\.([0-9E\\-]*)") || i == (values.length -1)){
			//value is an integer
				dataValues.add(processField(values[i],valueCounter++));
			/*Both all Integers share min and max: Fix */
			}

		}
		DataPoint d;
		//remove class label
		DataType classLabel = dataValues.remove(dataValues.size()-1);
		if(training && classLabel instanceof CategoricalData ){
			if(!classMap.containsKey(((CategoricalData) classLabel).getRaw())){
				classMap.put(((CategoricalData) classLabel).getRaw(),1);
				System.out.println("Now seen: "+ ((CategoricalData) classLabel).getRaw());
				seenClasses++;
			}
		}else if(training && classLabel instanceof IntegerData){
			//System.out.println("Label of this point is: " + ((IntegerData) classLabel).getRaw());
			if(!intClassMap.containsKey(((IntegerData) classLabel).getRaw())){
				intClassMap.put(((IntegerData) classLabel).getRaw(),1);
				
				seenClasses++;
			}
		}
		//simulation of unlabelled data
		if(!synthetic && classLabel instanceof CategoricalData){
			d = new DataPoint(dataValues, classLabel,((CategoricalData)classLabel).numerValue(),false);
		}else if(!synthetic && classLabel instanceof IntegerData){
			d = new DataPoint(dataValues, classLabel,((IntegerData)classLabel).getRaw(),false);
		}else{
			d = new DataPoint(dataValues, classLabel,(int) Math.round(Double.parseDouble(values[values.length-1])),false);
		}
		//System.out.println(d +" " +  (((CategoricalData) d.getClassLabel()).getRaw()));
		/*if(d.getData().size() == 19){
			System.out.println("Length of vector: " + values.length);
			for(int i = 0; i < values.length; i++){
				System.out.print(values[i] + " ");
			}
			System.out.println();
		}*/
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
			IntegerData data =  new IntegerData(result,i,vectorLength);
			
			if(result > data.getMax()){
				data.setMax(result);
			}
			return data;
		
		//values is a float
		}else if (value.matches("([0-9]*)\\.([0-9E\\-]*)")){
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

	public boolean moreInput() throws IOException {
		if(!synthetic){
			return br.ready();
		}else{
			return syndgen.hasMore();
		}
	}
}
