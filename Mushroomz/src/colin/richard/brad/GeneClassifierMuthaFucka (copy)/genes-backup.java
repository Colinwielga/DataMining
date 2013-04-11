import java.io.*;
import java.util.ArrayList;
import java.text.DecimalFormat;

class Record
{
	double[] attributes;
	String classname;
	
	Record(String line) {
		String[] fields = line.split(",");
		
		attributes = new double[fields.length - 1];
		for(int i = 0; i < fields.length - 1; i++)
			attributes[i] = (double)Double.parseDouble(fields[i]);
			
		classname = fields[fields.length - 1];
		
		assert(attributes.length == fields.length - 1);
		assert(classname.equals("ALL") || classname.equals("AML"));
	}
	
	public String toString() {
		String result = "";
		for(int i = 0; i < 5; i++)
			result += String.format("%10.5f", attributes[i]);
		result += "..." + String.format("%10.5f", attributes[attributes.length-1]) + " " + classname;
		return result;
	}
	
	String predictClass(ArrayList<Record> trainingRecords, DistanceMetric how, int k) {
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
		
		for(int i = 0; i < trainingRecords.size(); i++) {
			recsWithDist[i] = new RecordWithDistance(trainingRecords.get(i), how.distanceBetween(this, trainingRecords.get(i)));
		}
		
		java.util.Arrays.sort(recsWithDist); //Closest records first
		
		double weightedAverage = 0; //Let ALL be positive, AML negative
		for(int i = 0; i < k; i++) {
			if(recsWithDist[i].trainingRec.classname.equals("ALL")) {
				weightedAverage += 1/recsWithDist[i].distance;
			}
		}
		
		return weightedAverage > 0 ? "ALL" : "AML";
	}
}
	
abstract class DistanceMetric {
	public abstract double distanceBetween(Record a, Record b);	
	
	void printConfusionMatrix(ArrayList<Record> tests, ArrayList<Record> trainingRecords, int k) {
		int allTruePositives  = 0, allFalsePositives = 0,
			allFalseNegatives = 0, allTrueNegatives  = 0;

		for(Record test : tests) {
			String classPrediction = test.predictClass(trainingRecords, this, k);
			if(classPrediction.equals("ALL")) {
				if(test.classname.equals("ALL"))
					allTruePositives++;
				else allFalsePositives++;
			} else {
				if(test.classname.equals("ALL"))
					allFalseNegatives++;
				else allTrueNegatives++;
			}
		}
		
		final String ANSI_RESET = "\u001B[0m";
		final String ANSI_BLACK = "\u001B[40m";
		final String ANSI_YELLOW = "\u001B[33m";
		
		double accuracy  = ((double)allTruePositives+allTrueNegatives)/(allFalseNegatives+allFalsePositives+allTrueNegatives+allTruePositives),
			   precision = ((double)allTruePositives)/(allFalsePositives+allTruePositives),
			   recall    = ((double)allTruePositives)/(allFalseNegatives+allTruePositives),
			   Fmeasure  = ((double)2*allTruePositives)/(2*allTruePositives + allFalsePositives + allFalseNegatives);
		
		DecimalFormat dec = new DecimalFormat(".000000" + ANSI_RESET + "  ");
		System.out.println("\tALL\tAML - " + getClass().getName().replaceAll("_", " ") + " Predictions for k = " + k);
		System.out.println("ALL\t" + allTruePositives  + "\t" + allFalseNegatives + "\taccuracy  precision fmeasure  recall");
		System.out.println("ALL\t" + allFalsePositives + "\t" + allTrueNegatives + "\t" + dec.format(accuracy) + dec.format(precision) + ANSI_BLACK + ANSI_YELLOW + dec.format(Fmeasure) + ANSI_RESET + dec.format(recall));
		System.out.println();
	}
	
	void printConfusionMatrices(ArrayList<Record> tests, ArrayList<Record> trainingRecords) {
		for(int k = 3; k <= 11; k += 2)
			printConfusionMatrix(tests, trainingRecords, k);
		System.out.println("Press enter to continue.");
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
		return Math.sqrt(dist);
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

class genes {
	static final int EX_NOINPUT = 66;
	static final DistanceMetric[] metrics = new DistanceMetric[] {
		new Euclidean(),
		new Chebyshev(),
		new City_Block(),
		new Cosine()
	};
	
	static ArrayList<Record> parseArff(String fileName) throws IOException {
		ArrayList<Record> records = new ArrayList<Record>();
			
		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
		while (inputStream.readLine().indexOf("@data") == -1);
		
		String line;
		while ((line = inputStream.readLine()) != null && line.indexOf(",") != -1)
			records.add(new Record(line));
		
		return records;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.print("Automatic mode? Y/n: ");
		if(System.console().readLine() != "n") {
			classify("ALL_AML_AllGenes_NoID.train.arff", "ALL_AML_AllGenes_NoID.test.arff");
			classify("ALL_AML_SignificantGenes_NoID.train.arff", "ALL_AML_SignificantGenes_NoID.test.arff");
		} else {
			System.out.println("Name of training file: ");
			String trainFile = System.console().readLine();
			System.out.println("Name of testing file: ");
			String testFile = System.console().readLine();
			classify(trainFile, testFile);
		}
	}
	
	static void classify(String trainFile, String testFile) throws IOException {
		try {
			System.out.println("Testing " + trainFile + " with " + testFile);
			
			ArrayList<Record> trains = parseArff(trainFile),
							  tests  = parseArff(testFile);

			System.out.println("Pre-normalization:");
			System.out.println("Sample: " + tests.get(0));
			for(DistanceMetric metric : metrics) metric.printConfusionMatrices(tests, trains);
			
			altMinMaxNorm(tests, trains); //modifies records in place
			
			System.out.println("Post-normalization:");
			System.out.println("Sample: " + tests.get(0));
			for(DistanceMetric metric : metrics) metric.printConfusionMatrices(tests, trains);
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
			System.exit(EX_NOINPUT);
		}
	}
	
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
			
			/*double sum = 0, sum2 = 0;
			for(Record r : trains) {
				sum  += r.attributes[i];
				sum2 += r.attributes[i]*r.attributes[i];
			}
			double mean   = sum  / trains.size(),
				   mean2  = sum2 / trains.size(),
				   stddev = Math.sqrt(mean2 - mean*mean);*/
			
			for(Record r : trains)
				r.attributes[i] = (r.attributes[i] - mean)/stddev;

			for(Record r : tests)
				r.attributes[i] = (r.attributes[i] - mean)/stddev;
		}
	}
}

