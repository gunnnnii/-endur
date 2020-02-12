NanoLexer.class: NanoLexer.java
	javac NanoLexer.java
NanoLexer.java: nanolexer.jflex
	java -jar jflex-full-1.7.0.jar nanolexer.jflex
build: NanoLexer.java Parser.java
	javac NanoLexer.java Parser.java
clean:
	del *~ NanoLexer*.class NanoLexer.java
test: NanoLexer.class test.nm
	java -cp . NanoLexer test.nm