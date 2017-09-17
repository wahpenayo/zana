#! /bin/zsh
# John Alan McDonald
# 2015-12-03

export JAVA_HOME=/usr

export GC=

export PROF=-Xrunhprof:cpu=samples,depth=16,thread=y,doe=y
#export PROF=

export SIZE=4g

export XMX="-Xms${SIZE} -Xmx${SIZE}"

export CP="-cp ./src/scripts/clojure:lib/*"
export ASSERT="-ea -da:org.geotools... -da:org.opengis..."
export JAVA="${JAVA_HOME}/bin/java"
export CLJ="${JAVA} -server ${ASSERT} ${GC} ${PROF} ${XMX} ${CP} clojure.main"
${CLJ} $@
