
public class AbstractMethods3 {

	public void a(A d, int c){
    AbstractTripleImplField x = new ConcreteTripleImplFieldA();  //x={1}
    x.method(); //the 3 implementations of AbstractTripleImpl will make 1.field={2,3,4}
  }
  void method(){/*this.field= new A;*/ } //duplicate, not really called
}
