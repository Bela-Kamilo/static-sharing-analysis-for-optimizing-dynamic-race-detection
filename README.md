
# A static, context-insensitive, intra-procedural, set-based, may, points-to analysis for java based on Andersen's analysis for C <br/><br/>
_______________________
Using the rules bellow , a Points To Set is created and monitored for each variable, parameter, non void method and instance field

<br/>

$$ \frac{}{\left\| p= new\text{ } A_i() \right\| \mapsto l_i \in p }[new \text{-}assignment\text{-} statement] $$

$$\frac{}{\left\| p= q \right\| \mapsto p \supseteq q }[copy\text{-}statement] $$

$$\frac{}{\left\| i.m(a_1 ... a_n) \right\| \mapsto m.this \supseteq i,\ p_j \supseteq a_j }[method \text{-} invocation \text{-}value ] $$

$$\text {where }  p_j \text { are } m \text{'s parameters}$$

$$\frac{}{\left\| q= i.m(a_1 ... a_n) \right\| \mapsto q \supseteq m}[method \text{-} assignment\text{-}statement] $$

$$\frac{}{\left\| return \  p \right\| \mapsto m \supseteq p }[return\text{-}statement ]$$

$$ \text {where } m \text { is the enclosing method }  \newline $$

$$array \ elements\ are \ lumped \ together, \ that \ is \ we \ treat \  ar[i]...[j] \ as \ if
\ it \ were \ ar[i] $$
$$as \ if \ it \ were \ ar[j] \ mapped \ to \ a \   single \ points \ to   \ set$$
lumpArrays
_______________________
$$\frac{p \supseteq q \ \ \ \ \ l_x \in q}{l_x \in p}[superset] $$
$$\newline\newline\newline$$
___
$$FIELD \ SENSITIVITY $$

$$\frac{}{\left\|   p= q.f \right\| \mapsto p \supseteq q.f }[field\text-read\text{-} assignment\text{-} statement] \newline \newline$$
$$\frac{}{\left\|   p.f= q \right\| \mapsto p.f \supseteq q }[field\text-assign\text{-} assignment\text{-} statement] \newline$$
___
$$\frac{p \supseteq q.f \ \ \ \ \ l_q \in q \ \ \ \ \ l_f \in l_q.f}{l_f \in p }[field\text-read] \newline \newline$$
$$\frac{p.f \supseteq q \ \ \ \ \ l_p \in p \ \ \ \ \ l_q \in q}{l_q \in l_p.f }[field\text-assign] \newline \newline$$
___
\
\
sample execution :


<table>
<tr> <th> Java</th> <th> Jimple</th> </tr>
<tr>
<td>

```java 
public class A {
    
            A x , y;
            int fld;
            public A a(A d, int c){
                    A one= new A();     //memory location 1 is supposedly
                                        //created here
                    A two= new A();     // memory location 2 etc
                    A oneAndTwo;
    
                    oneAndTwo = ( c==1 )? one : two;
    
                    oneAndTwo.x=one.f(two,two);
                    return oneAndTwo;
            }
    
            public A f(A a1, A a2){
                    A three = new A();
                    three.x=new A();
                    return three.x;}
    }
 
```
</td>
<td>

```jimple
<A: A a(A,int)>
 27 {
 28     A $stack6, $stack7, $stack8, l1, l3, l4, l5, this;
 29     int l2;
 30 
 31 
 32     this := @this: A;
 33     l1 := @parameter0: A;
 34     l2 := @parameter1: int;
 35     $stack6 = new A;
 36     specialinvoke $stack6.<A: void <init>()>();
 37     l3 = $stack6;
 38     $stack7 = new A;
 39     specialinvoke $stack7.<A: void <init>()>();
 40     l4 = $stack7;
 41 
 42     if l2 != 1 goto label1;
 43     l5 = $stack6;
 44 
 45     goto label2;
 46 
 47   label1:
 48     l5 = $stack7;
 49 
 50   label2:
 51     $stack8 = virtualinvoke l3.<A: A f(A,A)>($stack7, $stack7);
 52     l5.<A: A x> = $stack8;
 53 
 54     return l5;
 55 }
 56 

<A: A f(A,A)>
 58 {
 59     A $stack4, $stack5, $stack6, l1, l2, l3, this;
 60 
 61 
 62     this := @this: A;
 63     l1 := @parameter0: A;
 64     l2 := @parameter1: A;
 65     $stack4 = new A;
 66     specialinvoke $stack4.<A: void <init>()>();
 67     l3 = $stack4;
 68     $stack5 = new A;
 69     specialinvoke $stack5.<A: void <init>()>();
 70     $stack4.<A: A x> = $stack5;
 71     $stack6 = $stack4.<A: A x>;
 72 
 73     return $stack6;
 74 }

```

</td>
</tr>
</table>

we see corresponding variables are as such :


| Java               | Jimple         |
|--------------------|----------------|
| ``` A one```       | ``` a: A l3``` |
| ``` A two```       | ``` a: A l4``` |
| ``` A oneAndTwo``` | ``` a: A l5``` |
| ```  A a1```       | ``` f: A l1``` |
| ```  A a2```       | ``` f: A l2``` |
| ```  A three```    | ``` f: A l3``` |

We get such results :

```
<A: A f(A,A)> = {4}          % A f(A,A) may return only the location 4
<A: A f(A,A)>:$stack6 = {4}
<A: A f(A,A)>:$stack4 = {3}
<A: A f(A,A)>.this = {1}
<A: A a(A,int)>:l3 = {1}
<A: A a(A,int)>:this = {}
<A: A a(A,int)>.this = {}
<A: A f(A,A)>:$stack5 = {4}
<A: A a(A,int)>:l5 = {1, 2}
<A: A a(A,int)>:$stack6 = {1}
<A: void <init>()>:this = {}
<A: void <init>()>.this = {}
<A: A f(A,A)>:l3 = {3}
<A: A a(A,int)>:$stack8 = {4}
<A: A f(A,A)>:this = {1}
<A: A f(A,A)>:l1 = {2}
<A: A f(A,A)>.1 = {2}           % the first argument of f(A,A) may contain location 2
<A: A a(A,int)>:l1 = {}
<A: A a(A,int)>.1 = {}
<A: A a(A,int)>:$stack7 = {2}
<A: A a(A,int)>:l4 = {2}
<A: A a(A,int)> = {1, 2}
<A: A f(A,A)>:l2 = {2}
<A: A f(A,A)>.2 = {2}
3.<A: A x> = {4}                % location 3 may hold location4 in its x field
1.<A: A x> = {4}
2.<A: A x> = {4}
````
# Use

------
Call ``` Map<String, Set<Integer>> analise(SootMethod)``` located in ```PointsToAnalysis.java```

see [retrieving a Sootmethod](https://soot-oss.github.io/SootUp/latest/getting-started/)</br>

The analysis is performed on [Jimple IR](https://soot-oss.github.io/SootUp/latest/jimple/)
and so the mapping is from Jimple value holders to (sets of) abstract locations

This project uses and relies on the  [SootUp framework](https://github.com/soot-oss/SootUp)
and [Choco-solver](https://github.com/chocoteam/choco-solver)
