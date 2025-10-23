package test;

import PTAnalysis.PointsToAnalysis;
//import analysis.my_analysis;
import PTAnalysis.PointsToSet;
import util.EmptyFormatter;
import sootup.core.model.SootMethod;
import sootup.java.core.views.JavaView;
import util.LoggerFactory;
import util.SootUpStuff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Test files for this class need be in such a format :
 *  locationHolder={X,Y,Z}      where X,Y,Z are integers
 *
 */
public class PTAnalysisTest extends Test {
    private final String entryMethodWSpace ="void a (A,int)";
    private static PrintWriter jimpleFile ;

    public static void main(String[] args){
        PTAnalysisTest test=new PTAnalysisTest();
        test.test();
    }

    public PTAnalysisTest(){
        super("test_files/PTAnalysis","void a(A,int)","Points To Analysis Test");

    }


    protected void singleTest(String parentDir, String testClassName){
        String filepath=basePath+"/"+parentDir+"/"+testClassName+".out";
        Map<String, Set<Integer>> expectedResults = parseTestFile(filepath,testClassName);
        JavaView pathView =SootUpStuff.getViewFromPath(basePath+"/"+parentDir);
        SootMethod entryMethod=SootUpStuff.getMethodFromView(pathView,"<"+testClassName+": "+entryMethodString+">");

        Map<String, PointsToSet> analysisResults = new PointsToAnalysis(pathView).analise(entryMethod);
        testLog.info(testClassName+".class ");
        testLog.info("Expected results :");
        expectedResults.forEach((name,intset)->testLog.info(name+"="+intset));
        testLog.info("");
        testLog.info("Actual results :");
        removeUseMethod(analysisResults);
        for (var entry : analysisResults.entrySet())
            testLog.info(entry.getKey() + "=" + entry.getValue());
        testLog.info("");

        if (expectedResults.equals(analysisResults)) pass(testClassName);
        else fail(testClassName);

   }

    public Map<String, Set<Integer>> parseTestFile(String filepath,String testclassName) {
        Map<String,Set<Integer>> res= new HashMap<>();

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
        JavaView pathView = SootUpStuff.getViewFromPath(filepath);
        boolean firstMethod=true;
        String methods[]={"A m (A,A)","A m1 (A,A)","A m2 (A,A)","void m (A,A)"
                        ,"void m1 (A,A)","void m2 (A,A)","int m (A,A,int)"};

        for(String sig : methods){
            SootMethod m=SootUpStuff.getMethodFromView(pathView,"<A: "+sig+">");
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
     * some testfiles define a use method to prevent variables splitting in jimple
     * this method is empty and does nothing, we will not consider it for simplicity
     * @param results results of {@link PTAnalysis.PointsToAnalysis#analise(SootMethod entryMethod) PointsToAnalysis}
     * */
   private void removeUseMethod(Map<String, PointsToSet> results){
       Map<String, Set<Integer>> resultsCopy = new HashMap<>(results);
       for (var entry : resultsCopy.entrySet())
           if(entry.getKey().contains("use"))
               results.remove(entry.getKey(),entry.getValue());
   }

}
