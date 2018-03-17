#!/usr/bin/env bash

# Create a modularised JDK that contains only the java modules needed by a specified jar file. If the jar file
# is an executable path and a main class is specified a launcher is created in $out/bin named "app" where $out
# is arg #2.

function usage {
    echo "usage: dplink.sh <jar> [<output dir> [<jar main class>]]"
}

if [ -z ${JAVA_HOME} ]
then
echo ERROR: JAVA_HOME not set. Please set JAVA_HOME to a JDK 9 or more recent JDK installation.
exit -1
fi

jar=$1
mods=""

if [ -z ${jar} ]
then
usage
exit 0
fi

out=$2
if [ -z ${out} ]
then
out=app
fi

for mod in $(jdeps --list-deps $jar | grep "java.")
do
    mods="$mod $mods"
done
mods="$(echo -e "${mods}" | sed -e 's/[[:space:]]*$//')"
mods="$(echo "${mods}" | sed -e 's/ /,/g')"

echo Dependent JDK modules: $mods

$JAVA_HOME/bin/jlink --module-path $JAVA_HOME/jmods:mlib --add-modules $mods --output $out --no-header-files --no-man-pages --compress=2

echo JRE created at $out

cp $jar $out/lib

echo $jar copied to $out/lib

main=$3
if [ -z ${main} ]
then
exit 0
fi

echo "#!/usr/bin/env bash" > $out/bin/app
echo "${out}/bin/java -jar ${jar} ${main}" >> $out/bin/app
chmod uog+x $out/bin/app

echo Launcher created at $out/bin/app

