package zana.java.accumulator;

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

  private long _positiveCount;
  public final long positiveCount () { return _positiveCount; }

  private final void incrementPositiveCount () { 
    _positiveCount++; 
    assert _positiveCount <= netCount(); }

  private final void decrementPositiveCount () {
    _positiveCount--;    
    assert _positiveCount >= 0L;  }

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final void clear () { super.clear(); _positiveCount = 0L; }

  @Override
  public final void add (final double z) {
    // TODO: allow more general representation for binary class values
    assert (0.0 == z) || (1.0 == z) : "invalid binary value: " + z; 
    incrementNetCount();
    if (1.0 == z) { incrementPositiveCount(); } } 

  @Override
  public final void add (final Object z) {
    if (z instanceof Number) { add(((Number) z).doubleValue()); }
    else { 
      throw new UnsupportedOperationException(
        getClass().getName() + ".add(" + z.getClass().getName() + ")"); } }

  @Override
  public final void delete (final double z) {
    // TODO: allow more general representation for binary class values
    assert (0.0 == z) || (1.0 == z) : "invalid binary value: " + z; 
    decrementNetCount();
    if (1.0 == z) { decrementPositiveCount(); } }

  @Override
  public final void delete (final Object z) {
    if (z instanceof Number) { delete(((Number) z).doubleValue()); }
    else { 
      throw new UnsupportedOperationException(
        getClass().getName() + ".delete(" + z.getClass().getName() + ")"); } }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public BinaryAccumulatorBase () { super(); _positiveCount = 0L; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
