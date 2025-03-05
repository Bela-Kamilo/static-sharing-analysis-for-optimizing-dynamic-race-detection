public class MethodAssignment2 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    A res = x.m1(x,y);
  }
  A m(A p1, A p2 ){return new A();} //duplicate, not called
}
