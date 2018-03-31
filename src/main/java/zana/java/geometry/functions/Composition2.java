package zana.java.geometry.functions;

//----------------------------------------------------------------
/** Composition of two {@link Function functions}.
 *
 * TODO: special case implementations, depending on terms. 
 * Probably use generic function to determine which composed
 * function implementation is used where.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-03-31
 */

@SuppressWarnings("unchecked")
public final class Composition2 extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final Function _f0;
  private final Function _f1;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  @Override
  public double[] value (final Object x) {
    return _f0.value(_f1.value(x)); }

  @Override
  public double[] value (final double[] x) {
    return _f0.value(_f1.value(x)); }

  @Override
  public double[] value (final double x) {
    return _f0.value(_f1.value(x)); }
  //--------------------------------------------------------------
  @Override
  public double doubleValue (final Object x) { 
    return _f0.doubleValue(_f1.value(x)); }

  @Override
  public double doubleValue (final double[] x) {
    return _f0.doubleValue(_f1.value(x)); }

  @Override
  public double doubleValue (final double x) {
    return _f0.doubleValue(_f1.value(x)); }
  //--------------------------------------------------------------

  @Override
  public Function derivativeAt (final double[] x) {
    final Function d1 = _f1.derivativeAt(x);
    final Function d0 = _f0.derivativeAt(_f1.value(x));
    return compose(d0,d1); }

  @Override
  public Function derivativeAt (final Object x) {
    final Function d1 = _f1.derivativeAt(x);
    final Function d0 = _f0.derivativeAt(_f1.value(x));
    return compose(d0,d1); }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = super.hashCode();
    h += 31*_f0.hashCode();
    h += 31*_f1.hashCode();
    return h; }

  @Override
  public boolean equals (final Object o) {
    // necessary but not sufficient
    // TODO: should this be left to subclasses?
    return
      (o instanceof Composition2)
      &&
      super.equals(o)
      &&
      _f0.equals(((Composition2) o)._f0)
      &&
      _f1.equals(((Composition2) o)._f1); }

  @Override
  public String toString () {
    return "(" 
      + _f0.toString() + " o "
      + _f1.toString() + ")"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Composition2 (final Function f0,
                        final Function f1) {
    super(f1.domain(),f0.codomain());
    assert f0.domain().equals(f1.codomain());
    _f0 = f0; 
    _f1 = f1; }

  public static final Function compose (final LinearFunctional f0,
                                        final LinearRows f1){
    return f0.compose(f1); }

  public static final Function compose (final Function f0,
                                        final Function f1){
    if ((f0 instanceof LinearFunctional)
      && (f1 instanceof LinearRows)) {
      return ((LinearFunctional) f0).compose((LinearRows) f1); }
    
    return new Composition2(f0,f1); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------