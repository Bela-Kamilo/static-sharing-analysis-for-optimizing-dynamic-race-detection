public class UsingLeaky {

    public static void main(String[] args){
        Leaky t = new Leaky();  //t={1}
        t.run();//this thread writes 1.unsafe
        String[] x =t.unsafe;   //reads 1.unsafe
    }
}
