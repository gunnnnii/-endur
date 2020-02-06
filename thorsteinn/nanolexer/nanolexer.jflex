

import java.io.*;

%%

%public
%class NanoLexer
%unicode
%byaccj

%{

// This part becomes a verbatim part of the program text inside
// the class, NanoLexer.java, that is generated.

// Definitions of tokens:
final static int ERROR = -1;
final static int IF= 1001;
final static int ELSEIF= 1002;
final static int ELSE= 1003;
final static int WHILE= 1004;
final static int RETURN= 1005;
final static int VAR= 1006;
final static int ASSIGNMENT= 1007;
final static int NAME = 1008;
final static int LITERAL = 1009;
final static int OPNAME = 10010;



// A variable that will contain lexemes as they are recognized:
private static String lexeme;
private int t;
// This runs the scanner:
public static void main( String[] args ) throws Exception
{
	NanoLexer lexer = new NanoLexer(new FileReader(args[0]));
	int token = lexer.yylex();
	while( token!=0 )
	{
		
		System.out.println(""+token+": \'"+lexeme+"\'");
		token = lexer.yylex();

	}
}


/**
 * Spurning hvort þetta sé það sem hann var að tala um?
 * 
 
public int getToken(){
	return t;
}

public String getLexeme(){
	return lexeme;
}

public int advance()(){
	return this.yylex();
}

*/
pub

%}

  /* Reglulegar skilgreiningar */

  /* Regular definitions */

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
_DELIM=[(){};,]
_NAME=([:letter:]|{_DIGIT})+
_OPNAME=[\+\-*/]



%%

  /* Lesgreiningarreglur */
  /* Scanning rules */

{_DELIM} {
	lexeme = yytext();
	// t = yycharat(0);
	return yycharat(0);
}



{_OPNAME} {
	lexeme = yytext();
	// t = OPNAME;
	return OPNAME;
}



{_STRING} | {_FLOAT} | {_CHAR} | {_INT} | null | true | false {
	lexeme = yytext();
	// t = LITERAL;
	return LITERAL;
}




"if" {
	lexeme = yytext();
	// t = IF;
	return IF;
}


"elseif" {
	lexeme = yytext();
	// t = ELSEIF;
	return ELSEIF;
}


"else" {
	lexeme = yytext();
	// t = ELSE;
	return ELSE;
}


"while" {
	lexeme = yytext();
	// t = WHILE;
	return WHILE;
}


"var" {
	lexeme = yytext();
	// t = VAR;
	return VAR;
}


"return" {
	lexeme = yytext();
	// t = RETURN;
	return RETURN;
}


"=" {
	lexeme = yytext();	
	// t = ASSIGNMENT;
	return ASSIGNMENT;
}


{_NAME} {
	lexeme = yytext();	
	// t = NAME;
	return NAME;
}



"//".*$ {
}

[ \t\r\n\f] {
}

. {
	lexeme = yytext();
	// t=ERROR;
	return ERROR;
}
