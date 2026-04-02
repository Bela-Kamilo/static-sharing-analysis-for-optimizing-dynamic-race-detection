public class Other2 {

	public void a(A d, int c){
    A x = c==0? new A(): new A();
    A y = c==0? new A(): new A();
    x.f =y;
    y.f = x;
  }
}
