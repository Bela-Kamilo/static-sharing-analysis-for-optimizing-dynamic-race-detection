
public class ConcreteTripleImplFieldB extends AbstractTripleImplField {
  //@Override
  public void method(){ //this={1}
    this.field= new A(); //1.field )={2 or 3 or 4}
  }
}
