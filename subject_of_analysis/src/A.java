public class A {

	A x, y,z,w;
	int fld;
	public A a(A d, int c){
		A x= new A();
		A y= new A();
		z= new A();
		
		w = ( c==1 )? x : y; 

		for(int i =0 ; i <100 ; i++)
			z= new A();	
		z=f(x,y);	
		x.f2(x,x);		
		f2(y,y);
		f2(x,x);
		this.x=this.y;
		this.y=this.x;
		return x;
	}

	public void f2(A p1, A p2){
		A k= new A();
		k.a(new A(), 5);
	}
	public void f2(int p1, int p2){}
	public A f(A a1, A a2){return a1;}
}
