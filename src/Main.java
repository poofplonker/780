import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader("input/kddcup.data_10_percent_corrected"));
		int vectorLength = 0;
		double percentUnlabelled = 0;
		try {
			vectorLength = br.readLine().split(",").length;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataProcessor d = new DataProcessor(vectorLength, percentUnlabelled,br);
	}

}
