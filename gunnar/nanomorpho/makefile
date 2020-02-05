Lexer.class: Lexer.java
	javac Lexer.java
Lexer.java: lexer.jflex
	java -jar jflex-full-1.7.0.jar lexer.jflex
clean:
	rm -Rf *~ Lexer*.class Lexer.java
test: Lexer.class test.s
	java Lexer test.s