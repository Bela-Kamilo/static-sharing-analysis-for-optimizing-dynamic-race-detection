public class A {

	private static int a(int n){
		int x,y,i;
		x=0;
		y=1;
		i=1;
		while(i < n){
		y=x+y;
		x=y -x;
		i= i+1;
		}	
		b();
		return y;
	}
	
	public static void b(){
		c();
		return;
	}

	public static void c(){
		System.out.println("hello world");
		return;
	}

}
