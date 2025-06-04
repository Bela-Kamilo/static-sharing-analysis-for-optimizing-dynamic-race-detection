package util;
 public class Tuple<T1,T2>{
   private T1 elem1=null;
   private T2 elem2=null;

   public Tuple(T1 e1, T2 e2){
        elem1=e1;
        elem2=e2;
   }
  /*
   public Tuple(){
         elem1=null;
         elem2=null;
   }

   public void setElem1(T1 elem1) {
       this.elem1 = elem1;
   }

   public void setElem2(T2 elem2) {
       this.elem2 = elem2;
   }
*/
   public T1 getElem1() {
       return elem1;
   }

   public T2 getElem2() {
       return elem2;
   }
 }
