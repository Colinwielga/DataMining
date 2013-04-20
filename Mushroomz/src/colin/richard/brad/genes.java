package colin.richard.brad;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import java.text.DecimalFormat;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.ui.*;
import javax.swing.JPanel;
import java.awt.GridLayout;

//Stores aggregate scores for all distance metrics and k-values in a plottable and queryable form.
class Class {
	public XYSeriesCollection
		precision = new XYSeriesCollection(),
		recall    = new XYSeriesCollection(),
		Fmeasure  = new XYSeriesCollection();
}

//Parses and stores train and test records, and predicts the class of test records with the help of a DistanceMetric.
class Record {
	double[] attributes;
	String classname;
	
	Record(String line) {
		String[] fields = line.split(",");
		
		attributes = new double[fields.length - 1];
		for(int i = 0; i < fields.length - 1; i++)
			attributes[i] = (double)Double.parseDouble(fields[i]);

		//Globally keep track of class names so we can properly form confusion matrices, and so we know how many graphs to plot
		classname = fields[fields.length - 1];
		if(!genes.classes.containsKey(classname))
			genes.classes.put(classname, new Class());
		
		assert(attributes.length == fields.length - 1);
	}
	
	//Used for debugging.
	public String toString() {
		String result = "";
		for(int i = 0; i < 5 && i < attributes.length; i++)
			result += String.format("%10.5f", attributes[i]);
		result += "..." + String.format("%10.5f", attributes[attributes.length-1]) + " " + classname;
		return result;
	}
	
	//Called by DistanceMetric to predict class using kNN.
	String predictClass(ArrayList<Record> trainingRecords, DistanceMetric how, int k) {
		//Wrap each training record up in this object in order to find the closest k records
		class RecordWithDistance implements Comparable<RecordWithDistance> {
			RecordWithDistance(Record r, double d) {trainingRec = r; distance = d;}
			Record trainingRec;
			double distance;
			public int compareTo(RecordWithDistance other) {
				if(distance < other.distance) return -1;
				else if(distance == other.distance) return 0;
				else return 1;
			}
		}
		RecordWithDistance[] recsWithDist = new RecordWithDistance[trainingRecords.size()];

		//Compute and store distance to each record
		for(int i = 0; i < trainingRecords.size(); i++)
			recsWithDist[i] = new RecordWithDistance(trainingRecords.get(i), how.distanceBetween(this, trainingRecords.get(i)));
		
		//Sort the closest records first, so the first k records in list are the closest ones.
		java.util.Arrays.sort(recsWithDist); 
		
		//Hold the classes found in the nearest k samples in a hash table, using the values to hold the weighted vote of each class on the final prediction
		HashMap<String, Double> closeClasses = new HashMap<String, Double>();
		for(int i = 0; i < k && i < recsWithDist.length; i++) {
			String foundClass = recsWithDist[i].trainingRec.classname;
			if(!closeClasses.containsKey(foundClass)) closeClasses.put(foundClass, 0.0);
			closeClasses.put(foundClass, closeClasses.get(foundClass) + 1/recsWithDist[i].distance); //Increase the weight of this class by the inverse of the distance to the neighbor
		}

		//Return the class with the greatest weight. TODO: Consider switching to priority queue/max heap
		
		Comparator<Map.Entry<String, Double>> findClosestClass = new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
				return a.getValue().compareTo(b.getValue());
			}
		};

		return Collections.max(closeClasses.entrySet(), findClosestClass).getKey();
	}
}
	
abstract class DistanceMetric {
	//Polymorphism allows the code organization to be simplified, and makes it more extensible.
	public abstract double distanceBetween(Record a, Record b);	
	
	class Scores {
		XYSeries precisionPlot, recallPlot, FmeasurePlot;
		String nameOfClass, nameOfMetric;
		int truePositives, falsePositives, trueNegatives, falseNegatives; //Reset after every k
		
		Scores(String metricName, String classNm) {
			nameOfMetric = metricName;
			nameOfClass = classNm;
			genes.classes.get(classNm).precision.addSeries(precisionPlot = new XYSeries(metricName));
			genes.classes.get(classNm).recall.addSeries(recallPlot    = new XYSeries(metricName));
			genes.classes.get(classNm).Fmeasure.addSeries(FmeasurePlot  = new XYSeries(metricName));
		}
		void calculate(int k) {
			double accuracy  = ((double)truePositives+trueNegatives)/(falseNegatives+falsePositives+trueNegatives+truePositives),
				   precision = ((double)truePositives)/(falsePositives+truePositives),
				   recall    = ((double)truePositives)/(falseNegatives+truePositives),
				   Fmeasure  = ((double)2*truePositives)/(2*truePositives + falsePositives + falseNegatives);
		
			precisionPlot.add(k, precision);
			recallPlot.add(k, recall);
			FmeasurePlot.add(k, Fmeasure);
			
			DecimalFormat dec = new DecimalFormat(".0000000  ");
			System.out.println("\t + \t -  - " + nameOfMetric + " " + nameOfClass + " Predictions for k = " + k);
			System.out.println(" + \t" + truePositives  + "\t" + falseNegatives + "\taccuracy  precision fmeasure  recall");
			System.out.println(" - \t" + falsePositives + "\t" + trueNegatives + "\t" + dec.format(accuracy) + dec.format(precision) + dec.format(Fmeasure) + dec.format(recall));
			System.out.println();

			truePositives = falsePositives = trueNegatives = falseNegatives = 0;
		}
	};
	
	void printConfusionMatricesForK(ArrayList<Record> tests, ArrayList<Record> trainingRecords, int k, HashMap<String, Scores> scoresPerClass) {
		//To calculate the confusion matrices which are needed to compute precision and other scores, loop over the test instances and check whether their predicted value is the same as their actual value.
		for(Record test : tests) {
			//Ask the record to predict its class using this distance algorithm.
			Scores predictedClass = scoresPerClass.get(test.predictClass(trainingRecords, this, k)),
				   actualClass = scoresPerClass.get(test.classname);
			
			if(predictedClass == actualClass) {
				predictedClass.truePositives++;
			} else {
				predictedClass.falsePositives++;
				actualClass.falseNegatives++;
			}
			
			for(Scores classInf : scoresPerClass.values())
				if(classInf != predictedClass && classInf != actualClass)
					classInf.trueNegatives++;
		}
		
		//Plot the precision, etc values for this value of k and reset the confusion matrices
		for(Scores classScores : scoresPerClass.values())
			classScores.calculate(k);
	}

	void printConfusionMatrices(ArrayList<Record> tests, ArrayList<Record> trainingRecords) {
		//For this run, during which will be calculated confusion matrices and precision, F1measure etc. scores for various values of k,
		//store a structure called Scores for each class in the dataset, which maintains information on the class's confusion matrix and
		//is called to computer it aggregate scores.
		HashMap<String, Scores> scoresPerClass = new HashMap<String, Scores>();
		for(String key : genes.classes.keySet())
			scoresPerClass.put(key, new Scores(getClass().getSimpleName().replaceAll("_", " "), key));
			//Make the new Scores objects here. They are used only temporarily to calculate the scores for this distance matrix. They will be destroyed after the function returns.
			//The Scores objects get passed the name of their target class so that they can add to the class's plot, as well as the name of this distance metric.

		for(int k = 3; k <= 11; k += 2)
			printConfusionMatricesForK(tests, trainingRecords, k, scoresPerClass);
	}
}

class Euclidean extends DistanceMetric {
	public double distanceBetween(Record a, Record b) {
		assert(a.attributes.length == b.attributes.length);
		
		double dist = 0;
		for (int attrib = 0; attrib < a.attributes.length; attrib++) {
			double temp = (a.attributes[attrib] - b.attributes[attrib]);
			dist += temp*temp;
		}
		return Math.sqrt(dist); //dist
	}
}

class Chebyshev extends DistanceMetric {
	public double distanceBetween(Record a, Record b) {
		assert(a.attributes.length == b.attributes.length);
		
		double dist = 0;
		for (int attrib = 0; attrib < a.attributes.length; attrib++)
			dist = Math.max(dist, Math.abs(a.attributes[attrib] - b.attributes[attrib]));
		return dist;
	}
}

class City_Block extends DistanceMetric {
	public double distanceBetween(Record a, Record b) {
		assert(a.attributes.length == b.attributes.length);
		
		double dist = 0;
		for (int attrib = 0; attrib < a.attributes.length; attrib++)
			dist += Math.abs(a.attributes[attrib] - b.attributes[attrib]);
		return dist;
	}
}

class Cosine extends DistanceMetric {
	public double distanceBetween(Record a, Record b) {
		assert(a.attributes.length == b.attributes.length);
		
		double ab_dot_product = 0, a_magnitude_squared = 0, b_magnitude_squared = 0;
		for (int attrib = 0; attrib < a.attributes.length; attrib++) {
			ab_dot_product += a.attributes[attrib] * b.attributes[attrib];
			a_magnitude_squared += a.attributes[attrib] * a.attributes[attrib];
			b_magnitude_squared += b.attributes[attrib] * b.attributes[attrib];
		}
		double cossim = ab_dot_product/(Math.sqrt(a_magnitude_squared)*Math.sqrt(b_magnitude_squared));
		return 1 - cossim;
	}
}

public class genes {
	//Error status for when the input file given is not available.
	static final int EX_NOINPUT = 66;
	
	public static HashMap<String, Class> classes = new HashMap<String, Class>();
	//Store an array of distance metrics for enumeration
	static final DistanceMetric[] metrics = new DistanceMetric[] {
		new Euclidean(),
		new Chebyshev(),
		new City_Block(),
		new Cosine()
	};
	
	static ArrayList<Record> parseArff(String fileName) throws IOException {
		ArrayList<Record> records = new ArrayList<Record>();
			
		//@data is the line immediately preceding csv
		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
		while (inputStream.readLine().indexOf("@data") == -1);
		
		String line;
		while ((line = inputStream.readLine()) != null && line.indexOf(",") != -1)
			records.add(new Record(line));
		
		return records;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.print("Type a to run on (a)ll genes, s for (s)ignificant genes, or return to enter a custom pair of filenames: ");
		String test = "s";//System.console().readLine();
		if(test.equals("a")) {
			classify("ALL_AML_AllGenes_NoID.train.arff", "ALL_AML_AllGenes_NoID.test.arff");
		} else if(test.equals("s")) {
			classify("ALL_AML_SignificantGenes_NoID.train.arff", "ALL_AML_SignificantGenes_NoID.test.arff");
		} else {
			System.out.println("Name of training file: ");
			String trainFile = System.console().readLine();
			System.out.println("Name of testing file: ");
			String testFile = System.console().readLine();
			classify(trainFile, testFile);
		}
	}
	
	//Create a ChartPanel from the given dataset, each of which will hold a series for every distance metric
	private static 
	ChartPanel createChart(final XYDataset dataset, String name, String className) {
        final JFreeChart chart = ChartFactory.createScatterPlot(
            className + " " + name + " vs. k",  // chart title
            "k",                      			// x axis label
            name,                     			// y axis label
            dataset,                  			// data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 300));
        return chartPanel;
    }

	static void classify(String trainFile, String testFile) throws IOException {
		try {
			System.out.println("Testing " + trainFile + " with " + testFile);
			
			//Parse files
			ArrayList<Record> trains = parseArff(trainFile);
			int trainClassCount = classes.size();
			ArrayList<Record> tests = parseArff(testFile);
			int testClassCount = classes.size() - trainClassCount;
			System.out.printf("Train stats: %d instances, %d attributes, %d classes\n", trains.size(), trains.get(0).attributes.length, trainClassCount);
			System.out.printf("Test stats: %d instances, %d attributes, %d classes\n", tests.size(), tests.get(0).attributes.length, testClassCount);

			//Set up display
			JPanel gridPanel = new JPanel();
			gridPanel.setLayout(new GridLayout(0, 3));
			for(Map.Entry<String, Class> className : classes.entrySet()) {
				gridPanel.add(createChart(className.getValue().precision, "Precision", className.getKey()));
				gridPanel.add(createChart(className.getValue().recall, "Recall", className.getKey()));
				gridPanel.add(createChart(className.getValue().Fmeasure, "F1-measure", className.getKey()));
			}
			final ApplicationFrame frame = new ApplicationFrame("Testing " + trainFile + " with " + testFile);
			frame.setContentPane(gridPanel);
			frame.pack();
			frame.setVisible(true);
			
			//System.out.println("Pre-normalization:");
			//System.out.println("Sample: " + tests.get(0));
			//Print confusion matrices and calculate and plot aggregates (F1-measure, recall, etc.)
			for(DistanceMetric metric : metrics) metric.printConfusionMatrices(tests, trains);
			
			//Print tables to stdout
			printTables();
			
			//altMinMaxNorm(tests, trains); //modifies records in place
			
			//System.out.println("Post-normalization:");
			//System.out.println("Sample: " + tests.get(0));
			//for(DistanceMetric metric : metrics) metric.printConfusionMatrices(tests, trains);
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
			System.exit(EX_NOINPUT);
		}
	}

	//Loop through the data series and print them out in csv form.
	static void printTables() {
		for(Map.Entry<String, Class> className : classes.entrySet()) {
			String precisionTables = "", recallTables = "", F1Tables = "";
			System.out.println(className.getKey());

			for(int k = 0; k < 5; k++) {
				precisionTables += "k=" + (k*2+3) + ",";
				recallTables += "k=" + (k*2+3) + ",";
				F1Tables += "k=" + (k*2+3) + ",";
				for(DistanceMetric dm : metrics) {
					precisionTables += className.getValue().precision.getSeries(dm.getClass().getSimpleName().replaceAll("_"," ")).getY(k) + ",";
					recallTables += className.getValue().recall.getSeries(dm.getClass().getSimpleName().replaceAll("_"," ")).getY(k) + ",";
					F1Tables += className.getValue().Fmeasure.getSeries(dm.getClass().getSimpleName().replaceAll("_"," ")).getY(k) + ",";
				}
				precisionTables += "\n";
				recallTables += "\n";
				F1Tables += "\n";
			}
			
			System.out.println("precision");
			System.out.println(precisionTables);
			System.out.println("recall");
			System.out.println(recallTables);
			System.out.println("F1");
			System.out.println(F1Tables);
		}
	}
	
	//The following methods can be used if normalization is necessary.
	static void altMinMaxNorm(ArrayList<Record> tests, ArrayList<Record> trains) {
		int numAttribs = tests.get(0).attributes.length;
		assert(trains.get(0).attributes.length == numAttribs);
		
		for(int i = 0; i < numAttribs; i++) {
			double min = 20, max = 16000;
			double range = max - min;
			for(Record r : trains) {
				if(range == 0) r.attributes[i] = 0.5;
				else r.attributes[i] =  (r.attributes[i] - min)/range;

				assert((r.attributes[i] >= 0.0) && (r.attributes[i] <= 1.0));
			}
			for(Record r : tests) {
				if(range == 0) r.attributes[i] = 0.5;
				else r.attributes[i] =  (r.attributes[i] - min)/range;

				assert((r.attributes[i] >= 0.0) && (r.attributes[i] <= 1.0));
			}
		}
	}
	
	static void minMaxNorm(ArrayList<Record> tests, ArrayList<Record> trains) {
		int numAttribs = tests.get(0).attributes.length;
		assert(trains.get(0).attributes.length == numAttribs);
		
		for(int i = 0; i < numAttribs; i++) {
			double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
			for(Record r : trains) {
				if(r.attributes[i] <= min) min = r.attributes[i];
				if(r.attributes[i] >= max) max = r.attributes[i];
			}
			for(Record r : tests) {
				if(r.attributes[i] <= min) min = r.attributes[i];
				if(r.attributes[i] >= max) max = r.attributes[i];
			}
			double range = max - min;
			for(Record r : trains) {
				if(range == 0) r.attributes[i] = 0.5;
				else r.attributes[i] =  (r.attributes[i] - min)/range;

				assert((r.attributes[i] >= 0.0) && (r.attributes[i] <= 1.0));
			}
			for(Record r : tests) {
				if(range == 0) r.attributes[i] = 0.5;
				else r.attributes[i] =  (r.attributes[i] - min)/range;

				assert((r.attributes[i] >= 0.0) && (r.attributes[i] <= 1.0));
			}
		}
	}
	
	//Doesn't work :(
	static void zscoreNorm(ArrayList<Record> tests, ArrayList<Record> trains) {
		int numAttribs = tests.get(0).attributes.length;
		assert(trains.get(0).attributes.length == numAttribs);
		
		for(int i = 0; i < numAttribs; i++) {
			double sum = 0;
			for(Record r : trains) sum += r.attributes[i];
			double mean = sum / trains.size();
			sum = 0;
			for(Record r : trains) sum += (r.attributes[i] - mean)*(r.attributes[i] - mean);
			double stddev = Math.sqrt(sum/(trains.size() - 1));
			
			for(Record r : trains)
				r.attributes[i] = (r.attributes[i] - mean)/stddev;
			for(Record r : tests)
				r.attributes[i] = (r.attributes[i] - mean)/stddev;
		}
	}
}
