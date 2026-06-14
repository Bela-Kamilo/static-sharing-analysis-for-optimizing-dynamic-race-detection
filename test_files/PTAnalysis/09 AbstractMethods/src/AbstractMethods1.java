public class AbstractMethods1 {
//Abstract-this-global rule
	public void a(A d, int c){
    AbstractSingleImpl x = new ConcreteSingleImpl();  //x={1}
    x.doNothing();
  }
  void doNothing(){  //duplicate, not really called
                          //Concr:method.this={1}
                          //Abs:method.this={1}
  } 
}                   
