public class Return4 {

	public void a(A d, int c){
    A x = new A();
    A[] y = new A[20];
    y[2]= new A();
    A.m2(x,y[2]);
  }
  static A m2(A p1, A p2 ){return p2;} //duplicate, not really called
}
