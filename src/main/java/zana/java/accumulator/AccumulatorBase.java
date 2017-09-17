package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Sharable code for {@link zana.java.accumulator.Accumulator} implementations.
 *
 * @author John Alan McDonald
 * @version 2017-01-04
 */

@SuppressWarnings("unchecked") 
public abstract class AccumulatorBase extends Object implements Accumulator {

  //----------------------------------------------------------------------------
  // slots
  //----------------------------------------------------------------------------
  /** Mutable! Not thread safe!
   */

  private long _netCount;

  public final void incrementNetCount () { _netCount += 1L; }

  public final void decrementNetCount () { 
    _netCount -= 1L; 
    assert _netCount >= 0; }

  public static final double NEGATIVE_WEIGHT_BOUND = -0.5*Math.ulp((float) 1.0);

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final long netCount () { return _netCount; }

  /** Subclasses need to call <code>super.clear()</code>!
   */

  @Override
  public void clear () { _netCount = 0L; }

  //----------------------------------------------------------------------------

  @Override
  public Object value () {
    throw new UnsupportedOperationException(getClass().getName()); }

  @Override
  public double doubleValue () {
    throw new UnsupportedOperationException(getClass().getName()); }

  @Override
  public double minimum () { return Double.NEGATIVE_INFINITY; }

  @Override
  public double maximum () { return Double.POSITIVE_INFINITY; }

  //----------------------------------------------------------------------------
  // unsupported weighted methods
  //----------------------------------------------------------------------------

  @Override
  public double netWeight () { 
    throw new UnsupportedOperationException(getClass().getName()); }

  @Override
  public void add (double z, double w) {
    throw new UnsupportedOperationException(getClass().getName()); }

  @Override
  public void delete (double z, double w) {
    throw new UnsupportedOperationException(getClass().getName()); }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public AccumulatorBase () { super(); this._netCount = 0L; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
