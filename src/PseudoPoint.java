import java.util.ArrayList;


public class PseudoPoint implements Comparable{
	private DataPoint centroid;
	private boolean labelled;
	private int weight;
	private int label;
	private int sameLabelNeighbour;
	private int neighbourCount;
	private double clusterRating;
	private double accuracy;
	
	public PseudoPoint(DataPoint centroid, int weight, int label, double clusterRating){
		this.centroid = centroid;
		this.weight = weight;
		this.label = label;
		if(label == -1){
			labelled = false;
		}else{
			labelled = true;
		}
		this.clusterRating = clusterRating;
	}
	
	public double getClusterRating(){
		return clusterRating;
	}
	
	public void resetAccuracy(){
		sameLabelNeighbour = 0;
		neighbourCount = 0;
	}
	
	private double calcAccuracy(){
		return ((double)sameLabelNeighbour)/neighbourCount; 
	}
	
	public double accurateNeighbour(){
		sameLabelNeighbour++;
		neighbourCount++;
		return calcAccuracy();
	}
	
	public double inAccurateNeighbour(){
		neighbourCount++;
		return calcAccuracy();
	}
	public double getAccuracy(){
		return ((double)sameLabelNeighbour)/neighbourCount;
	}
	
	public boolean isLabelled(){
		return labelled;
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
			return 0;
		}
		return 0;
		
	}
	public String toString(){
		String thing = "";
		thing += " Label: " + label;
		thing += " Originally Labelled: " + labelled;
		thing += " Number of points: " + weight;
		thing += " Centroid: " + centroid.toString();
		return thing;
	}

	public void setClass(int i) {
		this.label = i;
		
	}

	public void merge(PseudoPoint two) {
		ArrayList<DataType> thisValues = centroid.getData();
		ArrayList<DataType> otherValues = centroid.getData();
		int length = centroid.getData().size();
		ArrayList<DataType> finalValues = new ArrayList<DataType>(length);
		for(int i = 0; i < length; i++){
			if(thisValues.get(i) instanceof IntegerData){
				int thisVal = ((IntegerData) thisValues.get(i)).getRaw();
				int thatVal = ((IntegerData) otherValues.get(i)).getRaw();
				finalValues.add(new IntegerData(((thisVal + thatVal)/2),i,length));
			}else if(thisValues.get(i) instanceof DoubleData){
				double thisVal = ((DoubleData) thisValues.get(i)).getRaw();
				double thatVal = ((DoubleData) otherValues.get(i)).getRaw();
				finalValues.add(new DoubleData(((thisVal + thatVal)/2),i,length));
			}else{
				finalValues.add(thisValues.get(i));
			}
		}
		DataPoint updated = new DataPoint(finalValues, null, label, false);
		this.centroid = updated;
		
	}
	
}
