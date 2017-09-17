# Collections

Zana provides eager versions of <code>filter</code>, <code>map</code>, 
<code>reduce</code>, etc., which return immutable, non-persistent 
(and therefore more compact) data structures, usually from 
[<code>java.util</code>]
(https://docs.oracle.com/javase/8/docs/api/java/util/package-summary.html)
or 
[<code>com.guava.collect</code>](https://github.com/google/guava).
Zana's functions also directly accept a wider range of inputs than Clojure's
(usually any <code>Iterable</code>) rather than requiring collections to be 
converted into sequences first.

Zana is part-way thru a migration from hand-coded simple functions to generic
functions (<code>defmulti</code>), which allows user extension by adding new 
methods, and also permits defining, for example, <code>map</code> as an 
operation that returns data structures isomorphic to the input, rather than
converting everything to a sequence and returning a sequence, which is then
most often stuffed back into a data structure of the original type.

