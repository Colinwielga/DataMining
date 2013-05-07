import java.util.*;
import java.util.Map.Entry;

//this might only work for KNN, needs to be modified for DTI
//this is not anything close to Richard's Records class
//all this does is keep track of the classname and attributes, which is useful
//the Tree class and DTI will perform class assignment and everything else
class NominalInstance {
	String[] attributes;
	String classname;
	static ArrayList<String> allClasses = new ArrayList<String>(); 
	
	String getClassname(){return classname;}
	
	NominalInstance(String line) {
		String[] fields = line.split(",");
		attributes = fields;
		//Globally keep track of class names so we can properly form confusion matrices, and so we know how many graphs to plot
		classname = fields[0];
		
		allClasses.add(classname);
	}	
	
	Record featureExtract(HashMap<Integer, ArrayList<ArrayList<String>>> attrVals) {
		Record r = new Record();
		ArrayList<Double> a = new ArrayList<Double>(); 
		r.classname = classname;
		for(Entry<Integer, ArrayList<ArrayList<String>>> e : attrVals.entrySet())
			for(ArrayList<String> split : e.getValue())
				if(split.contains(attributes[e.getKey() + 1]))
					a.add(1.0);
				else a.add(0.0);

		if(!kNN.classes.containsKey(classname))
			kNN.classes.put(classname, new Class());
		r.attributes = a;
		return r;
	}
}
