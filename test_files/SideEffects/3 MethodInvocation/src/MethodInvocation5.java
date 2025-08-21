public class MethodInvocation5{

	public void a(){
	  boolean condition = true;
	  A x=(condition) ? new A() : new A();     //1 and 2 allocated         
    x.primField1=x.primField1;  //reads and writes 1.primField1 , 2.primField1
    sub_a(x);
    
  }
  
  public long sub_a(A param1){  //param1 = {1,2}
    param1.primField2=param1.primField2; //reads and writes 1.primField2 , 2.primField2
    sub_sub_a(param1);
    return 100;
  }
  
  public void sub_sub_a(A param1){ // param1={1,2}
    param1.primField3=param1.primField3;    // reads and writes 1.primfield3 , 2.primField3
    return;
  }
}
