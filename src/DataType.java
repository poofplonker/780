
public abstract class DataType {
	protected int index = -1;
	protected static int vectorLength;
	public abstract double rawDistance(DataType d);
	public abstract double normDistance(DataType d);
	
}
