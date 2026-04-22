
public class ConcreteTripleImplB extends AbstractTripleImpl {
  //@Override
  public A method(){ //this={1}
    return new A(); //method.return )={2 or 3 or 4}
  }
  //@Override
  //used in AbstractMethods4
  public void doNothing(){}
}
