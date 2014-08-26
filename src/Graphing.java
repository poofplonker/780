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
	
	public static void exportGraph(LinkedList<Double> results, String filename) throws FileNotFoundException, IOException{
		XYDataset data = genData(results);
		JFreeChart chart = createChart(data);
		BufferedImage chartImage = chart.createBufferedImage( 300, 300, null); 
		ImageIO.write( chartImage, "png", new FileOutputStream(filename));
	}
	
	private static XYDataset genData(LinkedList<Double> results){
		XYSeries bench = new XYSeries("Results of Tests");
		for(int i = 0; i < results.size(); i++){
			bench.add(i,results.get(i));
		}
		XYSeriesCollection d = new XYSeriesCollection();
		d.addSeries(bench);
		return d;
	}
	
	private static JFreeChart createChart(XYDataset data){
		final JFreeChart chart = ChartFactory.createXYLineChart(
	            "Line Chart Demo 6",      // chart title
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
        range.setRange(0.8, 1.0);
        return chart;
	}
}
