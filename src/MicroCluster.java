import java.util.ArrayList;
import java.util.LinkedList;

public class MicroCluster extends Cluster{
	
	private boolean labelled;
	private int microClusterLabel;

	public MicroCluster(DataPoint centroid, boolean labelled, int label){
		super(centroid);
		points = new LinkedList<DataPoint>();
		this.labelled = labelled;
		totalPoints = 0;
		microClusterLabel = label;
		
	}
	
	public int getLabel(){
		return microClusterLabel;
	}
	public void attachPoint(DataPoint d){
		totalPoints++;
		points.add(d);
		return;
	}
	
	public String toString(){
		String thing = "";
		thing += " Label: " + microClusterLabel;
		thing += " Labelled: " + labelled;
		thing += " Number of points: " + totalPoints;
		return thing;
	}

	
	
}
