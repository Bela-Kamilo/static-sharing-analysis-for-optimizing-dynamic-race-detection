public class Other1 {

	public void a(A d, int c){
    A x = c==0? new A(): new A();
    A y = c==0? new A(): new A();
    x.f = new A();
    y.f = c==0? x.f: new A();

  }
}
