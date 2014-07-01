import java.util.ArrayList;

public class MicroCluster extends Cluster{
	
	private boolean labelled;
	private int microClusterLabel;

	public MicroCluster(DataPoint centroid, boolean labelled, int label){
		super(centroid);
		points = new ArrayList<DataPoint>();
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

	
	
}
