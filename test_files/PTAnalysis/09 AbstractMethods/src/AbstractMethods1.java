public class AbstractMethods1 {

	public void a(A d, int c){
    AbstractSingleImpl x = new ConcreteSingleImpl();
    x.method();
  }
  void method(){/*this.field= new ConcreteSingleImpl();*/ } //duplicate, not really called
}
