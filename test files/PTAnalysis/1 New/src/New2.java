public class New2 {

	public void a(A d, int c){
    A x=new A();
  	// prevent variable splitting w/ branching
    if(c==1)x = new A();	
    if(c==2)x = new A();
    if(c==3)x = new A();
    x.use();
  }
}
