public class MethodAssignment1 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    A res = A.m(x,y);
  }
  static A m(A p1, A p2 ){return p2;} //duplicate, not really used
}
