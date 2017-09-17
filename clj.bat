@echo off
:: John Alan McDonald
:: 2016-04-21
set PROF=-Xrunhprof:cpu=samples,depth=32,thread=y,doe=y
::set PROF=
::set SIZE=12288m

set SIZE=4096m
set XMX=-Xms1024m -Xmx%SIZE%
set CP=-cp ./src/scripts/clojure;./src/test/clojure;./src/test/resources;lib/*
set ASSERT=-ea -da:org.geotools... -da:org.opengis...
set JAVA="%JAVA_HOME%\bin\java"

set CMD=%JAVA% -server %ASSERT% %PROF% %OPT% %PERM% %GC% %XMX% %CP% clojure.main %*
echo %CMD%
%CMD%
