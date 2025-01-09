package AliasAnalysis.ConstraintSolver;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;




/*  p )= q.f     l_q in q    l_f in l_q.f
 *-----------------------------------------[field-read]
 *              l_f in p
 *
 *  unsure if all l_q.fs should be held by the propagator in addition to q and f
 *  (seems to work as it is (?))
 */
public class FieldReadPropagator extends Propagator<SetVar> {


    SetVar p;
    SetVar q;
    String field;
    private final LocationsManager locationsManager;
    FieldReadPropagator(SetVar p, SetVar q , String field , LocationsManager locationsManager){
        super(p,q);
        this.p=p;
        this.q=q;
        this.field = field;
        this.locationsManager=locationsManager;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        //get l_qs
        ISet l_qs= q.getLB();
        // each l_f in l_q.f must be in p
        for(int l_q : l_qs){
            SetVar l_qField = locationsManager.getOrCreateField(l_q, field);// getFieldSet(l_q , field);
            ISet l_fs = l_qField.getLB();
            for(int l_f : l_fs)
                p.force(l_f,this);

        }
    }

    @Override
    public ESat isEntailed() {
        if(!q.isInstantiated()) return ESat.UNDEFINED;
        if(!p.isInstantiated()) return ESat.UNDEFINED;
        //get l_qs
        ISet l_qs= q.getValue();
        // each l_f in l_q.f must be in p
        for(int l_q : l_qs){
            SetVar l_qField = locationsManager.getOrCreateField(l_q,field);//getFieldSet(l_q , field);
            ISet l_fs = l_qField.getValue();
            for(int l_f : l_fs)
                if(!p.getLB().contains(l_f)) return ESat.FALSE;

        }
        return  ESat.TRUE;
    }

    @Override
    public int getPropagationConditions(int vIdx){
        if (vIdx ==1)   //this propagator will make changes  when q (OR SOME l_q.f !) changes
            return SetEventType.ADD_TO_KER.getMask();
        return SetEventType.VOID.getMask();
    }
}
