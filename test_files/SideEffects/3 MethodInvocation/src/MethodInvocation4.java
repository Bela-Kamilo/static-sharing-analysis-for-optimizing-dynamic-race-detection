public class MethodInvocation4{

	public void a(){
	  boolean condition = true;
	  A x=(condition) ? new A() : new A();     //1 and 2 allocated         
    x.primField1=10;          //writes 1.primField1 , 2.primField1
    x.primField2="10";      //writes 1.primField2 , 2.primField2
    x.primField3=10.0;    // writes 1.primfield3 , 2.primField3
	
    int x1 =x.primField1;          //reads 1.primField1 , 2.primField1
    double x3 =x.primField3;    // reads 1.primfield2 , 2.primField3
    sub_a(x);
    
  }
  
  public long sub_a(A param1){  //param1 = {1,2}
    String x2=param1.primField2;      //reads 1.primField2 , 2.primField2
    a();
    return 2;
  }
}
