
public class IntegerData extends DataType{
	private int rawValue;
	private static int[] minArray;
	private static int[] maxArray;
	
	public IntegerData(int value, int index, int vectorLength){
		this(value);
		
		this.index = index;
		DataType.vectorLength = vectorLength;
		if(minArray == null){
			minArray = new int[vectorLength];
		}
		if(maxArray == null){
			maxArray = new int[vectorLength];
		}
	}
	
	public IntegerData(int value){
		this.rawValue = value;
	}
	
	public boolean validIndex(){
		return index >= 0 && index < vectorLength;
	}
	public int getMax(){
		if (validIndex()){
			return maxArray[index];
		}else{
			System.out.println("We cannot get the max value for this data item because it has an invalid index");
			return 0;
		}
		
	}
	
	public void setMax(int temp){
		if (validIndex()){
			maxArray[index] = temp;
		}else{
			System.out.println("We cannot set the max value for this data item because it has an invalid index");
		}
	}
	
	public int getMin(){
		if (validIndex()){
			return minArray[index];
		}else{
			System.out.println("We cannot get the min value for this data item because it has an invalid index");
			return 0;
		}
		
	}
	
	public void setMin(int temp){
		if (validIndex()){
			minArray[index] = temp;
		}else{
			System.out.println("We cannot set the min value for this data item because it has an invalid index");
		}
	}
	
	public double normalise(int value){
		if(getMax() == getMin()){
			return 0;
		}else{
			return (double)value/(getMax()-getMin());
		}
		
	}
	
	public int getRaw(){
		return rawValue;
	}
	
	public double getNormalised(){
		return normalise(rawValue);
	}
	@Override
	public double distance(DataType d) {
		if(!(d instanceof IntegerData)){
			return -1;
		}
		return Math.abs(getNormalised() - ((IntegerData)d).getNormalised());
	}

}
