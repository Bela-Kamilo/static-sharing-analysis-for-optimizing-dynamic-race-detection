package test;

public abstract class Test {
    protected static int testsCount=0;
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    protected void pass(String testname){System.out.println("TEST "+testname+": "+ANSI_GREEN+"PASS"+ANSI_RESET);}
    protected void fail(String testname){System.out.println("TEST "+testname+": "+ANSI_RED+"FAIL"+ANSI_RESET);}
    public int getCount(){return testsCount;}

    abstract public void test();

}
