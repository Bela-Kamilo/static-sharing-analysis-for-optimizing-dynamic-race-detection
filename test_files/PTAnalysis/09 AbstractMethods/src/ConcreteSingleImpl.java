public class ConcreteSingleImpl extends AbstractSingleImpl {
  //@Override
  public ConcreteSingleImpl method(){
    this.field= new ConcreteSingleImpl();
    return this.field;
  }
}
