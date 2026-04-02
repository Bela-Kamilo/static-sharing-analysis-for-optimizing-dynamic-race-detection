public class Return5 {

	public void a(A d, int c){
    A x = new A();
    A[] y = new A[20];
    y[2]= new A();
    y[3]= new A();
    A.m2Arrays(x,y);
  }
  static A[] m2Arrays(A p1, A[] p2 ){return p2;} //duplicate, not really called
}
