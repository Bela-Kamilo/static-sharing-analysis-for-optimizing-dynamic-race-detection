package RuleApplicator;

import PTAnalysis.ConstraintManager;
import PTAnalysis.PTASupersetOfConstraint;
import PTAnalysis.PointsToSet;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootMethod;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;

public class RuleApplicatorGlobal {
    private final ConstraintManager constraintManager;

    public RuleApplicatorGlobal(ConstraintManager constraintManager){
        this.constraintManager=constraintManager;
    }

    public void abstractThisGlobalRule(SootMethod m, SootMethod implOfM){
        if(! m.isAbstract()) return;
        if(!implOfM.isConcrete()) return;
       //get m.this
        PointsToSet m_this = constraintManager.getOrCreateMappingOf(m.getSignature(),constraintManager.getTHIS_INDEX());
       //get impl.this
        PointsToSet impl_this = constraintManager.getOrCreateMappingOf(implOfM.getSignature(),constraintManager.getTHIS_INDEX());
       //add impl.this superset of m.this
        constraintManager.addPTA(new PTASupersetOfConstraint(impl_this,m_this));
    }
    public void abstractParamGlobalRule(SootMethod m, SootMethod implOfM){
        if(! m.isAbstract()) return;
        if(!implOfM.isConcrete()) return;
        for(int i =0;i< m.getParameterCount();i++) {
            Type paramType=m.getParameterType(i);
            if(!(paramType instanceof ReferenceType) || paramType.toString().equals("java.lang.String")) continue;

            PointsToSet m_p_i = constraintManager.getOrCreateMappingOf(m.getSignature(),i+1);
            PointsToSet impl_p_i = constraintManager.getOrCreateMappingOf(implOfM.getSignature(), i+1);
            constraintManager.addPTA(new PTASupersetOfConstraint(impl_p_i, m_p_i));
        }
    }
    public void abstractReturnGlobalRule(SootMethod m, SootMethod implOfM){
        if(! m.isAbstract()) return;
        if(!implOfM.isConcrete()) return;
        PointsToSet m_return = constraintManager.getOrCreateMappingOfMethod(m.getSignature());
        PointsToSet impl_return = constraintManager.getOrCreateMappingOfMethod(implOfM.getSignature());
        constraintManager.addPTA(new PTASupersetOfConstraint(m_return, impl_return));
    }
}
