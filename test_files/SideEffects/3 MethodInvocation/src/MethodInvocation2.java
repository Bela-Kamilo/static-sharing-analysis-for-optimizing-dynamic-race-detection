public class MethodInvocation2 {

	public void a(){
    sub_a(1);
    A x = new A();
    x.primField1 = 20;            //1.primField1 written
  }
  
 public String sub_a(int param1){
    A x = new A();            
    x.primField1=10;          //2.primField1 written
    x.primField2="10";      //2.primField2 written
    x.primField3=10.0;      //2.primField3 written
    return null;
  }
}
