public class A {

	A x , y;
	static A stat;
	int fld;
	public A a(A d, int c){
		A one= new A();
		A two= new A();
		A oneAndTwo;
	//	stat= new A();
	//	A.statMethod(stat);

		oneAndTwo = ( c==1 )? one : two;

		oneAndTwo.x=one.f(two,two);
		return oneAndTwo;
	}

	public A f(A a1, A a2){
		A three = new A();
		three.x=new A();
		return three.x;}

	private static void statMethod(A ax){
		ax.x=null;
	}


}
