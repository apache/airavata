libs=""
for lib in lib/*.jar
do
	libs=$libs$lib":"
done
java -cp $libs cct.JMolEditor
