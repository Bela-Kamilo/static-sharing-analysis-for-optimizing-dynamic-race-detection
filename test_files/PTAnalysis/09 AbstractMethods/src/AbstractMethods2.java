public class AbstractMethods2 {
//Abstract-this-global rule  
	public void a(A d, int c){
    AbstractTripleImpl x = new ConcreteTripleImplA();  //x={1}
    x.doNothing(); //the 3 implementations of AbstractTripleImpl will make
                //AbstractTripleImpl:method.this={1}
                //ConcreteA:method.this={1}
                //ConcreteB:method.this={1} <- this is illegal java
                //ConcreteC:method.this={1} <- this is illegal java
  }
  void doNothing(){ } //duplicate, not really called
}
