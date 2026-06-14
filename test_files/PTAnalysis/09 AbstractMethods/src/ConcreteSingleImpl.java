
public class ConcreteSingleImpl extends AbstractSingleImpl {
  //@Override
  public ConcreteSingleImpl method(String parameter ){ //this={1}
    this.field= new ConcreteSingleImpl(); //1.field={2}
    return this.field;    //ConcreteSingleImple:method()={2}
                          //will also result in AbstractSingleImpl:method()={2}
  }
  public void doNothing(){};
  public A methodReturns(){return new A();}
}
