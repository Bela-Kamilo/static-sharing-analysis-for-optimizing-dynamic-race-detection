public class A {
  static A m1(A p1, A p2 ){return new A();} //called in Return1.java
  static A m2(A p1, A p2 ){return p2;}  //called in Return2.java
  A m(A p1, A p2 ){return p2;}  //called in Return3.java
}
