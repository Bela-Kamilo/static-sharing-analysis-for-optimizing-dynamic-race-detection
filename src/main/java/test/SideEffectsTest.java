package test;

import PTAnalysis.AccessibleHeapLocation;
import PTAnalysis.PointsToAnalysis;
import PTAnalysis.PointsToSet;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;
import util.SootUpStuff;

import sootup.core.model.SootMethod;
import java.util.Map;
import java.util.Set;

public class SideEffectsTest extends Test{
    private final String entryMethodWSpace="void a ()";
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
        String filepath=basePath+"/"+parentDir+"/"+testClassName+".out";
        Map<String, Set<Integer>> expectedResults = null; //parseTestFile(filepath,testClassName);
        JavaView pathView = SootUpStuff.getViewFromPath(basePath+"/"+parentDir);
        SootMethod entryMethod=SootUpStuff.getMethodFromView(pathView,testClassName,entryMethodWSpace);  //TODO fix the space thing
        PointsToAnalysis analysis = new PointsToAnalysis(pathView);
        analysis.analise(entryMethod);
        Map<MethodSignature, Set<AccessibleHeapLocation>> READS;
        Map<MethodSignature, Set<AccessibleHeapLocation>> WRITES;
        try {
           READS = analysis.getReads();
           WRITES = analysis.getWrites();
        }
        catch (Exception e){
            System.err.println(e);
            fail(testClassName);
            return;
        }
        fail(testClassName);
        /*
        testLog.info(testClassName+".class ");
        testLog.info("Expected results :");
        expectedResults.forEach((name,intset)->testLog.info(name+"="+intset));
        testLog.info("");
        testLog.info("Actual results :");
       // removeUseMethod(analysisResults);
        for (var entry : analysisResults.entrySet())
            testLog.info(entry.getKey() + "=" + entry.getValue());
        testLog.info("");

        if (expectedResults.equals(analysisResults)) pass(testClassName);
        else fail(testClassName);

         */
    }
    //Map<String, Set<>>
}
