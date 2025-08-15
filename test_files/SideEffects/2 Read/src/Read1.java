public class Read1{

	public void a(){
    A x = new A();        //1 allocated
    //x.f = new A();
    //x.f = new A();
    A y = x.f;          //1.f read
  }
}
