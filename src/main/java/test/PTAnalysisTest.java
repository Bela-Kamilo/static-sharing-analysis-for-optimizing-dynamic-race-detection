package test;

import PTAnalysis.PointsToAnalysis;
import PTAnalysis.PointsToSet;
import analysis.my_analysis;
import sootup.core.jimple.basic.Value;
import sootup.core.model.SootMethod;
import sootup.java.core.views.JavaView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PTAnalysisTest extends Test {
    private final String basePath="test files/PTAnalysis";
    private String testClass="A1";
    private String dir="new/";
    private String filepath=basePath+"/"+dir+testClass+".out";
    private String entryMethodString ="void a(A,int)";
    private String defaultVarprefix = "<"+testClass+": "+entryMethodString+">:";
    private final Logger PTAtestLog;

   public PTAnalysisTest(){
        PTAtestLog=  Logger.getLogger("Points To Analysis Test");
        FileHandler fh;
        try {
            fh = new FileHandler("logs/PTATestLogFile.log");
            PTAtestLog.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
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
        Map<String, Set<Integer>> expectedResults = parseTestFile(filepath);
        JavaView pathView =my_analysis.getViewFromPath(basePath+"/new");
        SootMethod entryMethod=my_analysis.getMethodFromView(pathView,"A1","void a (A,int)");
        Map<String, Set<Integer>> analysisResults = new PointsToAnalysis(pathView).analise(entryMethod);
        PTAtestLog.info(testClass+".class ");
        PTAtestLog.info("Expected results :");
        expectedResults.forEach((name,intset)->PTAtestLog.info(name+"="+intset));
        PTAtestLog.info("Actual results :");

        for (var entry : analysisResults.entrySet())
            PTAtestLog.info(entry.getKey() + "=" + entry.getValue());


        if (expectedResults.equals(analysisResults)) pass(testClass);
        else fail(testClass);
    }

    public Map<String, Set<Integer>> parseTestFile(String filepath) {
        Map<String,Set<Integer>> res= new HashMap<>();

        //String varNameWSignature= "[$a-zA-Z0-9()<>.,:][$a-zA-Z0-9()<>.,:\\s]*[$a-zA-Z0-9()<>.,:]";
        File expectedResultFile = new File(filepath);
        try {
            Scanner lineScanner= new Scanner(expectedResultFile);
            while (lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                if(line.isEmpty()) continue;    //skip empty lines

                Scanner expectedResultsScanner= new Scanner(line);
                expectedResultsScanner.useDelimiter("\\s*,\\s*");
                //read variable name
                String var= parseLocationHolder(expectedResultsScanner);
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

    private String parseLocationHolder(Scanner s){
        String varName= "(\\$?[a-zA-Z][a-zA-Z0-9_]*)";
        String paramsAndRetlocations="\\d";
        String fieldsOfMemLocations="\\d\\."+varName;
        String signature="<.*>[:.]";
        String possibleFieldOfMemLocation = s.findInLine(fieldsOfMemLocations);
        if(possibleFieldOfMemLocation!=null) return possibleFieldOfMemLocation;
        String possibleSignature = s.findInLine(signature);
        String var =s.findInLine(varName+"|"+paramsAndRetlocations);
        if(var==null) throw new InputMismatchException();
        var = possibleSignature == null ? defaultVarprefix+var : possibleSignature+var ;

        return var;
    }

}
