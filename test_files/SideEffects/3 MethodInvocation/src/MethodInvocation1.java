public class MethodInvocation1{

	public void a(){
    A x = new A();    //1 allocated
    sub_a(x);
    x = new A();    //2 allocated
    A y =x.f;     //2.f read
  }
  
  public long sub_a(A param1){
    A y = param1.f;   //1.f read
    A z = new A().f;  //3.f read
    return 2;
  }
}
