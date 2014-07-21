import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cern.jet.random.engine.MersenneTwister;


public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader("input/kdduniq.data"));
		MersenneTwister twist = new MersenneTwister(new java.util.Date());
		int vectorLength = 0;	//length of datavector
		int chunkSize = 1000;	//chunksize
		int k = 5;	//number of clusters
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
		CategoricalData catData = (CategoricalData)chunk.getDataPointArray().get(0).getData().get(vectorLength-1);
		System.out.println("Number of classes: " + catData.getNumCategories(vectorLength-1));
		Model m = new Model(twist, chunk,k, catData.getNumCategories(vectorLength-1));
		
	}

}
