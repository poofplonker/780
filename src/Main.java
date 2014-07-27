import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cern.jet.random.engine.MersenneTwister;


public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader("input/EMtest1.txt"));
		MersenneTwister twist = new MersenneTwister(new java.util.Date());
		int vectorLength = 0;	//length of datavector
		int chunkSize = 6;	//chunksize
		int k = 2;	//number of clusters
		double percentUnlabelled = 0;
		try {
			vectorLength = br.readLine().split(",").length;
			System.out.println("Length: " + vectorLength);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataProcessor d = new DataProcessor(vectorLength, percentUnlabelled,br);
		
		DataChunk chunk = new DataChunk(chunkSize, d,twist);
		vectorLength = chunk.getDataPointArray().get(0).getData().size();
		System.out.println("Length now: " + vectorLength);
		CategoricalData catData = (CategoricalData)chunk.getDataPointArray().get(0).getClassLabel();
		System.out.println("Number of classes: " + catData.getNumCategories(vectorLength));
		Model m = new Model(twist, chunk,k, catData.getNumCategories(vectorLength));
		
	}

}
