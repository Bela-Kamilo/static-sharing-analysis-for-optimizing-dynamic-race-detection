public class Other5 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    int z=5;
    int res = x.m(x,y,z);
  }
  int m(A p1, A p2, int p3 ){return p3;}  //duplicate, not really called

}
