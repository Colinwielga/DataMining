package colin.richard.brad;

import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.paukov.combinatorics.util.*;



//TODO still needs to perform the combinatorics, maybe in the record class
//TODO still needs to check splits and create a tree
//that's pretty much it
public class protoMush{
	public ArrayList<Record> dataSet;
	
	public protoMush(File in){}
	
	public static void main(String [] args){
		//run parseArff/parseAttributes
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
	
	static ArrayList<ArrayList> parseAttributes(String fileName) throws IOException{
		ArrayList<ArrayList> results = new ArrayList<ArrayList>();
		
		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
		//we need the attributes section
		String in = inputStream.readLine();
		while (in.indexOf("@attribute") == -1);
		
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
			toAdd.add(attrName);
			String[] temp = attrVals.split(",");
			for (int i = 0; i < temp.length; i++){
				toAdd.add(temp[i]);
				}
			results.add(toAdd);
		}	
		//results will contain all of the arrayLists for the attributes
		//each attribute's arrayList will contain its name and its values as strings
		return results;
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
		
		//Globally keep track of class names so we can properly form confusion matrices, and so we know how many graphs to plot
		classname = fields[0];
		
	}
	
}

//performs DTI, contains methods which will also prune to later pass to modifiedKNN
class DTI{
	ArrayList<Record> dataSet;
	ArrayList<String[]> attributes;
	ArrayList<String> classes;
	
	//constructor just instantiates variables
	public DTI(ArrayList<Record> data){
		dataSet = data;
		ArrayList<String[]> att = new ArrayList<String[]>();
		ArrayList<String> c = new ArrayList<String>();
		for (Record r : dataSet){
			att.add(r.attributes);
			c.add(r.classname);
		}
		attributes = att;
		classes = c;
	}
	
	void run() throws IOException{
		ArrayList<ArrayList> allAttributes = protoMush.parseAttributes("filename");
		Tree initial = new Tree(allAttributes);
	}
	
	//dunno if I should have this
	abstract class Analysis{
		
		//counts how many times an attribute value occurs
		int count(int index, String val){
			int result = 0;
			for (String[] s : attributes){
				if (s[index].equals(val))
					result++;
			}
			return result;
		}
		
		double analyze(ArrayList<String[]> combo, ArrayList<Record> dataSect){
			//analyzes the combination for this section of the Data
			
			return -1.0;
		}
		
		
		
	}
	//override some math
	class GiniIndex extends Analysis{}
	class Entropy extends Analysis{}
	class InfoGain extends Entropy{}
	class SplitInfo extends Analysis{}
	
	//yeah, establishes hierarchy for Tree class, holy shit this is an ugly method and it doesn't work
	//I guess, it actually would, if one of the members of each split were pure, but I don't think that'll work
	ArrayList<ArrayList> establishHierarchy(Analysis chief, ArrayList<ArrayList> allAttributes, ArrayList<Record> data) throws IOException{ //chief is the analysis we're checking with
		
		allAttributes.remove(0); //we don't want to deal with the class
		ArrayList<Double> analBesties = new ArrayList<Double>(allAttributes.size()); //this will hold the best analysis for each attribute
		ArrayList<ArrayList> besties = new ArrayList<ArrayList>(allAttributes.size()); //this will hold the best split for each attribute
		
		for (ArrayList attr : allAttributes){attr.remove(0);} //we don't need the attribute's name or class
		
		for (ArrayList attr : allAttributes){ //for each attribute in allAttributes
			String [] attrCast = new String[attr.size()]; 
			attr.toArray(attrCast); //put the attribute's values in the array attrCast
			
			ArrayList<ArrayList> poss = possibleCombinations (attrCast, 2); //poss contains all possible splits on the attribute
			
			double bestAnalysis = -666666.666666; //all hail s@an
			ArrayList<String[]> bestSplit = new ArrayList<String[]>();
			
			for (ArrayList p : poss){ //for each possible split on the attribute, we're checking for the best
				double dummy = chief.analyze(p, data); //check the analysis of that split
				if (bestAnalysis == -666666.666666){ //if we're checking for the first time
					bestAnalysis = dummy;
					bestSplit = p;
				}
				else if (chief instanceof GiniIndex && dummy < bestAnalysis){ //if chief is Gini
					bestAnalysis = dummy;
					bestSplit = p;
				}
				else if(chief instanceof Entropy || chief instanceof InfoGain && dummy > bestAnalysis){ //if chief is something else
					bestAnalysis = dummy;
					bestSplit = p;
				}
			}
			
			analBesties.set(allAttributes.indexOf(attr), bestAnalysis); //store the best analysis for this attribute
			besties.set(allAttributes.indexOf(attr), bestSplit); //store the split for that attribute, the index will correspond with that of its analysis
			
		}
		
		ArrayList results = new ArrayList<ArrayList>();
		double best = -666666.666666;
		int indexOfBest = -1;
		//while (analBesties.size() > 0){
			for (double d : analBesties){
				if (best == -666666.666666){ //if we're checking for the first time
					best = d;
					indexOfBest = analBesties.indexOf(d);
					
				}
				else if (chief instanceof GiniIndex && d < best){ //if chief is Gini
					best = d;
					indexOfBest = analBesties.indexOf(d);
				}
				else if(chief instanceof Entropy || chief instanceof InfoGain && d > best){ //if chief is something else
					best = d;
					indexOfBest = analBesties.indexOf(d);
				}
			}
			results.add(besties.get(indexOfBest)); //add the best split
			//analBesties.remove(indexOfBest); //remove that attribute's analysis 
			//besties.remove(indexOfBest); //remove that attribute
		//}
		
		//return results;
			//results is an array of ArrayLists, in order of best analysis
		return besties.get(indexOfBest);
			//this is the best split of those remaining attributes
		
	}
	
	//generates all possible combinations for an array
	ArrayList<ArrayList> possibleCombinations(String [] attr, int n){
		
		   ICombinatoricsVector<String> vector = Factory.createVector(attr);
		   ArrayList<ArrayList> results = new ArrayList<ArrayList>();
		   ArrayList<String[]> results2 = new ArrayList<String[]>();
		   // Create a complex-combination generator
		   Generator<ICombinatoricsVector<String>> gen = new ComplexCombinationGenerator<String>(vector, n, false, true);
		   String[] r;
		   List<String> a1;
		   // Iterate through the different combinations
		   for (ICombinatoricsVector<ICombinatoricsVector<String>> comb : gen) {
			  //Iterate through the individual parts of the combination
			  for (ICombinatoricsVector<String> v : comb){
				  a1 = v.getVector(); //this might be redundant
				  r = new String[a1.size()]; //we're going to put it in an array
				  a1.toArray(r); //here's the array
				  results2.add(r); //woohoo, add that array to the array of arrays
			  }
			  
			  results.add(results2); //add the array of arrays to another array
		   }
		   return results;
		   //so, basically, what this looks like is, an array with many arrays in it
		   //each array in 'results' is an array of String[] arrays
		   //each String[] contains the individual attribute values
	}
	
	//this class does a lot of stuff
	class Tree{
		
		ArrayList<ArrayList> attrs;
		ArrayList<Record> data;
		
		ArrayList<Tree> nodes = new ArrayList<Tree>();
		
		Tree(ArrayList<ArrayList> allAttributes, ArrayList<Record> d){
			attrs = allAttributes;
			data = d;
			
		}
		
		void build(Analysis w) throws IOException{
			
			ArrayList<ArrayList> split = establishHierarchy(w, attrs, data);
			
		}
		
		
		
		
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


//DistanceMetric and its derivatives, pirated from Richard's code
