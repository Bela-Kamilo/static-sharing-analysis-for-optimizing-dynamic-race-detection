public class AbstractMethods1 {

	public void a(A d, int c){
    AbstractSingleImpl x = new ConcreteSingleImpl();  //x={1}
    x.method("ekzemplo");
  }
  void method(){  //duplicate, not really called
  /*this.field= new ConcreteSingleImpl();   //1.field={2}
    return this.field;    // ConcreteSingleImpl:method={2}
                          // AbstractSingleImple:method={2}
  */
  } 
}                   
