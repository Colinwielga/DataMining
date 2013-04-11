import java.io.*;
import java.util.ArrayList;

public class GeneClassifierTestSpace {
    private static File file; 
    private static OutputStream out;
    private static PrintStream printer; 
    private static String fileName;
    private static Exception e = new FileNotFoundException();
    private static ArrayList<Double> trainAttribMins;
    private static ArrayList<Double> trainAttribRanges;
    private static ArrayList<ArrayList> trainGenes;
    private static ArrayList<ArrayList> testGenes;
    //private static ArrayList<Double> testNums; //wit dis 4?
    //private static ArrayList<ArrayList> distances;

    public GeneClassifierTestSpace(File train, File test) throws IOException
    {
        trainGenes = new ArrayList<ArrayList>();
        testGenes = new ArrayList<ArrayList>();
        trainAttribMins = new ArrayList<Double>();
        trainAttribRanges = new ArrayList<Double>();
        BufferedReader inputStream = null;
        String n = "";
        int x = 1;
        String s;
        double d;
        int index = 0;
        int commaIndex = 0;
        
        try {
            while (x > -1) {
                if (x == 1) fileName = train.toString();
                else fileName = test.toString();
                
                inputStream = new BufferedReader(new FileReader(fileName));
                while (inputStream.readLine().indexOf("@data") == -1)
                    /*do nothing*/;
                n = inputStream.readLine();
                while (n != null){
                    ArrayList o = new ArrayList<Object>();
                    
                    while (commaIndex != -1 && n != null)
                    {
                        commaIndex = n.indexOf(",", index);
                        if (commaIndex != -1){                    
                            s = n.substring(index,commaIndex);

                            d = Double.parseDouble(s);

                            o.add(d);
                        }
                        index = commaIndex + 1;
                    }
                    
                    if (n != null) {
                        index = n.lastIndexOf(",");
                        commaIndex = n.length();
                        s = n.substring(index + 1, commaIndex);
                        o.add(s);
                        
                        if (x == 1) trainGenes.add(o);
                        else testGenes.add(o);
                        String testy1 = trainGenes.toString();
                        String testy2 = testGenes.toString();
            
                        index = 0;
                        commaIndex = 0;
                    } 
                    
                    n = inputStream.readLine();
                }
                x -= 1;        
                n = null;           
            }
            
            //System.out.println(trainGenes.toString());
            //System.out.println(testGenes.toString());
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
            return;
        }
        finally {
            if(inputStream != null)
            {
                inputStream.close();
            }
        }   
        
        minMaxNormalizeBoth();
        //System.out.println(testGenes.get(0));
    }
        
    void minMaxNormalizeBoth() {
		int numAttribs = trainGenes.get(0).size() - 1;
		assert(testGenes.get(0).size() - 1 == numAttribs);
		for(int i = 0; i < numAttribs; i++) {
			double min = 10000000, max = 0;
			for(int j = 0; j < trainGenes.size(); j++) {
				double geneval = (double)trainGenes.get(j).get(i);
				if(geneval <= min) min = geneval;
				if(geneval >= max) max = geneval;
			}
			for(int j = 0; j < testGenes.size(); j++) {
				double geneval = (double)testGenes.get(j).get(i);
				if(geneval <= min) min = geneval;
				if(geneval >= max) max = geneval;
			}
			double range = max - min;
			for(int j = 0; j < trainGenes.size(); j++) {
				double geneval = (double)trainGenes.get(j).get(i);
				if(range == 0) trainGenes.get(j).set(i, 0.5);
				else trainGenes.get(j).set(i, (geneval - min)/range);

				assert(((double)trainGenes.get(j).get(i) >= 0.0) && ((double)trainGenes.get(j).get(i) <= 1.0));
			}
			for(int j = 0; j < testGenes.size(); j++) {
				double geneval = (double)testGenes.get(j).get(i);
				if(range == 0) testGenes.get(j).set(i, 0.5);
				else testGenes.get(j).set(i, (geneval - min)/range);

				assert(((double)testGenes.get(j).get(i) >= 0.0) && ((double)testGenes.get(j).get(i) <= 1.0));
			}
		}			
	}
    
    private static ArrayList<ArrayList> thisGenesDistances(int i){
        ArrayList r = new ArrayList<ArrayList>();
        
        r.add(calcEuclidDistances(i));
        r.add(calcChebyDistances(i));
        r.add(calcCityBlocks(i));
        r.add(calcCosineDist(i));
        
        return r;
    }

    public static ArrayList<Double> calcEuclidDistances(int i){
        ArrayList r = new ArrayList<Double>();
        ArrayList q = new ArrayList<Double>();
        ArrayList<Object> thisNums = testGenes.get(i);
        
        for (Object o : thisNums){if ((o instanceof Double)){q.add(o);}}
        for (ArrayList<ArrayList> a : trainGenes)
        {
            double dist = 0;
            double temp;
            ArrayList l = new ArrayList<Double>();
            
            for (Object o : a)
            {
                if (o instanceof Double)
                {
                    l.add(o);
                }
            }
            
            for (int j = 0; j < l.size(); j++)
            {
                temp = Math.abs(((double)q.get(j) - (double)l.get(j))) * Math.abs(((double)q.get(j) - (double)l.get(j)));
                dist += temp;
            }
            r.add(Math.sqrt(dist));
        }
        return r;
    }

    public static ArrayList<Double> calcChebyDistances(int i)
    {
       ArrayList r = new ArrayList<Double>();
        ArrayList q = new ArrayList<Double>();
        ArrayList<Object> thisNums = testGenes.get(i);
        
        for (Object o : thisNums){if ((o instanceof Double)){q.add(o);}}
        for (ArrayList<ArrayList> a : trainGenes)
        {
            double dist = 0;
            double temp;

            ArrayList l = new ArrayList<Double>();
            
            for (Object o : a)
            {
                if (o instanceof Double)
                {
                    l.add(o);
                }
            }
            
            for (int j = 0; j < l.size(); j++)
            {
                temp = 0;
                temp = Math.abs((double)q.get(j) - (double)l.get(j)) + Math.abs((double)q.get(j) - (double)l.get(j));
                dist = Math.max(temp, dist);
            }
            r.add(dist);
        }
        return r;
    }

    public static ArrayList<Double> calcCityBlocks(int i)
    {
        ArrayList r = new ArrayList<Double>();
        ArrayList q = new ArrayList<Double>();
        ArrayList<Object> thisNums = testGenes.get(i);
        
        for (Object o : thisNums){if ((o instanceof Double)){q.add(o);}}

        for (ArrayList<ArrayList> a : trainGenes)
        {
            double dist = 0;
            double temp;
            
            ArrayList l = new ArrayList<Double>();
            
            for (Object o : a)
            {
                if (o instanceof Double)
                {
                    l.add(o);
                }
            }
            
            for (int j = 0; j < l.size(); j++)
            {
                temp = 0;
                temp = Math.abs((double)q.get(j) - (double)l.get(j)) + Math.abs((double)q.get(j) - (double)l.get(j));
                dist += temp;
            }
            r.add(dist);
        }
        return r;
    }

    //it could make sense to make a class for distance measures and subclass for each type, but i don't think we have time
    public  static ArrayList<Double> calcCosineDist(int i)
    {
        ArrayList r = new ArrayList<Double>();
        ArrayList q = new ArrayList<Double>();
        ArrayList<Object> thisNums = testGenes.get(i);
        
        for (Object o : thisNums){if ((o instanceof Double)){q.add((double)o);}}

        for (ArrayList<ArrayList> a : trainGenes)
        {
            ArrayList l = new ArrayList<Double>();
            
            for (Object o : a)
            {
                if (o instanceof Double)
                {
                    l.add(o);
                }
            }
            
            double ql_dot_product = 0, q_magnitude_squared = 0, l_magnitude_squared = 0;
            for (int j = 0; j < l.size(); j++) {
                ql_dot_product += (double)q.get(j) * (double)l.get(j);
                q_magnitude_squared += (double)q.get(j) * (double)q.get(j);
                l_magnitude_squared += (double)l.get(j) * (double)l.get(j);
            }
            double dist = ql_dot_product/(Math.sqrt(q_magnitude_squared)*Math.sqrt(l_magnitude_squared));
            r.add(1-dist);
        }
        return r;
    }

    //Return indices of k-nearest training records. 
    public static ArrayList<Integer> findKNearest(int measureIndex, int i, int k)
    {
        ArrayList<ArrayList> meases = thisGenesDistances(i);
        ArrayList<Double> meas = meases.get(measureIndex);
        ArrayList<Integer> r = new ArrayList<Integer>();
        int index = -1;
        double least;
        for (int j = 0; j < k; j++)
        {
            least = 99999999999999.9;
            for (int trainingRecordIndex = 0; trainingRecordIndex < meas.size(); trainingRecordIndex++) {
                if (meas.get(trainingRecordIndex) <= least //if this is the smallest element seen so far
                 && r.indexOf(trainingRecordIndex) == -1)  //and it has not already been added to the list of k-nearest elements
                {
                    least = meas.get(trainingRecordIndex); //it MIGHT be the next closest record/smallest distance
                    index = trainingRecordIndex;
                }
            }
            //meas.remove(index); //remove here only after we're sure it's the smallest element
                //actually we have to be more clever because if we do this, the ids will all get fucked up
                //really it would be better to make a new class for records with a classname attribute (for AML or ALL)
                //and an arraylist of doubles for each gene
                
                //okay, before this change we had [9, 21, 5, 16, 8, 5, 20, 21, 11, 2]
                //now we have [9, 22, 5, 18, 10, 6, 26, 28, 15, 2]
                //you can see how the indices shifted to the left if they were before a gene that already been found.
    
            r.add(index);
        }
        assert(r.size() == k);
        return r;
    }

    public static ArrayList<String> predictClasses(int measureIndex, int k) {
        ArrayList<String> classes = new ArrayList();
        
        //String[2] possibleClasses = {"ALL", "AML"}; //meh
        
        for(int testRec = 0; testRec < testGenes.size(); testRec++) {
            ArrayList<ArrayList> distances = thisGenesDistances(testRec);
            ArrayList<Integer> knearest = findKNearest(measureIndex, 0, k);
            
//             System.out.println(knearest);
                
            double[] classWeights = new double[] {0, 0}; //0 is ALL, 1 is AML
            for(int trainRecIndex : knearest) {
                assert(trainRecIndex < trainGenes.size());
                String trainRecClassName = (String)trainGenes.get(trainRecIndex).get(trainGenes.get(trainRecIndex).size() - 1); //damn java you so ugly
                assert(trainRecClassName.equals("AML") || trainRecClassName.equals("ALL")); 
                
                if(trainRecClassName.equals("ALL"))
                    classWeights[0] += 1/(double)distances.get(measureIndex).get(trainRecIndex);
                else if(trainRecClassName.equals("AML")) {
                    classWeights[1] += 1/(double)distances.get(measureIndex).get(trainRecIndex);
                } else assert(false); //badbadbadbadbadadadda
            }
            
//             System.out.println(metricnames[measureIndex] + ", k=" + k + ", " + "testRecIndex=" + testRec + " class weights: " + java.util.Arrays.toString(classWeights));
            classes.add(classWeights[0] > classWeights[1] ? "ALL" : "AML");
        }
        return classes;
    }

    public static int[] confusions(int measureIndex, int k)
    {
        int tALL = 0, tAML = 0, fALL = 0, fAML = 0;
        String test1 = null, test2 = null;
        ArrayList<String> actClass = new ArrayList();
        for (ArrayList<Object> a1 : testGenes)
        {
            actClass.add((String)a1.get(a1.size() - 1));
        }
        
        ArrayList<String> predictions = predictClasses(measureIndex,k);
        String test3 = actClass.toString(), test4 = predictions.toString();
        
        for (int j = 0; j < predictions.size(); j++)
        {
            test1 = actClass.get(j);
            test2 = predictions.get(j);
            if (test1.equals(test2))
            {
                if (test2.equals("ALL")){tALL++;}
                else if (test2.equals("AML")){tAML++;}
            }
            else
            {
                if (test2.equals("ALL")){fALL++;}
                else if (test2.equals("AML")){fAML++;}
            }
        }
        
        System.out.println("Confusion matrix for measure " + metricnames[measureIndex] + ". At k value " + k + ".");
        System.out.println("-----ALL----------AML-----");
        System.out.println("-----" + tALL + "----------" + fAML + "-----");
        System.out.println("-----" + fALL + "----------" + tAML + "-----");
        System.out.println("--------------------------");
        int [] r = {tALL, fALL, tAML, fAML};
        return r;
    }

    public double calcPrecision(int measureIndex, int k, int c)
    {
        //c denotes the class choice - 0 for ALL, 1 for AML
        c *= 2;
        int[] r = confusions(measureIndex, k);
        return (double)(r[c]/(r[c] + r[c+1])); 
    }

//     public double calcRecall(int measureIndex, int k, int c)
//     {
//         c *= 2;
//         int[] r = confusions(measureIndex, k);
//         return (double)
//     }
// 
//     public double calcF1(int measureIndex, int k, int c)
//     {
//         
//     }

    static final int num_metrics = 4;
    static final String[] metricnames = {"Euclid", "Chebyshev", "City Blocks", "Cosine Distance"};
        
    public static void main(String [] args) throws IOException
    {
        File tr = new File("ALL_AML_SignificantGenes_NoID.train.arff");
        File te = new File("ALL_AML_SignificantGenes_NoID.test.arff");
        GeneClassifierTestSpace g = new GeneClassifierTestSpace(tr, te);
//         printDistances(thisGenesDistances(0));
        
        for(int measureIndex = 0; measureIndex <= 3; measureIndex++)
            for(int k = 3; k <= 11; k += 2)
//                 System.out.println(metricnames[measureIndex] + ", k=" + k + ": " + predictClasses(measureIndex, k));
                confusions(measureIndex, k);
    }

    public static void printDistances(ArrayList<ArrayList> distances)
    {
        assert(distances.size() == num_metrics);
        for(int i = 0; i < num_metrics; i++)
            System.out.println(metricnames[i] + ": " + distances.get(i).toString());
    }
}