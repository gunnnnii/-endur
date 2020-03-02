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
final static int SEMICOLON = 1000;
final static int COMMA = 1001;
final static int OPEN_PAREN = 1002;
final static int OPEN_BLOCK = 1003;
final static int OPEN_BRACKET = 1004;
final static int CLOSE_PAREN = 1006;
final static int CLOSE_BLOCK = 1007;
final static int CLOSE_BRACKET = 1008;

final static int NAME = 1010;
final static int LITERAL = 1011;

final static int OP_INC = 1020;
final static int OP_DEC = 1021;
final static int OP_MUL = 1022;
final static int OP_DIV = 1023;
final static int OP_ADD = 1024;
final static int OP_SUB = 1025;
final static int OP_LIST = 1026;

final static int LGC_AND = 1030;
final static int LGC_OR = 1031;
final static int LGC_NOT = 1032;

final static int OP_SET = 1040;
final static int OP_CMP = 1041;

final static int BRN_IF = 1050;
final static int BRN_ELIF = 1051;
final static int BRN_ELSE = 1052;

final static int LP_WHILE = 1060;

// Miscellaneous keywords..
final static int VAR = 1070;
final static int RET = 1071;

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
        throws IOException, IllegalArgumentException
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

/* Revision to regular definitions.  Note that this list is a bit
    more comprehensive/exhaustive, and it may make life easier in
    the future, in exchange for a bit more code now. */
_NAME=[:letter:]([:letter:]|[_]|{_DIGIT})*
_LITERAL={_STRING}|{_FLOAT}|{_CHAR}|{_INT}|null|true|false
_LGC_AND="&&"
_LGC_OR="||"
_LGC_NOT="!"
_DELIM_SEMI=";"
_DELIM_COMMA=","
_DELIM_O_BRACKET="["
_DELIM_C_BRACKET="]"
_DELIM_O_PAREN="("
_DELIM_C_PAREN=")"
_DELIM_O_BLOCK="{"
_DELIM_C_BLOCK="}"
_OP_LIST=":"
_OP_MUL="*"
_OP_DIV="/"
_OP_ADD="+"
_OP_SUB="-"
_OP_INC="++"
_OP_DEC="--"
_OP_CMP=[!=]=|[<>]=?
_ASSIGN="="
_BRN_IF="if"
_BRN_ELIF="elseif"
_BRN_ELSE="else"
_LP_WHILE="while"
_VAR="var"
_RET="return"

%%
  /* Scanning rules */

"//".*$ {
    // Single line comment.  Can be trailing or alone.
}

"/*"([^*]|("*"[^/]))*"*/" {
    // Multi-line or block comment.  Can be inline or not.
}

/* Delimiter rules */
{_DELIM_SEMI} {
	lexeme = "";
	return SEMICOLON;
}

{_DELIM_COMMA} {
    lexeme = "";
    return COMMA;
}

{_DELIM_O_PAREN} {
	lexeme = "";
	return OPEN_PAREN;
}

{_DELIM_C_PAREN} {
	lexeme = "";
	return CLOSE_PAREN;
}

{_DELIM_O_BRACKET} {
	lexeme = "";
	return OPEN_BRACKET;
}

{_DELIM_C_BRACKET} {
	lexeme = "";
	return CLOSE_BRACKET;
}

{_DELIM_O_BLOCK} {
	lexeme = "";
	return OPEN_BLOCK;
}

{_DELIM_C_BLOCK} {
	lexeme = "";
	return CLOSE_BLOCK;
}


/* Literals may contain keywords. */
{_LITERAL} {
	lexeme = yytext();
	return LITERAL;
}


/* Keyword rules precede NAME */
{_BRN_IF} {
	lexeme = "";
	return BRN_IF;
}

{_BRN_ELIF} {
	lexeme = "";
	return BRN_ELIF;
}

{_BRN_ELSE} {
	lexeme = "";
	return BRN_ELSE;
}

{_LP_WHILE} {
	lexeme = "";
	return LP_WHILE;
}

{_VAR} {
	lexeme = "";
	return VAR;
}

{_RET} {
	lexeme = "";
	return RET;
}

/* NAME cannot be a keyword. */
{_NAME} {
	lexeme = yytext();
	return NAME;
}

/* Comparison operators before assignment. */
{_OP_CMP} {
	lexeme = yytext();
	return OP_CMP;
}

{_ASSIGN} {
	lexeme = "";
	return OP_SET;
}

/* Logical operator rules. */
{_LGC_AND} {
    lexeme = yytext();
    return LGC_AND;
}

{_LGC_OR} {
    lexeme = yytext();
    return LGC_OR;
}

{_LGC_NOT} {
    lexeme = yytext();
    return LGC_NOT;
}

/* List operator. */
{_OP_LIST} {
    lexeme = yytext();
    return OP_LIST;
}

/* Mathematical rules.  Note that inc/dec operators have precedence. */
{_OP_INC} {
    lexeme = yytext();
    return OP_INC;
}

{_OP_DEC} {
    lexeme = yytext();
    return OP_DEC;
}

{_OP_MUL} {
    lexeme = yytext();
    return OP_MUL;
}

{_OP_DIV} {
    lexeme = yytext();
    return OP_DIV;
}

{_OP_ADD} {
    lexeme = yytext();
    return OP_ADD;
}

{_OP_SUB} {
    lexeme = yytext();
    return OP_SUB;
}

[ \t\r\n\f] {
}

. {
	lexeme = yytext();
	return ERROR;
}
