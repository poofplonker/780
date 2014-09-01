import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import cern.jet.random.engine.MersenneTwister;


public class Main {

	public static void main(String[] args) throws IOException {
		PrintWriter writer = new PrintWriter("output/KDDtest1.txt", "UTF-8");
		int chunkSize = 500;
		boolean synthetic = true;
		int l = 6;
		int k = 50;
		int testNumber = 20;
		double percentUnlabelled = 0.9;
		writer.println("For each test: ");
		LinkedList<Double> results = new LinkedList<Double>();
		results = ReaSC(l, k, percentUnlabelled, chunkSize, synthetic);
		writePercents(results, writer, 0);
		LinkedList<Double> currentResult;
		for(int i = 1; i < testNumber; i++){
			System.out.println("Test " + i + " complete");
			currentResult = ReaSC(l,k,percentUnlabelled, chunkSize, synthetic);
			writePercents(currentResult, writer, i);
			for(int j = 0; j < currentResult.size(); j++){
				results.set(j, results.get(j)+currentResult.get(j));
			}
		}
		for(int j = 0; j < testNumber; j++){
			results.set(j,results.get(j)/testNumber);
		}
		writer.println("Final Result:");
		writePercents(results, writer, -1);
		writer.close();
		Graphing.exportGraph(results, "output/synD.png");
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
	
	public static LinkedList<Double> ReaSC(int l, int k, double percentUnlabelled, int chunSize, boolean synthetic) throws IOException{
		BufferedReader br;
		if(!synthetic){
			br = new BufferedReader(new FileReader("input/kddcup.data_10_percent_corrected"));
		}else{
			br = null;
		}
		MersenneTwister twist = new MersenneTwister(new java.util.Date());
		int c = 0;
		Ensemble ens = new Ensemble(l,k);
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
			vectorLength = 21;
		}
		DataProcessor d = new DataProcessor(vectorLength, percentUnlabelled,br,synthetic,twist);
		int iterations = 0;
		while(d.moreInput()){
			DataChunk chunk = new DataChunk(chunkSize, d,twist, percentUnlabelled);
			vectorLength = chunk.getDataPointArray().get(0).getData().size();
			if(chunk.getDataPointArray().size() < chunkSize){
				return percentArray;
			}
			c = d.getSeenClasses();
			ens.expandClasses(c);
			if(iterations > 2){
				ens.predictChunkForClustering(chunk);
			}
			//System.out.println("Length now: " + vectorLength);

			//System.out.println("Number of classes: " + c);
			Model m = new Model(twist, chunk,k, c);
			m.propagateLabels(3, 0.25);
			ens.addModel(m);
			iterations++;
			if(iterations > 2){
				ens.predictChunk(chunk);
			}
			System.out.println("After " + iterations +" iterations, the accuracy is:" + ens.getAccuracy());
			System.out.println();
			percentArray.add(ens.getAccuracy());
			
		}
		return percentArray;
	}

}

