

import java.io.*;
import java.util.*;

public class Classifier {
	public static void classify(String in, String inTest) throws IOException{
		ArrayList<NominalInstance> dataSet = parseArff(in);
		ArrayList<NominalInstance> testingDataSet = parseArff(inTest);
		
		ArrayList<String> attrNames = new ArrayList<String>();
		//run DTI
		new DTI(dataSet, testingDataSet, parseAttributes(new File(in).toString(), attrNames));
		//run KNN
		kNN.main(null);
		//run ModifiedKNN(just KNN, passed data pruned by DTI)
		System.out.println("Done");
	}
	
	public static void main(String [] args) throws IOException {
		classify("mushrooms.nostalkroot.shuffled.train.arff", "mushrooms.nostalkroot.shuffled.test.arff");
	}
	
	static ArrayList<NominalInstance> parseArff(String fileName) throws IOException {
		ArrayList<NominalInstance> records = new ArrayList<NominalInstance>();
			
		//@data is the line immediately preceding csv
		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
		while (inputStream.readLine().indexOf("@data") == -1);
		
		String line;
		while ((line = inputStream.readLine()) != null && line.indexOf(",") != -1)
			records.add(new NominalInstance(line));
		inputStream.close();
		return records;
	}
	
	static ArrayList<ArrayList<String>> parseAttributes(String fileName, ArrayList<String> attrNames) throws IOException{
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

			attrNames.add(in.substring(space + 1, space2)); //store attribute's name
			
			//the attributes values. space2 + 2 should step over the "{" 
			//while in.length() - 1 should step behind the "}"
			String attrVals = in.substring(space2 + 2, in.length() - 1);
			//each index in toAdd is an attribute
			//the attribute's name appears in index 0
			//the attribute's values appear in the following indices
			ArrayList<String> toAdd = new ArrayList<String>();

			String[] temp = attrVals.split(",");
			for (int i = 0; i < temp.length; i++)
				toAdd.add(temp[i]);

			results.add(toAdd);
			in = inputStream.readLine();
		}	
		//results will contain all of the arrayLists for the attributes
		//each attribute's arrayList will contain its name and its values as strings
		inputStream.close();
		return results;
		
	}
	static double lgBase(int n, double x){
		return x == 0 ? 0 : Math.log(x)/Math.log(n);
	}
}


//performs DTI, contains methods which will also prune to later pass to modifiedKNN
class DTI {
	public DTI(ArrayList<NominalInstance> data, ArrayList<NominalInstance> tests, ArrayList<ArrayList<String>> allAttributes) throws IOException{
		Tree decisionTree = new Tree(allAttributes, data, new GiniSplit());
		
		ArrayList<String> predictedClasses = predictClasses(tests, decisionTree);
		int successfulPredictions = 0;
		for(int i = 0; i < predictedClasses.size(); i++)
			if (predictedClasses.get(i).equals(tests.get(i).classname))
				successfulPredictions++;
		System.out.printf("%s/%s\n", successfulPredictions, predictedClasses.size());
		
		decisionTree.print(0);
	}
	
	abstract class Analysis {
		abstract double analyzeNode(ArrayList<NominalInstance> dataSet);
		abstract double analyze(ArrayList<ArrayList<NominalInstance>> dataSubs);
	}
	
	//override some math
	abstract class GiniIndex extends Analysis {
		double analyzeNode(ArrayList<NominalInstance> dataSet){
			int dataSize = dataSet.size();
			double sum = 0;
			HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
			
			for(NominalInstance r : dataSet)
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
		double analyze(ArrayList<ArrayList<NominalInstance>> childNodes){
			double GI; //Gini indices
			double sum = 0.0;
			int dataSize = 0; //size of the data we're dealing with at the entire node
			for (ArrayList<NominalInstance> r : childNodes) {
				dataSize += r.size(); //yeah it's silly, but good for generalization
			}
			
			
			for(ArrayList<NominalInstance> childNode : childNodes) {
				int counter = childNode.size();
				GI = super.analyzeNode(childNode); //check the GiniIndex at that branch, for the data subset that branch covers
				sum += ((double)counter/dataSize) * GI; //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
			}
			
			if(sum != sum) {
				return Double.POSITIVE_INFINITY;
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
	
	class Tree{		
		ArrayList<ArrayList<String>> attrs;
		ArrayList<NominalInstance> data;
		Analysis measure;
		
		ArrayList<Tree> nodes = new ArrayList<Tree>();
		
		//takes the attributes this tray can possibly cover, the data it covers, and the type of analysis
		Tree(ArrayList<ArrayList<String>> allAttributes, ArrayList<NominalInstance> d, Analysis h) throws IOException{
			attrs = allAttributes;
			data = d;
			measure = h;
			if(d.size() == 890) {
				System.out.println("Rtgtg");
			}
			if(d.size() > 5 || (d.size() > 1 && !isPure()))
				build(h);
		}
		
		void print(int indent) {
			System.out.println(new String(new char[indent]).replace("\0", "\t") + splitAttr);
			for(Tree node : nodes)
				node.print(indent + 1);
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
				ArrayList<NominalInstance> d = findUsedData(attrID, branch); //find the data which the split applies to 
				ArrayList<ArrayList<String>> a = findUnusedAttributes(attrID, branch); //find the attributes the split doesn't apply to
				if(a.size() > 1 && d.size() > 0 && d.size() < data.size()) {
					Tree node = new Tree(a, d, w); //create a child tree which works with the same data and unused attributes
					nodes.add(node); //nodes holds each tree child
				}
			}	
		}

		//yeah, establishes hierarchy for Tree class, holy shit this is an ugly method and it doesn't work
		//I guess, it actually would, if one of the members of each split were pure, but I don't think that'll work
		int establishHierarchy(Analysis chief, ArrayList<ArrayList<String>> allAttributes, ArrayList<NominalInstance> data, ArrayList<String[]> branch) throws IOException{ //chief is the analysis we're checking with
			if(data.size() == 889) {
				System.out.println("gggt");
			}
			
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
					ArrayList<ArrayList<NominalInstance>> DDDD = new ArrayList<ArrayList<NominalInstance>>();
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
		public ArrayList<NominalInstance> findUsedData(int attrID, String[] branch){
			HashSet<String> tester = new HashSet<String>(Arrays.asList(branch));
			ArrayList<NominalInstance> results = new ArrayList<NominalInstance>();
			
			for (NominalInstance r : data) //for each record in the trainingSet
				if(tester.contains(r.attributes[attrID + 1]))
					results.add(r);
			return results;//yeah
		}
		
		//returns attributes unused by the branch
		ArrayList<ArrayList<String>> findUnusedAttributes(int attrID, String[] branch){
			ArrayList<ArrayList<String>> results = (ArrayList<ArrayList<String>>) attrs.clone(); //copy all the attributes from the parent node 
			
			//results.set(attrID + 1, (ArrayList<String>)results.get(attrID + 1).clone()); //make a new copy of this arraylist because we're going to modify it by removing entries
			results.set(attrID + 1, new ArrayList<String>(Arrays.asList(branch)));

			return results;
		}
		
		//checks if a branch generated by a split is pure
		boolean isPure(){
			if(data.size() == 0) {
				System.err.print("ESRgersgrfg");
			}
			String c = data.get(0).classname; //check the first class in that data subset
			for (NominalInstance r : data){ //for each of the records in that subset
				if(!(r.classname.equals(c))){ //if one of the classes doesn't match
					return false; //the branch isn't pure
				}
			}
			return true; //if we've reached this point, the branch is pure
		}
		
		//returns the class the branch contains, if it is pure
		String getClassName() {
			if(isPure()) {//if the branch is pure
				return data.get(0).classname; //the classes will all be the same, return the first
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
		void prune(ArrayList<NominalInstance> node){nodes.remove(node);}
		
		//traverses recursively and assigns a class
		String assignClassTo(NominalInstance r) throws IOException{
			String[] theseAttributes = r.attributes; //get this record's attribute values
			String result; //prepare the result
			//ArrayList<String[]> branch = new ArrayList<String[]>();
			
			if(nodes.size() > 0) {
				int attrID = splitAttr;//t.splitAtThisLevel(branch);
				String[] s1 = splitValues.get(0);//branch.get(0); //get its first branch
				String[] s2 = splitValues.get(1); //get its second branch
				
				//if(attrID != t.splitAttr) {
				//	System.err.println("esrgsef");
				//}
				
				HashSet<String> s1Tester = new HashSet<String>(Arrays.asList(s1)),
								s2Tester = new HashSet<String>(Arrays.asList(s2));
				
				if(s1Tester.contains(theseAttributes[attrID + 1]))
					return nodes.get(0).assignClassTo(r); //otherwise, we go deeper on this node
				else //if(s2Tester.contains(theseAttributes[attrID + 1])) {
					return nodes.get(1).assignClassTo(r); //otherwise, we go deeper on this node
			} else {
				return getClassName();
			}
			
			
			/*else {
				System.err.println("shouldn't get here");
				System.exit(-1);
			}	*/			
			//}
			//return "error";
		}	
	}
	
	//pretty much just conglomerates test results
	ArrayList<String> predictClasses(ArrayList<NominalInstance> testSet, Tree decisionTree) throws IOException{
		ArrayList<String> results = new ArrayList<String>();
		for (NominalInstance r : testSet){
			results.add(decisionTree.assignClassTo(r));
			}
		return results;
		}
}


