public class MethodInvocation2 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    x.m1(x,y);
  }
  void m1(A p1, A p2 ){return;} //duplicate, not really called
}
