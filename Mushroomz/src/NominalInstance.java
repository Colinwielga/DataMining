

//this might only work for KNN, needs to be modified for DTI
//this is not anything close to Richard's Records class
//all this does is keep track of the classname and attributes, which is useful
//the Tree class and DTI will perform class assignment and everything else
class NominalInstance {
	String[] attributes;
	String classname;
	
	String getClassname(){return classname;}
	
	NominalInstance(String line) {
		String[] fields = line.split(",");
		attributes = fields;
		//Globally keep track of class names so we can properly form confusion matrices, and so we know how many graphs to plot
		classname = fields[0];
		
	}	
}