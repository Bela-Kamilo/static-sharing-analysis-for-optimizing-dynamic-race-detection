public class Leaky extends Thread{
    //this={1} instantiated in UsingLeaky.java
    public Leaky(){
      System.out.println("Leaky constructor called");
    }
    String[] unsafe;
    public void run(){
        String[] local =new String[3];
        unsafe=local;   //leak  , writes 1.unsafe
    }
}
