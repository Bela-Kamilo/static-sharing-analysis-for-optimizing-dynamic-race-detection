public class MethodInvocation1 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    A.m(x,y);
  }
  static void m(A p1, A p2 ){return;} //duplicate, not really called
}
