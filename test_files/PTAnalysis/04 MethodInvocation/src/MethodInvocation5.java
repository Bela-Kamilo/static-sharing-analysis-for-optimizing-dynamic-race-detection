public class MethodInvocation5 {

	public void a(A d, int c){
    A[] x = new A[100];
    A[] y = new A[200];
    x[10]= new A();
    y[20]= new A();
    A.mArrays(x,y);
  }
  static void mArrays(A[] p1, A[] p2 ){return;} //duplicate, not really called
}
