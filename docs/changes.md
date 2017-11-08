# change history

## 4.1.0

### wahpenayo-parent pom

Common maven configuration moved to 
(wahpenayo-parent)[https://github.com/wahpenayo/wahpenayo-parent]
project.

### Clojure 1.9.0 compatibility

#### spec restrictions on `(fn )` expressions.
 
Macros that expand into <code>`(fn foo [] ...)</code> need to 
ensure <code>foo</code> is not namespace qualified, ie,
<code>`(fn ~'foo [] ...)</code>.

### JDK 9 compatibility

#### illegal reflective access

```
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.parboiled.transform.AsmUtils 
(file:/C:/porta/projects/faster-multimethods/lib/parboiled-java-1.1.8.jar) 
to method java.lang.ClassLoader.findLoadedClass(java.lang.String)
WARNING: Please consider reporting this to the maintainers of org.parboiled.transform.AsmUtils
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
```

can be eliminated, at least for the present, by adding

`--add-opens java.base/java.lang=ALL-UNNAMED`

to the call to `java` in the launcher scripts.

## 4.0.0

Amazon contribution.