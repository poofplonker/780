import weka.core.Instance;
import cern.jet.random.engine.MersenneTwister;
import moa.options.FloatOption;
import moa.options.IntOption;
import moa.streams.generators.HyperplaneGenerator;


public class SYNDGen {
	
	private MersenneTwister twister;
	private HyperplaneGenerator hpg;
	private int recordCounter = 0;
	public SYNDGen(MersenneTwister twister){
		this.twister = twister;
		hpg = new HyperplaneGenerator();
		hpg.numberInstance = 250000;
		hpg.numClassesOption = new IntOption("numClasses", 'c',"The number of classes to generate.", 2, 2, Integer.MAX_VALUE);
		hpg.noisePercentageOption = new IntOption("noisePercentage",'n', "Percentage of noise to add to the data.", 5, 0, 100);
		hpg.instanceRandomSeedOption = new IntOption("instanceRandomSeed", 'i',"Seed for random generation of instances.", 1);
		hpg.numAttsOption = new IntOption("numAtts", 'a',"The number of attributes to generate.", 20, 0, Integer.MAX_VALUE);
		hpg.numDriftAttsOption = new IntOption("numDriftAtts", 'k', "The number of attributes with drift.", 4, 0, Integer.MAX_VALUE);
		double change = twister.nextDouble()*.9 + .1;
		hpg.magChangeOption = new FloatOption("magChange", 't',"Magnitude of the change for every example", change, 0.0, 1.0);
		hpg.sigmaPercentageOption = new IntOption("sigmaPercentage",'s', "Percentage of probability that the direction of change is reversed.", 10, 0, 100);
		hpg.prepareForUse();
	}
	
	private void updateGenerator(){
		double change = twister.nextDouble()*.9 + .1;
		//System.out.println("Change:" + change);
		hpg.magChangeOption = new FloatOption("magChange", 't',"Magnitude of the change for every example", change, 0.0, 1.0);
	}
	
	public String[] getPoint(){
		Instance inst = hpg.nextInstance();
		recordCounter++;
		if(recordCounter == 1000){
			updateGenerator();
			recordCounter = 0;
		}
		double[] doubs = inst.toDoubleArray();
		String[] tokens = new String[doubs.length];
		for(int i = 0; i < doubs.length; i++){
			tokens[i] = Double.toString(doubs[i]);
			//System.out.print(tokens[i] + " ");
		}
		//System.out.println();
		return tokens;
	}
	public boolean hasMore(){
		return hpg.hasMoreInstances();
	}
}
