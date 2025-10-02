package GenericSolver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FailureBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.tools.ArrayUtils;
import util.LoggerFactory;

import java.util.*;
import java.util.logging.Logger;

//possibly generalise more so that every set could have diff type
// TODO make it extendable for custom constraints
/**
 * An adapter class for using choco-solver on <code>Set</code>s with elements of any object
 * {@link ElementOfConstraint}'s and {@link SupersetOfConstraint}'s Sets are filled accordingly
 * when {@link GenericSolver#solve()} is called
 * @param <T> the type of the elements
 * @see <a href="https://choco-solver.org/">Choco-solver</a>
 */
public class GenericSolver<T> {
    private final Model model;
    private final Logger solverLog;
    private final LockedVector<T> elements;
    //might remove the locked classes idk
    private final LockedIdentityHashMap<Set<T>, SetVar> sets2Vars;
    //private final LockedHashMap<SetVar, Set<T>> vars2Sets;

    private GenericSolver(String problemName){
        this.model = new Model(problemName);
        this.solverLog= new LoggerFactory().createLogger(problemName+" SolverResults");
        this.elements= new LockedVector<T>();
        this.sets2Vars= new LockedIdentityHashMap<Set<T>,SetVar>();
        //this.vars2Sets= new LockedHashMap<>();
    }

    public GenericSolver(Collection <GenericConstraint<T>> constraints , String problemName){
        this(problemName);
        //run through every ElementsOfConstraint to note every possible element (need it for SetVar UB)
        for(GenericConstraint<T> c : constraints){
            if(c instanceof ElementOfConstraint<T>) {
                createElementIndex(((ElementOfConstraint<T>) c).getElement());
            }
        }
        elements.lock();
        //run through every Constraint to create all SetVars
        for(GenericConstraint<T> c : constraints){
            if(c instanceof SupersetOfConstraint<T>){
                createSetVar(((SupersetOfConstraint<T>) c).getSuperSet(),((SupersetOfConstraint<T>) c).getSuperSetName(),null);
                createSetVar(((SupersetOfConstraint<T>) c).getSubSet(),((SupersetOfConstraint<T>) c).getSubSetName(),null);
            }
            else if(c instanceof ElementOfConstraint<T>)
                createSetVar(((ElementOfConstraint<T>) c).getSet(),((ElementOfConstraint<T>) c).getSetName(),null);
            else
                throw new IllegalArgumentException();
        }
        sets2Vars.lock();
      //vars2Sets.lock();
        //lastly create the model's constraints
        constraints.forEach(this::addConstraint);
        //set objective and search strategy
        IntVar totalElements= totalElementsOfSetVarsOfModel();
        model.setObjective(Model.MINIMIZE, totalElements);
        org.chocosolver.solver.Solver solver = model.getSolver();
        solver.setSearch( Search.setVarSearch(new FailureBased<SetVar>(model.retrieveSetVars(),new Date().getTime(),1)
                ,new SetDomainMin()
                ,false
                , model.retrieveSetVars()));
    }


    private void addConstraint(GenericConstraint<T> c){
        if(c instanceof ElementOfConstraint<T>){
            int elementIndex =getElementIndex(((ElementOfConstraint<T>) c).getElement());
            SetVar set = getSetVar(((ElementOfConstraint<T>) c).getSet());
            model.member(elementIndex ,set).post();
        }
        else if(c instanceof SupersetOfConstraint<T>){
           SetVar superset = getSetVar(((SupersetOfConstraint<T>) c).getSuperSet());
           SetVar subset = getSetVar(((SupersetOfConstraint<T>) c).getSubSet());
            model.subsetEq(new SetVar[] {subset,superset}).post();
        }
        else
            throw new IllegalArgumentException();
    }
    private void createElementIndex(T element){
        if(elements.contains(element)) return ;
        elements.add(element);
    }

    private int getElementIndex(T element){
        int i= elements.indexOf(element);
        if(i==-1) throw new NoSuchElementException();
        return i;
    }

    private void createSetVar(Set<T> set, String setName , Set<T> allPossibleValues) {
        //already exists as a setVar
        if (sets2Vars.containsKey(set)) return;
        SetVar toBe;
                    //default upper bound
        if (allPossibleValues == null) {
            int ub = !elements.isEmpty() ? elements.size()-1 : 0;
            toBe = model.setVar(setName, new int[] {}, ArrayUtils.array(0, ub));
        } else {    //allPossibleValues upper bound
            int[] UBIDs = new int[allPossibleValues.size()];
            int i = 0;
            for (T value : allPossibleValues) {
                UBIDs[i++] = getElementIndex(value);
            }
            toBe= model.setVar(setName, new int[]{}, UBIDs);
        }
        sets2Vars.put(set, toBe);
        //vars2Sets.put(toBe, set);
        return;
    }
    private SetVar getSetVar(Set<T> set) {
        SetVar res = sets2Vars.get(set);
        if (res == null) throw new NoSuchElementException();
        return res;
    }

    public void solve(){

        /*
        org.chocosolver.solver.Solver solver = model.getSolver();
        solver.setSearch( Search.setVarSearch(new FailureBased<SetVar>(model.retrieveSetVars(),new Date().getTime(),1)
                ,new SetDomainMin()
                ,false
                , model.retrieveSetVars()));
        */

        boolean morethanonesolutions=false;
        try{
            while(model.getSolver().solve()){
                solverLog.info("+++solution found+++");
                if(morethanonesolutions) throw new RuntimeException("There exist more than one solutions for "+model.getName()+"model");
                exportSolution();
                morethanonesolutions =true;
            }
            model.getSolver().log().remove(System.out);
            // model.getSolver().log().add(LoggerPrintStream(solverLog));  TODO
            model.getSolver().printStatistics();
            LoggerFactory.closeHandlerls(solverLog);
            //PTSets.forEach(System.out::println);
            //System.out.println("stop");
        } catch (Exception e) {
            System.err.println(e);
        }
        return;
    }

    private void exportSolution(){
        for(Map.Entry<Set<T>,SetVar> entry : sets2Vars.entrySet()){
            ISet setVarElements = entry.getValue().getLB();
            Set<T> set = entry.getKey();
            for(Integer i :setVarElements)
                set.add(elements.get(i));
            solverLog.info(entry.getValue().getName()+"="+set.toString());
        }
    }
    IntVar totalElementsOfSetVarsOfModel( ){
        SetVar[] setvars =model.retrieveSetVars();
        if(setvars.length ==0){solverLog.info("!No setVars in model "+model+"!"); return model.intVar(0,0);}
        ArExpression sum= setvars[0].getCard();
        if(setvars.length ==1) return sum.intVar();
        for(int i =1;i< setvars.length;i++)
            sum=sum.add(setvars[i].getCard());
        return sum.intVar();
    }
}

class LockedIdentityHashMap<K,L> extends IdentityHashMap<K,L> {
    private boolean locked ;

    public LockedIdentityHashMap(){ this.locked= false;}

    public void lock(){ this.locked=true;}

    @Override
    public L put(K key, L value) {
        if(locked) throw new IllegalStateException();
        return super.put(key, value);
    }
}
class LockedVector<K> extends Vector<K> {
    private boolean locked ;

    public LockedVector(){ this.locked= false;}

   public void lock(){ this.locked=true;}

    @Override
    public synchronized boolean add(K k) {
        if (locked) throw new IllegalStateException();
        return super.add(k);
    }
}
