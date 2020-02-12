NanoLexer.class: NanoLexer.java
	javac NanoLexer.java
NanoLexer.java: nanolexer.jflex
	java -jar jflex-full-1.7.0.jar nanolexer.jflex
clean:
	rm -Rf *~ NanoLexer*.class NanoLexer.java
test: NanoLexer.class test.s
	java NanoLexer test.s