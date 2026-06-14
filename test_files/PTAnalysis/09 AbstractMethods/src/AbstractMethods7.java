public class AbstractMethods7 {
//Abstract-this-global rule
//Abstract-return-global rule
//writting to abs class field
	public void a(A d, int c){
    AbstractSingleImpl x = new ConcreteSingleImpl();  //x={1}
    x.method("ekzemplo");
  }
  void method(String s){  //duplicate, not really called
                          //Concr:method.this={1}
                          //Abs:method.this={1}
  /*this.field= new A();  //1.field={2}
    return this.field;    // ConcreteSingleImpl:method={2}
                          // AbstractSingleImple:method={2}
  */
  } 
}                   
