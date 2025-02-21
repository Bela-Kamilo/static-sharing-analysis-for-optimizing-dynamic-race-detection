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
import java.util.stream.Stream;

public class PTAnalysisTest extends Test {
    private final String basePath="test files/PTAnalysis";
    private String testClass="A1";
    private String dir="new/";

    private String entryMethodString ="void a(A,int)";

    private final Logger PTAtestLog;
    private final String entryMethodWSpace ="void a (A,int)";

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

       File[] files = new File(basePath).listFiles();
       Arrays.sort(files);

       for (File directory : files) {
           if (directory.isDirectory()) {
               File[] filesOfDir = new File(directory.getPath()).listFiles();
               //for(File testfile : testfiles)
                 //  System.out.println(testfile.getName());
               /*Arrays.stream(testfiles).filter(file->(file.isFile()&&
                       file.getName().split("\\.")[1].equals("class")
                       &&!file.getName().equals("A.class"))).forEach(file->singleTest(directory.getName(),file.getName().split("\\.")[0]));*/
               Stream<File>testfiles=Arrays.stream(filesOfDir).filter(file->(file.isFile()
                       && file.getName().split("\\.")[1].equals("class")
                       &&!file.getName().equals("A.class")));
                //testfiles.forEach(testfile->System.out.println(directory.getName() + ": "+testfile.getName().split("\\.")[0]));
                testfiles.forEach(file->singleTest(directory.getName() ,file.getName().split("\\.")[0]));
           }
           else {
              ;
           }
       }
        singleTest("1 New", "New1");
}

    private void singleTest(String parentDir, String testClassName){
        String filepath=basePath+"/"+parentDir+"/"+testClassName+".out";
        Map<String, Set<Integer>> expectedResults = parseTestFile(filepath,testClassName);
        JavaView pathView =my_analysis.getViewFromPath(basePath+"/"+parentDir);
        SootMethod entryMethod=my_analysis.getMethodFromView(pathView,testClassName,entryMethodWSpace);  //TODO fix the space thing
        Map<String, Set<Integer>> analysisResults = new PointsToAnalysis(pathView).analise(entryMethod);
        PTAtestLog.info(testClassName+".class ");
        PTAtestLog.info("Expected results :");
        expectedResults.forEach((name,intset)->PTAtestLog.info(name+"="+intset));
        PTAtestLog.info("Actual results :");

        for (var entry : analysisResults.entrySet())
            PTAtestLog.info(entry.getKey() + "=" + entry.getValue());


        if (expectedResults.equals(analysisResults)) pass(testClassName);
        else fail(testClassName);
    }

    public Map<String, Set<Integer>> parseTestFile(String filepath,String testclassName) {
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
        String varName= "(\\$?[a-zA-Z][a-zA-Z0-9_]*)";
        String paramsAndRetlocations="\\d";
        String fieldsOfMemLocations="\\d\\."+varName;
        String signature="<.*>[:.]";
        String defaultVarprefix = "<"+testClassName+": "+entryMethodString+">:";
        String possibleFieldOfMemLocation = s.findInLine(fieldsOfMemLocations);
        if(possibleFieldOfMemLocation!=null) return possibleFieldOfMemLocation;
        String possibleSignature = s.findInLine(signature);
        String var =s.findInLine(varName+"|"+paramsAndRetlocations);
        if(var==null) throw new InputMismatchException();
        var = possibleSignature == null ? defaultVarprefix+var : possibleSignature+var ;

        return var;
    }

}
