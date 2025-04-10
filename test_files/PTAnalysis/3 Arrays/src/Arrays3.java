public class Arrays3 {

	public void a(A d, int c){
    A[] x = new A[5];
    x[1]= new A();
    x[2]= new A();     //we dont keep track of these
    A[] y =x;
    y = new A[10];
    y[3]= new A();
  }
}
