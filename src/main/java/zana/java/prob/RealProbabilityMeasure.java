package zana.java.prob;

import clojure.lang.IFn;

/** Probability measure on <b>R</b> (actually on any set of Java
 * numbers).
 * <p>
 * TODO: should this be <code>DoubleProbabilityMeasure</code>?
 * 
 * @author wahpenayo at gmail dot com
 * @since 2017-11-01
 * @version 2017-11-01
 */
@SuppressWarnings("unchecked")
public interface RealProbabilityMeasure 

extends ProbabilityMeasure {

  /** Use {@link Double#TYPE} until there's something representing
   * <b>R</b>.
   */
  
  @Override
  public default Object sampleSpace () { return Double.TYPE; }
  
  /** Probability of the half line &mu;((-&infin;,z]).
   * 
   * @throw UnsupportedOperationException if not implemented
   */
  double cdf (double z);
  
  /** Pseudo inverse of <code>cdf</code>.
  * <code>(quantile p)</code> is infimum of the <code>z</code
  * where <code>p <= cdf(z)</code>.
  * Must have <code>0.0 <= p <= 1.0</code>.
  * May return +/- &infin;.
   * 
   * @throw UnsupportedOperationException if not implemented
  */ 
  double quantile (double p);
  
  /** What probability is assigned to the single point 
   * <code>x</code>?
   * 
   * @throw UnsupportedOperationException if not implemented
   */
  double pointmass (double z);

  /** Returns a function of no arguments that returns 
   * 'independent' doubles when called.
   * 
   * @param seed depends on the sampling strategy, probably a 
   * seed for an underlying 
   * <a href="https://maths.uncommons.org/api/org/uncommons/maths/random/MersenneTwisterRNG.html>
   * Mersenne Twister</a>.
   * 
   * @throw UnsupportedOperationException if not implemented
   */
  IFn.D doubleSampler (Object seed);

}
