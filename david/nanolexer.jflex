/**
	JFlex scanner example based on a scanner for NanoLisp.
	Author: Snorri Agnarsson, 2017-2020

	This stand-alone scanner/lexical analyzer can be built and run using:
		java -jar JFlex-full-1.7.0.jar nanolexer.jflex
		javac NanoLexer.java
		java NanoLexer inputfile > outputfile
	Also, the program 'make' can be used with the proper 'makefile':
		make test

	MODIFICATIONS:	David Francis Frigge, 29/01/2020
	
	Base lexer was modified/rewritten to scan the NanoMorpho language.
 */

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
final static int NAME = 1001;
final static int OPNAME = 1002;
final static int LITERAL = 1003;
final static int BRANCH = 1004;
final static int LOOP = 1005;
final static int VAR = 1006;
final static int RETURN = 1007;

final static int COMPARATOR = 1010;
final static int ASSIGN = 1011;

// A variable that will contain lexemes as they are recognized:
private static String lexeme;

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

%}

  /* Reglulegar skilgreiningar */

  /* Regular definitions */

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'

// Begin modified set of Regular Definitions:

_DELIM=[(){}\[\],;]
_NAME=[:letter:]([:letter:]|[_]|{_DIGIT})*
_OPNAME=[\+\-\*\/]|(\+\+)
_COMPARATOR=[!=]=|[<>]=?
_ASSIGN="="
_LITERAL={_STRING}|{_FLOAT}|{_CHAR}|{_INT}|null|true|false
_BRANCH="if"|"elseif"|"else"
_LOOP="while"|"for"
_VAR="var"
_RETURN="return"

%%

  /* Lesgreiningarreglur */
  /* Scanning rules */

"//".*$ {
}

{_DELIM} {
	lexeme = yytext();
	return yycharat(0);
}

{_LITERAL} {
	lexeme = yytext();
	return LITERAL;
}

{_BRANCH} {
	lexeme = yytext();
	return BRANCH;
}

{_LOOP} {
	lexeme = yytext();
	return LOOP;
}

{_VAR} {
	lexeme = yytext();
	return VAR;
}

{_RETURN} {
	lexeme = yytext();
	return RETURN;
}

{_NAME} {
	lexeme = yytext();
	return NAME;
}

{_COMPARATOR} {
	lexeme = yytext();
	return COMPARATOR;
}

{_ASSIGN} {
	lexeme = yytext();
	return ASSIGN;
}

{_OPNAME} {
	lexeme = yytext();
	return OPNAME;
}

[ \t\r\n\f] {
}

. {
	lexeme = yytext();
	return ERROR;
}
