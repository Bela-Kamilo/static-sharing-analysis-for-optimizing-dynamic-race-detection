public class Copy3 {

	public void a(A d, int c){
    A x = new A();
    A y = new A();
    A z = c==1 ? x : y;
    z.use();	//prevent z from splitting
		          //in jimple
  }
}
