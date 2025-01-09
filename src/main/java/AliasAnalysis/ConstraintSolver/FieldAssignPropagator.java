package AliasAnalysis.ConstraintSolver;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

/*  p.f )= q    l_p in p        l_q in q
 *-----------------------------------------[field-assign]
 *              l_q in l_p.f
 *
 *
 *
 */
public class FieldAssignPropagator extends Propagator<SetVar> {

    private final SetVar p;
    private final SetVar q;
    private final String field;
    private final LocationsManager locationsManager;

    FieldAssignPropagator(SetVar p , String field, SetVar q, LocationsManager locationsManager){
        super(new SetVar[]{p,q});
        this.p=p;
        this.q=q;
        this.field=field;
        this.locationsManager = locationsManager;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        //get l_ps
        for(int l_p : p.getLB())
            //get l_qs
            for( int l_q : q.getLB()){
                //l_q in l_p.f
                SetVar l_pfield=locationsManager.getOrCreateField(l_p, field);
                //if(l_pfield==null) throw new ContradictionException();
                l_pfield.force(l_q,this);
            }
    }



    @Override
    public ESat isEntailed() {

        if(!q.isInstantiated()) return ESat.UNDEFINED;
        if(!p.isInstantiated()) return ESat.UNDEFINED;
        //get l_ps
        for(int l_p : p.getValue()){
            // each l_q in q must be in each l_p.f  (where l_p in p)
            SetVar l_pFieldSet=locationsManager.getOrCreateField(l_p, field);
            if(!l_pFieldSet.isInstantiated()) return ESat.UNDEFINED;
            ISet l_fs = l_pFieldSet.getValue();

            for(int l_q : q.getValue()){
                if(!l_fs.contains(l_q)) return ESat.FALSE;
            }
        }
        return  ESat.TRUE;


    }

    public int getPropagationConditions(int vIdx){
        //this propagator will make changes  when q or p changes
        return SetEventType.ADD_TO_KER.getMask();
    }
}
