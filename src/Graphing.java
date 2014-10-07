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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Graphing {
	
	public static void exportGraph(LinkedList<Double> results1, LinkedList<Double> results2, String title,String filename) throws FileNotFoundException, IOException{
		XYDataset data = genData(results1, results2);
		double minValue = getMinValue(results1, results2);
		double maxValue = getMaxValue(results1, results2);
		JFreeChart chart = createChart(data, title, minValue,maxValue);
		BufferedImage chartImage = chart.createBufferedImage( 300, 300, null); 
		ImageIO.write( chartImage, "png", new FileOutputStream(filename));
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
	private static XYDataset genData(LinkedList<Double> results1, LinkedList<Double> results2){
		XYSeries bench = new XYSeries("Without Cluster Rating");
		for(int i = 0; i < results1.size(); i++){
			bench.add(i,results1.get(i));
		}
		XYSeries bench2 = new XYSeries("With Cluster Rating");
		for(int i = 0; i < results2.size(); i++){
			bench2.add(i,results2.get(i));
		}
		XYSeriesCollection d = new XYSeriesCollection();
		d.addSeries(bench);
		d.addSeries(bench2);
		return d;
	}
	
	private static JFreeChart createChart(XYDataset data, String title,double minValue, double maxValue){
		final JFreeChart chart = ChartFactory.createXYLineChart(
	            title,      // chart title
	            "Number of chunks processed",                      // x axis label
	            "Cumulative accuracy of predictions",                      // y axis label
	            data,                  // data
	            PlotOrientation.VERTICAL,
	            true,                     // include legend
	            true,                     // tooltips
	            false                     // urls
	        );
		chart.setBackgroundPaint(Color.white);
        XYPlot xyPlot = (XYPlot) chart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
        range.setRange(minValue, maxValue);
        return chart;
	}
}
