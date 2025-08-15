public class Read2{

	public void a(){
    boolean condition= true;
    A x=(condition) ? new A() : new A();     //1 and 2 allocated
    A y =x.f;     //1.f ,2.f read
  }
  
}
