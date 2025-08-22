
# A static, context-insensitive, intra-procedural, may, side effect analysis for java  <br/><br/>

We consider a side effect to be a memory access on the heap. </br>
That is, either a read or a write of some field.

Our [points-to analysis ](/src/main/java/PTAnalysis) gives us a sense of which locals on the stack may contain which objects on the heap.

Using the rules bellow we track which methods read or write which fields of which objects


$$\frac{ }{|p=q.f| \mapsto q.f :: Read \ of \ m}[side\text{-}effect\text{-}read\text{-}statement] \newline \newline$$

$$\frac{ }{|p.f=q| \mapsto p.f :: \  Write \ of \ m}[side\text{-}effect\text{-}write\text{-}statement] \newline \newline$$

$$\frac{ }{|m_2(...)| \mapsto  READS(m) \supseteq \ READS(m_2) \ , \ WRITES(m) \supseteq \ WRITES(m_2) }[side\text{-}effect\text{-}invocation\text{-}value] \newline \newline$$
$$\text{where  m  is  the  enclosing  method}$$
(implemented [here](src/main/java/PTAnalysis/ConstraintGenStmtVisitor.java))

-----
$$\frac{q.f :: Read \ of \ m \ \ \ \ \ l_q \in q}{l_q.f \in READS(m)}[side\text{-}effect\text{-}read\text{-}constraint] \newline \newline$$

$$\frac{p.f :: Write \ of \ m \ \ \ \ \ l_p \in p}{l_p.f \in WRITES(m)}[side\text{-}effect\text{-}write\text{-}constraint] \newline \newline$$
