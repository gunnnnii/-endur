
build: NanoLexer.class Parser.class TreeCode.class SymbolTable.class
#	javac NanoLexer.java Parser.java TreeCode.java SymbolTable.java

SymbolTable.class: SymbolTable.java
	javac -cp . SymbolTable.java
TreeCode.class: TreeCode.java SymbolTable.class
	javac -cp . TreeCode.java

TcAssign.class: TreeCode.class TcAssign.java
	javac -cp . TcAssign.java
TcCall.class: TreeCode.class TcCall.java
	javac -cp . TcCall.java
TcFunction.class: TreeCode.class TcFunction.java
	javac -cp . TcFunction.java
TcLiteral.class: TreeCode.class TcLiteral.java
	javac -cp . TcLiteral.java
TcProgram.class: TreeCode.class TcProgram.java
	javac -cp . TcProgram.java
TcReturn.class: TreeCode.class TcReturn.java
	javac -cp . TcReturn.java
TcValue.class: TreeCode.class TcValue.java
	javac -cp . TcValue.java

Parser.class: Parser.java NanoLexer.class TcAssign.class TcCall.class TcFunction.class TcLiteral.class TcProgram.class TcReturn.class TcValue.class
	javac -cp . Parser.java
NanoLexer.class: NanoLexer.java
	javac NanoLexer.java
NanoLexer.java: nanolexer.jflex
	java -jar jflex-full-1.7.0.jar nanolexer.jflex

clean:
	del *~ NanoLexer.java *.class
test: build test.nm
	java -cp . Parser test.nm
gen: build code.nm
	java -cp . Parser code.nm
	java -jar morpho.jar -c code.masm
	java -jar morpho.jar code