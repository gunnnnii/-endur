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
%line
%column
%byaccj

%{

// This part becomes a verbatim part of the program text inside
// the class, NanoLexer.java, that is generated.

// Definitions of tokens:
final static int ERROR = -1;
final static int EOF = 0;
final static int DELIM = 1000;

final static int NAME = 1010;
final static int LITERAL = 1011;

final static int MOD_OP = 1020;
final static int MUL_OP = 1021;
final static int SUM_OP = 1022;
final static int CMP_OP = 1023;
final static int LOGIC_OP = 1024;

final static int ASSIGN = 1030;

final static int BRANCH = 1040;
final static int LOOP = 1041;
final static int VAR = 1042;
final static int RETURN = 1043;

// A variable that will contain lexemes as they are recognized:
private static String lexeme;

// Local variables containing a ring-buffer of tokens and lexemes,
//  the size of which is determined by the value passed to init(..)
private static int          bufferSize;
private static int          bufferAt;
private static String[]     lexBuffer;
private static int[]        tokBuffer;
private static int[]        lineBuffer;
private static int[]        colBuffer;
private static Boolean      eof;

// This runs the scanner:
public static void main( String[] args ) throws Exception
{
	NanoLexer lexer = new NanoLexer(new FileReader(args[0]));

	lexer.init(5);
	int token = 0;
	do {
	    token = lexer.peekToken(0);
	    String lex = lexer.peekLexeme(0);
	    System.out.println(""+token+": \'"+lex+"\'");
	    lexer.advance();
	} while (token != 0);

	/*
	int token = lexer.yylex();
	while( token!=0 )
	{
		System.out.println(""+token+": \'"+lexeme+"\'");
		token = lexer.yylex();
	}
	*/
}

public int line() {
    return lineBuffer[bufferAt] + 1;
}

public int column() {
    return colBuffer[bufferAt] + 1;
}

public int peekToken(int ahead)
        throws IllegalArgumentException
{
    if (ahead < 0 || ahead >= bufferSize) {
        throw new IllegalArgumentException();
    }

    return tokBuffer[ringIndex(bufferAt + ahead)];
}

public String peekLexeme(int ahead)
        throws IllegalArgumentException
{
    if (ahead < 0 || ahead >= bufferSize) {
        throw new IllegalArgumentException();
    }

    return lexBuffer[ringIndex(bufferAt + ahead)];
}

// ringIndex(..) method generates a valid index into the ring-
//  buffer from an arbitrary index (positive or negative).
private int ringIndex(int index) {
    while (index < 0) {
        index += bufferSize;
    }
    while (index >= bufferSize) {
        index -= bufferSize;
    }
    return index;
}

// advance(..) method populates the next slot in the ring buffer with the
//  next token/lexeme from the source file.  If EOF has already been reached,
//  then further advance(..) calls only generate more EOF.
public void advance()
        throws IOException
{
    if (eof) {
        lexBuffer[bufferAt] = "";
        tokBuffer[bufferAt] = 0;
        lineBuffer[bufferAt] = yyline;
        colBuffer[bufferAt] = yycolumn;
        bufferAt = ringIndex(bufferAt + 1);
    } else {
        int token = this.yylex();
        if (token != 0) {
            tokBuffer[bufferAt] = token;
            lexBuffer[bufferAt] = lexeme;
            lineBuffer[bufferAt] = yyline;
            colBuffer[bufferAt] = yycolumn;
            bufferAt = ringIndex(bufferAt + 1);
        } else {
            eof = true;
            this.advance();
        }
    }
}

// init(..) method initializes the scanner -- this entails generating
//  a ring buffer of size n and filling it with the first n tokens and
//  lexemes.  Note that if the file itself is shorter than n tokens, then
//  any tokens beyond the first EOF will also be EOF.  The lexeme associated
//  with the EOF instance in this case is \eps, the empty string.
public void init(int lookAhead)
        throws Exception
{
    if (lookAhead <= 0) {
        throw new IllegalArgumentException();
    }

    eof = false;
    bufferSize = lookAhead;
    bufferAt = 0;
    lexBuffer = new String[bufferSize];
    tokBuffer = new int[bufferSize];
    lineBuffer = new int[bufferSize];
    colBuffer = new int[bufferSize];

    // Fill out the initial state of the ring buffer in
    //  a quick loop.
    for (int i = 0; i < bufferSize; ++i) {
        this.advance();
    }
}

%}
  /* Regular definitions */

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'

// Begin modified set of Regular Definitions:

_DELIM=[(){}\[\],;]
_NAME=[:letter:]([:letter:]|[_]|{_DIGIT})*
_LOGIC_OP="&&"|"||"|"!"
_MOD_OP=(\+\+)|(\-\-)
_MUL_OP=[\*/]
_SUM_OP=[\+\-]
_COMPARATOR=[!=]=|[<>]=?
_ASSIGN="="
_LITERAL={_STRING}|{_FLOAT}|{_CHAR}|{_INT}|null|true|false
_BRANCH="if"|"elseif"|"else"
_LOOP="while"
_VAR="var"
_RETURN="return"

%%
  /* Scanning rules */

"//".*$ {
    // Single line comment.  Can be trailing or alone.
}

"/*"([^*]|("*"[^/]))*"*/" {
    // Multi-line or block comment.  Can be inline or not.
}

{_DELIM} {
	lexeme = yytext();
	return DELIM;
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
	return CMP_OP;
}

{_ASSIGN} {
	lexeme = yytext();
	return ASSIGN;
}

{_LOGIC_OP} {
    lexeme = yytext();
    return LOGIC_OP;
}

{_MOD_OP} {
    lexeme = yytext();
    return MOD_OP;
}

{_MUL_OP} {
    lexeme = yytext();
    return MUL_OP;
}

{_SUM_OP} {
    lexeme = yytext();
    return SUM_OP;
}

[ \t\r\n\f] {
}

. {
	lexeme = yytext();
	return ERROR;
}
