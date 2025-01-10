$$ \frac{}{\left\| p= new\text{ } A_i() \right\| \mapsto l_i \in p }[new] $$

$$\frac{}{\left\| p= q \right\| \mapsto p \supseteq q }[copy] $$

$$\frac{}{\left\| i.m(a_1 ... a_n) \right\| \mapsto m.this \supseteq i,\ p_j \supseteq a_j }[method \text{-} invocation] $$

$$\text {where }  p_j \text { are } m \text{'s parameters}$$

$$\frac{}{\left\| q= i.m(a_1 ... a_n) \right\| \mapsto q \supseteq m, \ m.this \supseteq i,\ p_j \supseteq a_j }[method \text{-} assignment] $$

$$\frac{}{\left\| return \  p \right\| \mapsto m \supseteq p }[return]$$

$$ \text {where } m \text { is the enclosing method }  \newline $$
$$-------------------------- \newline$$
$$\frac{p \supseteq q \ \ \ \ \ l_x \in q}{l_x \in p}[superset] $$
$$\newline\newline\newline$$

$$FIELD \ SENSITIVITY $$\
$$\frac{}{\left\|   p= q.f \right\| \mapsto p \supseteq q.f }[field\text-read] \newline \newline$$
$$\frac{}{\left\|   p.f= q \right\| \mapsto p.f \supseteq q }[field\text-assign] \newline$$
$$-----------------------------$$
$$\frac{p \supseteq q.f \ \ \ \ \ l_q \in q \ \ \ \ \ l_f \in l_q.f}{l_f \in p }[field\text-read] \newline \newline$$
$$\frac{p.f \supseteq q \ \ \ \ \ l_p \in p \ \ \ \ \ l_q \in q}{l_q \in l_p.f }[field\text-assign] \newline \newline$$