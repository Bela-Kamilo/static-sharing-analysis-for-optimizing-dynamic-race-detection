public class MethodInvocation4 {

	public void a(A d, int c){
    A[] x = new A[100];
    A[] y = new A[200];
    x[10]= new A();
    y[20]= new A();
    A.m(x[1],y[2]);
  }
  static void m(A p1, A p2 ){return;} //duplicate, not really called
}
