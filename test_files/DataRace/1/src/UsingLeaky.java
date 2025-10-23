public class UsingLeaky {

    public static void main(String[] args){
        Leaky t = new Leaky();  //t={1}
        LeakyToo t2 = new LeakyToo("ESPERANTO"); // t2={2}
        t.run();//this thread writes 1.unsafe
        t2.run();//                  2.unsafe;
        String[] x =t.unsafe;   //reads 1.unsafe
        //System.out.println(t.genericThing+1);
    }
}
