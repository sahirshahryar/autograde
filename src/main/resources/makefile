all: source jar alias

source:
	[[ -e bin ]] || mkdir bin
	find ../java -name "*.java" > sources.txt
	javac -d bin -sourcepath ../java @sources.txt

jar:
	[[ -e bin ]] || echo "Run 'make source' first!"
	[[ -e doc ]] && jar cvfm AutoGrade.jar manifest.mf \
	-C bin grader -C doc grader -C ../java grader
	[[ -e doc ]] || jar cvfm AutoGrade.jar manifest.mf \
	-C bin grader -C ../java grader

javadoc:
	[[ -e doc ]] || mkdir doc
	javadoc -d doc -sourcepath ../java -private grader

alias:
	g++ -o autograde autograde.cpp

clean:
	rm AutoGrade.jar
	rm autograde
	[[ -e bin ]] && rm -r bin
	[[ -e doc ]] && rm -r doc