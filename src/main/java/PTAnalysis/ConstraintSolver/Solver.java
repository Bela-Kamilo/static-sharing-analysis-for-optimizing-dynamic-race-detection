package PTAnalysis.ConstraintSolver;

import PTAnalysis.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FailureBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.*;
import java.util.stream.IntStream;

public class Solver {
    private final Model model;
    private final Set<Constraint> PTconstraints;
    private static int[] AllLocationsArray =null;
    private final LocationsManager locationsManager;

    public Solver(Set<Constraint> constraints){
        this.PTconstraints=constraints;
        this.model = new Model("Points To Analysis");
        this.locationsManager= new LocationsManager(model);
        createModelConstraints();
       org.chocosolver.solver.Solver solver = model.getSolver();
       solver.setSearch( Search.setVarSearch(new FailureBased<SetVar>(model.retrieveSetVars(),new Date().getTime(),1)
               ,new SetDomainMin()
               ,false
               , model.retrieveSetVars()));

   }
    public void createModelConstraints(){
        AllLocationsArray = IntStream.rangeClosed(1, MemoryLocation.getLocationCounter()).toArray();
        PTconstraints.forEach(this::PTConstraint2ModelConstraint);
        IntVar totalElements= totalElementsOfSetVarsOfModel();
        model.setObjective(Model.MINIMIZE, totalElements);

   }

    private void PTConstraint2ModelConstraint(Constraint c){
        if (c instanceof SupersetOfConstraint){

            PointsToSet superSet=((SupersetOfConstraint) c).getSuperSet();
            PointsToSet subSet=((SupersetOfConstraint) c).getSubSet();
            SetVar modelSuperSet=getOrCreateModelSetFromPTSet(superSet);
            SetVar modelSubSet=getOrCreateModelSetFromPTSet(subSet);

            switch (((SupersetOfConstraint )c).getSuperSetConstraintType()){
                case FIELD_SUPERSET_OF_FIELD : throw new RuntimeException(" found a.f=b.f-like assignment while converting constraints");
                case SUBSET_FIELD:
                    Optional<String> field= ((SupersetOfConstraint) c).getSubSetField();
                    if(field.isEmpty()) throw new RuntimeException("SUBSET_FIELD constraint should have subset field");
                    org.chocosolver.solver.constraints.Constraint fieldRead =
                            new org.chocosolver.solver.constraints.Constraint(superSet+"superset of"+subSet+"."+field.get(),
                                    new FieldReadPropagator(modelSuperSet,modelSubSet, field.get(), locationsManager));
                    model.post(fieldRead);
                    break;
                case SUPERSET_FIELD:
                    Optional<String> field2= ((SupersetOfConstraint) c).getSuperSetField();
                    if(field2.isEmpty()) throw new RuntimeException("SUPERSET_FIELD constraint should have superset field");
                    org.chocosolver.solver.constraints.Constraint fieldAssign =
                            new org.chocosolver.solver.constraints.Constraint(superSet+"."+field2.get()+"superset of"+subSet,
                                    new FieldAssignPropagator(modelSuperSet, field2.get(),modelSubSet,locationsManager));
                    model.post(fieldAssign);
                    break;
                case FIELDLESS:
                    model.subsetEq(new SetVar[]{modelSubSet,modelSuperSet}).post();
                    break;
            }
           return;
        }
        if (c instanceof ElementOfConstraint){
            PointsToSet set=((ElementOfConstraint) c).getSet();
            MemoryLocation m = ((ElementOfConstraint) c).getElement();
            locationsManager.add(m);
            SetVar modelSet= getOrCreateModelSetFromPTSet(set);
            model.member(m.getId(),modelSet).post();
            return;
        }
    }
    private SetVar getOrCreateModelSetFromPTSet(PointsToSet set){
      ///*
        if(set.constraintSolverSet ==null){
            //set.constraintSolverSet = new SolverSetHolder<SetVar>();
            set.constraintSolverSet =model.setVar(set.getVarName(),new int[]{}, Solver.AllLocationsArray);
        }//*/

       return (SetVar) set.constraintSolverSet;
    }

     IntVar totalElementsOfSetVarsOfModel( ){
        SetVar[] setvars =model.retrieveSetVars();
        if(setvars.length ==0){System.out.println("!No setVars in model "+model+"!"); return model.intVar(0,0);}
        ArExpression sum= setvars[0].getCard();
        if(setvars.length ==1) return sum.intVar();
        for(int i =1;i< setvars.length;i++)
            sum=sum.add(setvars[i].getCard());
        return sum.intVar();
    }

    public void solve(){
      try{
          while(model.getSolver().solve()){
              System.out.println("+++solution found+++");
              Arrays.stream(model.retrieveSetVars()).forEach(System.out::println);

          }
          model.getSolver().printStatistics();
      } catch (Exception e) {
          System.out.println(e);
          ;
      }
    }
    public static int[] allLocations(){return AllLocationsArray;}
}
