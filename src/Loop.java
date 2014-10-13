import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.commons.math3.special.Erf;



public class Loop {
	private static int lambda = 3; 	//std deviation, should have no effect on Loop scores
	public static double pDist(DataPoint o, MicroCluster micro){
		double pDist = 0;
		LinkedList<DataPoint> microPoints = micro.getDataPoints();
		for(int i = 0; i < microPoints.size(); i++){
			double distance = o.getDistanceValue(microPoints.get(i));
			pDist += distance*distance;
		}
		pDist /= microPoints.size();
		//System.out.println("Pdist: "+lambda*Math.sqrt(pDist) );
		if(lambda*Math.sqrt(pDist) == Double.NaN){
			System.out.println("pDist alert");
		}
		return lambda*Math.sqrt(pDist);
		
	}
	
	public static double Plof(DataPoint o, MicroCluster micro){
		double plof = pDist(o,micro);
		double denom = 0;
		int counter = 0;
		if(!micro.isPlofDenomSet()){
			for(DataPoint d: micro.getDataPoints()){
				denom += pDist(d,micro);
				counter++;
			}
			micro.setPlofDenom(denom, counter);
		}else{
			counter = micro.getPlofCounter();
			plof = micro.getPlofDenom();
		}
		if(denom == 0){
			plof = 0;
		}else{
			denom /= counter;
			plof /= denom;
		}
		
		plof--;
		//System.out.println("Plof: "+ plof );
		o.setPlof(plof);
		return plof;
		
	}
	
	public static double NPlof(ArrayList<MicroCluster> microClusters){
		double nPlof = 0;
		int counter = 0;
		for(MicroCluster m: microClusters){
			for(DataPoint d: m.getDataPoints()){
				double plof;
				if(!d.isPlofSet()){
					plof = Plof(d,m);
				}else{
					plof = d.getPlof();
				}
				nPlof += plof*plof;
				counter++;
			}
		}
		nPlof /= counter;
		//System.out.println("Nplof " + nPlof);
		if(lambda*Math.sqrt(nPlof) == Double.NaN){
			System.out.println("nPlof alert");
		}
		return lambda*Math.sqrt(nPlof);
	}
	
	public static double loop(DataPoint o, MicroCluster targetCluster, double nPlof ){
		double loop = 0;
		double plof;
		if(o.isPlofSet()){
			plof = o.getPlof();
		}else{
			plof = Plof(o,targetCluster);
		}
		loop = Math.max(0, Erf.erf(plof/(Math.sqrt(2)*nPlof)));
		return loop;
		
	}
}
