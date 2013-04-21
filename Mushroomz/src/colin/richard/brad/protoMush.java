package colin.richard.brad;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import colin.richard.brad.DistanceMetric.Scores;


public class protoMush{
	public ArrayList<Record> dataSet;
	
	public protoMush(File in){}
	
	public static void main(String [] args){
		//run parseArff
		//run DTI
		//run KNN
		//run ModifiedKNN(just KNN, passed data pruned by DTI)
	}
	
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
	
	static ArrayList<ArrayList> parseAttributes(String fileName){return null;} 
	
}

//this might only work for KNN, needs to be modified for DTI
//Parses and stores train and test records, and predicts the class of test records with the help of a DistanceMetric.
class Record {
	double[] attributes;
	String classname;
	
	String getClassname(){return classname;}
	
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

//performs DTI, contains methods which will also prune to later pass to modifiedKNN
class DTI{
	ArrayList<Record> dataSet;
	ArrayList<String[]> attributes;
	ArrayList<String> classes;
	
	//constructor just instantiates variables
	public DTI(ArrayList<Record> data, ArrayList<String[]> att, ArrayList<String> c){
		dataSet = data;
		attributes = att;
		classes = c;
	}
	
	//dunno if I should have this
	abstract class Analysis{
		//override some  math
		class GiniIndex extends Analysis{}
		class Entropy extends Analysis{}
		class InfoGain extends Analysis{}
		class SplitInfo extends Analysis{}
	}
	
	//dunno if I need this either
	class DTIRecord extends Record{
		DTIRecord(String in){
			super(in);
			}
	}
	
	//yeah, establishes hierarchy for Tree class
	ArrayList<ArrayList> establishHierarchy(){
		//it will use the attributes array for reference
		//it needs to make instance counts for each attribute - ?
		//it will analyze the attributes based on the analysis chosen
		//it will then add the best split (I might need a possible splits array?)
		//it will then add that split to the results array
		//it will then move on, remove that attribute from the temp array and reiterate
		//maybe it could be a recursive algorithm?
		//ick
		return null;
	}
	
	//this class does a lot of stuff
	class Tree{
		//it's an arrayList of arrayLists. Each index is a branch from the initial node and so-on 
		//we're able to follow the hierarchy through 
		ArrayList<ArrayList> nodes;
		
		Tree(ArrayList<ArrayList> hierarchy){
			nodes = hierarchy;
		}
		
		//this actually isn't right
		int length(){return nodes.size();}
		
		//yay toString - I want a nice visual of the tree
		public String toString(){return "";}
		
		//yeah - that
		int findAttributeNode(String attribute){return -1;}
		
		//deletes a node and its children - ick maybe not good stuff happening
		void prune(int nodeIndex){nodes.remove(nodeIndex);}
		
		//same as above but stupider
		void prune(ArrayList<Record> node){nodes.remove(node);}
		
		//the test
		String assignClassTo(Record r){return null;}
	}
	
	//pretty much just conglomerates test results
	ArrayList<String> predictClasses(ArrayList<Record> testSet, Tree decisionTree){
		ArrayList<String> results = new ArrayList<String>();
		for (Record r : testSet){
			results.add(decisionTree.assignClassTo(r));
			}
		return results;
		}
}

//performs KNN
class KNN{}

//DistanceMetric and its derivatives, pirated from Richard's code
abstract class DistanceMetric {
	//Polymorphism allows the code organization to be simplified, and makes it more extensible.
	public abstract double distanceBetween(Record a, Record b);	
	
	class Scores {
		//XYSeries precisionPlot, recallPlot, FmeasurePlot;
		String nameOfClass, nameOfMetric;
		int truePositives, falsePositives, trueNegatives, falseNegatives; //Reset after every k
		
		Scores(String metricName, String classNm) {
			nameOfMetric = metricName;
			nameOfClass = classNm;
			//genes.classes.get(classNm).precision.addSeries(precisionPlot = new XYSeries(metricName));
			//genes.classes.get(classNm).recall.addSeries(recallPlot    = new XYSeries(metricName));
			//genes.classes.get(classNm).Fmeasure.addSeries(FmeasurePlot  = new XYSeries(metricName));
		}
		void calculate(int k) {
			double accuracy  = ((double)truePositives+trueNegatives)/(falseNegatives+falsePositives+trueNegatives+truePositives),
				   precision = ((double)truePositives)/(falsePositives+truePositives),
				   recall    = ((double)truePositives)/(falseNegatives+truePositives),
				   Fmeasure  = ((double)2*truePositives)/(2*truePositives + falsePositives + falseNegatives);
		
			//precisionPlot.add(k, precision);
			//recallPlot.add(k, recall);
			//FmeasurePlot.add(k, Fmeasure);
			
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
			scoresPerClass.put(key, new Scores(getClass().getName().replaceAll("_", " "), key));
			//Make the new Scores objects here. They are used only temporarily to calculate the scores for this distance matrix. They will be destroyed after the function returns.
			//The Scores objects get passed the name of their target class so that they can add to the class's plot, as well as the name of this distance metric.

		for(int k = 3; k <= 11; k += 2)
			printConfusionMatricesForK(tests, trainingRecords, k, scoresPerClass);
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


