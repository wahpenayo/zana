package zana.java.prob;

import clojure.lang.IFn;

/** General probability measure interface.
 * <p>
 * <i>TODO:</i> implement clojure.lang.IFn.OD?
 *
 * @author wahpenayo at gmail dot com
 * @since 2017-11-01
 * @version 2017-11-01
 */
@SuppressWarnings("unchecked")
public interface ProbabilityMeasure {

  /** The domain of a probability measure is a set of subsets
   * of the <code>sampleSpace</code>.
   */
  
  Object sampleSpace ();
  
  /** What probability is assigned to <code>set</code>,
   * a subset of the {@link sampleSpace()}?
   * 
   * @throw UnsupportedOperationException if not implemented
   */
  double probability (Object set);

  /** Returns a function of no arguments that returns 
   * 'independent' elements of the {@link smapleSpace} when
   * called.
   * @param seed depends on the sampling strategy, probably a 
   * seed for an underlying 
   * <a href="https://maths.uncommons.org/api/org/uncommons/maths/random/MersenneTwisterRNG.html>
   * Mersenne Twister</a>.
   * 
   * @throw UnsupportedOperationException if not implemented
   */
  IFn sampler (Object seed);

}
