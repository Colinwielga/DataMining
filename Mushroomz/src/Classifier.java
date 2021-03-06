import java.io.*;
import java.util.*;
import java.util.Map.Entry;


public class Classifier {
	public static void classify(String in, String inTest) throws IOException{
		//Scanner lol = new Scanner(System.in);
		System.out.println("Please enter what percentage you'd like to train with: ");
		//int p = lol.nextInt();
		int p = 15;
		String knnMethod = //"";
		   				   "FS-";
		   				   //"FE-";
						   //"cFE-";
		
		int pDTI = p;
		if(knnMethod.equals("cFE-"))
			pDTI = 100;
		
		ArrayList<NominalInstance> dataSet = parseArff(in, pDTI); //use pDTI% of the dataset
		ArrayList<NominalInstance> testingDataSet = parseArff(inTest, -pDTI); //use everything else
		
		ArrayList<String> attrNames = new ArrayList<String>();
		//run DTI
		long sRT = System.currentTimeMillis();
		DTI.Tree decisionTree = new DTI(dataSet, testingDataSet, parseAttributes(new File(in).toString(), attrNames), attrNames).decisionTree;
		long aRT = System.currentTimeMillis();
		long l = aRT - sRT;
		System.out.println();
		System.out.println("DTI runtime is " + l/1000.0 + " seconds."); 
		System.out.println();
		
		//run KNN 
		long start = System.nanoTime();
		if(knnMethod.equals("")) {
			kNN.classify("mushrooms.expanded.shuffled.nostalkroot.arff", "mushrooms.expanded.shuffled.nostalkroot.arff", null, p);
		} else if (knnMethod.equals("FS-")) {
			//run ModifiedKNN(just KNN, passed data pruned by DTI)
			HashSet<Integer> attrIDs = decisionTree.getTopLevelAttributes(7);
			kNN.classify("mushrooms.expanded.shuffled.nostalkroot.arff", "mushrooms.expanded.shuffled.nostalkroot.arff", attrIDs, p);	
		} else {
			HashMap<Integer, ArrayList<ArrayList<String>>> attrIDVals = new HashMap<Integer, ArrayList<ArrayList<String>>>();
			decisionTree.getTopLevelAttrVals(attrIDVals);
			for(ArrayList<ArrayList<String>> e : attrIDVals.values()) {
				while(e.remove(new ArrayList<String>())); //Remove [] entries which will always be 0.0 
				ArrayList<String> biggest = null;
				for(ArrayList<String> ee : e)
					if(biggest == null || ee.size() > biggest.size())
						biggest = ee; //Find the largest remaining set of values for this attribute
				if(biggest != null)
					e.remove(biggest); //Remove this set of values as it is the negation of the other values
			}
			
			if(knnMethod.equals("cFE-")) {
				//fix these two datasets because we already
				dataSet = parseArff(in, p);
				testingDataSet = parseArff(inTest, -p);				
			}
			
			//Now that features have been extracted from the full dataset, train kNN using a small (p%) subset
			ArrayList<Record> numericDataSet = new ArrayList<Record>(); 
			ArrayList<Record> testDataSet = new ArrayList<Record>(); 
			for(NominalInstance i : dataSet) {
				Record r = i.featureExtract(attrIDVals);
				numericDataSet.add(r);
				kNN.classes.get(r.classname).count++;
				if(kNN.classes.get(r.classname).avgAttrVal == null) {
					kNN.classes.get(r.classname).avgAttrVal = new ArrayList<Double>(r.attributes);
					kNN.classes.get(r.classname).stddevAttrVal = new ArrayList<Double>(r.attributes);
					for(int a = 0; a < kNN.classes.get(r.classname).stddevAttrVal.size(); a++)
						kNN.classes.get(r.classname).stddevAttrVal.set(a, 
								kNN.classes.get(r.classname).stddevAttrVal.get(a)*kNN.classes.get(r.classname).stddevAttrVal.get(a));
				} else for(int a = 0; a < r.attributes.size(); a++) {
					kNN.classes.get(r.classname).avgAttrVal.set(a, 
							kNN.classes.get(r.classname).avgAttrVal.get(a) + r.attributes.get(a));
					kNN.classes.get(r.classname).stddevAttrVal.set(a, 
							kNN.classes.get(r.classname).stddevAttrVal.get(a) + r.attributes.get(a)*r.attributes.get(a));
				}
			}
			for(Class c : kNN.classes.values())
				for(int d = 0; d < c.avgAttrVal.size(); d++) {
					c.avgAttrVal.set(d, c.avgAttrVal.get(d)/c.count);
					c.stddevAttrVal.set(d, Math.sqrt(c.stddevAttrVal.get(d)/c.count - c.avgAttrVal.get(d)*c.avgAttrVal.get(d)));
				}
			ArrayList<Double> S2N = new ArrayList<Double>();
			ArrayList<Double> Tval = new ArrayList<Double>();
			for(int a = 0; a < kNN.classes.get("poisonous").avgAttrVal.size(); a++) {
				S2N.add(Math.abs(kNN.classes.get("poisonous").avgAttrVal.get(a) - 
						kNN.classes.get("edible").stddevAttrVal.get(a))/(kNN.classes.get("poisonous").stddevAttrVal.get(a) + 
								kNN.classes.get("edible").stddevAttrVal.get(a)));
				Tval.add(Math.abs(kNN.classes.get("poisonous").avgAttrVal.get(a) - 
						kNN.classes.get("edible").stddevAttrVal.get(a))/
						Math.sqrt(kNN.classes.get("poisonous").stddevAttrVal.get(a)*kNN.classes.get("poisonous").stddevAttrVal.get(a)/
								kNN.classes.get("poisonous").count + 
								kNN.classes.get("edible").stddevAttrVal.get(a)*kNN.classes.get("edible").stddevAttrVal.get(a)/
								kNN.classes.get("edible").count));
			}
			for(NominalInstance i : testingDataSet)
				testDataSet.add(i.featureExtract(attrIDVals));
			//kNN.classifyRecords(numericDataSet, testDataSet, null);
			int x = 0;
			for(Entry<Integer, ArrayList<ArrayList<String>>> e : attrIDVals.entrySet())
				for(ArrayList<String> vals : e.getValue()) {
					System.out.printf("%s: %s (S2N: %6.6f, Tval: %6.6f)\n", attrNames.get(e.getKey() + 1), vals, S2N.get(x), Tval.get(x));
					x++;
				}
		}
		long between = System.nanoTime();

		System.out.println("Time to run kNN: " + (between - start)/1000000000);
		System.out.println("Done");
	}
	
	public static void main(String [] args) throws IOException {
		classify("mushrooms.expanded.shuffled.nostalkroot.arff", "mushrooms.expanded.shuffled.nostalkroot.arff");
	}
	
	static ArrayList<NominalInstance> parseArff(String fileName, int percentFilter) throws IOException {
		ArrayList<NominalInstance> records = new ArrayList<NominalInstance>();
			
		//@data is the line immediately preceding csv
		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
		while (inputStream.readLine().indexOf("@data") == -1);
		
		String line;
		while ((line = inputStream.readLine()) != null && line.indexOf(",") != -1)
			records.add(new NominalInstance(line));
		inputStream.close();
		
		if(percentFilter > 0) {
			System.out.println("to: " + (percentFilter*records.size())/100);
			return new ArrayList<NominalInstance>(records.subList(0, (percentFilter*records.size())/100));
		} else {
			System.out.println("from: " + (-percentFilter*records.size())/100);
			return new ArrayList<NominalInstance>(records.subList((-percentFilter*records.size())/100, records.size()));
		}
		//return records;
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
	public Tree decisionTree;
	
	public DTI(ArrayList<NominalInstance> data, ArrayList<NominalInstance> tests, ArrayList<ArrayList<String>> allAttributes, ArrayList<String> attrNames) throws IOException{
		decisionTree = new Tree(allAttributes, data, new GiniSplit());
		
 		String[] c = {"poisonous", "edible"};
 		
		double truePos = 0;
		double falsePos = 0;
		double trueNeg = 0;
		double falseNeg = 0;
		ArrayList<String> predictedClasses = predictClasses(tests, decisionTree);
		int successfulPredictions = 0, TP = 0, AllP = 0;
		
		for(int i = 0; i < predictedClasses.size(); i++) {
			if (predictedClasses.get(i).equals(tests.get(i).classname)) {
				successfulPredictions++;
				if (predictedClasses.get(i).equals(c[0]))
					truePos++;
				else trueNeg++;
			} else {
				if (predictedClasses.get(i).equals(c[0]))
					falsePos++;
				else falseNeg++;
			}
		}
		double accu = (truePos + trueNeg)/(truePos + trueNeg + falsePos + falseNeg); 
		double prec = (truePos)/(truePos + falsePos);
		double rec = (truePos)/(truePos + falseNeg);
		String confusion ="  +       -\n" + "+ " + truePos + "       " + falseNeg +"\n"
				+ "- " + falsePos + "       " + trueNeg;
		System.out.println(confusion);
		System.out.println("Accuracy: " + accu * 100.0 +"\nPrecision: " + prec * 100.0 + "\nRecall: " +rec * 100.0);
		System.out.println(); 
		System.out.println();
		decisionTree.print(0, attrNames);
	}
	
	abstract class Analysis {
		abstract double analyzeNode(ArrayList<NominalInstance> dataSet);
		abstract double analyze(Tree parentTree, ArrayList<ArrayList<NominalInstance>> dataSubs);
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
	abstract class Entropy extends Analysis {
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
				sum += ((double)count)/((double)dataSize)*Classifier.lgBase(2, ((double)count)/((double)dataSize));

			return -sum;
		}
	}

	class GiniSplit extends GiniIndex{
		double analyze(Tree parentTree, ArrayList<ArrayList<NominalInstance>> childNodes){
			double GI; //Gini indices
			double sum = 0.0;
			int dataSize = parentTree.data.size();
			int dsz2 = 0;
			for(ArrayList<NominalInstance> node : childNodes)
				dsz2 += node.size();
			assert(dataSize == dsz2);
			
			for(ArrayList<NominalInstance> childNode : childNodes) {
				GI = super.analyzeNode(childNode); //check the GiniIndex at that branch, for the data subset that branch covers
				sum += ((double)childNode.size()/dataSize) * GI; //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
			}
			
			if(sum != sum) {
				return Double.POSITIVE_INFINITY;
			}
			return sum;//return the sum
		}
	}
	class InfoGain extends Entropy {
		double analyze(Tree parentTree, ArrayList<ArrayList<NominalInstance>> childNodes){
			double sum = 0.0;
			int dataSize = parentTree.data.size();
			
			for(ArrayList<NominalInstance> childNode : childNodes) {
				double E = super.analyzeNode(childNode); //check the GiniIndex at that branch, for the data subset that branch covers
				sum += ((double)childNode.size()/dataSize) * E; //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
			}
			
			if(sum != sum)
				return Double.POSITIVE_INFINITY;

			return analyzeNode(parentTree.data) - sum;//return the sum
		}
	}
	
	/*class SplitInfo extends Analysis{
		
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
			if(!isPure())
				build(h);
		}
		
		public void getTopLevelAttrVals(HashMap<Integer, ArrayList<ArrayList<String>>> attrs) {
			if(splitValues != null) { //If this is not a leaf node
				if(!attrs.containsKey(splitAttr)) //if the split attribute of this node has not been added yet
					attrs.put(splitAttr, new ArrayList<ArrayList<String>>()); //add it
				for(String[] splitVal : splitValues) { //for the splitting criteria of this node's two child nodes
					ArrayList<String> splitVals = new ArrayList<String>(Arrays.asList(splitVal));
					for(ArrayList<String> extractedSubfeatures : attrs.get(splitAttr))
						//first check to see if these values are already checked by another derived attribute
						if(extractedSubfeatures.containsAll(splitVals)) 
							extractedSubfeatures.removeAll(splitVals); //if they are, remove them from that check
					//add the values for this split to their own derived attribute check
					attrs.get(splitAttr).add(splitVals);
				}
				for(Tree node : nodes)
					node.getTopLevelAttrVals(attrs); //recurse on child nodes
			}
		}

		public HashSet<Integer> getTopLevelAttributes(int i) {
			HashSet<Integer> attrs = new HashSet<Integer>(Arrays.asList(new Integer[] {0})); 
			if(i > 0) {
				attrs.add(splitAttr + 1);
				for(Tree node : nodes)
					attrs.addAll(node.getTopLevelAttributes(i - 1));
			}
			return attrs;
		}

		void print(int indent, ArrayList<String> attrNames) {
			System.out.print(attrNames.get(splitAttr + 1) + "->");
			for(int i = 0; i < 2; i++) {
				System.out.print("\n" + new String(new char[indent + 1]).replace("\0", "\t") + Arrays.toString(splitValues.get(i)));

				System.out.print(" - ");
				if(nodes.size() > i && nodes.get(i).splitValues != null)
					nodes.get(i).print(indent + 1, attrNames);
				else
					System.out.print(nodes.get(i).getClassName() + " (" + nodes.get(i).data.size() + ")");
			}
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
		
		int establishHierarchy(Analysis chief, ArrayList<ArrayList<String>> allAttributes, ArrayList<NominalInstance> data, ArrayList<String[]> branch) throws IOException{ //chief is the analysis we're checking with
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
					
					double dummy = chief.analyze(this, DDDD); //check the analysis of that split
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