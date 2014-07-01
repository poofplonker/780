import java.util.HashMap;


public class CategoricalData extends DataType{
	private static HashMap<String,Integer> dict = new HashMap<String,Integer>();
	private static int numCategories = 1;
	private String stringValue;

	
	public CategoricalData(String stringValue){
		this.stringValue = stringValue;
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


	public double numerValue() {
		if (!dict.containsKey(stringValue)){
			numCategories++;
			dict.put(stringValue, (int) numCategories);
		}
		return dict.get(stringValue);
	}

}
