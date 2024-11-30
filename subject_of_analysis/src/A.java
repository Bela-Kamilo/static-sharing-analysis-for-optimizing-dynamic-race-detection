public class A {

	A x, y,z,w;
	int fld;
	public int a(int c){
		A x= new A();
		A y= new A();
		A z= new A();
		
		w = ( c==1 )? x : y; 
	
		this.z= f (x,y);	
		f2(c,c);		

		return c;
	}

	public void f2(A p1, A p2){};
	public void f2(int p1, int p2){};
	public A f(A a1, A a2){return a1;}
}
