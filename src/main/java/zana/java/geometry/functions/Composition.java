package zana.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

//----------------------------------------------------------------
/** Compositions of other {@link Function function}.
 *
 * TODO: special case implementations, depending on terms. 
 * Probably use generic function to determine which composed
 * function implementation is used where.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-03-14
 */

@SuppressWarnings("unchecked")
public final class Composition extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------
  // TODO: do we need to iterate in both directions?
  private final ImmutableList _reversedTerms;
  public final List reversedTerms () { return _reversedTerms; }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  @Override
  public double doubleValue (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @Override
  public double doubleValue (final double[] x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @Override
  public double doubleValue (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }
  //--------------------------------------------------------------
  @Override
  public double[] value (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @Override
  public double[] value (final double[] x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @Override
  public double[] value (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }
  //--------------------------------------------------------------

  @Override
  public Function derivativeAt (final double[] x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @Override
  public Function derivativeAt (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

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
      _reversedTerms.equals(((Composition) o)._reversedTerms); }

  //  @Override
  //  public String toString () {
  //    return getClass().getSimpleName() + "[" + name() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Composition (final List reversedTerms) {
    super(
      ((Function) Iterables.getFirst(reversedTerms,null))
      .domain(),
      ((Function) Iterables.getLast(reversedTerms,null))
      .codomain());
    _reversedTerms = ImmutableList.copyOf(reversedTerms); }
  
  public static final Composition compose (final List terms ) {
    return new Composition(Lists.reverse(terms)); }

  public static final Composition compose (final Object ...terms ) {
    return compose(Arrays.asList(terms)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------