package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Base for binary classification accumulators.
 *
 * @author John Alan McDonald
 * @version 2017-01-03
 */

public abstract class BinaryAccumulatorBase extends AccumulatorBase {

  //----------------------------------------------------------------------------
  // slots
  //----------------------------------------------------------------------------
  /** Mutable! Not thread safe!
   */

  private double _positiveWeight;

  /** Mutable! Not thread safe!
   */

  private double _positiveCorrection;

  public final double positiveWeight () { return _positiveWeight; }

  
  /** See <a href=https://en.wikipedia.org/wiki/Kahan_summation_algorithm>
   * Kahan summation algorithm</a>.
   */
  private final void incrementPositiveWeight (final double w) { 
    assert w >= 0.0 : "adding negative weight: " + w;
    
    final double w0 = w - _positiveCorrection;
    final double w1 = _positiveWeight + w0;
    _positiveCorrection = (w1 - _positiveWeight) - w0;
    _positiveWeight = w1; }

  
  /** See <a href=https://en.wikipedia.org/wiki/Kahan_summation_algorithm>
   * Kahan summation algorithm</a>.
   */
 private final void decrementPositiveWeight (final double w) {
    assert w >= 0.0 : "removing negative weight: " + w;
    
    final double w0 = (-w) - _positiveCorrection;
    final double w1 = _positiveWeight + w0;
    _positiveCorrection = (w1 - _positiveWeight) - w0;
    _positiveWeight = w1; 
    
    assert _positiveWeight >= AccumulatorBase.NEGATIVE_WEIGHT_BOUND 
      : "positiveWeight is too negative: " + _positiveWeight +
      " after removing: " + w;  }

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final void clear () { super.clear(); _positiveWeight = 0.0; }

  @Override
  public final void add (final double z) {
    // TODO: allow more general representation for binary class values
    assert (0.0 == z) || (1.0 == z) : "invalid binary value: " + z; 
    incrementNetCount();
    incrementNetWeight(1.0);
    if (1.0 == z) { incrementPositiveWeight(1.0); } } 

  @Override
  public final void add (final double z, final double w) {
    // TODO: allow more general representation for binary class values
    assert (0.0 == z) || (1.0 == z) : "invalid binary value: " + z; 
    assert (0.0 <= w) : "negative weight: " + w;
    incrementNetCount();
    if (0.0 < w) {
      incrementNetWeight(w);
      if (1.0 == z) { incrementPositiveWeight(w); } } }

  @Override
  public final void add (final Object z) {
    throw new UnsupportedOperationException(
      getClass().getName() + ".add(" + z.getClass().getName() + ")"); } 

  @Override
  public final void delete (final double z) {
    // TODO: allow more general representation for binary class values
    assert (0.0 == z) || (1.0 == z) : "invalid binary value: " + z; 
    decrementNetCount();
    decrementNetWeight(1.0);
    if (1.0 == z) { decrementPositiveWeight(1.0); } }

  @Override
  public final void delete (final double z, final double w) {
    // TODO: allow more general representation for binary class values
    assert (0.0 == z) || (1.0 == z) : "invalid binary value: " + z; 
    assert (0.0 <= w) : "negative weight: " + w;
    decrementNetCount();
    if (0.0 < w) {
      decrementNetWeight(w);
      if (1.0 == z) { decrementPositiveWeight(w); } } }

  @Override
  public final void delete (final Object z) {
    throw new UnsupportedOperationException(
      getClass().getName() + ".delete(" + z.getClass().getName() + ")"); } 

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public BinaryAccumulatorBase () { super(); _positiveWeight = 0.0; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
