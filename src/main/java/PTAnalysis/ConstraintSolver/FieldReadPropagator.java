package PTAnalysis.ConstraintSolver;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.Arrays;


/**  p )= q.f ,    l_q in q ,   l_f in l_q.f            </br>
 *-----------------------------------------[field-read] </br>
 *              l_f in p                                </br>
 *
 *
 * @see <a href="https://github.com/Bela-Kamilo/Java-thread-locality-static-analysis/blob/master/src/main/java/PTAnalysis/README.md">Repo Readme</a>
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
            SetVar l_qField = getField(l_q, field);
            ISet l_fs = l_qField.getLB();
            for(int l_f : l_fs)
                p.force(l_f,this);

        }
    }
    private SetVar getField(int lId, String field){
        SetVar fieldSet = locationsManager.getOrCreateField(lId,field);
        if(!Arrays.asList(this.vars).contains(fieldSet))
            this.addVariable(fieldSet);
        return fieldSet;
    }

    @Override
    public ESat isEntailed() {
        if(!q.isInstantiated()) return ESat.UNDEFINED;
        if(!p.isInstantiated()) return ESat.UNDEFINED;
        //get l_qs
        ISet l_qs= q.getValue();
        // each l_f in l_q.f must be in p
        for(int l_q : l_qs){
            SetVar l_qField = locationsManager.getOrCreateField(l_q,field);
            if(!l_qField.isInstantiated()) return ESat.UNDEFINED;
            ISet l_fs = l_qField.getValue();
            for(int l_f : l_fs)
                if(!p.getValue().contains(l_f)) return ESat.FALSE;

        }
        return  ESat.TRUE;
    }

    @Override
    public int getPropagationConditions(int vIdx){
        if (vIdx !=0)   //this propagator will make changes  when q (OR SOME l_q.f !) changes
            return SetEventType.ADD_TO_KER.getMask();
        return SetEventType.VOID.getMask();
    }
}
