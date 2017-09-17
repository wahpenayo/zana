package zana.java.accumulator;

//----------------------------------------------------------------------------
/**
 * An <code>Accumulator</code> provides logic for high performance reductions of
 * weighted primitive values to a single <code>double</code> value, for example,
 * computing the mean of an array of weighted numbers.
 * <p>
 * Accumulators have state, which allows the value of the reduction to be
 * updated efficiently when adding or removing new inputs.
 * <p>
 * Accumulators should be optimized for speed, and <strong>NOT</strong>
 * thread-safety. They should be used so they are only visible to a single
 * thread.
 * <p>
 * Two motivating applications:
 * <ol>
 * <li>Optimizing the split predicate in greedy decision tree
 * growth, where fast updating and down-dating is critical to performance.
 * <li>Applying an 'additive' model, represented by a collection of term
 * functions, to a single datum.
 * </ol>
 * <p>
 * The basic operation is to add or delete a single weighted primitive number.
 * The weight must be a non-negative number, usually <code>double</code>.
 * Unweighted add or delete must be equivalent to adding or deleting with weight
 * 1.0.
 * <p>
 * Other operations update the value by adding or deleting arrays or collections
 * of numbers.
 * <p>
 * Accumulators support efficient 'map-reduce' updating operations of two basic
 * kinds, corresponding to the motivating applications above:
 * <ol>
 * <li>Add/delete with a single value function, weight function, and a data
 * array/collection. The value and weight functions are applied to
 * every element of the data, and the accumulator is updated with those
 * primitives.
 * <li>Add/delete with collections/arrays of value and weight functions,
 * and a single 'datum'. Each element of the weight and value functions is
 * applied to the datum, and the accumulator is updated with the result.
 * </ol>
 * <p>
 * Note that nothing ensures consistency --- weight-value pairs may be deleted
 * that were never added.
 *  <p>
 *  <em>TODO:</em> Reduce the number of methods in the interface to a minimum,
 *  let implementing classes handle special cases themselves.
 *  
 *  
 * @author John Alan McDonald
 * @version 2017-01-04
 */

public interface  

Accumulator {

  /** Reset the accumulator to an empty state.
   */
  void clear ();

  /** The weight that has been added minus the weght that has been deleted. 
   * It's possible this will not be the
   * same value you would get by summing the weights in the calls to
   * <code>add</code> and <code>deleted</code>, 
   * because an accumulator may filter its inputs.
   */
  double netWeight ();

  /** The number of adds minus the number of deletes. 
   * It's possible this will not be the
   * same value you would get by counting adds and deletes, 
   * because an accumulator may filter its inputs.
   */
  long netCount ();

  /** The current accumulated value.
   */
  Object value ();

  /** The current accumulated value.
   */
  double doubleValue ();

  /** Minimum possible doubleValue().
   */
  double minimum ();

  /** Maximum possible doubleValue().
   */
  double maximum ();

  //----------------------------------------------------------------------------
  /** Update the state of the accumulator by adding <code>z</code> with 
   * weight <code>w</code>.
   */
  void add (final double z, 
            final double w);

  /** Update the state of the accumulator by adding <code>z</code> with 
   * weight <code>1.0</code>.
   */
  void add (final double z);

  /** Update the state of the accumulator by adding the <code>zs</code> with 
   * all weights <code>1.0</code>.
   */
  void add (final Object z);

  //----------------------------------------------------------------------------
  /** Update the state of the accumulator by deleting the <code>z</code> with 
   * weight <code>w</code>.
   */
  void delete (final double z, 
               final double w);

  /** Update the state of the accumulator by deleting <code>z</code> with 
   * weight <code>1.0</code>.
   */
  void delete (final double z);

  /** Update the state of the accumulator by deleting the <code>zs</code> with 
   * all weights <code>1.0</code>.
   */
  void delete (final Object z);

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
