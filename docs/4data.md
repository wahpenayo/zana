# Data

Zana is a supporting library for Taiga (a machine learning library) and Tu
(a visualization library). Together these 2 libraries are something like a small 
subset of [R](https://www.r-project.org/).

R is essentially a language for manipulating data frames, and that is its 
biggest weakness. They are slow, consume much more memory than necessary,
and fail at representing anything other than row by column tables. 

Zana, and the libraries it supports, represent data sets (for training, test, 
prediction, or visualization) using any Java <code>Iterable</code>. Cases, the 
rows of a data frame, are represented by the elements of the 
<code>Iterable</code>, which can be instances any Java class. You can even mix
multiple classes in a single Iterable.

## Attributes

Attributes (data frame columns) are represented by Clojure functions. 
This means the set of attributes is unbounded --- you can always define another
function to create a new attribute. This implies you can't determine the 
attributes from the data set, but have to pass them to machine learning and 
visualization functions separately. 

In general, attribute functions can return any value they like.

Functions that implement <code>clojure.lang.IFn$OD</code>
or <code>clojure.lang.IFn$OL</code> will have much higher performance
in numerical contexts. You can create such functions with
```Clojure
(defn x ^double [datum] ...)
```
or
```Clojure
(defn x ^long [datum] ...)
```
or by defining your own Java class that implements one of the 2 interfaces.

## Data sets

The most common case is for the data set to be a <code>List</code> of 
record-like objects, and the attributes to be accessor functions for the 
record fields. In this case, the data set occupies significant memory, and the 
attributes are trivial.

Another common case is for the data set to be a <code>List</code> of keys,
and for the attributes to be functions wrapping (primitive) hashmaps from
key to value. In this case, the data set occupies a relatively small amount of
memory, with the bulk of the data stored in the attribute functions.
Such hashmap attributes can be implemented in a lazy fashion, which is useful 
for memoizing expensive direct attribute functions. 

They are also useful for low memory implementation of join operations: 
The key sequence can be the intersection or union of corresponding key fields
from multiple 'record' data sets, and the attribute functions then lookup
the records they need, compute a value, and cache it for future use.

## Datum classes

Zana provides a 
<code>defrecord</code>-like macro to ease the definition of
data record-like classes: [[zana.api/define-datum]] (aka [[zana.data.datum/define]]):

`(zana.api/define-datum name [& field-specs]) `
<dl>
<dt>name</dt>
<dd>
Dynamically generates compiled bytecode for a class with the given _name_, in a package with the same name as the current namespace, with the given fields.
</dd>
<dt>field-specs</dt>
<dd>
a field specification is:
<ul>
<li> a field name: `ymd`
</li>
<li> a type-hinted field name: `^java.time.LocalDate ymd`
</li>
<li> a 2 element vector: 
```Clojure
[ymd (fn [tuple archetype] 
       (archetype (LocalDate/parse ^String (:ymd tuple))))]
```   
</li>    
<li> a type-hinted 2 element vector: 
```Clojure
^java.time.LocalDate [ymd (fn [tuple archetype] 
                            (archtype (LocalDate/parse ^String (:ymd tuple))))]
```
</li>
</ul>
In the 2 element vector forms, the second element is a custom parsing function
used in text input.
See the section on <a href="#textIO">text IO</a> for details.
</dd>


(`zana.api/define-datum` is a complex macro. In the future, it's functionality
may be broken up into simpler pieces.
For the present, see the `zana.test.defs.data`
and <code>zana.test.data</code> namespaces for examples.
If a datum class doesn't behave as you expect, it can be very helpful to view 
(and compile and run) the emitted code. 
The examples in `zana.test.defs.data` contain commented expressions you can 
adapt to your case, and run to get the 
output of the macro expansion.)

*Warning:* `zana.api/define-datum` assumes only one datum class is defined in
a namespace, but at present does not check for that.

A <a name="typical">typical</a> example:
```Clojure
(zana.api/define-datum Typical
  [^long n 
   ^double x 
   ^String string 
   ^Primitive p 
   ^LocalDate [ymd (fn [tuple archetype]
                     (archetype (LocalDate/parse ^String (:ymd tuple))))]
   ^LocalDateTime [dt (fn [tuple archetype]
                        (LocalDateTime/parse ^String (:dt tuple)))]])
```

`zana.api/define-datum` takes 2 args, a name for the datum class and a vector of annotated fields. A field can be a name, a type hint plus name, or
a type hint on a 2 element vector, where the first element is the field name and the second a <a href="#parsingFunction">parsing function</a>. 
(Note that parsing functionality is likely to change in the future.)

Fields whose type hint is another datum class are special. In the example, `Primitive` is another datum class:
```Clojure
(zana.api/define-datum Primitive
  [^boolean tf 
   ^byte b 
   ^short sh 
   ^int i 
   ^long l 
   ^float f 
   ^double d 
   ^char c])
```

`zana.api/define-datum` emits
<dl>
<dt>a class definition</dt>
<dd>
Datum classes are simliar to classes created with `defrecord`, with differences intended to help in ML and data visualization applications.
<ul>
<li> instances of datum classes are immutable.
</li>
<li> instances can be created with a positional constructor:
`(Primitive. true 0 1 2 3 4 5 \a)` or (probably better performance)
`(Primitive. true (byte 0) (short 1) (int 2) (long 3) (float 4.0) (double 5.0) \a)`
</li>
<li> getter methods are defined:
`(.d primitive)` returns a primitive `double` `5.0`.
</li>
<li>`hashcode` and `equals` methods are defined, but, unlike `defrecord`, 
they use identity semantics (as in `java.util.IdentityHashMap`.
</li>
<li>an [EDN](https://github.com/edn-format/edn) compatible `toString` method:
`#zana.test.defs.data.primitive/Primitive {:tf true :b 0 :sh 1 :i 2 :l 3 :f 4.0 :d 5.0 :c \a}`
</li>
<li> Datum classes implement the `clojure.lang` interfaces necessary to permit instances to be treated, to a certain degree, like Clojure maps.
</li>
<li>`(:x typical)`,`(typical :x)`, and `(get typical :x)`, though inefficient compared to using the accessor functions described below, will 
return a `Double` containing the value of the `x` field  in `typical`. 
</li>
<li>`(assoc typical :x 1.0)` would return a new instance of `Typical` with 
the `x` field set to `1.0`. 
</li>
<li>Unlike a `defrecord` class, `(assoc typical :foo bar)` will throw an exception. In other words, new key-value pairs can't be added.
</li>
<li> Unlike `defrecord` and `deftype`, you _cannot_ add methods by implementing an interface or protocol, or any other way.
</li>
</ul>
</dd>
<dt>a map-based factory function</dt>
<dd>
`(map->Primitive {:tf false :c \a :l 1 :i 1 :b 127 :s 13 :f 1.0 :d Math/PI})` will work, if much more slowly than the positional constructor.
</dd>

<dt>type-hinted accessor functions</dt>
<dd><code>(x typical)</code> will return a primitive <code>double</code>, and is just a thin wrapper around <code>(.x typical)</code>.
<br>
If any field is hinted with another datum class, 
<a name="recursiveAccessors">recursive accessor functions</a>
are generated by joining the outer field name with the names of the inner fields with dashes. For example: <code>(p-b typical)</code> is equivalent to 
<code>(b (p typical))</code> in the <code>Typical</code> example. 
</dd>

<dt>attribute function lists</dt>
<dd>
as a convenience, <code>zana.api/define-datum</code> creates 3 global Vars: <code>attributes</code>, <code>numerical-attributes</code>, and <code>non-numerical-attributes</code> containing all the attribute functions,
the ones that implement <code>IFn$OD</code> or <code>IFn$OL</code>,
and the ones that don't.
</dd>

<dt>functions for binary IO</dt>
<dd>
<ul>
<li><code>(write-binary ^java.io.ObjectOutput oo ^Typical typical)</code> writes one <code>typical</code> object to <code>oo</code> in a compact
binary representation which can be read with:
</li>
<li> <code>(read-binary ^java.io.ObjectInput oi archetype)</code>,
where <code>archetype</code> is a de-duping function similar to <code>String/intern</code>, but which works for any <code>Object</code>, and is usually has limited scope and extent, so the inner hashmap can eventually be garbage collected.
</li>
<li><code>(write-binary-file ^Iterable data ^java.io.File f)</code>
calls <code>write-binary</code> on an <code>ObjectOutput</code> constructed for <code>f</code>, repeatedly on the elements of <code>data</code>, 
throwing an exception if any are not instances of <code>Typical</code>.
</li>
<li><code>(read-binary-file ^java.io.File f)</code> tries to read <code>f</code> as though it were written with <code>write-binary-file</code>, by repeatedly calling <code>read-binary</code>, using an internal temporary <code>archetype</code> function.
</li>
</ul>
</dd>
 
<dt><a name="textIO">functions for text IO</a></dt>
<dd>
<em>Note:</em> Text IO is currently over-complicated and likely to change.
Future versions are likely to abandon the attempt to automatically generate
TSV file parsers, and instead just provide useful building blocks. 

One of the things that complicates the current design is the attempt to automatically handle nested datum classes. 

Current (version 3.0.0, 2016-08-29) implementation:
<ul>
<li><code>header-tokens</code>
A vector of <code>String</code>s, the names of the accessor functions,
in definition order. 
For <a href="#recursiveAccessors"> recursive accessors</a>) the names are accumulated depth first.
In the <code>Typical</code> example, we get:
<pre>
<code>
(def header-tokens 
  ["n" "x" "string" "p-tf" "p-b" "p-sh" "p-i" "p-l" "p-f" "p-d" "p-c" "ymd" "dt"]) 
</code>
</pre>
</li>
<li><code>(values-string datum sep)</code> applies <code>str</code> to each 
field value and joins the result with <code>sep</code>.
</li>
<li><code>(values-string datum)</code> same as 
<code>(values-string datum "\t")</code>.
</li>
<li><code>(header sep)</code> joins the <code>header-tokens</code> with <code>sep</code>.
</li>
<li><code>(header)</code> same as <code>(header "\t")</code>.
</li>
<li><code>(write-tsv-file ^Iterable data ^java.io.File f sep)</code>
writes <code>(header sep)</code> to the file, followed by
<code>(values-string datum sep)</code> for each element of <code>data</code>.
Throws an exception if any of the elements of <code>data</code> is not of the
defining datum class.
</li>
<li><code>(write-tsv-file ^Iterable data ^java.io.File f</code>: same as 
<code>(write-tsv-file data f "\t")</code>.
</li>
<li><code>(read-tsv-file ^File f)</code> same as 
<code>(read-tsv-file f #"\t")</code> 
</li>
<li><code>(read-tsv-file ^File f ^Pattern sep)</code> same as
<code>(read-tsv-file f sep default-header-keys)</code>.
<code>default-header-keys</code> maps a sequence of header <code>String</code> tokens into (nested) sequences of <code>Keyword</code>s. 
It first tokenizes the header <code>String</code> using <code>sep</code>,
then lowercases the tokens and replaces all "\_" by "-".
It then break each token <code>String</code> at the "-", and applies <code>keyword</code> to each.
When this works as intended, the tree of nested keyword sequences corresponds to the tree of nested field names of the nested datum classes.
In the <code>Typical</code> example, we would get:
<code>(default-header-keys "N X String P_TF P_B P_SH ... YMD DT")
</code>
returns
<code>[:n :x :string [:p :b] &#91;:p :sh] ... :ymd :dt]</code>
</li>
<li><code>(read-tsv-file ^File f ^Pattern sep ^IFn header-keys)</code>
reads the whole file, using a custom <code>header-keys</code>.
The job of <code>header-keys</code> is to map each header token to a sequence of keywords that corresponds to the path through the nested fields to where the values under that header token should go.
In the <code>Typical</code> example, the custom header-key function handles a
case where the path isn't represented by dashes/underscores in the heade tokens:
<pre>
<code>
(defn header-key [^String token]
  (let [k (keyword (.replace (.toLowerCase token) "_" "-"))]
    (case k 
      :ptf [:p :tf] 
      :pb [:p :b] 
      :psh [:p :sh] 
      :pi [:p :i] 
      :pl [:p :l] 
      :pf [:p :f] 
      :pc [:p :c] 
      :pd [:p :d]
      ;; else
      k)))
</code>
</pre> 
<code>read-tsv-file</code> parses a file into a collection of datum objects by
<ol>
<li> Tokenizing the first line of the file and then using the <code>header-key</code> to turn the tokens into keyword sequences.
</li>
<li> Tokening each subsequent line and creating a tuple tree, using <code>assoc-in</code> to do a nested version of <code>zipmap</code>.
</li>
<li>
Getting a value for each field in the by calling the 
<a name="parsingFunction"> parsing function</a> for that field.
A parsing function takes 2 arguments:
<ul>
<li><code>tuple</code> a nested Clojure hashmap.</li>
<li><code>archetype</code> a de-duping function takes any Object as an input, 
and returns a prototype equivalent object (like <code>String/intern</code>, 
but for any <code>Object</code>). 
The parsing function is free to ignore <code>archetype</code>.<br>
Default parsing functions are generated using each field's type hint. 
They handle parsing <code>String</code>s into primitive types, 
and handle converting "nil" and "null", etc., to <code>nil</code>.
Custom <a href="#typical">parsing functions</a> can be provided, for example:
<pre>
<code>
(fn [tuple archetype] (archetype (LocalDate/parse ^String (:ymd tuple))))
</code>
</pre>
and
<pre>
<code>
(fn [tuple _] (LocalDateTime/parse ^String (:dt tuple))))
</code>
</pre>
In the first example, <code>archetype</code> is used to reduce the number of
distinct <code>LocalDate</code> objects that are created. It's common to have data sets with 10s or 100s millions of records, but no more than 100s of 
distinct dates. On the other hand, if we have data covering a year, we could easily have 10s of millions of distinct <code>LocalDateTime</code>s.
<br>
When in doubt, it's better to use the archetype function to de-dupe. It may
slow down the parsing, and add a little GC overhead collecting the archetype
map. But text parsing is slow at best, and the usual strategy should be to 
parse text input rarely, cache the results in binary form, and read the binary
representation most of the time, only parsing text when something has changed.

</li>
</ul>
<li> Calling the positional constructor to return an instance of the datum 
class.
</li>
</ol>
</li>
<li><code>(read-tsv-file ^File f ^Pattern sep ^IFn header-keys ^long n)</code>
Reads the first <code>n</code> lines in the file. 
</li>
</ul> 
</dd>
</dl>







