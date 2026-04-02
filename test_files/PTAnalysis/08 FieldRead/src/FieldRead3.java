public class FieldRead3 {

	public void a(A d, int c){
    A x = new A();	
    x.f = new A();
    A y =c==0? new A() : x.f;
    y.use();
  }
}
