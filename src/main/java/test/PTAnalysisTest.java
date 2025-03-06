package test;

import PTAnalysis.PointsToAnalysis;
import PTAnalysis.PointsToSet;
import analysis.my_analysis;
import other.EmptyFormatter;
import sootup.core.jimple.basic.Value;
import sootup.core.model.SootMethod;
import sootup.java.core.views.JavaView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

public class PTAnalysisTest extends Test {
    private final String basePath="test files/PTAnalysis";
    private String testClass="A1";
    private String dir="new/";

    private String entryMethodString ="void a(A,int)";

    private final Logger PTAtestLog;
    private final String entryMethodWSpace ="void a (A,int)";
    private static PrintWriter jimpleFile ;

   public PTAnalysisTest(){
        PTAtestLog=  Logger.getLogger("Points To Analysis Test");
        FileHandler fh;
        try {
           // PTAnalysisTest.jimpleFile= new PrintWriter("jimple.test");
            fh = new FileHandler("logs/PTATestLogFile.log");
            PTAtestLog.addHandler(fh);
            EmptyFormatter formatter = new EmptyFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PTAtestLog.setUseParentHandlers(false);
        PTAtestLog.info("Points To Analysis Test Log created");

    }

    public void test(){
       File[] files = new File(basePath).listFiles();
       Arrays.sort(files);
       System.out.println("+PTAnalysis test+");

       for (File directory : files) {
           if (directory.isDirectory()) {
               File[] filesOfDir = new File(directory.getPath()).listFiles();
               Arrays.sort(filesOfDir);
           /*    Arrays.stream(filesOfDir).filter(file->(file.isFile()
                       && file.getName().equals("A.class"))).forEach((x)->print_A_class(directory.getPath()));*/
               Stream<File>testfiles=Arrays.stream(filesOfDir).filter(file->(file.isFile()
                       && file.getName().split("\\.")[1].equals("class")
                       &&!file.getName().equals("A.class")));
                
               testfiles.forEach(file->singleTest(directory.getName() ,file.getName().split("\\.")[0]));
           }
           else {
              ;
           }
       }
       //PTAnalysisTest.jimpleFile.close();

}

    private void singleTest(String parentDir, String testClassName){
        String filepath=basePath+"/"+parentDir+"/"+testClassName+".out";
        Map<String, Set<Integer>> expectedResults = parseTestFile(filepath,testClassName);
        JavaView pathView =my_analysis.getViewFromPath(basePath+"/"+parentDir);
        SootMethod entryMethod=my_analysis.getMethodFromView(pathView,testClassName,entryMethodWSpace);  //TODO fix the space thing
        //
       //  PTAnalysisTest.jimpleFile.println(testClassName.split("\\.")[0]);
       // PTAnalysisTest.jimpleFile.println(entryMethod.getBody());
      // print_to_outfile(parentDir,testClassName);
       // System.out.println(testClassName);
        ///*
        Map<String, Set<Integer>> analysisResults = new PointsToAnalysis(pathView).analise(entryMethod);
        PTAtestLog.info(testClassName+".class ");
        PTAtestLog.info("Expected results :");
        expectedResults.forEach((name,intset)->PTAtestLog.info(name+"="+intset));
        PTAtestLog.info("");
        PTAtestLog.info("Actual results :");
        removeUseMethod(analysisResults);
        for (var entry : analysisResults.entrySet())
            PTAtestLog.info(entry.getKey() + "=" + entry.getValue());
        PTAtestLog.info("");

        if (expectedResults.equals(analysisResults)) pass(testClassName);
        else fail(testClassName);
        //*/

   }

    public Map<String, Set<Integer>> parseTestFile(String filepath,String testclassName) {
        Map<String,Set<Integer>> res= new HashMap<>();

        //String varNameWSignature= "[$a-zA-Z0-9()<>.,:][$a-zA-Z0-9()<>.,:\\s]*[$a-zA-Z0-9()<>.,:]";
        File expectedResultFile = new File(filepath);
        try {
            Scanner lineScanner= new Scanner(expectedResultFile);
            while (lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                if(line.isEmpty()||line.charAt(0)=='%') continue;    //skip empty lines or commented lines

                Scanner expectedResultsScanner= new Scanner(line);
                expectedResultsScanner.useDelimiter("\\s*,\\s*");
                //read variable name
                String var= parseLocationHolder(expectedResultsScanner,testclassName);
                //read '='
                if (expectedResultsScanner.findInLine("=")==null) throw new InputMismatchException();
                //read '{'
                if ( expectedResultsScanner.findInLine("\\{|\\[")==null)throw new InputMismatchException();
                //read ints
                Set<Integer> memLocations = new HashSet<>();
                while(expectedResultsScanner.hasNextInt())
                    memLocations.add(expectedResultsScanner.nextInt());
                String lastIntString=expectedResultsScanner.findInLine("\\d|}|]");
                if(lastIntString.equals("}")||lastIntString.equals("]")){
                    res.put(var ,memLocations);
                    continue;
                }
                int lastInt = Integer.parseInt(lastIntString);
                memLocations.add(lastInt);
                //read '}'
                if (expectedResultsScanner.findInLine("}|]")==null)throw new InputMismatchException();

                res.put(var ,memLocations);
                expectedResultsScanner.close();
            }
            lineScanner.close();
        } catch (Exception e) {
            if (e instanceof  FileNotFoundException)
                System.err.println("!Error! test file "+filepath+" not found");
            else if (e instanceof InputMismatchException || e instanceof NumberFormatException)
                System.err.println("!Error! test file" + filepath + " has invalid syntax");
            else
                System.err.println(e);
        }
        return res;
    }

    private String parseLocationHolder(Scanner s, String testClassName){
        String varName= "(\\$?[a-zA-Z][a-zA-Z0-9_#]*)";
        String paramsAndRetlocations="\\d";
        String fieldsOfMemLocations="\\d\\.<.*>";
        String signature="<.*>[:.]";
        String defaultVarprefix = "<"+testClassName+": "+entryMethodString+">:";
        String possibleFieldOfMemLocation = s.findInLine(fieldsOfMemLocations);
        if(possibleFieldOfMemLocation!=null) return possibleFieldOfMemLocation;
        String possibleSignature = s.findInLine(signature);
        String var =s.findInLine(varName+"|"+paramsAndRetlocations+"|<.*>");        //append w/ method
        if(var==null) throw new InputMismatchException();

        if(possibleSignature==null)
            return isMethodSignature(var)? var :defaultVarprefix+var;
        return possibleSignature+var;

    }

    private boolean isMethodSignature(String s){
       return s.charAt(0)=='<'&& s.charAt(s.length()-1)=='>';
    }
    private void  print_A_class(String filepath){
        System.out.println(filepath);
        JavaView pathView =my_analysis.getViewFromPath(filepath);
        boolean firstMethod=true;
        String methods[]={"A m (A,A)","A m1 (A,A)","A m2 (A,A)","void m (A,A)"
                        ,"void m1 (A,A)","void m2 (A,A)","int m (A,A,int)"};

        for(String sig : methods){
            SootMethod m=my_analysis.getMethodFromView(pathView,"A",sig);
            if(m==null) continue;
            if(firstMethod){
                firstMethod=false;
                PTAnalysisTest.jimpleFile.println("A.class");
            }
            PTAnalysisTest.jimpleFile.println(sig+" :");
            PTAnalysisTest.jimpleFile.println(m.getBody());

        }
        return;
    }
    private void print_to_outfile(String filepath, String testClass){
       try {
           PrintWriter outfile = new PrintWriter(basePath +"/"+filepath+"/"+testClass+".out");
      /*
        <New1: void a(A,int)>.this=[]
        <A: void <init>()>.this=[]
        <A: void <init>()>:this=[]
        <New1: void a(A,int)>.1=[]
        <New1: void a(A,int)>:l1=[]
        <New1: void a(A,int)>:this=[]
        */
           outfile.println("");
           outfile.println("");
           outfile.println("<"+testClass+": void a(A,int)>.this=[]");
           outfile.println("<A: void <init>()>.this=[]");
           outfile.println("<A: void <init>()>:this=[]");
           outfile.println("<"+testClass+": void a(A,int)>.1=[]");
           outfile.println("<"+testClass+": void a(A,int)>:l1=[]");
           outfile.println("<"+testClass+": void a(A,int)>:this=[]");

           outfile.close();
       }
       catch (FileNotFoundException e){
           System.err.println("file "+filepath+" not found");
       }
   }

    /**
     * the testfiles define a use method to prevent variables splitting in jimple
     * this method is empty and does nothing, we will not consider it for simplicity
     * @param results
     */
   private void removeUseMethod(Map<String, Set<Integer>> results){
       Map<String, Set<Integer>> resultsCopy = new HashMap<>(results);
       for (var entry : resultsCopy.entrySet())
           if(entry.getKey().contains("use"))
               results.remove(entry.getKey(),entry.getValue());
   }

}
