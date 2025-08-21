public class MethodInvocation6{

	public void a(){
	  boolean condition = true;
	  A x=(condition) ? new A() : new A();     //1 and 2 allocated         
    x.primField1=x.primField1;  //reads and writes 1.primField1 , 2.primField1
    sub_a(x);
    sub_a2();
  }
  
  public long sub_a(A param1){  //param1 = {1,2}
    param1.primField2= "Zamenhof"; //writes 1.primField2 , 2.primField2
    return 100;
  }
  
  public void sub_a2(){
    A x = new A();    //3 allocated
    double t = x.primField3;    // reads  3.primField3
    return;
  }
}
