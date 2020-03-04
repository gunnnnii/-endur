
codes = TcAnd.class TcAssign.class TcBranch.class TcCall.class TcFunction.class \
         TcLiteral.class TcLoop.class TcNot.class TcOr.class TcProgram.class \
         TcReturn.class TcValue.class TreeCode.class

aux = CompilerError.class FatalError.class TreeCode.class SymbolError.class SymbolTable.class

c: build #$(in).nm
	java -cp . Compiler $(in).nm $(in).masm
	java -jar morpho.jar -c $(in).masm
	java -jar morpho.jar $(in)

build: Compiler.class

$(aux): Error.java TreeCode.java SymbolTable.java
	javac -cp . Error.java TreeCode.java SymbolTable.java

TcAnd.class: $(aux) TcAnd.java
	javac -cp . TcAnd.java
TcAssign.class: $(aux) TcAssign.java
	javac -cp . TcAssign.java
TcBranch.class: $(aux) TcBranch.java
	javac -cp . TcBranch.java
TcCall.class: $(aux) TcCall.java
	javac -cp . TcCall.java
TcFunction.class: $(aux) TcFunction.java
	javac -cp . TcFunction.java
TcLiteral.class: $(aux) TcLiteral.java
	javac -cp . TcLiteral.java
TcLoop.class: $(aux) TcLoop.java
	javac -cp . TcLoop.java
TcNot.class: $(aux) TcNot.java
	javac -cp . TcNot.java
TcOr.class: $(aux) TcOr.java
	javac -cp . TcOr.java
TcProgram.class: $(aux) TcProgram.java
	javac -cp . TcProgram.java
TcReturn.class: $(aux) TcReturn.java
	javac -cp . TcReturn.java
TcValue.class: $(aux) TcValue.java
	javac -cp . TcValue.java

Compiler.class: $(aux) Compiler.java NanoLexer.class $(codes)
	javac -cp . Compiler.java
NanoLexer.class: NanoLexer.java
	javac NanoLexer.java
NanoLexer.java: nanolexer.jflex
	java -jar jflex-full-1.7.0.jar nanolexer.jflex

clean:
	del *~ NanoLexer.java *.class *.masm *.mexe