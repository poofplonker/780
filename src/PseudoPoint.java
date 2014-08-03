
public class PseudoPoint implements Comparable{
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

	@Override
	public int compareTo(Object x) {
		if(x instanceof PseudoPoint){
			if(this.label < ((PseudoPoint)x).getLabel()){
				return 1;
			}else if(this.label > ((PseudoPoint)x).getLabel()){
				return -1;
			}
		}
		return 0;
	}
	public String toString(){
		String thing = "";
		thing += " Label: " + label;
		thing += " Number of points: " + weight;
		thing += " Centroid: " + centroid.toString();
		return thing;
	}

	public void setClass(int i) {
		this.label = i;
		
	}
	
}
