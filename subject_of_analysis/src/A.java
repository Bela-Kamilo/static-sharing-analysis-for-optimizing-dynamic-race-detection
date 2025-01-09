public class A {

	A x, y,z,w;
	int fld;
	public A a(A d, int c){
		A ekz= new A();
		A ekz2 = new A();
		ekz.x =ekz2;
		ekz.x=ekz;
		//ekz2=ekz.x;	prolvima
		return ekz;
	}
}
