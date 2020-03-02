import java.io.IOException;
import java.io.FileNotFoundException;

/*
Fer ekki alveg eftir þessu, en studdist við þetta og gerði breytingar

En þetta er annars mjög bruteforce parser og eg held að hann virki ekki alveg 100% 
en hann virkar á testið.

program		=	{ function }
			;

function	= 	NAME, '(', [ NAME, { ',', NAME } ] ')'
				'{', { decl, ';' }, { expr, ';' }, '}'
			;

decl		=	'var', NAME, { ',', NAME }
			;

expr		=	'return', expr
			|	NAME, '=', expr
			|	orexpr
			;

orexpr		=	andexpr, [ '||', orexpr ]
			;

andexpr		=	notexpr, [ '&&', andexpr ]
			;

notexpr		=	'!', notexpr | binopexpr1
			;

binopexpr1	=	binopexpr2, { OPNAME1 binopexpr2 }
			;

binopexpr2	=	binopexpr3, [ OPNAME2, binopexpr2 ]
			;

binopexpr3	=	binopexpr4, { OPNAME3, binopexpr4 }
			;


smallexpr	=	NAME
			|	NAME, '(', [ NAME, { ',', NAME } ], ')'
			|	OPNAME, smallexpr
			| 	LITERAL 
			|	'(', expr, ')'
			|	ifexpr
			|	'while', '(', expr, ')', body
			
opname		=	OPNAME1
			|	OPNAME2
			|	OPNAME3

			;
ifexpr		= 'if', '(', expr, ')', body, elsepart
			;

elsepart	=    emptystring 
|                |'else', body
|                |'elsif', '(', expr, ')', body, elsepart
;
			

body		=	'{', { expr, ';' }, '}'
			

*/

import java.io.FileReader;

public class Parser {

    private NanoLexer lex;
    

    public Parser(String in) throws Exception {
        lex = new NanoLexer(new FileReader(in));
        
    }
    
   
    public void program() throws Exception 
    { 
       
        lex.advance();
        while(lex.getToken() != 0){
            function();
            lex.advance();
        } 
    } 
  
    
    public void function() throws Exception {

        if (lex.getToken() != NanoLexer.NAME) { 
            throw new Exception("Syntax error at line: "+lex.lineNr());
        } 
        else {
            lex.advance();
        }

        if(!lex.getLexeme().equals("(")){
            throw new Exception("Syntax error at line: "+lex.lineNr());
        } 
        else {
            lex.advance();
        }

        if(lex.getToken() == NanoLexer.NAME){
            while(lex.getToken() == NanoLexer.NAME){    
                lex.advance();
                
                if(lex.getLexeme().equals(",")){
                    lex.advance();
                } 

            }
        }

        if(!lex.getLexeme().equals(")") ){
            throw new Exception("Syntax error at line: "+lex.lineNr());
        } 
        else {
            lex.advance();
        }
        
        if(!lex.getLexeme().equals("{") ){
            throw new Exception("Syntax error at line: "+lex.lineNr());
        } 
        else {
            lex.advance();
        } 

        while(lex.getToken()==NanoLexer.VAR){
            decl();
        }
        
        while(!lex.getLexeme().equals("}")){
            expr();
            checkSemiCol();
        }
        
        //expr();
        
        //lex.advance();

        if(!lex.getLexeme().equals("}") ){
            throw new Exception("Syntax error at line: "+lex.lineNr());
        } 
        
    } 
    
    public void decl() throws Exception {

        if(lex.getLexeme().equals("}")){
            return;
        }

        if(lex.getToken() != NanoLexer.VAR){
            throw new Exception("Syntax error at line: "+lex.lineNr());
        } 
        else {
            lex.advance();
        }
        
        if(lex.getToken() == NanoLexer.NAME){
            while(lex.getToken() == NanoLexer.NAME){    
                lex.advance();

                if(lex.getLexeme().equals(",")){
                    lex.advance();
                } 

            }
        } 
        else {
            throw new Exception("Syntax error at line: "+lex.lineNr());
        }
        checkSemiCol();
    }

    public void expr() throws Exception{
        
        

        if(lex.getToken() == NanoLexer.RETURN){
            lex.advance();
            expr();
            
        }

        else if(lex.getToken() == NanoLexer.NAME){
            lex.advance();
            assign();
                  
        }
       
        else {
            orexpr();
            
        }
        
    }

    public void assign() throws Exception{
        if(lex.getToken() == NanoLexer.ASSIGNMENT){
            lex.advance();
            smallexpr();
        }
        else {
            orexpr();
        }
    }
    public void orexpr() throws Exception{
        andexpr();

        if(lex.getToken() == NanoLexer.LOGIC_OR){
            lex.advance();
            orexpr();
        }
       
    }

    public void andexpr() throws Exception{
        binopexpr1();

        if(lex.getToken() == NanoLexer.LOGIC_AND){
            lex.advance();
            andexpr();
        }
    }


    public void binopexpr1() throws Exception{
        binopexpr2();
        
        if(lex.getToken() == NanoLexer.COMPARATOR_OP){
            lex.advance();
            binopexpr1();
        }
    }

    public void binopexpr2() throws Exception{
        binopexpr3();

        if(lex.getToken() == NanoLexer.ADDITIVE_OP){
            lex.advance();
            binopexpr2();
        }
    }

    public void binopexpr3() throws Exception{
        smallexpr();

        if(lex.getToken() == NanoLexer.MULT_OP){
            lex.advance();
            binopexpr3();
        }
    }



    public void smallexpr() throws Exception{

        if(lex.getToken() == NanoLexer.NAME){
            lex.advance();
        

            if(lex.getLexeme().equals("("))
                lex.advance();

                while(!lex.getLexeme().equals(")")){    
                    expr();
                    lex.advance();
                    if(lex.getLexeme().equals(",")){
                        lex.advance();
                    } 
                    else break;
    
                }
            
                if(lex.getLexeme().equals(")")){    
                    lex.advance();
                }
                else {
                    throw new Exception("Syntax error at line: "+lex.lineNr());
                }
            }
           
        
        
        else if(opname()){
            lex.advance();
            smallexpr();
        }
        else if(lex.getToken() == NanoLexer.LITERAL){
            lex.advance();
        }
        else if(lex.getLexeme().equals("(")){
            lex.advance();
            
            expr();
            if(lex.getLexeme().equals(")")){
                lex.advance();
            } 
            else {
                throw new Exception("Syntax error at line: "+lex.lineNr());          
            }
        }
        else if(lex.getToken() == NanoLexer.IF){
            lex.advance();
            ifexpr();
        }
        else if(lex.getToken() == NanoLexer.WHILE){
            lex.advance();
            if(lex.getLexeme().equals("(")){
                lex.advance();
                expr();
            }
            else{
                throw new Exception("Syntax error at line: "+lex.lineNr());
            }
            if(lex.getLexeme().equals(")")){
                lex.advance();
                body();
                lex.advance();
            }
        }  

    }

    public boolean opname() throws Exception{
        
        switch (lex.getToken()) {
            case NanoLexer.ASSIGNMENT:
                return true;

            case NanoLexer.COMPARATOR_OP:
                return true;

            case NanoLexer.ADDITIVE_OP:
                return true;

            case NanoLexer.MULT_OP:
                return true;

  
            default:
                return false;

        }
    }

    public void ifexpr() throws Exception{
 
        if(lex.getLexeme().equals("(")){
            lex.advance();
        }
        else{
            throw new Exception("Syntax error at line: "+lex.lineNr());
        }

        expr();

        if(lex.getLexeme().equals(")")){
            lex.advance();
        }
        else{
            throw new Exception("Syntax error at line: "+lex.lineNr());
        }

        body();
        lex.advance();
        elsepart();
    
    
    }

    public void elsepart() throws Exception{

        if(lex.getToken() == NanoLexer.ELSEIF){
            lex.advance();
            if(lex.getLexeme().equals("(")){
                lex.advance();
            }
            else{
                throw new Exception("Syntax error at line: "+lex.lineNr());
            }

            expr();
            
            
            if(lex.getLexeme().equals(")")){
                lex.advance();
            }
            else{
                throw new Exception("Syntax error at line: "+lex.lineNr());
            }
            
            body();
            lex.advance();
            elsepart();
        }
        else if(lex.getToken() == NanoLexer.ELSE){
            lex.advance();
            body();
            lex.advance();
        }
        else{
            return;
        }
    }

    public void body() throws Exception{

        if(lex.getLexeme().equals("{")){
            lex.advance();
        }
        else{
            throw new Exception("Syntax error at line: "+lex.lineNr());

        }
        while(!lex.getLexeme().equals("}")){
            expr();
            checkSemiCol();
        }
            // semikomma     
    }


    public void checkSemiCol()  throws Exception{
        if (lex.getLexeme().equals(";")){
            lex.advance();
        }
        else{
            throw new Exception("Syntax error at line: "+lex.lineNr());
        }


    }

    


    public static void main(String[] args) throws Exception {
        Parser p = new Parser(args[0]);
        p.program();
    }

}