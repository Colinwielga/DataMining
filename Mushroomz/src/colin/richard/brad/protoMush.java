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
	
	//dunno if I should have this
	abstract class Analysis{
		//override some math
		class GiniIndex extends Analysis{}
		class Entropy extends Analysis{}
		class InfoGain extends Analysis{}
		class SplitInfo extends Analysis{}
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
	
	void combinatorics(String [] attr, int n){
		
		   ICombinatoricsVector<String> vector = Factory.createVector(attr);

		   // Create a complex-combination generator
		   Generator<ICombinatoricsVector<String>> gen = new ComplexCombinationGenerator<String>(vector, n, false, true);
		   
		   List<String> a1;
		   // Iterate the combinations
		   for (ICombinatoricsVector<ICombinatoricsVector<String>> comb : gen) {
		      //System.out.println(ComplexCombinationGenerator.convert2String(comb) + " - " + comb);
			  for (ICombinatoricsVector<String> v : comb){
				  a1 = v.getVector();
			  }
		   }
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


//DistanceMetric and its derivatives, pirated from Richard's code
