import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Graphing {
	
	public static void exportGraph(LinkedList<Double> results1, LinkedList<Double> results2,LinkedList<Double> error1, LinkedList<Double> error2, int errorInterval, String title,String filename) throws FileNotFoundException, IOException{
		XYDataset data = genData(results1, results2, error1, error2, errorInterval);
		double minValue = getMinValue(results1, results2);
		double maxValue = getMaxValue(results1, results2);
		JFreeChart chart = createChart(data, title, minValue,maxValue);
		BufferedImage chartImage = chart.createBufferedImage( 300, 300, null); 
		ImageIO.write( chartImage, "png", new FileOutputStream(filename +"Line.png"));
	}
	
	private static double getMinValue(LinkedList<Double> results1, LinkedList<Double> results2){
		double min = 1.1;
		for(double d: results1){
			if(min > d){
				min = d;
			}
		}
		for(double d: results2){
			if(min > d){
				min = d;
			}
		}
		return (min-0.05 > 1) ? 0: min-0.05; 
	}
	
	private static double getMaxValue(LinkedList<Double> results1, LinkedList<Double> results2){
		double max = 0;
		for(double d: results1){
			if(max < d){
				max = d;
			}
		}
		for(double d: results2){
			if(max < d){
				max = d;
			}
		}
		return (max+0.05 > 1) ? 1: max+0.05; 
	}
	private static XYDataset genData(LinkedList<Double> results1, LinkedList<Double> results2, LinkedList<Double> error1, LinkedList<Double> error2,int errorInterval){
		XYIntervalSeries bench = new XYIntervalSeries("Without Cluster Rating");
		int counter = 0;
		for(int i = 0; i < results1.size(); i++){
			if((i % errorInterval)!= 0){
				bench.add(i, i, i,results1.get(i),results1.get(i),results1.get(i));
			}else{
				bench.add(i,i,i,results1.get(i), results1.get(i)-error1.get(counter), results1.get(i)+error1.get(counter));
				counter++;
			}
		}
		XYIntervalSeries bench2 = new XYIntervalSeries("With Cluster Rating");
		counter = 0;
		for(int i = 0; i < results2.size(); i++){
			if((i%errorInterval) != 0){
				bench2.add(i, i, i,results2.get(i),results2.get(i),results2.get(i));
			}
			else{
				bench2.add(i,i,i,results2.get(i), results2.get(i)-error2.get(counter), results2.get(i)+error2.get(counter));
				counter++;
			}
		}
		XYIntervalSeriesCollection d = new XYIntervalSeriesCollection();
		d.addSeries(bench);
		d.addSeries(bench2);
		return d;
	}
	
	private static JFreeChart createChart(XYDataset data, String title,double minValue, double maxValue){
        NumberAxis range = new NumberAxis("Number of chunks processed");
        NumberAxis domain = new NumberAxis("Cumulative accuracy of predictions");
        domain.setRange(minValue, maxValue);
        XYErrorRenderer error = new XYErrorRenderer();
        error.setSeriesShapesVisible(0,false);
        error.setSeriesShapesVisible(1,false);
        error.setSeriesLinesVisible(0,true);
        error.setSeriesLinesVisible(1,true);
        
        XYPlot xyPlot = new XYPlot(data,range,domain,error);
		final JFreeChart chart = new JFreeChart(title,xyPlot);
		chart.setBackgroundPaint(Color.white);
        
        xyPlot.setDomainCrosshairVisible(true);
        return chart;
	}
}
