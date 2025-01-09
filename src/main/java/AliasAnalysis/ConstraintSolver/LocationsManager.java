package AliasAnalysis.ConstraintSolver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FailureBased;
import org.chocosolver.solver.variables.SetVar;

import java.util.Date;
import java.util.HashMap;
import java.util.stream.IntStream;

public class LocationsManager {
    HashMap<Integer, Location> locationsMap;
    Model model;
    final int totalLocations;

    LocationsManager(Model m){
        this.model=m;
        locationsMap= new HashMap<>();
        totalLocations=20;
    }

    public void add(int id){
        if(locationsMap.containsKey(id)) return;
        locationsMap.put(id, new Location(id));
    }

    public void setField(int locationId, String field, SetVar setVar){
        add(locationId);
        if(locationsMap.get(locationId).fields.containsKey(field)){
            System.out.println("!"+locationId+"."+field+" already exists!");return;}
        locationsMap.get(locationId).fields.put(field, setVar);
    }



    public SetVar getField(int locationId, String field) {
        add(locationId);
        Location l = locationsMap.get(locationId);
        if (!l.fields.containsKey(field)) {
            SetVar l_field = model.setVar(locationId + "." + field, new int[]{}, IntStream.rangeClosed(1, totalLocations).toArray());
            l.fields.put(field, l_field);
            idkman();
            return l_field;
        }
        return locationsMap.get(locationId).fields.get(field);
    }

    void idkman(){
        model.setObjective(Model.MINIMIZE, Main.totalElementsOfSetVarsOfModel(model));
        model.getSolver().
                setSearch(
                        Search.setVarSearch(new FailureBased<SetVar>(model.retrieveSetVars(),
                                new Date().getTime(),1) ,
                                new SetDomainMin(),
                                false ,
                                model.retrieveSetVars()));
    }
}
