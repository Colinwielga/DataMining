package colin.richard.brad;

//title: DataTrainer
//purpose: to calculate and print analyses for a specifically formatted piece of data
//datecreated: February 27th, 2012
//datelastmodified: March 5th, 2012
//author: Brad Baker 
// other comments denote the purpose of lines of code I though necessary to clarify

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


//This DataTrainer takes Data which is formatted with one Numeric column and one Class column (the Class column having two possible values)
//it instructs the user how to use the program through the console
public class DataTrainer{ 
    private static ArrayList<ArrayList> cols; //the initial arraylist, which contains the ArrayLists containing the data
    private static ArrayList<Double> nums; //this ArrayList contains the numeric data
    private static ArrayList classes; //this ArrayList contains the class data
    private static ArrayList analyses; //contains the three ArrayLists which contain the Analyses for each split
    private static ArrayList Ginis; //contains the Gini analyses
    private static ArrayList InfoGains; //contains the Entropy and InfoGain analyses
    private static ArrayList GainRatios; //contains the GainRatio analyses
                                
    private static File file; 
    private static OutputStream out;
    private static PrintStream printer; 
    private static String fileName;
    private static Exception e = new FileNotFoundException();
    
    private static long sRT;    //runtime for the sorting algorithm
    private static long aRT;    //rumtime for calculating and displaying the analyses
    private static double used; //percent of virtual memory used
    
    //helps with the math involving logarithms. Place before the instructor because it's a facilitating tool, and not necessarily part of the working program. 
    private static double logBase2 (double x){
        if (x == 0){return 0;}
        else{
        return -Math.log(1/x)/Math.log(2);}
    }
    
    //the constructor takes an IO File input, watches for the IOException, parses through the file, instantiates the ArrayLists,
    public DataTrainer(File file1) throws IOException{
       cols = new ArrayList<ArrayList>();
       nums = new ArrayList<Double>();
       classes = new ArrayList<Integer>();
       analyses = new ArrayList<ArrayList>();
       Ginis = new ArrayList<Double>();
       InfoGains = new ArrayList<Double>();
       GainRatios = new ArrayList<Double>();
       
       cols.add(nums);
       cols.add(classes);
       analyses.add(Ginis);
       analyses.add(InfoGains);
       analyses.add(GainRatios);
       
       BufferedReader inputStream = null;
       out = new FileOutputStream(fileName + ".txt");
       printer = new PrintStream(out);
        
        try {
            inputStream = new BufferedReader(new FileReader(file1.toString()));
           
            String n = inputStream.readLine();
            
            //this loop sticks the decimal values to a double array, nums, and the second attribute to an int array, which will denote +, -, or not
            while (n != null && n.trim().length()>0) {
                String s = n.substring(0,n.indexOf(","));
                double d = Double.parseDouble(s);
                String s2 = n.substring(n.lastIndexOf(",") + 1, n.length());
                int i;
                if (s2.equals("+") == true){
                    i = 1;
                }
                else if (s2.equals("-") == true){
                    i = -1;
                }
                else {
                    i = 0;
                }
                nums.add(d);
                classes.add(i);
                //The below console commands were used for initial testing. All they do is print out the record and class associations. Which is useful.
                //printer.print(d);
                //printer.print(" --- ");
                //printer.print(i);
                //printer.println();
                n = inputStream.readLine();
            }
            //printer.println(cols.toString());
        }
        catch (FileNotFoundException e){
            System.out.println();
            System.out.println("Error. This file does not exist. Be careful to choose a file within the correct folder structure.");
            file = null;
            return;
        }
         finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        
        //at this point, we are done with the constructor. We have parsed through the text file and stuck it to the Array
    }
        
    // this sort method, and its corresponding doItQuick[a modified quicksort] and flips methods sort through the Numeric ArrayList while simultaneously sorting the Classes ArrayList 
   public static void sort(ArrayList<ArrayList> b) {      
       if (b == null || b.size() == 0){
           return;
        }
       doItQuick(b, 0, b.get(0).size() - 1);  
   }        
   //The following methods are indented, to signify their interdependence
   
       //this is a standard quicksort algorithm with a runtime of Onlogn
       //I chose this algorithm because I don't know what size of data I'll be dealing with, and quicksort works well with large datasets
       //the only serious modification is the exchange - called flip(), which facilitates an exchange of elements within both record and class arrays
       private static void doItQuick(ArrayList<ArrayList> d,int l, int h){
           ArrayList<Double> dub = d.get(0);
           ArrayList<Integer> ints = d.get(1);
           double pivot;
           try{pivot = dub.get((l + (h-l)/2));} catch(Exception e) {System.out.print("This file is empty."); return;}
           int low = l;
           int high = h;
           while (l <= h){
               while (dub.get(l) < pivot){
                   l++;
               }
               while (dub.get(h) > pivot){
                   h--;
               }
               if (l <= h){
                   flip(dub,ints,l,h);
                   l++;
                   h--;
                }
            }
           if (low < h){
               doItQuick(d,low, h);
           }
           if (l < high){
               doItQuick(d,l,high);
            }
       }
       
       //the flip method. It takes the Numeric and Class ArrayLists as it arguments, as well as indexes of the elements to be flipped
       private static void flip(ArrayList<Double> dub, ArrayList<Integer> ints, int l, int h){
           double set1 = dub.get(l);
           int set10 = ints.get(l);
           double set2 = dub.get(h);
           int set20 = ints.get(h);
           dub.set(l, set2);
           ints.set(l,set20);
           dub.set(h, set1);
           ints.set(h,set10);
        }    
    //end sorting methods
    
    //this creates a splits ArrayList, which is an analog to the split-table we created in class. It conveniently contains possible split points in the numeric records. 
    //it takes the Numeric ArrayList as its argument
    public static ArrayList splits(ArrayList<Double> d) {
        if (d.size() == 0) return null;
        ArrayList s = new ArrayList<Double>();
        s.add(d.get(0) - 5); //5 might seem a little much, but it doesn't really matter.
        for (int i = 0; i < d.size() - 1; i++){
            double toAdd = d.get(i) + (d.get(i + 1) - d.get(i))/2;
            if (s.indexOf(d.get(i) + (d.get(i + 1) - d.get(i))/2) == -1){
                if (d.get(i) + ((d.get(i + 1) - d.get(i))/2) != d.get(i)){
            s.add(d.get(i) + (d.get(i + 1) - d.get(i))/2);}
        }
    }
    s.add(d.get(d.size() - 1) + 5);
    return s;   
   }
    
   //this method obtains the GiniInfo for a given split. It takes as its arguments: the splits ArrayList, the Numeric ArrayList, the Classes ArrayList, and the index of the split.
    // It outputs an ArrayList with the GiniInformation, which is formatted in the following fashion:
    // Index 0 contains the GINI index on the side <= the split pivot.
    // Index 1 contains the GINI index on the side > the split pivot.
    // Index 2 contains the GINI split for the given split.
    // This information was contained all in one methods because of issues with the range of local variables while calculating sums at a given split. 
    // I'll walk through this method with the documentation. This is partially as an exercise for myself to make sure everything is necessary,
    // and partially for the reader, who may encounter some trouble understanding certain parts.
    // The arguments are (in order of appearance) : the splits array, the Numeric Array, the Classes Array, the index of the split.
    public static ArrayList getGiniInfo(ArrayList<Double> a, ArrayList<Double> dubs, ArrayList<Integer> ints, int i) {
       ArrayList oh = new ArrayList<Double>();                      
       int place = 0;                                               
       double count1 = 0.0;                                         //these variables count the records corresponding to each class
       double count2 = 0.0;                                         //they are used in the math
       double save1;                                                //these variables keep track of the sum of the counting variables,
       double save2;                                                //which is necessary, because the counting variables are reset
       
       for (double de1 : dubs){                                    //parses through the Numeric array
           if (de1 <= a.get(i) && ints.get(place) == -1){            //if the value falls into the <= side of the split and corresponds to the '-' class
                count1++;                                           //iterate count1
           }
           else if (de1 <= a.get(i) && ints.get(place) == 1){       //if the value falls into the <= side of teh split and corresponds to the '+' class
               count2++;                                            //iterate count2
            }
           place++;
       }
       if (count1 + count2 != 0){                                    //that is, if the <= side of the split isn't empty
                                                                    //do some GINI math. Stick that Math to the dummy ArrayList.
          oh.add(1.0 - (((count1/(count1+count2)) * (count1/(count1+count2))) + ((count2/(count1+count2)) * (count2/(count1+count2)))));      
       }
       else{                                                        //if the <= side of the array is empty
           oh.add(1.0);                                             //the GINI index for that side will be 1.0
        }

       save1 = count1 + count2;                                    //saves the number of values recorded <= the split. This will be useful for the weighted measurements.
       
       count1 = 0.0;                                               //reset count1 and count2
       count2 = 0.0;
       place = 0;
       for (double de1 : dubs){                                     //the following steps are identical to what is above, just for the side > the split. 
           if (de1 > a.get(i) && ints.get(place) == -1.0){
           
                count1++;    
           }
           else if (de1 > a.get(i) && ints.get(place) == 1.0){
               count2++;
            }
           place++;
       }
       if (count1 + count2 != 0){
           oh.add(1.0 - (((count1/(count1+count2)) * (count1/(count1+count2))) + ((count2/(count1+count2)) * (count2/(count1+count2))))); 
       }
       else{
          oh.add(1.0);
        }
       save2 = count1 + count2;
                                                                  //the following steps get the GINISplit and add it to our ArrayList
       double d1 = (double)oh.get(0);                             //gets the indices. Downcasting is necessary.
       double d2 = (double)oh.get(1);                             //below is math for the GINISplit
       double splitI = ((save1/dubs.size()) * (d1)) + ((save2/dubs.size()) * (d2)); 
       oh.add(splitI);                                            //stick it to the array
       return oh;                                                 //done deal
   }      
        
   //the below method is essentially the same in format as the getGiniInfo method.
   //the only major differences are in the Math
   //and in the outputs which are formatted as follows:
   //Index 0 is the Entropy for <= the split
   //Index 1 is the Entropy for > the split
   //Index 2 is the InfoGain for the split
   //Index 3 is the GainRatio for the split
   public static ArrayList getEntropyInfo(ArrayList<Double> a, ArrayList<Double> dubs, ArrayList<Integer> ints, int i) {
       ArrayList oh = new ArrayList<Double>();
       int place = 0;
       double count1 = 0.0;
       double count2 = 0.0;
       double save1;
       double save2;
       
       for (double de1 : dubs){
           if (de1 <= a.get(i) && ints.get(place) == -1){
                count1++;    
           }
           else if (de1 <= a.get(i) && ints.get(place) == 1){
               count2++;
            }
           place++;
       }
       if (count1 + count2 != 0){ 
           oh.add( -( ( count1/(count1+count2) * logBase2(count1/(count1+count2)) ) + ( count2/(count1+count2) * logBase2(count2/(count1+count2)) ))); 
       }
       else{
           oh.add(0.0);
       }
       save1 = count1 + count2;
       
       count1 = 0.0;
       count2 = 0.0;
       place = 0;
       for (double de1 : dubs){
           if (de1 > a.get(i) && ints.get(place) == -1){
                count1++;    
           }
           else if (de1 > a.get(i) && ints.get(place) == 1){
               count2++;
            }
           place++;
       }
       if (count1 + count2 != 0){  
         oh.add( -( ( count1/(count1+count2) * logBase2(count1/(count1+count2)) ) + ( count2/(count1+count2) * logBase2(count2/(count1+count2)) ))); 
        }
        else{
            oh.add(0.0);
        }
       save2 = count1 + count2;
       double d1 = (double)oh.get(0);
       double d2 = (double)oh.get(1);
       count1 = 0.0;
       count2 = 0.0;
       
       for (int j : ints){
           if (j == -1){
               count1++;
            }
           else if (j == 1){
               count2++;
            }
        }
       
       //below, parent entropy, infogain, splitINFO, and Gainratio are calculated 
       double pEnt = - (( count1/ints.size() * logBase2(count1/ints.size())) + ( count2/ints.size() * logBase2(count2/ints.size())));
       double infoG = pEnt - (((save1/dubs.size()) * (d1)) + ((save2/dubs.size()) * (d2)));
       oh.add(infoG);
     
       double gR;
       double splitINFO = -( (save1/dubs.size() * logBase2(save1/dubs.size())) + (save2/dubs.size() * logBase2(save2/dubs.size())) );

       if (splitINFO != 0){
            gR = infoG/splitINFO;}
       else
       {
           gR = 0.0;
        }
       oh.add(gR);
       return oh;
   }  
   
   //This method writes the analyses for all possible splits to a file, and then sticks them to the analyses ArrayList    
   public static void printAnalysis() 
   {
       if(splits(nums) == null) return;
       ArrayList dubs = nums;
       ArrayList ints = classes;
       ArrayList s = splits(dubs);
       ArrayList g;
       ArrayList e;
       
       for (int j = 0; j < s.size(); j++){
           g = getGiniInfo(s,dubs,ints,j);
           e = getEntropyInfo(s,dubs,ints,j);
           printer.println("");
           printer.println("For the split at " + s.get(j) + " the following analyses apply:");
           printer.println("GINIIndices: " + g.get(0) + " for <= " + s.get(j) + " / " + g.get(1) + " for > " + s.get(j) + ".");
           printer.println("GINISplit: " + g.get(2));
           Ginis.add(g.get(2));
           printer.println("Entropy: " + e.get(0) + " for <= " + s.get(j) + " / " + e.get(1) + " for > " + s.get(j) + ".");
           printer.println("InfoGain: " + e.get(2) +".");
           InfoGains.add(e.get(2));
           printer.println("GainRatio: " +e.get(3) + ".");
           GainRatios.add(e.get(3));
       }
   }
   
   //This method gets and writes the best Analyses to the output File
   public static void getBesties(){
       ArrayList s = splits(nums);
       if (s == null){ printer.print("You tried to analyze an empty or incorrectly formatted file");return;}
       double bestGini = 2.0;                //these variables are the values of the Analyses for the best Splits 
       double bGini = -1;                    //these variables, with shorter names, are the best splits
       double bestInfoGain = -1.0;
       double bIG = -1;
       double bestGR = -1.0;
       double bGR = -1;
       double d;                            //changing variable
       
       for (int i = 0; i < Ginis.size(); i++) //I couldn't just do a for-each loop for some reason. 
       {
           d = (Double)Ginis.get(i);
           if (bestGini > d)                 //looks for the lowest GINI value
           {
               bestGini = d;                 //if found, makes it the best
               bGini = (Double)s.get(Ginis.indexOf(d));
           }
       }                                     //essentially the same for other values
       for (int i = 0; i < InfoGains.size(); i++){
           d = (Double)InfoGains.get(i);
           if (bestInfoGain < d){
               bestInfoGain = d;
               bIG =  (Double)s.get(InfoGains.indexOf(d));
           }
       }
       for (int i = 0; i < GainRatios.size(); i++){
           d = (Double)GainRatios.get(i);
           if (bestGR < d){
               bestGR = d;
               bGR = (Double)s.get(GainRatios.indexOf(d));
           }
       }
       printer.println("");
       printer.println("Best analyses");
       printer.println("");
       printer.println("Best Gini: "  + bestGini + ", at " + bGini + ".");
       printer.println("Best InfoGain: " + bestInfoGain + ", at " + bIG + ".");
       printer.println("Best GainRatio: " + bestGR + ", at " + bGR + ".");
       
       return;
    }
   
   //this method just sets the output filename, setting a default name if no name or a name with a special character is aded
   public static void setFileName(String s)
   {
              if (s != null && s.equals("") == false && s.indexOf('/') == -1 && s.indexOf('&') == -1 && s.indexOf('*') == -1 && s.indexOf(';') == -1 && s.indexOf(':') == -1
        && s.indexOf('>') == -1 && s.indexOf('<') == -1 && s.indexOf('!') == -1 && s.indexOf('?') == -1 && s.indexOf('?') == -1 && s.indexOf(' ') == -1)
            {
               fileName = s;
            }
       else {
           fileName = "TrainingResults";
        }
    }
    
   //executes the program
   public static void main(String [] args) throws IOException
   {
       Console conch = new Console(); //our GUI
       System.out.println(); 
       System.out.println("**********Welcome to the DataTrainer Analyses***********");
       System.out.println();
       System.out.println("The field for interfacing with this console is located above this text display window");
       System.out.println();
       System.out.println("To submit a statement to the field, enter '@' at the end of your entry");
       System.out.println("Make sure not to overwrite files by naming them the same");
       System.out.println("When choosing an input, please include the .txt suffix; otherwise, it is not necessary.");
       System.out.println("--------------------------------------------------------------------------------------");
       
       String looking = null; //this is a loop control variable wihch will control the loop which will allow the user to Train from multiple files within one session, if they wish
       while (looking == null || looking.equals("Y") == true)  //loops while user wishes to continue using the program
       {
           file = null;
           conch.resetField(); //a GUI command I wrote for clearing the field before entry is requesting
           System.out.print("Please copy the data file from which you wish to train into the DataTraining folder.");
           System.out.println();
           System.out.print("Please enter the name of the training file: ");

           while (conch.getInField().indexOf("@") == -1 || conch.getInField().indexOf(".txt") == -1 ) //this loops just control the field input, because I didn't want to deal with too many threads... they wait for an @ at the end
           {}
           file = new File(conch.getInField().substring(1,conch.getInField().length() - 1));
           System.out.print(conch.getInField().substring(1,conch.getInField().length() - 1));
            System.out.println();
            System.out.println("------------------------------------------------------------");
           conch.resetField();
           System.out.println("Please enter a name for the output file (you do not need to include the .txt suffix).");
           System.out.println("The default output is TrainingResults.txt. Enter '@' for default");
           System.out.print("Filename : ");

            while (conch.getInField().indexOf("@") == -1)
            {} 
           fileName = conch.getInField().substring(1,conch.getInField().length() - 1); //this substring, also used above, ignores the ':' and '@' in the field boundaries
           setFileName(fileName);
           System.out.print(fileName + ".txt");
           System.out.print("---------------------------------------------------------------");
           DataTrainer t = new DataTrainer(file);     //instantiate constructor, FINALLY!
           if (file != null && cols.size() != 0) {                   //avoids any issues with files not appearing in the correct folder structure or being empty
           
           System.out.println();
           System.out.println("Your results will be written to '" + fileName + ".txt'");
           System.out.println("");
           System.out.println("Thank you");
           System.out.println("------------------------------------------------------------");
           sRT = System.currentTimeMillis();
           sort(cols);                          //sort the Numeric records and Classes
           sRT = System.currentTimeMillis() - sRT;
           ArrayList s = splits(nums);          //find the possible splits
           
           printer.println("");
           printer.println("Here are the analyses for your file '" + file.toString() + "'.");
           aRT = System.currentTimeMillis();
           printAnalysis();                     //writes the analyses to a File
           getBesties();                        //writes the best analyses to a File
           aRT = System.currentTimeMillis();
           System.out.println("Your data was sorted in " + sRT + " milliseconds.");
           System.out.println("Your data was analyzed and printed in " + aRT + " milliseconds.");
           printer.close();                     //we don't need the printer anymore                                    
           conch.resetField();
           file = new File(fileName + ".txt");  
           Desktop.getDesktop().edit(file);     //opens up the Results file for the user
           System.out.println();
           looking = "";        
           System.out.println("Would you like to train with another file? [Y,N]");
           while (looking.equals("Y") == false && looking.equals("N") == false) //while the user isn't complying with the inputs we're looking for
           {    
               while (conch.getInField().indexOf("@") == -1 || conch.getInField() == null)
               {}
               looking = conch.getInField().substring(1,conch.getInField().length() - 1);
            }
           if (looking.equals("N") == true) //if the user user elects to stop the program
           {
               break;                       //end the loop
            }
         }
       }   
       System.out.println();
       System.out.println("Goodbye");
       System.out.println();
       conch.bye();
    }
   //end       
}

//below is the code for the GUI
//it is a console adapted from this open-source code: http://www.comweb.nl/java/Console/Console.html
//I modified it to contain a field for input and some minor interfacing methods
class Console extends WindowAdapter implements WindowListener,  ActionListener, Runnable
 {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField; //this is my textField
    private Thread reader;
    private Thread reader2;
    private ImageIcon image;
    private JLabel imageLabel;
    private boolean quit;

    private final PipedInputStream pin=new PipedInputStream();
    private final PipedInputStream pin2=new PipedInputStream();

    Thread errorThrower; // just for testing (Throws an Exception at this Console

    public Console()
    {
        // create all components and add them
        frame=new JFrame("DataTrainer Console");
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize=new Dimension((int)(screenSize.width/2),(int)(screenSize.height/2));
        int x=(int)(frameSize.width/2) + 100;
        int y=(int)(frameSize.height/2) + 100;
        frame.setBounds(x,y,frameSize.width+100,frameSize.height+100);

        textArea=new JTextArea();
        
        textArea.setEditable(false);
        JButton button=new JButton("clear");
        textField = new JTextField();
        textField.setText(":"); //keep a colon in the field - it looks cool
        image = createImageIcon("nick.png","NICK"); //this is mine too...
        imageLabel = new JLabel(image);
        
        
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
        frame.getContentPane().add(new JScrollPane(textField),BorderLayout.NORTH);
        frame.getContentPane().add(button,BorderLayout.SOUTH);
        frame.getContentPane().add(imageLabel,BorderLayout.EAST);
        frame.setVisible(true);

        frame.addWindowListener(this);
        button.addActionListener(this);

        try
        {
            PipedOutputStream pout=new PipedOutputStream(this.pin);
            System.setOut(new PrintStream(pout,true));
        }
        catch (java.io.IOException io)
        {
            textArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
        }
        catch (SecurityException se)
        {
            textArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
            }

        try
        {
            PipedOutputStream pout2=new PipedOutputStream(this.pin2);
            System.setErr(new PrintStream(pout2,true));
        }
        catch (java.io.IOException io)
        {
            textArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
    }
        catch (SecurityException se)
        {
            textArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
        }

        quit=false; // signals the Threads that they should exit

        // Starting two separate threads to read from the PipedInputStreams
        //
        reader=new Thread(this);
        reader.setDaemon(true);
        reader.start();
        //
        reader2=new Thread(this);
        reader2.setDaemon(true);
        reader2.start();
    }
    public String getInField()
    {
        return textField.getText();
       }
    public synchronized void windowClosed(WindowEvent evt)
    {
        quit=true;
        this.notifyAll(); // stop all threads
        try { reader.join(1000);pin.close();   } catch (Exception e){}
        try { reader2.join(1000);pin2.close(); } catch (Exception e){}
        System.exit(0);
    }
    public synchronized void windowClosing(WindowEvent evt)
    {
        frame.setVisible(false); // default behaviour of JFrame
        frame.dispose();
    }

    public synchronized void actionPerformed(ActionEvent evt)
    {
        textArea.setText("");
    }
    
    public void resetField()
    {
        textField.setText(":");
        textField.setCaretPosition(1);
     }

    public synchronized void run()
    {
        try
        {
            while (Thread.currentThread()==reader)
            {
                try { this.wait(100);}catch(InterruptedException ie) {}
                if (pin.available()!=0)
                {
                    String input=this.readLine(pin);
                    textArea.append(input);
                }
                if (quit) return;
            }

            while (Thread.currentThread()==reader2)
            {
                try { this.wait(100);}catch(InterruptedException ie) {}
                if (pin2.available()!=0)
                {
                    String input=this.readLine(pin2);
                    textArea.append(input);
                }
                if (quit) return;
            }
        } catch (Exception e)
        {
            textArea.append("\nConsole reports an Internal error.");
            textArea.append("The error is: "+e);
        }
    }
    
    public void bye() //method for forcing a shutdown of the GUI
    {
        frame.setVisible(false); // default behaviour of JFrame
        frame.dispose();
       }
    
    protected ImageIcon createImageIcon(String path, //I added this as well
                                           String description) {
    java.net.URL imgURL = getClass().getResource(path);
    if (imgURL != null) {
        return new ImageIcon(imgURL, description);
    } else {
        System.err.println("Couldn't find file: " + path);
        return null;
    }
   }   
       
    public synchronized String readLine(PipedInputStream in) throws IOException
    {
        String input="";
        do
        {
            int available=in.available();
            if (available==0) break;
            byte b[]=new byte[available];
            in.read(b);
            input=input+new String(b,0,b.length);
        }while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
        return input;
    }
    
}
//end GUI code