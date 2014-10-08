import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import cern.jet.random.engine.MersenneTwister;


public class Main {
	
	private static final int CHUNKSIZE = 1000;
	private static final int L = 6;
	private static final int K = 50;
	private static final int TESTNUMBER = 2;
	private static final int ITERATIONS = 50;
	private static final double PERCENTUNLABELLED = 0.9;
	private static final String OUTPUTGRAPHNAME = "output/SynDFinal";
	private static final String GRAPHTITLE = "SynD Dataset";
	private static final boolean SYNTHETIC = true;
	private static final int ERRORINTERVAL = 24;
	private static final int SYNTHETICLENGTH = 21;
	private static final String FILE1 = "input/kddcup.data_10_percent_corrected";
	private static final String FILE2 = "input/covtype.data";

	
	public static void main(String[] args) throws IOException {
		PrintWriter writer = new PrintWriter("output/KDDtest1.txt", "UTF-8");
		writer.println("For each test: ");
		LinkedList<Double> error1 = new LinkedList<Double>();
		LinkedList<Double> error2 = new LinkedList<Double>();
		LinkedList<Double> results1 = singleTest(false, error1);
		LinkedList<Double> results2 = singleTest(true, error2);


		for(Double d: results1){
			System.out.println("Recorded point" + d);
		}
		for(Double d: error1){
			System.out.println("Recorded point error" + d);
		}
		writer.println("Final Result:");
		writePercents(results1, writer, -1);
		writer.close();
		Graphing.exportGraph(results1, results2, error1, error2,ERRORINTERVAL, GRAPHTITLE, OUTPUTGRAPHNAME);
	}
	
	private static LinkedList<Double> singleTest(boolean ratingCluster, LinkedList<Double> error) throws IOException{
		LinkedList<Double> results = ReaSC(L, K, PERCENTUNLABELLED, CHUNKSIZE, SYNTHETIC, ratingCluster);
		LinkedList<Double> currentResult;
		LinkedList<LinkedList<Double>> store = new LinkedList<LinkedList<Double>>();
		LinkedList<Double> tempStore = new LinkedList<Double>();
		int interval = ERRORINTERVAL;
		if(ratingCluster){
			interval++;
		}
		for(int j = 0; j < results.size(); j++){
			if(j%interval == 0){
				tempStore.add(results.get(j));
			}
		}
		store.add(tempStore);
		for(int i = 1; i < TESTNUMBER; i++){
			System.out.println("Test " + i + " complete");
			currentResult = ReaSC(L, K, PERCENTUNLABELLED, CHUNKSIZE, SYNTHETIC, ratingCluster);
			tempStore = new LinkedList<Double>();
			for(int j = 0; j < currentResult.size(); j++){
				results.set(j, results.get(j)+currentResult.get(j));
				if(j%ERRORINTERVAL == 0){
					tempStore.add(currentResult.get(j));
				}
			}
			store.add(tempStore);
		}
		LinkedList<Double> avs = new LinkedList<Double>();
		for(int j = 0; j < results.size(); j++){
			results.set(j,results.get(j)/TESTNUMBER);
			if(j%interval == 0){
				avs.add(results.get(j));
				System.out.println("Average of " + j + ": " + results.get(j));
			}
		}
		System.out.println("Value of store.size():" + store.size());

		System.out.println("Values of avs: " +avs.size());
		for(int i = 0; i < store.size(); i++){
			System.out.println("Value of store.get(" + i+ "):" + store.get(i).size());
			LinkedList<Double> temp = store.get(i);
			for(int j = 0; j < store.get(i).size(); j++){
				//store now contains array of (value -avs)^2
				store.get(i).set(j, (temp.get(j) - avs.get(j))*(temp.get(j) - avs.get(j)));
				System.out.println("Setting " + i + " " + j + " to be" + store.get(i).get(j));
			}
			
		}
		for(int j = 0; j < store.get(0).size(); j++){
			error.add(0.0);
			for(int i = 0; i < store.size(); i++){
				error.set(j,error.get(j)+store.get(i).get(j));
			}
			error.set(j, Math.sqrt(error.get(j)));
		}
		return results;
	}
	
	public static void writePercents(LinkedList<Double> list, PrintWriter p, int test){
		if(test > -1){
			p.print(test + ":");
		}
		for(Double d: list){
			p.print(d + " ");
		}
		p.println();
	}
	
	public static LinkedList<Double> ReaSC(int l, int k, double percentUnlabelled, int chunSize, boolean synthetic, boolean ratingCluster) throws IOException{
		BufferedReader br;
		if(!synthetic){
			br = new BufferedReader(new FileReader(FILE2));
		}else{
			br = null;
		}
		MersenneTwister twist = new MersenneTwister(new java.util.Date());
		int c = 7;
		Ensemble ens = new Ensemble(l,k,c);
		int vectorLength = 0;	//length of datavector
		int chunkSize = chunSize;	//chunksize
			//number of clusters
		LinkedList<Double> percentArray = new LinkedList<Double>();
		if(!synthetic){
			try {
				vectorLength = br.readLine().split(",").length;
				System.out.println("Length: " + vectorLength);
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			vectorLength = SYNTHETICLENGTH;
		}
		DataProcessor d = new DataProcessor(vectorLength, percentUnlabelled,br,synthetic,twist);
		int iterations = 0;
		while(iterations < ITERATIONS){
			DataChunk chunk = new DataChunk(chunkSize, d,twist, percentUnlabelled);
			ens.countNewDataPoints(chunk.getTrainingData());
			vectorLength = chunk.getDataPointArray().get(0).getData().size();
			if(chunk.getDataPointArray().size() < chunkSize){
				return percentArray;
			}
			c = d.getSeenClasses();
			System.out.println("Seen classes: " + c);
			ens.expandClasses(c);
			if(iterations > 3){
				ens.predictChunkForClustering(chunk);
			}
			if(iterations > l+3){
				ens.predictChunk(chunk);
			}
			//System.out.println("Length now: " + vectorLength);

			//System.out.println("Number of classes: " + c);
			Model m = new Model(twist, chunk,k, c, ens.getClassCounter(), ens.getTotalPoints(), iterations, ratingCluster);
			ArrayList<PseudoPoint> pp = m.getPseudo();
			int oneCounter = 0;
			int zeroCounter = 0;
			for(int i = 0; i < pp.size(); i++){
				if(pp.get(i).getLabel() == 0){
					zeroCounter++;
				}else if(pp.get(i).getLabel() == 1){
					oneCounter++;
				}
			}
			System.out.println("Pseudopoints in model with label 0 before:" + zeroCounter);
			System.out.println("Pseudopoints in model with label 1 before:" + oneCounter);
			if(iterations > 3){
				m.propagateLabels(3, 0.25, ens.getContig());
			}
			zeroCounter = 0;
			oneCounter = 0;
			for(int i = 0; i < pp.size(); i++){
				if(pp.get(i).getLabel() == 0){
					zeroCounter++;
				}else if(pp.get(i).getLabel() == 1){
					oneCounter++;
				}
			}
			System.out.println("Pseudopoints in model with label 0 after:" + zeroCounter);
			System.out.println("Pseudopoints in model with label 1 after: " + oneCounter);
			ens.addModel(m);
			iterations++;
			System.out.println("After " + iterations +" iterations, the accuracy is:" + ens.getAccuracy());
			System.out.println();
			if(iterations > l+3){
				percentArray.add(ens.getAccuracy());
			}
			
		}
		return percentArray;
	}

}

