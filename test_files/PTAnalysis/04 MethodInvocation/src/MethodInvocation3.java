public class MethodInvocation3 {

	public void a(A d, int c){
    A x = c==0? new A(): new A();
    A y = c==0? new A(): new A();
    y.m2(x,y);
  }
  void m2(A p1, A p2 ){return;} //duplicate, not really called
}
