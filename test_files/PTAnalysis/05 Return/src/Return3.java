public class Return3 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    y.m(x,y);
  }
  A m(A p1, A p2 ){return p2;}  //duplicate, not really called
}
