
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
final static int NAME = 1001;
final static int LITERAL = 1002;
final static int IF= 1003;
final static int ELSEIF= 1004;
final static int ELSE= 1005;
final static int WHILE= 1006;
final static int RETURN= 1007;
final static int VAR= 1008;
final static int ASSIGNMENT= 1009;
final static int COMPARATOR_OP = 1011;
final static int ADDITIVE_OP = 1012;
final static int MULT_OP = 1013;
final static int LOGIC_OR = 1014;
final static int LOGIC_AND = 1015;


private static String lexeme;
private static int token;

public static void main( String[] args ) throws Exception
{
	NanoLexer lexer = new NanoLexer(new FileReader(args[0]));

	

	lexer.advance();

	while( token!=0 )
	{
		System.out.println(""+token+": \'"+lexeme+"\'");
        token =lexer.getToken();
        lexer.advance();

	}
	
}

public int lineNr() {
    return yyline + 1;

}

public int columnNr() {
    return yycolumn + 1;
}

public int getToken()  throws Exception{
	return token;
}

public String getLexeme(){
	return lexeme;
}

public void advance()throws IOException {
    token=this.yylex();
}

%}
  /* Regular definitions */

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
_DELIM=[(){}\[\],;]
_NAME=[:letter:]([:letter:]|[_]|{_DIGIT})*
_LOGIC_OR="||"
_LOGIC_AND="&&"
_MULT_OP=[\*/]
_ADDITIVE_OP=[\+\-]
_COMPARATOR_OP=[!=]=
_ASSIGNMENT="="
_LITERAL={_STRING}|{_FLOAT}|{_CHAR}|{_INT}|null|true|false
_IF="if"
_ELSEIF="elseif"
_ELSE ="else"
_WHILE="while"
_VAR="var"
_RETURN="return"

%%
  /* Scanning rules */

"//".*$ {
    
}

"/*"([^*]|("*"[^/]))*"*/" {
   
    }

{_DELIM} {
	lexeme = yytext();
    token = yycharat(0);
	return yycharat(0);
}

{_LITERAL} {
	lexeme = yytext();
    token = LITERAL;
	return LITERAL;
}

{_IF} {
	lexeme = yytext();
    token = IF;
	return IF;
}

{_ELSE} {
	lexeme = yytext();
    token = ELSE;
	return ELSE;
}

{_ELSEIF} {
	lexeme = yytext();
    token = ELSEIF;
	return ELSEIF;
}

{_WHILE} {
	lexeme = yytext();
    token = WHILE;
	return WHILE;
}

{_VAR} {
	lexeme = yytext();
    token = VAR;
	return VAR;
}

{_RETURN} {
	lexeme = yytext();
	return RETURN;
}

{_NAME} {
	lexeme = yytext();
    token = NAME;
	return NAME;
}

{_COMPARATOR_OP} {
	lexeme = yytext();
    token = COMPARATOR_OP;
	return COMPARATOR_OP;
}

{_ASSIGNMENT} {
	lexeme = yytext();
    token = ASSIGNMENT;
	return ASSIGNMENT;
}

{_LOGIC_OR} {
    lexeme = yytext();
    token = LOGIC_OR;
    return LOGIC_OR;
}

{_LOGIC_AND} {
    lexeme = yytext();
    token = LOGIC_AND;
    return LOGIC_AND;
}


{_MULT_OP} {
    lexeme = yytext();
    token = MULT_OP;
    return MULT_OP;
}

{_ADDITIVE_OP} {
    lexeme = yytext();
    token = ADDITIVE_OP;
    return ADDITIVE_OP;
}

[ \t\r\n\f] {
}



. {
	lexeme = yytext();
    token = ERROR;
	return ERROR;
}