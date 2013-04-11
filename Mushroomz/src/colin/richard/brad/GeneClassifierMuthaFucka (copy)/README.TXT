
PROJECT TITLE: Gene Classifier
PURPOSE OF PROJECT: to classify genes based on stuff
VERSION or DATE: started on March 9th, 2013
AUTHORS: Richard Rand, Bradley Baker
USER INSTRUCTIONS:

 Compute FOUR different distance/similarity measures between every test sample and all training samples. 
Use the following measures: Euclidian distance, Chebyshev distance, City block (i.e. Manhattan) distance and 
cosine similarity (NOTE THAT THE LATTER IS A SIMILARITY MEASURE AND NOT A DISTANCE MEASURE).
 For each distance/similarity measure above, find the k nearest neighbors for different values for k (try 3, 5, 
7, 9 and 11).
 For each distance/similarity measure and k value combination, predict the class for each test sample using 
weighted plurality voting. Each test sample will get 20 predictions: 4 measures X 5 k values.
 For each distance/similarity measure and k value combination, create one confusion matrix and compute 
precision, recall and F-1 measure per class label.
 (Include in report) Complete the following tables per class label (you’ll need six tables in total: 3 for ALL and 
3 AML).

import java.util.io;
import java.util.ArrayList;

private ArrayList<ArrayList> trainData; 
//this is the base ArrayList for parsing through training data, 
//it will contain two ArrayLists: 
//one which contains the numeric values for each gene
//one which contains the class values for each gene

private ArrayList<ArrayList> trainGenes; 
//this is the ArrayList containing all of the genes. Each ArrayList will contain the corresponding numeric values and the class identifier at the end.

private ArrayList<ArrayList> testGenes;
//same as above, but for test.

private ArrayList<ArrayList> distances;
//this ArrayList will contain all of the distance arrayLists for each gene in the testing dataSet,
//which, in turn, will contain all of the different distance types for each gene

private ArrayList<ArrayList> thisGenesDistances{}
//I put this above the constructor, even though it technically is a method,
//because it's part of the Arrays within Arrays...
//This arrayList just puts together all of the different Distance Measurements for one gene.

public GeneClassifier(File train, File test){}
//the constructor will take the two data-files as input. 
//It will treat the ARFF files at .txt files, parsing through their information
//and sticking it to the respective Arrays. It doesn't calculate distances yet.

public ArrayList<Double> calcEuclidDistances(int i){}
//i is the index of the gene
//this will calculate the distances between the gene in the test data
//and all of the training data, via a loop. 

public ArrayList<Double> calcChebyDistance(int i){}
//same as above, but for Chebyshev distances

public ArrayList<Double> calcCityBlock(int i){}
//same as above, but for City Block distances

public ArrayList<Double> calcCosineSim(int i){}
//same as above, but for cosine similarity

public ArrayList<Integer> findKNearest(int measureIndex, int i, int k){}
//where int measureIndex is the index of the type of measurement
//where int i is the index of the gene we are testing
//where int k is the K value, for the amount of neighbors we need
//returns the indexes of the nearest genes

public ArrayList<String> predictClasses(int measureIndex, int k){}
//where k is the k-value
//where m is the type of measurement
//returns a list containing the predicted classes for each gene

public ArrayList<String> confusions(int measureIndex, int k, String className){}
//where measureIndex is the index of the type of measurement
//where k is the k value
//this takes the predicted classes array 
//and compares it with the actual classes for all genes
//returns a list of all of the comparisons (TP for truepositive, TN for truenegative, FP for falsepositive, FN for falsenegative)
//for that measure and k-value combination. 

public double calcPrecision(int measureIndex, int k){}
//This calculates the precision measure an m/k value combination

public double calcRecall(int measureIndex, int k){}
//calculates the recall measure for an m/k value combination

public double calcF1(int measureIndex, int k){}
//calculates the F1 measure for an m/k value combination

public void writeConfusions(){}
//this writes the tables we need for all classes



