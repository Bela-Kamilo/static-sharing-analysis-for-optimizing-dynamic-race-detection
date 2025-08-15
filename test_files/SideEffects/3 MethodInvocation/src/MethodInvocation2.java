public class MethodInvocation2 {

	public void a(){
    sub_a(1);
    A x = new A();
    x.primField1 = 20;            //writes 1.primField1
  }
  
 public String sub_a(int param1){
    A x = new A();            
    x.primField1=10;          //writes 2.primField1
    x.primField2="10";      //writes 2.primField2
    x.primField3=10.0;      //writes 2.primField3
    return null;
  }
}
