package test;

import util.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Class to run a series of tests located in basePath/X
 * Call {@link Test#test() test method}
 */
public abstract class Test {
    private final String testName;
    protected int testsCount=1;
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    protected void pass(String singleTestName){System.out.println("TEST "+testsCount+" "+singleTestName+": "+ANSI_GREEN+"PASS"+ANSI_RESET);}
    protected void fail(String singleTestName){System.out.println("TEST "+testsCount +" " +singleTestName+": "+ANSI_RED+"FAIL"+ANSI_RESET);}
    public int getCount(){return testsCount;}

    protected final String basePath;
    protected String entryMethodString ;
    protected final Logger testLog;

    /**
     * Calls singleTest for each class file (except A.class) in depth 2 from basePath
     */
    public void test(){
        File[] files = new File(basePath).listFiles();
        Arrays.sort(files);
        System.out.println("+"+testName+"+");

        for (File directory : files) {
            if (directory.isDirectory()) {
                File[] filesOfDir = new File(directory.getPath()).listFiles();
                Arrays.sort(filesOfDir);
                Stream<File> testfiles=Arrays.stream(filesOfDir).filter(file->(file.isFile()
                        && file.getName().split("\\.")[1].equals("class")
                        &&!file.getName().equals("A.class")));

                testfiles.forEach(file->{
                    singleTest(directory.getName() ,file.getName().split("\\.")[0]);
                    testsCount++;
                });
            }
            else {
                ;
            }
        }
        LoggerFactory.closeHandlerls(testLog);
        System.out.println("See logs for details");
    }
    abstract protected void singleTest(String parentDir, String testClassName);

    public Test(String basePath, String entryMethodString, String testName){
        this.testName=testName;
        this.basePath=basePath;
        this.entryMethodString=entryMethodString;
        this.testLog= new LoggerFactory().createLogger(testName);
        testLog.info(testName+" created");
    }

}
