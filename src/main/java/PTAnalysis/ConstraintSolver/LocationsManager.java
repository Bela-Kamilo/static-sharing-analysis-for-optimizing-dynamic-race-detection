package PTAnalysis.ConstraintSolver;

import PTAnalysis.MemoryLocation;
import PTAnalysis.PointsToSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FailureBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.*;

/** Acts as an interface between MemoryLocations and a model
 * A memory location for the model is an integer
 * A points-to set for the model is a set variable (SetVar) containing ints
 * */
public class LocationsManager {
    private final HashMap<Integer, MemoryLocation> locationsMap;
    private final Model model;
    final int totalLocations;

    LocationsManager(Model m){
        this.model=m;
        locationsMap= new HashMap<>();
        totalLocations= MemoryLocation.getLocationCounter();
    }

    /**Stores a MemoryLocation in a HashMap
     * @param l
     */
    public void add(MemoryLocation l){
        int id =l.getId();
        if(locationsMap.containsKey(id)){
            System.out.println("Location "+id+" already exists in LocationsManager");
            return;
        }
        locationsMap.put(id, l);
    }

    /**
     *
     * @param locationId    memory location
     * @param field     name of the field
     * @return   locationId.field SetVar
     */
    public SetVar getOrCreateField(int locationId, String field) {
        if(!locationsMap.containsKey(locationId)) throw new RuntimeException("Location "+locationId+" doesnt exist in LocationsManager");
        MemoryLocation l = locationsMap.get(locationId);

        if (!l.existsField(field)) {
            SetVar l_field = model.setVar(locationId + "." + field, new int[]{}, Solver.allLocations());
            PointsToSet fieldPTSet = new PointsToSet(locationId + "."+field);
            fieldPTSet.constraintSolverSet=l_field;
            l.setField(field, fieldPTSet);
            updateSearch();
            return l_field;
        }
        return (SetVar) locationsMap.get(locationId).getField(field).constraintSolverSet;
    }

    /**
     *
     * @return every PointsToSet of every field of every
     * memory location
     */
    public Set<PointsToSet> getFieldSets(){
        HashSet<PointsToSet> allfields= new HashSet<>();
        locationsMap.values().forEach(x->x.getAllFields().forEach(allfields::add));
        return allfields;
    }

    private  void updateSearch(){
            model.setObjective(Model.MINIMIZE, totalElementsOfSetVarsOfModel());
            model.getSolver().
                    setSearch(
                            Search.setVarSearch(new FailureBased<SetVar>(model.retrieveSetVars(), new Date().getTime(), 1),
                                    new SetDomainMin(),
                                    false ,
                                    model.retrieveSetVars()));
    }

     private IntVar totalElementsOfSetVarsOfModel(){
        SetVar[] setvars =model.retrieveSetVars();
        if(setvars.length ==0){System.out.println("!No setVars in model "+model+"!"); return model.intVar(0,0);}
        ArExpression sum= setvars[0].getCard();
        if(setvars.length ==1) return sum.intVar();
        for(int i =1;i< setvars.length;i++)
            sum=sum.add(setvars[i].getCard());
        return sum.intVar();
    }
}
