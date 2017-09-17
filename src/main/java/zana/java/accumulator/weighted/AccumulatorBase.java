package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Sharable code for {@link zana.java.accumulator.Accumulator} implementations.
 *
 * @author John Alan McDonald
 * @version 2017-01-03
 */

@SuppressWarnings("unchecked") 
public abstract class AccumulatorBase 
extends zana.java.accumulator.AccumulatorBase {

  //----------------------------------------------------------------------------
  // slots
  //----------------------------------------------------------------------------
  /** Mutable! Not thread safe!
   */

  private double _netWeight;

  /** Mutable! Not thread safe!
   */

  private double _netCorrection;

  /** See <a href=https://en.wikipedia.org/wiki/Kahan_summation_algorithm>
   * Kahan summation algorithm</a>.
   */
  public final void incrementNetWeight (final double w) { 
    assert w >= 0.0 : "adding negative weight: " + w;

    final double w0 = w - _netCorrection;
    final double w1 = _netWeight + w0;
    _netCorrection = (w1 - _netWeight) - w0;
    _netWeight = w1; }

  public final void incrementNetWeight () { 
    final double w0 = 1.0 - _netCorrection;
    final double w1 = _netWeight + w0;
    _netCorrection = (w1 - _netWeight) - w0;
    _netWeight = w1; }

  /** See <a href=https://en.wikipedia.org/wiki/Kahan_summation_algorithm>
   * Kahan summation algorithm</a>.
   */
  public final void decrementNetWeight (final double w) {
    assert w >= 0.0 : "removing negative weight: " + w;

    final double w0 = (-w) - _netCorrection;
    final double w1 = _netWeight + w0;
    _netCorrection = (w1 - _netWeight) - w0;
    _netWeight = w1; 

    assert _netWeight >= NEGATIVE_WEIGHT_BOUND
      : "netWeight is too negative: " + _netWeight +
      " after removing: " + w;  } 

  public final void decrementNetWeight () {
    final double w0 = -1.0 - _netCorrection;
    final double w1 = _netWeight + w0;
    _netCorrection = (w1 - _netWeight) - w0;
    _netWeight = w1; 

    assert _netWeight >= NEGATIVE_WEIGHT_BOUND
      : "netWeight is too negative: " + _netWeight +
      " after removing: 1.0";  } 

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  @Override
  public final double netWeight () { return _netWeight; }

  /** Subclasses need to call <code>super.clear()</code>!
   */

  @Override
  public void clear () { super.clear(); _netWeight = 0.0;}

//  @Override
//  public final void add (final double[] zs, 
//                         final double[] ws) {
//    assert ws.length == zs.length;
//    for (int i = 0; i<zs.length; i++) { add(zs[i],ws[i]); } }

//  @Override
//  public final void add (final Iterable zs, 
//                         final Iterable ws) {
//    final Iterator zi = zs.iterator();
//    final Iterator wi = ws.iterator();
//    while (zi.hasNext() && wi.hasNext()) { 
//      add(((Number) zi.next()).doubleValue(),
//        ((Number) wi.next()).doubleValue()); } 
//    assert (! zi.hasNext()) : "more values than weights";
//    assert (! wi.hasNext()) : "more weights than values"; }

//  private final void add (final OD zf, 
//                          final OD wf, 
//                          final RandomAccess data) {
//    final List d = (List) data;
//    final int n = d.size();
//    for (int i=0;i<n;i++) { 
//      final Object di = d.get(i);
//      add(zf.invokePrim(di), wf.invokePrim(di)); } }

//  @Override
//  public final void add (final OD zf, 
//                         final OD wf, 
//                         final Iterable data) {
//    if (data instanceof RandomAccess) {
//      add(zf,wf,(RandomAccess) data); }
//    else {
//      for (final Object datum : data) { 
//        add(zf.invokePrim(datum), wf.invokePrim(datum)); } } }

//  private static final Callable<Double> task (final OD f, final Object datum) {
//    return new Callable<Double>() {
//      public final Double call () throws Exception {
//        return Double.valueOf(f.invokePrim(datum)); } }; } 

//  @Override
//  public final void add (final Collection zfs, 
//                         final double w, 
//                         final Object datum) {
//    try {
//      final int np = Runtime.getRuntime().availableProcessors();
//      final List<Callable<Double>> tasks = new ArrayList(zfs.size());
//      for (final Object zf : zfs) { tasks.add(task((OD) zf,datum)); }
//      final ExecutorService pool = Executors.newFixedThreadPool(np);
//      final List<Future<Double>> futures = pool.invokeAll(tasks); 
//      for (final Future<Double> future : futures) {
//        add(future.get().doubleValue(),w); } }
//    catch (final Exception e) { throw new RuntimeException(e); } }

  //----------------------------------------------------------------------------
//  @Override
//  public final void delete (final double[] zs, 
//                            final double[] ws) {
//    assert ws.length == zs.length;
//    for (int i = 0; i<zs.length; i++) { delete(zs[i],ws[i]); } }

//  @Override
//  public final void delete (final Iterable zs, 
//                            final Iterable ws) {
//    final Iterator zi = zs.iterator();
//    final Iterator wi = ws.iterator();
//    while (zi.hasNext() && wi.hasNext()) { 
//      delete(((Number) zi.next()).doubleValue(),
//        ((Number) wi.next()).doubleValue()); } 
//    assert (! zi.hasNext()) : "more values than weights";
//    assert (! wi.hasNext()) : "more weights than values"; }

//  @Override
//  public final void delete (final OD zf, 
//                            final OD wf, 
//                            final Iterable data) {
//    for (final Object datum : data) { 
//      delete(zf.invokePrim(datum), wf.invokePrim(datum)); } }

//  @Override
//  public final void delete (final Collection zfs, 
//                            final double w, 
//                            final Object datum) {
//    for (final Object zf : zfs) { delete(((OD) zf).invokePrim(datum), w); } }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public AccumulatorBase () { super(); this._netWeight = 0.0; }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
