
public class DoubleData extends DataType{


	private static double max = 0;
	private static double min = 0;
	private double rawValue;

	public DoubleData(double value){
		this.rawValue = value;
	}

	public static double getMax(){
		return max;
	}

	public static void setMax(double temp){
		max = temp;
	}

	public static double getMin(){
		return min;
	}

	public static void setMin(double temp){
		min = temp;
	}

	public double normalise(double input){
		return (double)input/(max-min);
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
