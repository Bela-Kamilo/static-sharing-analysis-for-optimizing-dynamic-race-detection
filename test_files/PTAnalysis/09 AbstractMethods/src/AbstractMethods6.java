public class AbstractMethods6 {
//Abstract parameters rule
	public void a(A d, int c){
    ConcreteTripleImplA A = new ConcreteTripleImplA(); //A={1}
    ConcreteTripleImplB B = new ConcreteTripleImplB();  //B={2}
    AbstractTripleImpl x = new ConcreteTripleImplC();  //x={3}
    A four = new A();  //four={4}  
    A five = new A();  //five={5} 
    A.doNothing(four, 1 ,'c',five); //A.this(={1}  A.1(={4} A.4(={5}
    B.doNothing(five, 2 ,'d',four); //B.this(={2}  B.1(={5} B.4(={4}
    x.doNothing(new A(), 3, 'e', new A()); //Abstract.this={3}
                //A.this={1,3} A.1={4,6} A.4={5,7}
                //B.this={2,3} B.1={5,6} B.4={4,7}
                //C.this={3}  C.1={6} C.4={7}
    }    
    void doNothing() {} //duplicate, not really called
}
