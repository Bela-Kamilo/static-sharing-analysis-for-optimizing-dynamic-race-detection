public class Read1{

	public void a(){
    A x = new A();        //1 allocated
    A y = x.f;          //1.f read
  }
}
