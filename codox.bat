@echo off
:: John Alan McDonald
:: 2016-08-31

call mvn package -DskipTests=true
if errorlevel 1 goto :end

::set PROF=
set SIZE=4096m
set XMX=-Xms1024m -Xmx%SIZE%
set CP=-cp ./src/scripts/clojure;./src/test/clojure;./src/test/resources;lib/*
set ASSERT=-ea -da:org.geotools... -da:org.opengis...
set JAVA="%JAVA_HOME%\bin\java"

set SCRIPT=src/scripts/clojure/zana/scripts/doc/codox.clj
set CMD=%JAVA% -server %ASSERT% %PROF% %OPT% %PERM% %GC% %XMX% %CP% clojure.main %SCRIPT%
echo %CMD%
%CMD%

:end
echo error: %errorlevel%
