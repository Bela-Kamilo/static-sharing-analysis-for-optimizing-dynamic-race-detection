public class Return2 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    A.m2(x,y);
  }
  static A m2(A p1, A p2 ){return p2;} //duplicate, not really called
}
