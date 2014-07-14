import java.util.ArrayList;
import java.util.HashMap;


public class Cluster {
	
	protected ArrayList<DataPoint> points;
	protected DataPoint centroid;
	protected int totalPoints;
	
	public Cluster(DataPoint centroid){
		this.centroid = centroid;
		points = new ArrayList<DataPoint>();
		totalPoints = 0;
	}
	
	public DataPoint recalculateCentroid(){
		DataPoint newCentroid;
		int length = centroid.getVectorLength();
		double[] doubleRaws = new double[length];
		int[] integerRaws = new int[length];
		ArrayList<HashMap<String,Integer>> categoricalCounts = new ArrayList<HashMap<String,Integer>>(length);
		for(int i =0; i < length; i++){
			categoricalCounts.set(i,new HashMap<String,Integer>());
		}
		
		for(DataPoint d: points){
			for(int i =0; i < length; i++){
			DataType dat = d.getData().get(i);
				if(dat instanceof DoubleData){
					doubleRaws[i] += ((DoubleData)dat).getRaw();
				}else if(dat instanceof IntegerData){
					integerRaws[i] += ((IntegerData)dat).getRaw();
				}else{
					//categorical data will be the majority category when recalcing centroid.
					if(!categoricalCounts.get(i).containsKey(((CategoricalData)dat).getRaw())){
						categoricalCounts.get(i).put(((CategoricalData)dat).getRaw(), 1);
					}else{
						int count = categoricalCounts.get(i).get(((CategoricalData)dat).getRaw());
						count++;
						categoricalCounts.get(i).put(((CategoricalData)dat).getRaw(), count);
					}
				}
			}
		}
		DataPoint samplePoint = points.get(0);
		ArrayList<DataType> newCentroidData = new ArrayList<DataType>(centroid.getVectorLength());
		for(int i =0; i < centroid.getVectorLength(); i++){
			if(samplePoint.getData().get(i) instanceof DoubleData){
				newCentroidData.add(i,new DoubleData(doubleRaws[i]/centroid.getVectorLength(),i,centroid.getVectorLength()));
			}else if(samplePoint.getData().get(i) instanceof IntegerData){
				newCentroidData.add(i,new IntegerData(integerRaws[i]/centroid.getVectorLength(),i,centroid.getVectorLength()));
			}else{
				//need to find most common string using the dictionary set up
				int max = 0;
				String common = null;
				HashMap<String,Integer> dict = categoricalCounts.get(i);
				for(String s:dict.keySet()){
					if(dict.get(s) > max){
						max = dict.get(s);
						common = s;
					}
				}
				newCentroidData.add(i, new CategoricalData(common,i,centroid.getVectorLength()));
			}
		}
		newCentroid = new DataPoint(newCentroidData,length);
		return newCentroid;
	}
	
	public ArrayList<DataPoint> getDataPoints(){
		return points;
	}
}