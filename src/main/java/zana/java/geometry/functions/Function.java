package zana.java.geometry.functions;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;

import clojure.lang.IFn;
import clojure.lang.ISeq;

//----------------------------------------------------------------
/** Base class for functions from and to geometric spaces.
 *
 * @author wahpenayo at gmail dot com
 * @version 2018-04-12
 */

@SuppressWarnings("unchecked")
public abstract class Function
implements IFn, IFn.DD, IFn.OD, Serializable {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final Object _domain;
  public final Object domain () { return _domain; }

  private final Object _codomain;
  public final Object codomain () { return _codomain; }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  private static final double EPSILON = Math.ulp(1.0F);

  @SuppressWarnings("static-method")
  public double accuracy () { return EPSILON; }

  private static final double
  SQRT_ACCURACY = Math.sqrt(EPSILON);

  @SuppressWarnings("static-method")
  public double sqrtAccuracy () { return SQRT_ACCURACY; }

  private static final double
  CBRT_ACCURACY = Math.pow(EPSILON,1.0/3.0);

  @SuppressWarnings("static-method")
  public double cbrtAccuracy () { return CBRT_ACCURACY; }

  //----------------------------------------------------------

  @SuppressWarnings("static-method")
  public double typicalParameter (@SuppressWarnings("unused") final int i) { 
    return 1.0; }

  //----------------------------------------------------------

  @SuppressWarnings("static-method")
  public double typicalValue () { return 1.0; }

  //----------------------------------------------------------
  /** A crude heuristic choice, based on Dennis-Schnabel,
   * algorithm 5.6.4.
   */

  public final double[] centralDifferenceStepsizes (final double[] p) {

    final double alpha = cbrtAccuracy();
    final int n = p.length;
    final double[] stepsizes = new double[n];
    for (int i=0;i<n;i++) {
      final double
      stepsize = alpha * Math.max(Math.abs(p[i]),
        Math.abs(typicalParameter(i)));
      stepsizes[i] = 1.0e-1*Math.min(1.0,stepsize); } 
    return stepsizes; }

  //----------------------------------------------------------
  /** A crude heuristic choice, based on Dennis-Schnabel,
   * algorithm 5.6.3.
   */

  private final double[] forwardDifferenceStepsizes (final double[] p) {

    final double alpha = sqrtAccuracy();
    final int n = p.length;
    final double[] stepsizes = new double[n];
    for (int i=0;i<n;i++) {
      final double stepsize
      = alpha * Math.max(Math.abs(p[i]),
        Math.abs(typicalParameter(i)) );
      stepsizes[i] = 1.0e-1*stepsize; } 

    return stepsizes; }

  //----------------------------------------------------------
  /** Based on Dennis-Schnabel, algorithm 5.6.4. */

  public final double[] centralDifferenceGradient (final double[] p) {

    final double[] stepsizes = centralDifferenceStepsizes(p);
    final int n = p.length;
    final double[] g = new double[n];
    for (int i=0;i<n;i++) {
      final double pi = p[i];
      final double si = stepsizes[i];
      if (! (si > EPSILON)) { // negative test handles NaN
        throw new IllegalArgumentException(
          "stepsize["+ i + "]= " + si +
          toString() + "\n" +
          "at " + "\n" +
          Arrays.toString(p) + "\n"); }

      final double x0 = pi + si;
      p[i] = x0;
      final double v0 = doubleValue(p);
      // this increases accuracy slightly, see Dennis-Schnabel
      final double s0 = x0 - pi;

      final double x1 = pi - si;
      p[i] = x1;
      final double v1 = doubleValue(p);
      // this increases accuracy slightly, see Dennis-Schnabel
      final double s1 = pi - x1;

      final double gi = (v0 - v1) / (s0 + s1);
      g[i] = gi;
      p[i] = pi; } 

    return g; }

  //----------------------------------------------------------
  /** Based on Dennis-Schnabel, algorithm 5.6.3.
   */

  public final double[] forwardDifferenceGradient (final double[] p) {

    final double[] stepsizes = forwardDifferenceStepsizes(p);
    final double v = doubleValue(p);
    final int n = p.length;
    final double[] g = new double[n];
    for (int i=0;i<n;i++) {
      final double pi = p[i];
      final double si = stepsizes[i];
      if (! (si > EPSILON)) { // negative test handles NaN
        throw new IllegalArgumentException("stepsize["+ i + "]= " + si); }

      final double xi = pi + si;
      p[i] = xi;
      final double vi = doubleValue(p);
      // this increases accuracy slightly, see Dennis-Schnabel
      final double di = xi - pi;
      final double gi = (vi - v) / di;
      g[i] = gi;
      p[i] = pi; }
    return g; }

  //----------------------------------------------------------
  /** For comparison with forward difference.
   * Based on Dennis-Schnabel, algorithm 5.6.3.
   */

  public final double[] backwardDifferenceGradient (final double[] p) {

    final int n = p.length;
    final double[] g = new double[n];
    final double[] stepsizes = forwardDifferenceStepsizes(p);
    final double v = doubleValue(p);
    for (int i=0;i<n;i++) {
      final double pi = p[i];
      final double si = stepsizes[i];
      if (! (si > EPSILON)) { // negative test handles NaN
        throw new IllegalArgumentException("stepsize["+ i + "]= " + si); }
      final double xi = pi - si;
      p[i] = xi;
      final double vi = doubleValue(p);
      // this increases accuracy slightly, see Dennis-Schnabel
      final double di = xi - pi;
      final double gi = (vi - v) / di;
      g[i] = gi;
      p[i] = pi; } 

    return g; }

  //----------------------------------------------------------
  // derivative tests
  //----------------------------------------------------------
  /** Compare the dual vector of the {@link #derivativeAt} 
   * (that is, the gradient) to the forward,
   ** backward, and central difference gradients at the
   ** current point.
   **/

  public final boolean checkGradient (final double[] p,
                                      final PrintWriter out) {
    final int n = p.length;

    final double val = doubleValue(p);
    final double[] ag = ((LinearFunctional) derivativeAt(p)).dual();
    final double[] cg = centralDifferenceGradient(p);
    final double[] cstep = centralDifferenceStepsizes(p);
    final double[] fg = forwardDifferenceGradient(p);
    final double[] fstep = forwardDifferenceStepsizes(p);
    final double[] bg = backwardDifferenceGradient(p);

    final double epsilon = 
      Math.sqrt(accuracy()) 
      * 
      Math.max(Math.abs(val),Math.abs(typicalValue()));

    final StringBuffer m = new StringBuffer("\n");
    m.append(toString()); m.append("\n");
    boolean close_enough = true;
    for (int i=0;i<n;i++) {
      final double gi = ag[i];
      final double ci = cg[i];
      final double fi = fg[i];
      final double bi = bg[i];
      // check if estimated derivatives are too far apart
      final double fc = Math.abs(fi-ci);
      final double bc = Math.abs(bi-ci);
      final double fb = Math.abs(bi-fi);
      final double cfb = 3*(1.0 + Math.abs(ci) + Math.abs(fi) + Math.abs(bi));
      final double ddg = (fb + fc + bc) / cfb;

      if (ddg > 1.0e-3) {
        final String w = "\n"
          + "est. partial derivatives inconsistent:"
          + "\n"
          + String.format("%3d",Integer.valueOf(i))
          + String.format(" %#13.6e",Double.valueOf(p[i]))
          + String.format(" %#13.6e",Double.valueOf(gi))
          + String.format(" %#13.6e",Double.valueOf(ci))
          + String.format(" %#13.6e",Double.valueOf(cstep[i]))
          + String.format(" %#13.6e",Double.valueOf(fi))
          + String.format(" %#13.6e",Double.valueOf(fstep[i]))
          + String.format(" %#13.6e",Double.valueOf(bi))
          + "\n";
        System.out.println(w); }

      final double diff = gi - ci;
      final double absdiff = Math.abs(diff);
      final double
      reference = 2*Math.max(epsilon,Math.max(Math.abs(fi-ci),Math.abs(bi-ci)));
      if (!(absdiff < reference)) { close_enough = false; m.append("* "); }
      else { m.append("  "); }
      double ratio = 0;
      if (EPSILON < Math.abs(ci)) { ratio = gi/ci; }
      m.append(String.format("%3d",Integer.valueOf(i)));
      m.append(String.format(" %#13.6e",Double.valueOf(p[i])));
      m.append(String.format(" %#13.6e",Double.valueOf(cstep[i])));
      m.append(String.format(" %#13.6e",Double.valueOf(gi)));
      m.append(String.format(" %#13.6e",Double.valueOf(ci)));
      m.append(String.format(" %#13.6e",Double.valueOf(fi)));
      m.append(String.format(" %#13.6e",Double.valueOf(bi)));
      m.append(String.format(" %#13.6e",Double.valueOf(ratio)));
      m.append(String.format(" %#13.6e",Double.valueOf(diff)));
      m.append(String.format(" %#13.6e",Double.valueOf(reference)));
      m.append("\n");
    }

    final double gnorm = zana.java.arrays.Arrays.l2norm(ag);
    final double cnorm = zana.java.arrays.Arrays.l2norm(cg);
    final double cosine = (EPSILON < gnorm*cnorm)
      ? zana.java.arrays.Arrays.dot(ag,cg) / (gnorm*cnorm)
        : 1.0;
      final double angle = (1.0 > cosine)
        ? Math.acos(cosine)*180/Math.PI
          : 0.0;
        //final double dcnorm = Math.abs(gnorm-cnorm);
        //final double rdcnorm = dcnorm/(1.0+gnorm+cnorm);

        m.append("  i");
        m.append(String.format(" %13s","position"));
        m.append(String.format(" %13s","step*10^6"));
        m.append(String.format(" %13s","analytic"));
        m.append(String.format(" %13s","central"));
        m.append(String.format(" %13s","forward"));
        m.append(String.format(" %13s","backward"));
        m.append(String.format(" %13s","ratio"));
        m.append(String.format(" %13s","absolute"));
        m.append(String.format(" %13s","reference"));
        m.append("\n");
        m.append("\n");
        m.append(String.format(" %#13.6e",Double.valueOf(val)));
        m.append(" value"); m.append("\n");
        m.append(String.format(" %#13.6e",Double.valueOf(accuracy())));
        m.append(" estimated accuracy");
        m.append("\n");
        m.append(String.format(" %#13.6e",Double.valueOf(cosine)));
        m.append(" cosine of error angle");
        m.append("\n");
        m.append(String.format(" %#13.6e",Double.valueOf(angle)));
        m.append(" error angle degrees");
        m.append("\n");
        m.append(toString());
        m.append("\n");

        final String msg = close_enough
          ? this + " gradient ok"
            + "\n" + m.toString()
            : this + " gradient failed"
            + "\n" + m.toString();
        if (close_enough) {
          System.out.println(msg); }
        else {
          System.out.println(msg); 
          //throw new IllegalStateException(msg); 
          }

        return close_enough; }

  public final boolean checkGradient (final double[] p,
                                      final OutputStream out) {
   return checkGradient(p,new PrintWriter(out)); }
  //--------------------------------------------------------------

  /** If the {@link #codomain() codomain} of this function is
   * 1-dimensional, return the value as a <code>double</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double doubleValue (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //  /** If the {@link #codomain() codomain} of this function is
  //   * 1-dimensional, and the {@link #domain() domain} contains
  //   * {@link Function functions}
  //   * return the value as a <code>double</code>.
  //   * <br>Otherwise throw an {@link UnsupportedOperationException}.
  //   */
  //  @SuppressWarnings("unused")
  //  public double doubleValue (final Function x) {
  //    throw new UnsupportedOperationException(
  //      getClass().getName()); }

  //  /** If the {@link #codomain() codomain} of this function is
  //   * 1-dimensional, and the {@link #domain() domain} contains
  //   * <code>double[]</code> arrays,
  //   * return the value as a <code>double</code>.
  //   * <br>Otherwise throw an {@link UnsupportedOperationException}.
  //   */
  //  @SuppressWarnings("unused")
  //  public double doubleValue (final double[] x) {
  //    throw new UnsupportedOperationException(
  //      getClass().getName()); }

  /** If the {@link #codomain() codomain} and
   * {@link #codomain() codomain} of this function are
   * 1-dimensional, take a <code>double</code> argument return the
   * value as a <code>double</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double doubleValue (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //--------------------------------------------------------------
  /** If the elements of the {@link #codomain() codomains} can be
   * represented by <code>double[]</code>,
   * return the value as a <code>double[]</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public Object value (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //  /** If the elements of the {@link #codomain() codomains} can be
  //   * represented by <code>double[]</code>
  //   * can, and the {@link #domain() domain} contains
  //   * {@link Function functions},
  //   * return the value as a <code>double[]</code>.
  //   * <br>Otherwise throw an {@link UnsupportedOperationException}.
  //   */
  //  @SuppressWarnings("unused")
  //  public Object value (final Function f) {
  //    throw new UnsupportedOperationException(
  //      getClass().getName()); }

  //  /** If the elements of the {@link #codomain() codomain} can be
  //   * represented by <code>double[]</code>
  //   * can, and {@link #domain() domain} contains elements that can
  //   * be represented by <code>double[]</code>,
  //   * return the value as a <code>double[]</code>.
  //   * <br>Otherwise throw an {@link UnsupportedOperationException}.
  //   */
  //  @SuppressWarnings("unused")
  //  public Object value (final double[] x) {
  //    throw new UnsupportedOperationException(
  //      getClass().getName()); }

  /** If the elements of the {@link #codomain() codomain} can be
   * represented by <code>double[]</code>
   * can, and {@link #domain() domain} is 1-dimensional,
   * take a <code>double</code> as the arg and
   * return the value as a <code>double[]</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public Object value (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //--------------------------------------------------------------
  /** Return the derivative of this function at <code>x</code>.
   * Recall that the general definition of the derivative
   * of a function is the <em>linear</em> function that
   * approximates it in the limit as we approach x.
   * <br> Optional operation.
   */
  @SuppressWarnings("unused")
  public Function derivativeAt (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //  /** Return the derivative of this function at <code>x</code>.
  //   * Recall that the general definition of the derivative
  //   * of a function is the <em>linear</em> function that
  //   * approximates it in the limit as we approach x.
  //   * <br> Optional operation.
  //   */
  //  @SuppressWarnings("unused")
  //  public Function derivativeAt (final double[] x) {
  //    throw new UnsupportedOperationException(
  //      getClass().getName()); }
  //
  //  @SuppressWarnings("unused")
  //  public Function derivativeAt (final Function x) {
  //    throw new UnsupportedOperationException(
  //      getClass().getName()); }

  @SuppressWarnings("unused")
  public Function derivativeAt (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @SuppressWarnings("unused")
  public double slopeAt (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //--------------------------------------------------------------
  // IFn interfaces
  //--------------------------------------------------------------

  @Override
  public final Object invoke (final Object x) {
    return value(x); }

  @Override
  public final double invokePrim (final Object x) {
    return doubleValue(x); }

  @Override
  public final double invokePrim (final double x) {
    return doubleValue(x); }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = 17;
    h += 31*_domain.hashCode();
    h += 31*_codomain.hashCode();
    return h; }

  @Override
  public boolean equals (final Object o) {
    // necessary but not sufficient
    // TODO: should this be left to subclasses?
    return
      (o instanceof Function)
      &&
      _domain.equals(((Function) o)._domain)
      &&
      _codomain.equals(((Function) o)._codomain); }

  //  @Override
  //  public String toString () {
  //    return getClass().getSimpleName() + "[" + name() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public Function (final Object domain,
                   final Object codomain) {
    assert null != domain;
    assert null != codomain;
    _domain = domain;
    _codomain = codomain; }

  //--------------------------------------------------------------
  // unsupported IFn operations
  //--------------------------------------------------------------
  @Override
  public Object call ()
    throws Exception {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public void run () {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object applyTo (final ISeq x0) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke () {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17,
                        final Object x18) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17,
                        final Object x18, final Object x19) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17,
                        final Object x18, final Object x19,
                        final Object... x20) {
    throw new UnsupportedOperationException(getClass().getName());
  }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------