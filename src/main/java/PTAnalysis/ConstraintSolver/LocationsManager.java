package AliasAnalysis.ConstraintSolver;

import AliasAnalysis.MemoryLocation;
import AliasAnalysis.PointsToSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FailureBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.Date;
import java.util.HashMap;

/*
*
 */
public class LocationsManager {
    HashMap<Integer, MemoryLocation> locationsMap;
    Model model;
    final int totalLocations;

    LocationsManager(Model m){
        this.model=m;
        locationsMap= new HashMap<>();
        totalLocations= MemoryLocation.getLocationCounter();
    }

    public void add(MemoryLocation l){
        int id =l.getId();
        if(locationsMap.containsKey(id)){
            System.out.println("Location "+id+" already exists in LocationsManager");
            return;
        }
        locationsMap.put(id, l);
    }


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

    void updateSearch(){
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
