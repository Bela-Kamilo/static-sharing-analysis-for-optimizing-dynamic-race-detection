public class A {

	A x , y;
	int fld;
	public A a(A d, int c){
		A one= new A();
		A two= new A();
		A oneAndTwo;

		oneAndTwo = ( c==1 )? one : two;

		oneAndTwo.x=one.f(two,two);
		return oneAndTwo;
	}

	public A f(A a1, A a2){
		A three = new A();
		three.x=new A();
		return three.x;}
}
