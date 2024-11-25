public class A {

	A x, y,z,w;

	public int a(int c){
		A x= new A();
		A y= new A();
		A z= new A();
		
		w = ( c==1 )? x : y; 
		return c;
	}

}
