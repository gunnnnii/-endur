import java.io.*;

%%
%public
%class Lexer
%unicode
%byaccj

%{
  final static int ERROR = -1;
  final static int NAME = 1001;
  final static int OPNAME = 1002;
  final static int LITERAL = 1003;
  final static int VAR = 1004;
  final static int RETURN = 1005;
  final static int IF = 1006;
  final static int ELSE = 1007;
  final static int ELSEIF = 1008;
  final static int WHILE = 1009;
  // A variable that will contain lexemes as they are recognized:
  private static String lexeme;

  // This runs the scanner:
  public static void main( String[] args ) throws Exception
  {
    Lexer lexer = new Lexer(new FileReader(args[0]));
    int token = lexer.yylex();
    while( token!=0 )
    {
      System.out.println(""+token+": \'"+lexeme+"\'");
      token = lexer.yylex();
    }
  }
%}

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
_DELIM=(,|;|[{}]|[()])
_NAME=([:letter:]|[\+\-*/!%=><\:\^\~&|?]|{_DIGIT})+
_OPNAME=[\+\-*/!%=><\:\^\~&|?]

%%

{_DELIM} {
  lexeme = yytext();
  return yycharat(0);
}

{_STRING} | {_FLOAT} | {_INT} | {_CHAR} {
  lexeme = yytext();
  return LITERAL;
}

"if" {
  lexeme = yytext();
  return IF;
}

"elseif" {
  lexeme = yytext();
  return ELSEIF;
}

"else" {
  lexeme = yytext();
  return ELSE;
}

"var" {
  lexeme = yytext();
  return VAR;
}

"return" {
  lexeme = yytext();
  return RETURN;
}

"#".* {
  // comment
}

^(##)(.|\n)*(##)$ {
  // multiline comment
}

[ \t\r\n\f] {
  // whitespace
}

{_OPNAME} {
  lexeme = yytext();
  return OPNAME;
}

{_NAME} {
  lexeme = yytext();
  return NAME;
}

. {
  lexeme = yytext();
  return ERROR;
}