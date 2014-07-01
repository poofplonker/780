
public class IntegerData extends DataType{
	private static int max = 0;
	private static int min = 0;
	private int rawValue;
	
	public IntegerData(int value){
		this.rawValue = value;
	}
	
	public static int getMax(){
		return max;
	}
	
	public static void setMax(int temp){
		max = temp;
	}
	
	public static int getMin(){
		return max;
	}
	
	public static void setMin(int temp){
		min = temp;
	}
	
	public double normalise(int value){
		return (double)value/(max-min);
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
