package colin.richard.brad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;

import javax.xml.ws.Holder;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;

/**
 * 
 * @author colin
 *
 */

public class Knn {
		private static final int EUCLIDIAN = 0;
		private static final int CITYBLOCK = 1;
		private static final int CHEBYSHEV = 2;
		private static final int COSINE = 3;

		/**
		 * @param args
		 * @throws IOException 
		 */
		public static void main(String[] args) throws IOException {
			 InputStreamReader istream = new InputStreamReader(System.in) ;
			 BufferedReader bufRead = new BufferedReader(istream) ;
			 System.out.println("files should be stored in the same folder as this java project, where ALL_AML_SignificantGenes.train.arff is");
			 System.out.println("files should have the class in the last collum");
			 System.out.println("if there is an id attribute it should be labelled ID");
			 System.out.println();
			 System.out.println("if you enter 'a' for both file names it will run ALL_AML_SignificantGenes");
			 System.out.println("if you enter 'b' for both file names it will run ALL_AML_allgenes");
			 System.out.println();
			 System.out.println("please enter the name of the training file");
			 String trainingfile = bufRead.readLine();
			 System.out.println("please enter the name of the testing file");
			 String testingfile = bufRead.readLine();
			 if (trainingfile.equals("a")){
				 trainingfile =  "ALL_AML_SignificantGenes.train.arff";
			 }else if (trainingfile.equals("b")){
				 trainingfile =  "ALL_AML_allgenes.train.arff";
			 }
			 
			 if (testingfile.equals("a")){
				 testingfile =  "ALL_AML_SignificantGenes.test.arff";
			 }else if (testingfile.equals("b")){
				 testingfile =  "ALL_AML_allgenes.test.arff";
			 }
			 //"ALL_AML_SignificantGenes.train.arff"
			 BufferedReader reader = new BufferedReader(new FileReader(trainingfile));
			 ArffReader arff = new ArffReader(reader);
			 Instances train = arff.getData();
			 train.setClassIndex(train.numAttributes() - 1);
			 //"ALL_AML_SignificantGenes.test.arff"
			 reader = new BufferedReader(new FileReader(testingfile));
			 arff = new ArffReader(reader);
			 Instances test = arff.getData();
			 test.setClassIndex(test.numAttributes() - 1);
			 
			 
			// System.out.println(test.size());
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
			int result =0;
			while (ratio/(2.0) > 1){
				ratio = ratio/(2.0);
				result++;
			}
			return result;
		}

		private static void printData(Instances data) {
			 for (Instance i : data){
				 for (int j=0;j<i.numAttributes();j++){
					 System.out.print(i.value(j) +",");
				 }
				 System.out.println();
			 }
			
		}
		
		
	}

}
