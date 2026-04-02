public class MethodInvocation6 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    String strngLocal="string local";
    x.m6("first string");
    y.m6("second string");
  }
  public void m6(String s ){return;} //duplicate, not really called
}
