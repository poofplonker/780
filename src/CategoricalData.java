import java.util.ArrayList;
import java.util.HashMap;


public class CategoricalData extends DataType{
	private static ArrayList<HashMap<String,Integer>> dict; //keeps dictionary to index for each categorical field.
	private static int[] categoryArray; //counts how many category of string data there is for each categorical field.
	private String stringValue; 	//String value of particular categorical field.

	
	public CategoricalData(String stringValue, int index, int v){
		this.stringValue = stringValue;
		vectorLength = v;
		this.index = index;
		//System.out.println("Creating categorical data on index " + index+ " on data of length " + v + " with value " + stringValue);
		if(dict == null){
			dict = new ArrayList<HashMap<String,Integer>>(v);
			for(int i = 0; i < v; i++){
				dict.add(i,new HashMap<String,Integer>());
			}
		}
		if(categoryArray == null){
			categoryArray = new int[vectorLength];
		}
		numerValue();
	}
	
	public CategoricalData(String stringValue){
		this.stringValue = stringValue;
		if(dict == null){
			dict = new ArrayList<HashMap<String,Integer>>();
		}
		if(dict.get(index) == null){
			dict.set(index,new HashMap<String,Integer>());
		}
		if(categoryArray == null){
			categoryArray = new int[vectorLength];
		}
		numerValue();
	}


	public double distance(DataType d) throws IllegalArgumentException{
		if(!(d instanceof CategoricalData)){
			return -1;
		}
		if ((int)this.numerValue() == (int)((CategoricalData)d).normalisedValue()){
			return 0;
		}
		return 1;
	}

	public double normalisedValue() {
		// TODO Auto-generated method stub
		return numerValue();
	}

	public String getRaw(){
		return stringValue;
	}


	public int numerValue() {
		if (!dict.get(index).containsKey(stringValue)){
			categoryArray[index]++;
			dict.get(index).put(stringValue, (int) categoryArray[index]);
		}
		return dict.get(index).get(stringValue);
	}
	
	public int getNumCategories(int index){
		return categoryArray[index];
	}

}
