public class AbstractMethods5 {
//Abstract-return-global rule
	public void a(A d, int c){
    AbstractTripleImpl x = new ConcreteTripleImplA();  //x={1}
    x.method(); //the 3 implementations of AbstractTripleImpl will make
                //AbstractTripleImpl:method={2,3,4}
                //ConcreteA:method={2}
                //ConcreteB:method={3}
                //ConcreteC:method={4}
                //but also
                //ConcreteA:method.this={1}
                //ConcreteB:method={1}  <- this is impossible to ever be true however
                //ConcreteC:method={1}  <- this too
  }
  A method(){ return new A(); } //duplicate, not really called
}
