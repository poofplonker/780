
public class PseudoPoint {
	private DataPoint centroid;
	private int weight;
	private int label;
	
	public PseudoPoint(DataPoint centroid, int weight, int label){
		this.centroid = centroid;
		this.weight = weight;
		this.label = label;
	}
	
	public int getWeight(){
		return weight;
	}
	
	public DataPoint getCentroid(){
		return centroid;
	}
	
	public int getLabel(){
		return label;
	}
	
	
}
