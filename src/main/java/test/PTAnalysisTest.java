package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TestParser {
    private final String basePath="test files/PTAnalysis";
    private String filepath=basePath+"/new/A1.out";

    public void test(){
        parseTestFile(filepath);
    }

    public Map<String, Set<Integer>> parseTestFile(String filepath) {
        Map<String,Set<Integer>> res= new HashMap<>();
        String word= "[a-zA-Z][a-zA-Z0-9_]*";
        File expectedResultFile = new File(filepath);
        try {
            Scanner expectedResultsScanner=new Scanner(expectedResultFile);
            expectedResultsScanner.useDelimiter("\\s*,\\s*");

            while (expectedResultsScanner.hasNextLine()) {
                //read word
                String var =expectedResultsScanner.findInLine(word);
                if(var==null) throw new InputMismatchException();
                //read '='
                if (expectedResultsScanner.findInLine("=")==null) throw new InputMismatchException();
                //read '{'
                if ( expectedResultsScanner.findInLine("\\{")==null)throw new InputMismatchException();
                //read ints
                Set<Integer> memLocations = new HashSet<>();
                while(expectedResultsScanner.hasNextInt())
                    memLocations.add(expectedResultsScanner.nextInt());
                int lastInt = Integer.parseInt(expectedResultsScanner.findInLine("\\d"));
                memLocations.add(lastInt);
                //read '}'
                if (expectedResultsScanner.findInLine("}")==null)throw new InputMismatchException();
            //    expectedResultsScanner.close();
               expectedResultsScanner.nextLine();
                res.put(var ,memLocations);
            }
        //return res;
        } catch (Exception e) {
            if (e instanceof  FileNotFoundException)
                System.out.println("!Error! test file "+filepath+" not found");
            else if (e instanceof InputMismatchException || e instanceof NumberFormatException)
                System.out.println("!Error! test file" + filepath + " has invalid syntax");
            else
                System.out.println(e);
        }
        return res;
    }
}
