package zana.java.data;

import java.util.List;

import clojure.lang.IFn;

//----------------------------------------------------------------------------
/** AKA 'one-hot encoding'.
 *
 * Convert records with general attribute values to elements
 * of affine spaces --- points in <b>E</b><sup>n</sup>
 * represented by instances of <code>double[n+1]</code>
 * holding homogeneous coordinates.
 * 
 * This is identical to a <code>LinearEmbedding</code>, except that it
 * adds a constant 1.0 as the least element in the returned
 * arrays, supporting affine models (linear plus constant term).
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-06
 */

@SuppressWarnings("unchecked")
public final class AffineEmbedding extends FlatEmbedding {
  
  private static final long serialVersionUID = 0L;
  
  //--------------------------------------------------------------
  // IFn interface
  //--------------------------------------------------------------
  
  @Override
  public final Object invoke (final Object record) {
    final int n = dimension();
    final double[] coords = new double[n+1];
    coords[n] = 1.0; // constant term
    int start = 0;
    for (final Object pair : attributeEmbeddings()) {
      final IFn x = (IFn) ((List) pair).get(0);
      final AttributeEmbedding al = 
        (AttributeEmbedding) ((List) pair).get(1);
      start = 
        al.updateCoordinates(
          x.invoke(record),
          coords,
          start); }
    return coords; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final String toString () { 
    return name() + "-homogenizer"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  public AffineEmbedding (final String name,
                          final List attributeEmbeddings) {
    super(name,attributeEmbeddings); }
  //---------------------------------------------------------------
  /** Construct an embedding in an affine space 
   * (<b>E</b><sup>n</sup>) for selected attributes and attribute
   * values.
   * 
   * This method will construct a map from record objects to 
   * points in <b>E</b><sup>n</sup>, represented by instances
   * of <code>double[n+1]</code> holding homogeneous coordinates.
   */
  public static final AffineEmbedding
  make (final String name,
        final List attributeValues) {
    return
      new AffineEmbedding(
        name,
        makeAttributeEmbeddings(attributeValues)); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------