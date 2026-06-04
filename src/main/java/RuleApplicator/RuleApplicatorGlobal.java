package RuleApplicator;

import PTAnalysis.ConstraintManager;
import PTAnalysis.PTASupersetOfConstraint;
import PTAnalysis.PointsToSet;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
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

    public void inheritedMethodsRule(SootMethod m, MethodSignature s){
        if(!m.getSignature().getSubSignature().equals(s.getSubSignature()))
            return;
        //returns
        PointsToSet method = constraintManager.getOrCreateMappingOfMethod(m.getSignature());
        PointsToSet signature = constraintManager.getOrCreateMappingOfMethod(s);
        constraintManager.addPTA(new PTASupersetOfConstraint(method,signature));
        constraintManager.addPTA(new PTASupersetOfConstraint(signature, method));
        //this
        PointsToSet method_this = constraintManager.getOrCreateMappingOf(m.getSignature(),constraintManager.getTHIS_INDEX());
        PointsToSet signature_this = constraintManager.getOrCreateMappingOf(s,constraintManager.getTHIS_INDEX());
        constraintManager.addPTA(new PTASupersetOfConstraint(method_this,signature_this));
        constraintManager.addPTA(new PTASupersetOfConstraint(signature_this, method_this));
        //params
        for(int i =0;i< m.getParameterCount();i++) {
            Type paramType=m.getParameterType(i);
            if(!(paramType instanceof ReferenceType) || paramType.toString().equals("java.lang.String")) continue;

            PointsToSet method_p_i = constraintManager.getOrCreateMappingOf(m.getSignature(),i+1);
            PointsToSet sign_p_i = constraintManager.getOrCreateMappingOf(s, i+1);
            constraintManager.addPTA(new PTASupersetOfConstraint(method_p_i, sign_p_i));
            constraintManager.addPTA(new PTASupersetOfConstraint(sign_p_i,method_p_i));
        }
    }
}
