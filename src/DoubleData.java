
public class DoubleData extends DataType{


	private static double[] minArray;
	private static double[] maxArray;
	private double rawValue;

	public DoubleData(double value){
		this.rawValue = value;
	}
	
	public DoubleData(double value, int index, int vectorLength){
		this(value);
		this.index = index;
		this.vectorLength = vectorLength;
		if(minArray == null){
			minArray = new double[vectorLength];
		}
		if(maxArray == null){
			maxArray = new double[vectorLength];
		}
	}

	public boolean validIndex(){
		return index >= 0 && index < vectorLength;
	}
	public double getMax(){
		if (validIndex()){
			return maxArray[index];
		}else{
			System.out.println("We cannot get the max value for this data item because it has an invalid index");
			return 0;
		}
		
	}
	
	public void setMax(double temp){
		if (validIndex()){
			maxArray[index] = temp;
		}else{
			System.out.println("We cannot set the max value for this data item because it has an invalid index");
		}
	}
	
	public double getMin(){
		if (validIndex()){
			return minArray[index];
		}else{
			System.out.println("We cannot get the min value for this data item because it has an invalid index");
			return 0;
		}
		
	}
	
	public void setMin(double temp){
		if (validIndex()){
			minArray[index] = temp;
		}else{
			System.out.println("We cannot set the min value for this data item because it has an invalid index");
		}
	}

	public double normalise(double value){
		if(getMax() == getMin()){
			return 0;
		}else{
			return (double)value/(getMax()-getMin());
		}
	}
	
	public double getRaw(){
		return rawValue;
	}
	
	public double getNormalised(){
		return normalise(rawValue);
	}

	public double distance(DataType d) {
		if(!(d instanceof DoubleData)){
			return -1;
		}
		return Math.abs(getNormalised() - ((DoubleData)d).getNormalised());

	}


}
