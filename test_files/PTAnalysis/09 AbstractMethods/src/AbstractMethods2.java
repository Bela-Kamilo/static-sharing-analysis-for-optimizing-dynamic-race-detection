public class AbstractMethods2 {

	public void a(A d, int c){
    AbstractTripleImpl x = new ConcreteTripleImplA();  //x={1}
    x.method(); //the 3 implementations of AbstractTripleImpl will make
                //AbstractTripleImpl:method={2,3,4}
  }
  A method(){ return new A(); } //duplicate, not really called
}
