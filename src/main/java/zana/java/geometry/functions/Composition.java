package zana.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

//----------------------------------------------------------------
/** Compositions of other {@link Function functions}.
 *
 * TODO: special case implementations, depending on terms. 
 * Probably use generic function to determine which composed
 * function implementation is used where.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class Composition extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------
  // TODO: do we need to iterate in both directions?
  private final Function[] _reversedTerms;
  public final List terms () { 
    return Lists.reverse(Arrays.asList(_reversedTerms)); }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  @Override
  public Object value (final Object x) {
    Object y = _reversedTerms[0].value(x);
    for (int i=1;i<_reversedTerms.length;i++) {
      y = _reversedTerms[i].value(y); }
    return y; }

  @Override
  public Object value (final double x) {
    Object y = _reversedTerms[0].value(x);
    for (int i=1;i<_reversedTerms.length;i++) {
      y = _reversedTerms[i].value(y); }
    return y; }
  //--------------------------------------------------------------
  @Override
  public double doubleValue (final Object x) { 
    return ((double[]) value(x))[0]; }

  @Override
  public double doubleValue (final double x) {
    return ((double[]) value(x))[0]; }
  //--------------------------------------------------------------

  @Override
  public Function derivativeAt (final Object x) {
    final Function[] d = new Function[_reversedTerms.length];
    d[0] = _reversedTerms[0].derivativeAt(x);
    Object y = _reversedTerms[0].value(x);
    for (int i=1;i<_reversedTerms.length;i++) {
      d[i] = _reversedTerms[i].derivativeAt(y);
      y = _reversedTerms[i].value(y); }
    return new Composition(d); }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = super.hashCode();
    h += 31*_reversedTerms.hashCode();
    return h; }

  @Override
  public boolean equals (final Object o) {
    // necessary but not sufficient
    // TODO: should this be left to subclasses?
    return
      (o instanceof Composition)
      &&
      super.equals(o)
      &&
      Arrays.equals(
        _reversedTerms, 
        ((Composition) o)._reversedTerms); }

  //  @Override
  //  public String toString () {
  //    return getClass().getSimpleName() + "[" + name() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Composition (final Function[] reversedTerms) {
    super(
      reversedTerms[0].domain(),
      reversedTerms[reversedTerms.length-1].codomain());
    assert 2 <= reversedTerms.length;
    _reversedTerms = reversedTerms; }
  
  public static final Composition compose (final Function ...terms) {
    final int n = terms.length;
    final Function[] reversed = new Function[n];
    for (int i=0; i<n;i++) { reversed[i] = terms[n-1-i]; }
    return new Composition(reversed); }

  public static final Composition compose (final List terms ) {
    return 
      compose(
        (Function[]) terms.toArray(new Function[terms.size()])); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------