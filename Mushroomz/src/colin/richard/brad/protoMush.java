package colin.richard.brad;

import java.io.*;
//import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Map;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.paukov.combinatorics.util.*;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ArffLoader.ArffReader;



//that's pretty much it
public class protoMush{
	public static ArrayList<Record> dataSet;
	public static ArrayList<Record> testingDataSet;
	static File inputTrain;
	static File inputTest;
	public protoMush(String in, String inTest) throws IOException{
		dataSet = parseArff(in.toString());
		testingDataSet = parseArff(inTest.toString());
		inputTrain = new File(in);
		inputTest = new File(inTest);
	}
	
	public static void main (String [] args) throws IOException{
		//run parseArff/parseAttributes
		
		protoMush mushy = new protoMush("mushrooms.train.arff", "mushrooms.test.arff"); 
		DTI dRunner = new DTI(dataSet, testingDataSet);
		dRunner.run();
		Knn2 knn = new Knn2();
		knn.run();
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
		inputStream.close();
		return records;
	}
	
	static ArrayList<ArrayList<String>> parseAttributes(String fileName) throws IOException{
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		
		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
		//we need the attributes section
		String in = inputStream.readLine();
		while (in.indexOf("@attribute") == -1){
			in = inputStream.readLine();
		}
		
		//while in the attributes section
		while (in.indexOf("@attribute") != -1){
			//keep track of spaces on the line
			int space = in.indexOf(" ");
			int space2 = in.lastIndexOf(" ");
			//the attributes name 
			String attrName = in.substring(space + 1, space2);
			//the attributes values. space2 + 2 should step over the "{" 
			//while in.length() - 1 should step behind the "}"
			String attrVals = in.substring(space2 + 2, in.length() - 1);
			//each index in toAdd is an attribute
			//the attribute's name appears in index 0
			//the attribute's values appear in the following indices
			ArrayList<String> toAdd = new ArrayList<String>();
			//toAdd.add(attrName);
			String[] temp = attrVals.split(",");
			for (int i = 0; i < temp.length; i++){
				toAdd.add(temp[i]);
				}
			results.add(toAdd);
			in = inputStream.readLine();
		}	
		//results will contain all of the arrayLists for the attributes
		//each attribute's arrayList will contain its name and its values as strings
		inputStream.close();
		return results;
		
	}
	static double lgBase(int n, double x){
		if (x == 0)
			return 0;
		else 
			return Math.log(x)/Math.log(n);
	}
}


//this might only work for KNN, needs to be modified for DTI
//this is not anything close to Richard's Records class
//all this does is keep track of the classname and attributes, which is useful
//the Tree class and DTI will perform class assignment and everything else
class Record {
	String[] attributes;
	String classname;
	
	String getClassname(){return classname;}
	
	Record(String line) {
		String[] fields = line.split(",");
		attributes = fields;
		//Globally keep track of class names so we can properly form confusion matrices, and so we know how many graphs to plot
		classname = fields[0];
		
	}
	
}

//performs DTI, contains methods which will also prune to later pass to modifiedKNN
class DTI{
	//constructor just instantiates fields, which may be redundant at this point
	ArrayList<Record> dataSet;
	ArrayList<String[]> attributes;
	ArrayList<String> classes;
	ArrayList<Record> testSet;
	ArrayList<String> evaluations;
	
	
	public DTI(ArrayList<Record> data, ArrayList<Record> tests){
		dataSet = data;
		ArrayList<String[]> att = new ArrayList<String[]>();
		ArrayList<String> eval = new ArrayList<String>();
		ArrayList<String> c = new ArrayList<String>();
		for (Record r : dataSet){
			att.add(r.attributes);
			c.add(r.classname);
		}
		testSet = tests;
		for (Record r :testSet){
			eval.add(r.classname);
		}
		attributes = att;
		evaluations = eval;
		classes = c;
	}

	void run() throws IOException{
		System.out.println("Running DTI");
		System.out.println("Training on:" + protoMush.inputTrain.toString());
		System.out.println("Testing on:" + protoMush.inputTest.toString());
		System.out.print("Building Tree... Please wait...");
		ArrayList<ArrayList<String>> allAttributes = protoMush.parseAttributes(protoMush.inputTrain.toString());
		Tree decisionTree = new Tree(allAttributes, dataSet, new GiniSplit());
		System.out.println();
		System.out.print("Traversing Tree... Please wait...");
		ArrayList<String> s = predictClasses(testSet, decisionTree);
		System.out.println();
		System.out.println("Finished traversing.");
		int COUNT_OUR_SUCCESS = 0;
		System.out.println("Now evaluating the success of the predictions");
		for (String s2 : s){
			if (s2.equals(evaluations.get(s.indexOf(s2)))){
				COUNT_OUR_SUCCESS++;
			}
		}
		System.out.print("Decision Tree has correctly predicted " + COUNT_OUR_SUCCESS + " records");
		System.out.print(" out of " + testSet.size() + " test records.");
		System.out.println();
		System.out.println("The program will now run KNN.");
		try {
		    Thread.sleep(10000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	//dunno if I should have this
	abstract class Analysis{
		abstract double analyzeNode(ArrayList<Record> dataSet);
		abstract double analyze(ArrayList<ArrayList<Record>> dataSubs);
	}
	//override some math
	abstract class GiniIndex extends Analysis {
		double analyzeNode(ArrayList<Record> dataSet){
			int dataSize = dataSet.size();
			double sum = 0;
			HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
			
			for(Record r : dataSet)
				if(!classCounts.containsKey(r.classname))
					classCounts.put(r.classname, 1);
				else
					classCounts.put(r.classname, classCounts.get(r.classname) + 1);
			
			for(int count : classCounts.values())
				sum += ((double)count)/((double)dataSize)*((double)count)/((double)dataSize);

			return 1.0 - sum;
		}
	}
	
	class GiniSplit extends GiniIndex{
		double analyze(ArrayList<ArrayList<Record>> childNodes){
			double GI; //Gini indices
			double sum = 0.0;
			int dataSize = 0; //size of the data we're dealing with at the entire node
			for (ArrayList<Record> r : childNodes) {
				dataSize += r.size(); //yeah it's silly, but good for generalization
			}
			
			for(ArrayList<Record> childNode : childNodes) {
				int counter = childNode.size();
				GI = super.analyzeNode(childNode); //check the GiniIndex at that branch, for the data subset that branch covers
				sum += ((double)counter/dataSize) * GI; //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
			}
			return sum;//return the sum
		}
	}/*
	abstract class Entropy extends Analysis{
		double analyze(String[] branch, ArrayList<Record> dataSet){
			double dataSize = (double)dataSet.size();
			double sum = 0;
			double counter = 0;
			
			for (int i = 0; i < branch.length; i++){ //parse through that branch
				counter += count(branch[i]); //and add the count of how many times the ith value of that branch appears in the data
				sum += (counter/dataSize)*protoMush.lgBase(2,counter/dataSize);	
			}
			return -sum;
		}
		abstract double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs);
	}
	class InfoGain extends Entropy{
		double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs){
			int sizeOfStuff = 0;
			double counter = 0.0;
			double E;
			double sum = 0.0;
			
			for (String[] s : combos){
				sizeOfStuff += s.length;
			}
			String[] allData = new String[sizeOfStuff];
			int placeHolder = 0;
			for (String [] s : combos){
				for (int i = 0 + placeHolder; i < s.length; i++){
					allData[i] = s[i];
				}
				placeHolder = s.length - 1;
			}
			double parent = super.analyze(allData, dataSet);
			
			for (String[] s : combos){//for each branch in the node
				for (int i = 0; i < s.length; i++){ //iterate through the branch
					counter += count(s[i]); //count how many records are at the branch
				}
				E = super.analyze(s, dataSubs.get(combos.indexOf(s))); //check the GiniIndex at that branch, for the data subset that branch covers
				sum += (counter/dataSet.size()) * E; //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
			}
			
			return parent - sum;
		}
	}
	
	class SplitInfo extends Analysis{
		
		double analyze(String[] branch, ArrayList<Record> dataSubs){
			return -1.0;
		}
		double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs){
			double counter = 0.0;
			double sum = 0.0;
			double j = 0.0;
			for (ArrayList<Record> a : dataSubs){
				j += a.size();
			}
			for (String[] s : combos){//for each branch in the node
				for (int i = 0; i < s.length; i++){ //iterate through the branch
					counter += count(s[i]); //count how many records are at the branch
				}
				sum += (counter/j) * protoMush.lgBase(2,(counter/j)); //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
			}
			return -sum;
		}
	}*/

	//generates all possible combinations for an array
	ArrayList<ArrayList<String[]>> possibleCombinations(String [] attr, int n) {
		assert(n < 30); //This function (hackily) uses the properties of binary integer operations to partition a set of strings into two subsets, so there should be less elements than there are bits in an int
		ArrayList<ArrayList<String[]>> results = new ArrayList<ArrayList<String[]>>();
		for(int i = 1; i < 1 << (attr.length - 1); i++) {
			ArrayList<String[]> result = new ArrayList<String[]>();
			String[] a, b;
			ArrayList<String> result_left  = new ArrayList<String>(),
							  result_right = new ArrayList<String>();
			for(int j = 0; j < attr.length; j++) {
				if((i & (1 << j)) != 0) {
					result_left.add(attr[j]);
				} else result_right.add(attr[j]);
			}
			a = new String[result_left.size()];
			result_left.toArray(a);
			result.add(a);
			//System.out.println(result_left);
			b = new String[result_right.size()];
			//System.out.println(result_right);
			result_right.toArray(b);
			result.add(b);
			results.add(result);
		}
		return results;
	}
	
	
	//generates all possible combinations for an array
	ArrayList<ArrayList<String[]>> possibleCombinations1(String [] attr, int n){
		
		   ICombinatoricsVector<String> vector = Factory.createVector(attr);
		   ArrayList<ArrayList<String[]>> results = new ArrayList<ArrayList<String[]>>();
		   // Create a complex-combination generator
		   Generator<ICombinatoricsVector<String>> gen = new ComplexCombinationGenerator<String>(vector, n);
		   String[] r;
		   List<String> a1;
		   // Iterate through the different combinations
		   ArrayList<String[]> results2 = null;
		   for (ICombinatoricsVector<ICombinatoricsVector<String>> comb : gen) {
			  results2 = new ArrayList<String[]>();
			  //Iterate through the individual parts of the combination
			  for (ICombinatoricsVector<String> v : comb){
				  a1 = v.getVector(); //this might be redundant
				  r = new String[a1.size()]; //we're going to put it in an array
				  a1.toArray(r); //here's the array
				  results2.add(r); //woohoo, add that array to the array of arrays
			  }
			  
			  results.add(results2); //add the array of arrays to another array
			  results2 = null;
		   }
		   return results;
		   //so, basically, what this looks like is, an array with many arrays in it
		   //each array in 'results' is an array of String[] arrays
		   //each String[] contains the individual attribute values
	}
	
	//this class does a lot of stuff
	class Tree{		
		
		ArrayList<ArrayList<String>> attrs;
		ArrayList<Record> data;
		Analysis measure;
		
		ArrayList<Tree> nodes = new ArrayList<Tree>();
		
		//takes the attributes this tray can possibly cover, the data it covers, and the type of analysis
		Tree(ArrayList<ArrayList<String>> allAttributes, ArrayList<Record> d, Analysis h) throws IOException{
			System.out.print(".");
			attrs = allAttributes;
			data = d;
			measure = h;
			if(d.size() > 5)
				build(h);
		}
		
		//finds the best attribute to split on and its split
		//iterates through the children of that split
		//adds them as trees
		//this is recursive
		void build(Analysis w) throws IOException{
			ArrayList<String[]> split = new ArrayList<String[]>();
			int attrID = establishHierarchy(w, attrs, data, split);
			
			splitAttr = attrID;
			splitValues = split;
			
			assert(split.size() == 2);
			assert(attrID >= 0);
			
			for (int i = 0; i < split.size(); i++){ //iterate through each of the children
				String[] branch = split.get(i); //branch is the array with the attribute values its split on
				ArrayList<Record> d = findUsedData(attrID, branch); //find the data which the split applies to 
				ArrayList<ArrayList<String>> a = findUnusedAttributes(attrID, branch); //find the attributes the split doesn't apply to
				if(a.size() > 1) {
					Tree node = new Tree(a, d, w); //create a child tree which works with the same data and unused attributes
					nodes.add(node); //nodes holds each tree child
				}
			}	
		}

		//yeah, establishes hierarchy for Tree class, holy shit this is an ugly method and it doesn't work
		//I guess, it actually would, if one of the members of each split were pure, but I don't think that'll work
		int establishHierarchy(Analysis chief, ArrayList<ArrayList<String>> allAttributes, ArrayList<Record> data, ArrayList<String[]> branch) throws IOException{ //chief is the analysis we're checking with
			ArrayList<ArrayList<String>> allAttributesCopy = new ArrayList<ArrayList<String>>(allAttributes.size() - 1);
			for(int i = 1; i < allAttributes.size(); i++) {
				allAttributesCopy.add(allAttributes.get(i));
			}
			allAttributes = allAttributesCopy;
			//allAttributes.remove(0); //we don't want to deal with the class
			//that line would keep removing the first attribute i think..? each level would have one less?
			
			ArrayList<Double> analBesties = new ArrayList<Double>(allAttributes.size()); //this will hold the best analysis for each attribute
			ArrayList<ArrayList<String[]>> besties = new ArrayList<ArrayList<String[]>>(allAttributes.size()); //this will hold the best split for each attribute
			for (int i = 0; i < allAttributes.size(); i++) {
				analBesties.add(null);
				besties.add(null);
			}
			
			for (int i = 0; i < allAttributes.size(); i++) { //for each attribute in allAttributes
				ArrayList<String> attr = allAttributes.get(i);
				
				String [] attrCast = new String[attr.size()]; 
				attr.toArray(attrCast); //put the attribute's values in the array attrCast
				
				ArrayList<ArrayList<String[]>> poss = possibleCombinations (attrCast, 2); //poss contains all possible splits on the attribute
				
				double bestAnalysis = Double.POSITIVE_INFINITY; //all hail s@an
				ArrayList<String[]> bestSplit = new ArrayList<String[]>();
				
				for (ArrayList<String[]> p : poss){ //for each possible split on the attribute, we're checking for the best
					ArrayList<ArrayList<Record>> DDDD = new ArrayList<ArrayList<Record>>();
					for (String [] s : p){
						DDDD.add(findUsedData(i, s));
					}
					
					double dummy = chief.analyze(DDDD); //check the analysis of that split
					if (bestAnalysis == Double.POSITIVE_INFINITY){ //if we're checking for the first time
						bestAnalysis = dummy;
						bestSplit = p;
					}
					else if (chief instanceof GiniIndex && dummy < bestAnalysis){ //if chief is Gini
						bestAnalysis = dummy;
						bestSplit = p;
					}/*
					else if(chief instanceof Entropy || chief instanceof InfoGain && dummy > bestAnalysis){ //if chief is something else
						bestAnalysis = dummy;
						bestSplit = p;
					}*/
				}
				analBesties.set(i, bestAnalysis); //store the best analysis for this attribute
				besties.set(i, bestSplit); //store the split for that attribute, the index will correspond with that of its analysis
			}
			
			double best = Double.POSITIVE_INFINITY;
			int indexOfBest = -1;
			//while (analBesties.size() > 0){
				for (int i = 0; i < analBesties.size(); i++) {
					if(analBesties.get(i) == null) {
						System.out.println("uhoh");
					}
					double d = analBesties.get(i);

					if (best == Double.POSITIVE_INFINITY){ //if we're checking for the first time
						best = d;
						indexOfBest = i; //analBesties.indexOf(d); -- this searches through the whole array just to find the index. that's not good.
					} else if (chief instanceof GiniIndex && d < best){ //if chief is Gini
						best = d;
						indexOfBest = i;
					}/*
					else if(chief instanceof Entropy || chief instanceof InfoGain && d > best){ //if chief is something else
						best = d;
						indexOfBest = i;
					}*/
				}
				if(indexOfBest == -1) {
					System.out.println("indexOfBest = -1");
				}
			
				branch.addAll(besties.get(indexOfBest)); //add the best split
				//analBesties.remove(indexOfBest); //remove that attribute's analysis 
				//besties.remove(indexOfBest); //remove that attribute
			//}
			
			//return results;
				//results is an array of ArrayLists, in order of best analysis
			return indexOfBest;
				//this is the best split of those remaining attributes		
		}
		
		//returns the data which a branch applies to
		public ArrayList<Record> findUsedData(int attrID, String[] branch){
			HashSet<String> tester = new HashSet<String>(Arrays.asList(branch));
			ArrayList<Record> results = new ArrayList<Record>();
			
			for (Record r : data) //for each record in the trainingSet
				if(tester.contains(r.attributes[attrID + 1]))
					results.add(r);
			return results;//yeah
		}
		
		//returns attributes unused by the branch
		ArrayList<ArrayList<String>> findUnusedAttributes(int attrID, String[] branch){
			ArrayList<ArrayList<String>> results = (ArrayList<ArrayList<String>>) attrs.clone(); //copy all the attributes from the parent node 
			
			results.set(attrID + 1, (ArrayList<String>)results.get(attrID + 1).clone()); //make a new copy of this arraylist because we're going to modify it by removing entries
			
			results.get(attrID + 1).removeAll(Arrays.asList(branch));

			return results;
		}
		
		//checks if a branch generated by a split is pure
		boolean isPure(int attrID, String[] branch){
			ArrayList<Record> d = findUsedData(attrID, branch); //we work with the data this branch applies to
			String c = d.get(0).classname; //check the first class in that data subset
			for (Record r : d){ //for each of the records in that subset
				if(!(r.classname.equals(c))){ //if one of the classes doesn't match
					return false; //the branch isn't pure
				}
			}
			return true; //if we've reached this point, the branch is pure
		}
		
		//returns the class the branch contains, if it is pure
		String getClass(int attrID, String[] branch) {
			if(isPure(attrID, branch)) {//if the branch is pure
				return findUsedData(attrID, branch).get(0).classname; //the classes will all be the same, return the first
			}
			return null; //if we've reached this point, the branch isn't pure and will return null
		}
		
		int splitAttr;
		ArrayList<String[]> splitValues;
		
		//just accesses establishHierarchy for the tree at this point in the recursion
		int splitAtThisLevel(ArrayList<String[]> branch) throws IOException{
			return establishHierarchy(measure, attrs, data, branch);
		}
		
		//deletes a child node and its children
		void prune(int nodeIndex){nodes.remove(nodeIndex);}
		
		//same as above but with different arg
		void prune(ArrayList<Record> node){nodes.remove(node);}
		
		//traverses recursively and assigns a class
		String assignClassTo(Record r) throws IOException{
			System.out.print(".");
			String[] theseAttributes = r.attributes; //get this record's attribute values
			String result; //prepare the result
			//for (Tree t: nodes){ //for each child node
			Tree t = this;
				//ArrayList<String[]> branch = new ArrayList<String[]>();
				int attrID = splitAttr;//t.splitAtThisLevel(branch);
				String[] s1 = splitValues.get(0);//branch.get(0); //get its first branch
				String[] s2 = splitValues.get(1); //get its second branch
				
				//if(attrID != t.splitAttr) {
				//	System.err.println("esrgsef");
				//}
				
				HashSet<String> s1Tester = new HashSet<String>(Arrays.asList(s1)),
								s2Tester = new HashSet<String>(Arrays.asList(s2));
				
				if(s1Tester.contains(theseAttributes[attrID + 1])) {
					if (isPure(attrID, s1))//if this branch is pure
						return getClass(attrID, s1); //we've found the class - BASECASE HOORAY
					else
						return t.nodes.get(0).assignClassTo(r); //otherwise, we go deeper on this node
				} else if(s2Tester.contains(theseAttributes[attrID + 1])) {
					if (isPure(attrID, s2))//if this branch is pure
						return getClass(attrID, s2); //we've found the class - BASECASE HOORAY
					else
						return t.nodes.get(1).assignClassTo(r); //otherwise, we go deeper on this node
				} else {
					System.err.println("shouldn't get here");
					System.exit(-1);
				}				
			//}
			return "error";
		}	
	}
	
	//pretty much just conglomerates test results
	ArrayList<String> predictClasses(ArrayList<Record> testSet, Tree decisionTree) throws IOException{
		ArrayList<String> results = new ArrayList<String>();
		for (Record r : testSet){
			results.add(decisionTree.assignClassTo(r));
			}
		return results;
		}
}

 class Knn2 {
	private static final int EUCLIDIAN = 0;
	private static final int CITYBLOCK = 1;
	private static final int CHEBYSHEV = 2;
	private static final int COSINE = 3;

	/**
	 * @param args
	 * @throws IOException 
	 */
	void run() throws IOException {
		Instances train = new ArffReader(new BufferedReader(new FileReader("mushrooms.train.arff"))).getData();
		train.setClassIndex(0);

		Instances test = new ArffReader(new BufferedReader(new FileReader("mushrooms.test.arff"))).getData();
		test.setClassIndex(0);			 

		//System.out.println(test.size());
		System.out.println("code is the measure");
		System.out.println("EUCLIDIAN = 0");
		System.out.println("CITYBLOCK = 1");
		System.out.println("CHEBYSHEV = 2");
		System.out.println("COSINE = 3");
		System.out.println();
		System.out.println("class is displayed as a number");
		System.out.println();

		int[] ks = {3,5,7,9,11};
		int[] codes = {0,1,2,3};
		int[] classlabels = new int[train.numClasses()];
		for (int i=0;i<train.numClasses();i++){
			classlabels[i]= i;
		}


		double p;
		double r;
		double f1;

		int[][] o;
		System.out.println("k , method , clss , p , r  ,  f1");
		for (int clss:classlabels){
			for (int code:codes){
				for (int k:ks){
					o = fullkNN(k,train,test,code);
					p = precision(o,clss);
					r = recall(o,clss);
					f1 = f1(o,clss);


					System.out.println(k + " , "+ code+ " , " + clss + " , " + p + " , " + r + " , " + f1);
				}
			}
		}

	}

	private static int[][] fullkNN(int k,Instances train, Instances test,int code) {
		int[][] confusionMat = new int[train.numClasses()][train.numClasses()];
		for (int i = 0; i<test.size();i++){
			int[] o = kNN(k,train,test.get(i),code);
			confusionMat[o[0]][o[1]]++; // guess, what it should be
		}
		return confusionMat;
	}

	private static double precision(int[][] cm, int clss){
		int total = 0;
		int TP = 0;
		for (int i=0;i<cm[clss].length;i++){
			if (i == clss){
				TP = cm[clss][clss];
				total+= cm[clss][clss];
			}else{
				total+= cm[i][clss];
			}
		}
		if (total == 0){
			return 0;
		}
		return (((double)TP)/((double)total));
	}

	private static double recall(int[][] cm, int clss){
		int total = 0;
		int TP = 0;
		for (int i=0;i<cm.length;i++){
			if (i == clss){
				TP = cm[clss][clss];
				total+= cm[clss][clss];
			}else{
				total+= cm[clss][i];
			}
		}
		if (total == 0){
			return 0;
		}
		return (((double)TP)/((double)total));

	}

	private static double f1(int[][] cm, int clss){
		double P = precision(cm,clss);
		double R = recall(cm,clss);
		if (P+R == 0){
			return 0;
		}
		return (2*P*R)/(P+R);
	}


	private static int[] kNN(int k, Instances train, Instance test,int code){
		Instance[] insts = new Instance[k];
		double[] distances = new double[k];
		double dis=0;
		for (Instance ins:train){

			if (code == EUCLIDIAN){
				dis = Euclidian(ins,test);
			}else if (code == CITYBLOCK){
				dis = CityBlock(ins,test);
			}else if (code == CHEBYSHEV){
				dis = Chebyshev(ins,test);
			}else if (code == COSINE){
				dis = CosineSimilarity(ins,test);
			}

			Instance temp;
			double tempScore;


			for (int i=0;i< insts.length && ins != null;i++){
				if (insts[i]!= null){
					if (code != COSINE){
						if (dis < distances[i]){
							temp = insts[i];
							tempScore = distances[i];
							distances[i] = dis;
							insts[i] = ins;
							dis = tempScore;
							ins = temp;
						}
					}else{
						if (dis > distances[i]){
							temp = insts[i];
							tempScore = distances[i];
							distances[i] = dis;
							insts[i] = ins;
							dis = tempScore;
							ins = temp;
						}
					}
				}else{
					temp = insts[i];
					tempScore = distances[i];
					distances[i] = dis;
					insts[i] = ins;
					dis = tempScore;
					ins = temp;
				}
			}
		}

		double[] options = new double[train.numClasses()];
		for (int i=0;i<k;i++){
			if (code != COSINE){
				options[(int) insts[i].value(insts[i].classIndex())]+= 1/Math.pow(distances[i],2);
			}else{
				options[(int) insts[i].value(insts[i].classIndex())]+= Math.pow(distances[i],2);	
			}
		}
		int[] result = new int[2];
		int winner =0;
		double winningScore = options[0];
		for (int i=1;i<options.length;i++){
			if (winningScore < options[i]){
				winningScore = options[i];
				winner = i;
			}
		}
		result[0] = winner;
		result[1] = (int) test.value(test.classIndex());
		return result;
	}

	private static double Euclidian(Instance a, Instance b){
		double sum = 0;
		for (int i=0;i<a.numAttributes();i++){
			if (!a.attribute(i).equals("ID numeric") && i != a.classIndex()){
				sum += Math.pow(a.value(i) - b.value(i),2); 
			}
		}
		return Math.sqrt(sum);
	}
	private static double CityBlock(Instance a, Instance b){
		double sum = 0;
		for (int i=0;i<a.numAttributes();i++){
			if (!a.attribute(i).equals("ID numeric") && i != a.classIndex()){
				sum += Math.abs(a.value(i) - b.value(i)); 
			}
		}
		return sum;
	}
	private static double CosineSimilarity(Instance a, Instance b){
		double sum = 0;
		double A = 0;
		double B =0;
		for (int i=0;i<a.numAttributes();i++){
			if (!a.attribute(i).equals("ID numeric") && i != a.classIndex()){
				sum += a.value(i)*b.value(i); 
				A += Math.pow(a.value(i), 2);
				B += Math.pow(b.value(i), 2);
			}
		}
		A = Math.sqrt(A);
		B = Math.sqrt(B);
		return (sum/(A*B));
	}
	private static double Chebyshev(Instance a, Instance b){
		double max = 0;
		double dis;
		for (int i=0;i<a.numAttributes();i++){
			if (!a.attribute(i).equals("ID numeric") && i != a.classIndex()){
				dis = Math.abs(a.value(i) - b.value(i)); 
				if (dis>max){
					max = dis;
				}
			}
		}
		return max;
	}

	private static void doStuff(Instances train,Instances test) throws IOException {
		double[][] hold = tohold(train);

		double[] averages = new double[hold[0].length];
		double[] standardD = new double[hold[0].length];
		double[] Tvalue = new double[hold[0].length];
		double[] s2n = new double[hold[0].length];

		// get averages
		int total;
		for (int j =0 ;j<hold[0].length;j++){
			total = 0;
			for (int i=0;i<hold.length;i++){
				total+= hold[i][j];
			}
			averages[j] = total/((double)hold[0].length);
		}

		// get standardD
		for (int j =0 ;j<hold[0].length;j++){
			total = 0;
			for (int i=0;i<hold.length;i++){
				total+= Math.pow(hold[i][j] - averages[j],2);
			}
			standardD[j] = Math.sqrt(total/((double)hold[0].length));
		}

		//Tvale
		for (int j=0; j < Tvalue.length;j++){
			Tvalue[j] = Math.abs(averages[j] - averages[averages.length-1])/Math.sqrt((Math.pow(standardD[j], 2)/((double)hold.length))+ (Math.pow(standardD[averages.length-1], 2)/((double)hold.length)));
		}

		//S2N
		for (int j=0; j < s2n.length;j++){
			s2n[j] = Math.abs(averages[j] - averages[averages.length-1])/(standardD[j] + standardD[averages.length-1]);
		}


		for (int i= train.numAttributes()-1;i>=0;i--){
			if (Tvalue[i] < 0.9002553111163426 || s2n[i] < 0.14604069594980987  ){
				train.deleteAttributeAt(i);
				test.deleteAttributeAt(i);
			}
		}

		ArffSaver saver = new ArffSaver();
		saver.setInstances(train);
		saver.setFile(new File("ALL_AML_SignificantGenes.train.arff"));
		saver.writeBatch();

		System.out.println("done 1");

		saver = new ArffSaver();
		saver.setInstances(test);
		saver.setFile(new File("ALL_AML_SignificantGenes.test.arff"));
		saver.writeBatch();
	}

	private static double[][] tohold(Instances data) {
		// reduces that the data to an array
		double[][] result = new double[data.size()][data.numAttributes()];

		for (int i = 0 ; i< data.size();i++){
			for (int j=0;j<data.get(i).numAttributes();j++){
				result[i][j] = data.get(i).value(j);
			}
		}
		return result;
	}

	private static void printFoldRatio(Instances data) {
		int[] count = new int[10];
		double min;
		double max;
		double ratio;
		double current;
		double globalMin = 0;
		double globalMax = 0;
		int globalMaxCount = 0;
		int globalMinCount = 0;
		for (int j=1;j<data.numAttributes()-1;j++){ 
			min = data.get(0).value(j);
			max = data.get(0).value(j);
			for (Instance i : data){
				current = i.value(j);
				if (current < min){
					min = current;
				}else if (current > max){
					max = current;
				}
			}
			ratio = max/min;
			if (j == 1){
				globalMin = ratio;
				globalMax = ratio;
				globalMaxCount = 1;
				globalMinCount= 1;
			}else{
				if (ratio > globalMax){
					globalMax = ratio;
					globalMaxCount = 1;
				}else if (ratio == globalMax){
					globalMaxCount++;
				}else if (ratio < globalMin){
					globalMin = ratio;
					globalMinCount = 1;
				}else if (ratio == globalMin){
					globalMinCount++;
				}
			}
			count[log2(ratio)]++;
		}

		System.out.println("global max is "+ globalMax+" it happened " + globalMaxCount+" times");
		System.out.println("global min is "+ globalMin+" it happened " + globalMinCount+" times");

		for (int i: count){
			System.out.println(i);
		}

	}

	private static int log2(double ratio) {
		return (int)(Math.log(ratio)/Math.log(2));
	}

	private static void printData(Instances data) {
		for (Instance i : data){
			for (int j = 0; j <- i.numAttributes(); j++) {
				System.out.print(i.value(j));
				if(j < (i.numAttributes() - 1))
					System.out.print(",");
			}
			System.out.println();
		}
	}
}


