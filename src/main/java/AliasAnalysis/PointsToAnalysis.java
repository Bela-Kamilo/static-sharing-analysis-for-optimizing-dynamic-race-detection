package AliasAnalysis;

import sootup.core.types.ReferenceType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

//this class will implement a field sensitive Andersen's points to analysis
//on jimple code
public class PointsToAnalysis {
    private Map<ReferenceType,PointsToSet> sets= new HashMap<ReferenceType,PointsToSet>();

}
