public class AbstractMethods4 {
//abstract-this-global rule
	public void a(A d, int c){
    ConcreteTripleImplA A = new ConcreteTripleImplA(); //A={1}
    ConcreteTripleImplB B = new ConcreteTripleImplB();  //B={2}
    AbstractTripleImpl x = new ConcreteTripleImplC();  //x={3}
    A.doNothing(); //A.this(={1}
    B.doNothing(); //B.this(={2}
    x.doNothing(); //Abstract.this={3}
                //A.this={1,3}
                //B.this={2,3}
                //C.this={3}
    }    
    void doNothing() {} //duplicate, not really called
}
