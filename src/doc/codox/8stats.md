# Stats

## zana.stats.accumulators

Factory functions for classes implementing <code>zana.java.stats.accumulators.Accumulator</code>:

An <code>Accumulator</code> provides logic for high performance and numerically stable reductions of
weighted primitive values to a single <code>double</code> value, for example,
computing the mean of an array of weighted numbers.
<p>
Accumulators have state, which allows the value of the reduction to be
updated efficiently when adding or removing new inputs.
<p>
Accumulators should be optimized for speed and/or floating point accuracy, and <strong>NOT</strong>
thread-safety. They should be used so they are only visible to a single
thread.
<p>
Two motivating applications:
<ol>
<li>Optimizing the split predicate in greedy decision tree
growth, where fast updating and down-dating is critical to performance.
<li>Applying an 'additive' model, represented by a collection of term
functions, to a single datum.
</ol>
<p>
The basic operation is to add or delete a single weighted primitive number.
The weight must be a non-negative number, usually <code>double</code>.
Unweighted add or delete must be equivalent to adding or deleting with weight 1.0.
<p>
Other operations update the value by adding or deleting arrays or collections
of numbers.
<p>
Accumulators support efficient 'map-reduce' updating operations of two basic
kinds, corresponding to the motivating applications above:
<ul>
<li>Add/delete with a single value function, weight function, and a data
array/collection. The value and weight functions are applied to
every element of the data, and the accumulator is updated with those
primitives.
<li>Add/delete with collections/arrays of value and weight functions,
and a single 'datum'. Each element of the weight and value functions is
applied to the datum, and the accumulator is updated with the result.
</ul>
<p>
<em>Note:</em> Nothing ensures consistency --- weight-value pairs may be deleted that were never added.

## zana.stats.prng

Pseudo-random number generators plus a little truly random seeds for those generators, based primarily on <a href="http://maths.uncommons.org/">Uncommons Maths</a>.

Functions using pseudo-random number generators to sample from collections.

## zana.stats.statistics

A mixed bag of functions that probably ought to be broken up, and turned into generic functions (<code>defmulti</code>) for easier extension. The integration with and difference from accumulators needs to be rationalized.

Most of the functions compute 'statistics' over 'data sets', both 
loosely defined. <br>
Most of the 'statistics' are either <code>boolean</code> or <code>double</code> valued.<br>
Most of the functions can be applied to <code>double</code> arrays, <code>Iterable</code> 'data sets', and a combination of one or more <code>double</code>-valued attribute functions and an <code>Iterable</code> 'data set'. 