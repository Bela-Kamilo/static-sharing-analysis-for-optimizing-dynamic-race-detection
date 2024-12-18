package AliasAnalysis.ConstraintSolver;

import AliasAnalysis.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;

public class Solver {
    private Model model;
    private Set<Constraint> PTconstraints;
    private static int[] AllLocationsArray =null;
   public Solver(Set<Constraint> constraints){
        this.PTconstraints=constraints;
        this.model = new Model("Subsets");
        createModelConstraints();
    }
    public void createModelConstraints(){
        AllLocationsArray = IntStream.rangeClosed(1, MemoryLocation.getLocationCounter()).toArray();
        PTconstraints.forEach(this::PTConstraint2ModelConstraint);
        IntVar totalElements= model.intVar("Total Elements",0,Integer.MAX_VALUE-1);
        equalsTotalElementsOfSetVarsOfModel(totalElements).post();
        model.setObjective(Model.MINIMIZE, totalElements);
   }

    private void PTConstraint2ModelConstraint(Constraint c){
        if (c instanceof SubsetOfConstraint){
            PointsToSet superSet=((SubsetOfConstraint) c).getSuperSet();
            PointsToSet subSet=((SubsetOfConstraint) c).getSubSet();

            //SetVar modelSuperSet=model.setVar(superSet.toString(),new int[]{}, Solver.AllLocationsArray);
            //SetVar modelSubSet=model.setVar(subSet.toString(),new int[]{}, Solver.AllLocationsArray);

            SetVar modelSuperSet=getOrCreateModelSetFromPTSet(superSet);
            SetVar modelSubSet=getOrCreateModelSetFromPTSet(subSet);
            model.subsetEq(new SetVar[]{modelSubSet,modelSuperSet}).post();
           return;
        }
        if (c instanceof ElementOfConstraint){
            PointsToSet set=((ElementOfConstraint) c).getSet();
            MemoryLocation m = ((ElementOfConstraint) c).getElement();
            SetVar modelSet= getOrCreateModelSetFromPTSet(set);
            model.member(m.getId(),modelSet).post();
            return;
        }
    }
    private SetVar getOrCreateModelSetFromPTSet(PointsToSet set){
      ///*
        if(set.constraintSolverSet ==null){
            //set.constraintSolverSet = new SolverSetHolder<SetVar>();
            set.constraintSolverSet =model.setVar(set.toString(),new int[]{}, Solver.AllLocationsArray);
        }//*/

       return (SetVar) set.constraintSolverSet;
    }

    private  ReExpression equalsTotalElementsOfSetVarsOfModel(IntVar totalElements){
        SetVar[] setvars =model.retrieveSetVars();
        if(setvars.length ==0){System.out.println("!No setVars in model "+model+"!"); return totalElements.eq(0);}
        ArExpression sum= setvars[0].getCard();
        if(setvars.length ==1) return totalElements.eq(sum);
        for(int i =1;i< setvars.length;i++)
            sum=sum.add(setvars[i].getCard());
        return totalElements.eq(sum);
    }

    public void solve(){
       while(model.getSolver().solve()){
            System.out.println("+++solution found+++");
            Arrays.stream(model.retrieveSetVars()).forEach(System.out::println);
            new Scanner(System.in).nextLine();

       }
    }
}
