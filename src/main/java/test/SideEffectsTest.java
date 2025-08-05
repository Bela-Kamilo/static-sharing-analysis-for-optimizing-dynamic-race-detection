package test;

import PTAnalysis.AccessibleHeapLocation;
import PTAnalysis.PointsToAnalysis;
import PTAnalysis.PointsToSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.java.core.views.JavaView;
import util.SootUpStuff;

import sootup.core.model.SootMethod;

import javax.lang.model.type.ReferenceType;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Test files for this class need be in such a format :
 * METHODSIGNATURE._READS={X.FIELDSIGNATURE,Y.FIELDSIGNATURE,Z.FIELDSIGNATURE}      or
 * METHODSIGNATURE.WRITES={X.FIELDSIGNATURE,Y.FIELDSIGNATURE,Z.FIELDSIGNATURE}      where X,Y,Z are integers
 *
 */
public class SideEffectsTest extends Test{
    private static final Logger log = LoggerFactory.getLogger(SideEffectsTest.class);
    private final String entryMethodWSpace="void a ()";
    private JavaView pathView;
    //private enum MemOp={READ,WRITE};
    public SideEffectsTest(){
        super("test_files/SideEffects","void a()","Side Effects Test");
    }

    public static void main(String[] args){
        SideEffectsTest sideEffectsTest = new SideEffectsTest();
        sideEffectsTest.test();
    }

    @Override
    protected void singleTest(String parentDir, String testClassName) {
        nextThingBuffer="";
        String filepath=basePath+"/"+parentDir+"/"+testClassName+".out";
        pathView = SootUpStuff.getViewFromPath(basePath+"/"+parentDir);
        ExpectedSideEffects expectedResults = parseTestFile(filepath,testClassName);
        SootMethod entryMethod=SootUpStuff.getMethodFromView(pathView,"<"+testClassName+": "+entryMethodString+">");
        PointsToAnalysis analysis = new PointsToAnalysis(pathView);
        analysis.analise(entryMethod);
        analysis.getReads();
        Map<MethodSignature, Set<AccessibleHeapLocation>> READS;
        Map<MethodSignature, Set<AccessibleHeapLocation>> WRITES;
        try {
         READS=analysis.getReads();
         WRITES=analysis.getWrites();
        }
        catch (Exception e){
            System.err.println(e);
            fail(testClassName);
            return;
        }
        Map<MethodSignature,Set<AccessibleHeapLocation>> expectedREADS=expectedResults.getREADS();
        Map<MethodSignature,Set<AccessibleHeapLocation>> expectedWRITES=expectedResults.getWRITES();
        logResults(READS,WRITES,expectedREADS,expectedWRITES,testClassName);
       if( expectedREADS.equals(READS) && expectedWRITES.equals(WRITES))
            pass(testClassName);
       else
           fail(testClassName);
    }


     public ExpectedSideEffects parseTestFile(String filepath,String testclassName) {
         ExpectedSideEffects results = new ExpectedSideEffects();
         String signaturePattern = "<.*>";
         String readsOrWritesPattern = "\\._(READS|WRITES)";
         String AccessibleHeapLocationPattern = "\\d+\\.<.*>";


         File expectedResultFile = new File(filepath);
         try {
             Scanner TFScanner = new Scanner(expectedResultFile);
             TFScanner.useDelimiter("");
             while (TFScanner.hasNextLine()) {
                 //parse     METHODSIGNATURE._READS or METHODSIGNATURE._WRITES
                 String methodSigString = getNextThingFrom(TFScanner, signaturePattern + "(?!\\})");
                 MethodSignature m = pathView.getIdentifierFactory().parseMethodSignature(methodSigString);
                 String readOrWrite = getNextThingFrom(TFScanner, readsOrWritesPattern);
                 results.noteMethod(m);
                 Set<AccessibleHeapLocation> sideEffect;
                 // parse = {
                 getNextThingFrom(TFScanner, "=\\s*\\{");
                 while (true) {
                     // parse X.FIELDSIGNATURE
                     String AccHeapLocationString = getNextThingFrom(TFScanner, AccessibleHeapLocationPattern + "|\\}");
                     if (AccHeapLocationString.equals("}")) break;
                     String XString = AccHeapLocationString.substring(0, AccHeapLocationString.indexOf('.'));
                     int X = Integer.parseInt(XString);
                     String fieldString = AccHeapLocationString.substring(AccHeapLocationString.indexOf('.') + 1);
                     FieldSignature field = pathView.getIdentifierFactory().parseFieldSignature(fieldString);
                     // store expected results
                     if (readOrWrite.matches("\\._READS"))
                         results.reads(m, X, field);
                     else if (readOrWrite.matches("\\._WRITES"))
                         results.writes(m, X, field);

                     else throw new InputMismatchException();

                     // parse , or }
                     String commaOrBrckt = getNextThingFrom(TFScanner, "(\\s*,\\s*)|\\}");
                     if (commaOrBrckt.equals("}")) break;
                 }
             }


         } catch (Exception e) {
             if (e instanceof FileNotFoundException)
                 System.err.println("!Error! test file " + filepath + " not found");
             else {
                 System.err.println("!Error! test file" + filepath + " has invalid syntax");
                 System.err.println(e);
             }
         }

             return results;

     }

     private void logResults(Map<MethodSignature,Set<AccessibleHeapLocation>> READS, Map<MethodSignature,Set<AccessibleHeapLocation>> WRITES,
                             Map<MethodSignature,Set<AccessibleHeapLocation>> expectedREADS, Map<MethodSignature,Set<AccessibleHeapLocation>> expectedWRITES,
                             String testClassName){
         testLog.info("------"+testClassName+".class ------");
         testLog.info("Expected results :");
         testLog.info("");
         testLog.info("     READS:");
         testLog.info("");
         for(var entry :expectedREADS.entrySet())
             testLog.info(entry.getKey() + " ->\n" +
                     entry.getValue().toString().replace("[","[\t").replace(",",",\n\t")
                     +"\n");
         testLog.info("      WRITES:");
         testLog.info("");
         for(var entry :expectedWRITES.entrySet())
             testLog.info(entry.getKey() + " ->\n" +
                     entry.getValue().toString().replace("[","[\t").replace(",",",\n\t")
                     +"\n");

         testLog.info("");
         testLog.info("Actual results :");
         testLog.info("");
         testLog.info("     READS:");
         testLog.info("");
         for(var entry :READS.entrySet())
             testLog.info(entry.getKey() + " ->\n" +
                     entry.getValue().toString().replace("[","[\t").replace(",",",\n\t")
                     +"\n");
         testLog.info("      WRITES:");
         testLog.info("");
         for(var entry :WRITES.entrySet())
             testLog.info(entry.getKey() + " ->\n" +
                     entry.getValue().toString().replace("[","[\t").replace(",",",\n\t")
                     +"\n");
    }
}

class ExpectedSideEffects{

    private final Map<MethodSignature, Set<AccessibleHeapLocation>> READS;
    private final Map<MethodSignature, Set<AccessibleHeapLocation>> WRITES;

    public ExpectedSideEffects(){
        READS= new HashMap<>();
        WRITES= new HashMap<>();
    }

    public void noteMethod(MethodSignature m){
        //READS.equals()
        if(!READS.containsKey(m))
            READS.put(m, new HashSet<>());
        if(!WRITES.containsKey(m))
            WRITES.put(m, new HashSet<>());
    }

    public void reads(MethodSignature m, int object, FieldSignature field){
        if(!READS.containsKey(m)) READS.put(m, new HashSet<>());
        READS.get(m).add( new AccessibleHeapLocation(object,field));
    }
    public void writes(MethodSignature m, int object, FieldSignature field){
        if(!WRITES.containsKey(m)) WRITES.put(m, new HashSet<>());
        WRITES.get(m).add( new AccessibleHeapLocation(object,field));
    }
    public Map<MethodSignature, Set<AccessibleHeapLocation>> getREADS() {
        return READS;
    }

    public Map<MethodSignature, Set<AccessibleHeapLocation>> getWRITES() {
        return WRITES;
    }
}