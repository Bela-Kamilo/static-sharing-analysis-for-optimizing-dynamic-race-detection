public class Return1 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    A.m1(x,y);
  }
  static A m1(A p1, A p2 ){return new A();}  //duplicate, not really called
}
