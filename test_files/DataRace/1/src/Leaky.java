public class Leaky extends Thread{
    public String[] unsafe;
    //this={1} instantiated in UsingLeaky.java
    public Leaky(){
    }
    
    public void run(){
        String[] local =new String[3];
        unsafe=local;   //leak  , writes 1.unsafe
    }
}
