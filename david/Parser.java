// Parser For compilers course

// Uses output of nanolexer.jflex -> nanolexer.java process
// Parses a NanoMorpho-esqe grammar, producing no output
// if successful, or an error message if invalid grammer is
// detected which cannot be parsed.

// CFG for our variant of NanoMorpho:
/*
		Do we want a program to be able to consist of multiple functions?
	<program>		::=		<function>
							<function> <program>

	<function>		::=		<NAME> '(' <arg_list> ')' <func_body>

	<arg_list>		::=		\eps
							<NAME> <arg_tail>

	<arg_tail>		::=		\eps
							',' <NAME> <arg_tail>

	<func_body>		::=		'{' <decl_list> <expr_list> '}'

	<decl_list>		::=		\eps
							<decl> ';' <decl_list>

	<expr_list>		::=		<expr> ';' <expr_tail>

	<expr_tail>		::=		\eps
							<expr> ';' <expr_tail>

	<decl>			::=		'var' <NAME> <decl_tail>

	<decl_tail>		::=		\eps
							',' <NAME> <decl_tail>

	<expr>			::=		<and_expr>
							<NAME> '=' <expr>
							<NAME> '(' <value_list> ')'
							'return' <expr>
							<branch_expr>
							<loop_expr>

		Unary prefix operation: -var, --var, ++var
	<prefix_expr>	::=		<MOD_OP> <NAME>
							'-' <NAME>

		Unary postfix operation: var--, var++
	<postfix_expr>	::=		<NAME> <MOD_OP>

	<and_expr>		::=		<or_expr>
							<or_expr> '&&' <and_expr>

	<or_expr>		::=		<not_expr>
							<not_expr> '||' <or_expr>

	<not_expr>		::=		<cmp_expr>
							'!' <cmp_expr>

	<cmp_expr>		::=		<sum_expr>
							<sum_expr> <CMP_OP> <sum_expr>

	<sum_expr>		::=		<mul_expr>
							<mul_expr> <BIN_SUM_OP> <sum_expr>

	<mul_expr>		::=		<value_expr>
							<value_expr> <BIN_MUL_OP> <mul_expr>

	<value_expr>	::=		<NAME>
							<LITERAL>
							<prefix_expr>
							<postfix_expr>
							'(' <expr> ')'

	<value_list>	::=		\eps
					::=		<value_expr> <value_tail>

	<value_tail>	::=		',' <value_expr> <value_tail>

	<branch_expr>	::=		'if' '(' <and_expr> ')' <block> <branch_opt> <branch_final>

	<branch_opt>	::=		\eps
							'elseif' '(' <and_expr> ')' <block> <branch_opt>

	<branch_final>	::=		\eps
							'else' <block>

	<loop_expr>		::=		'while' '(' <and_expr> ')' <block>

	<block>			::=		'{' <expr_list> '}'
 */

import java.io.FileReader;
import java.util.Stack;


public class Parser {
	private final NanoLexer			lexer;
	private final Stack<String>		scope;

	// Constructor builds and initializes lexer.
	public Parser(String file)
			throws Exception
	{
		scope = new Stack<>();
		lexer = new NanoLexer(new FileReader(file));
		lexer.init(5);
	}




	private void p_program()
			throws Exception
	{
		scope.push("<program>");
		while (lexer.peekToken(0) != lexer.EOF) {
			p_function();
		}
		scope.pop();
	}

	private void p_function()
			throws Exception
	{
		scope.push("<function>");
		Fragment f = peek(0);

		if (f.token != lexer.NAME) {
			err(map(lexer.NAME), f);
		} else {
			lexer.advance();
		}

		consumeDelim("(");

		p_arg_list();

		consumeDelim(")");

		p_func_body();
		scope.pop();
	}

	private void p_arg_list()
			throws Exception
	{
		scope.push("<arg_list>");
		Fragment f = peek(0);

		if (f.token != lexer.NAME) {
			scope.pop();
			return;
		} else {
			lexer.advance();
		}

		p_arg_tail();
		scope.pop();
	}

	private void p_arg_tail()
			throws Exception
	{
		scope.push("<arg_tail>");
		Fragment f = peek(0);
		if (!checkDelim(",")) {
			scope.pop();
			return;
		} else {
			lexer.advance();
		}

		f = peek(0);
		if (f.token != lexer.NAME) {
			err(map(lexer.NAME), f);
		} else {
			lexer.advance();
		}

		scope.pop();
		p_arg_tail();
	}

	private void p_func_body()
			throws Exception
	{
		scope.push("<func_body>");
		consumeDelim("{");

		p_decl_list();
		p_expr_list();

		consumeDelim("}");
		scope.pop();
	}

	private void p_decl_list()
			throws Exception
	{
		scope.push("<decl_list>");

		// Declaration list could be empty or not, so we must check FIRST(<decl>) in
		//	order to know whether to expand or end.
		// FIRST(<decl>) = { <VAR> }
		Fragment f = peek(0);
		if (f.token != lexer.VAR) {
			scope.pop();
			return;
		} else {
			p_decl();
			consumeDelim(";");
			scope.pop();
			p_decl_list();
		}
	}

	private void p_decl()
			throws Exception
	{
		scope.push("<decl>");
		Fragment f = peek(0);

		if (f.token != lexer.VAR) {
			err(map(lexer.VAR), f);
		} else {
			lexer.advance();
		}

		f = peek(0);
		if (f.token != lexer.NAME) {
			err(map(lexer.NAME), f);
		} else {
			lexer.advance();
		}

		p_decl_tail();

		scope.pop();
	}

	private void p_decl_tail()
			throws Exception
	{
		scope.push("<decl_tail>");
		Fragment f = peek(0);

		if (!checkDelim(",")) {
			scope.pop();
			return;
		} else {
			lexer.advance();
		}

		f = peek(0);
		if (f.token != lexer.NAME) {
			err(map(lexer.NAME), f);
		} else {
			lexer.advance();
		}

		scope.pop();
		p_decl_tail();
	}

	private void p_expr_list()
			throws Exception
	{
		scope.push("<expr_list>");

		p_expr();

		consumeDelim(";");

		p_expr_tail();

		scope.pop();
	}

	private void p_expr_tail()
			throws Exception
	{
		scope.push("<expr_tail>");

		// NOTE: FOLLOW(<expr_tail>) = { "}" }, thus to determine whether to
		//	expand the tail or end, we check whether the current token is "}"
		if (checkDelim("}")) {
			scope.pop();
			return;
		} else {
			p_expr();

			consumeDelim(";");

			scope.pop();
			p_expr_tail();
		}
	}

	private void p_expr()
			throws Exception
	{
		scope.push("<expr>");
		Fragment f = peek(0);
		if (f.token == lexer.LOOP) {
			p_loop_expr();
		} else if (f.token == lexer.BRANCH) {
			p_branch_expr();
		} else if (f.token == lexer.RETURN) {
			lexer.advance();
			p_expr();
		} else {
			// Here we need to check the next token further:
			Fragment f2 = peek(1);
			if (f2.token == lexer.ASSIGN) {
				// <expr> -> <NAME> '=' <expr>
				if (f.token != lexer.NAME) {
					err(map(lexer.NAME), f);
				} else {
					lexer.advance();
					lexer.advance();	// Already checked 2nd token is '='
					p_expr();
				}
			} else if (f2.delim("(")) {
				// <expr> -> <NAME> '(' <value_list> ')'
				if (f.token != lexer.NAME) {
					err(map(lexer.NAME), f);
				} else {
					lexer.advance();
					consumeDelim("(");
					p_value_list();
					consumeDelim(")");
				}
			} else {
				p_and_expr();
			}
		}

		scope.pop();
	}

	private void p_and_expr()
			throws Exception
	{
		scope.push("<and_expr>");

		p_or_expr();

		Fragment f = peek(0);
		if (!f.check(lexer.LOGIC_OP, "&&")) {
			scope.pop();
			return;
		} else {
			lexer.advance();
			scope.pop();
			p_and_expr();
		}
	}

	private void p_or_expr()
			throws Exception
	{
		scope.push("<or_expr>");

		p_not_expr();

		Fragment f = peek(0);
		if (!f.check(lexer.LOGIC_OP, "||")) {
			scope.pop();
			return;
		} else {
			lexer.advance();
			scope.pop();
			p_or_expr();
		}
	}

	private void p_not_expr()
			throws Exception
	{
		scope.push("<not_expr>");
		Fragment f = peek(0);
		if (f.check(lexer.LOGIC_OP, "!")) {
			lexer.advance();
		}
		p_cmp_expr();
		scope.pop();
	}

	private void p_cmp_expr()
			throws Exception
	{
		scope.push("<cmp_expr>");

		p_sum_expr();

		Fragment f = peek(0);
		if (f.token == lexer.CMP_OP) {
			lexer.advance();
			p_sum_expr();
		}

		scope.pop();
	}

	private void p_sum_expr()
			throws Exception
	{
		scope.push("<sum_expr>");

		p_mul_expr();

		Fragment f = peek(0);
		if (f.token == lexer.SUM_OP) {
			lexer.advance();
			p_sum_expr();
		}

		scope.pop();
	}

	private void p_mul_expr()
			throws Exception
	{
		scope.push("<mul_expr>");

		p_value_expr();

		Fragment f = peek(0);
		if (f.token == lexer.MUL_OP) {
			lexer.advance();
			p_mul_expr();
		}

		scope.pop();
	}

	private void p_value_expr()
			throws Exception
	{
		scope.push("<value_expr>");
		Fragment f = peek(0);
		Fragment f2 = peek(1);

		if (f2.token == lexer.MOD_OP) {
			p_postfix_expr();
		} else if (f.token == lexer.MOD_OP || f.check(lexer.SUM_OP, "-")) {
			p_prefix_expr();
		} else if (f.check(lexer.DELIM, "(")) {
			lexer.advance();
			p_expr();
			consumeDelim(")");
		} else if (f.token == lexer.NAME || f.token == lexer.LITERAL) {
			// <NAME> or <LITERAL>
			lexer.advance();
		} else {
			err(map(-2), f);
		}

		scope.pop();
	}

	private void p_value_list()
			throws Exception
	{
		// FOLLOW(<value_list>) is only { ")" }, so we check against this
		//	to determine whether to expand or epsilon.
		scope.push("<value_list>");
		Fragment f = peek(0);
		if (f.check(lexer.DELIM, ")")) {
			scope.pop();
			return;
		} else {
			p_value_expr();
			p_value_tail();
		}

		scope.pop();
	}

	private void p_value_tail()
			throws Exception
	{
		scope.push("<value_tail>");
		Fragment f = peek(0);

		if (!f.check(lexer.DELIM, ",")) {
			scope.pop();
			return;
		} else {
			consumeDelim(",");
			p_value_expr();
			scope.pop();
			p_value_tail();
		}
	}

	private void p_prefix_expr()
			throws Exception
	{
		scope.push("<prefix_expr>");
		Fragment f = peek(0);
		if (f.token == lexer.MOD_OP || f.check(lexer.SUM_OP, "-")) {
			lexer.advance();

			f = peek(0);
			if (f.token != lexer.NAME) {
				err(map(lexer.NAME), f);
			} else {
				lexer.advance();
			}
		} else {
			// Note: Error message doesn't mention prefix '-'
			err(map(lexer.MOD_OP), f);
		}

		scope.pop();
	}

	private void p_postfix_expr()
			throws Exception
	{
		scope.push("<postfix_expr>");
		Fragment f = peek(0);
		if (f.token != lexer.NAME) {
			err(map(lexer.NAME), f);
		} else {
			lexer.advance();
			f = peek(0);
			if (f.token != lexer.MOD_OP) {
				err(map(lexer.MOD_OP), f);
			} else {
				lexer.advance();
			}
		}

		scope.pop();
	}

	private void p_branch_expr()
			throws Exception
	{
		scope.push("<branch_expr>");
		Fragment f = peek(0);
		if (!f.check(lexer.BRANCH, "if")) {
			err(map(lexer.BRANCH), f);
		} else {
			lexer.advance();
			consumeDelim("(");
			p_and_expr();
			consumeDelim(")");
			p_block();
			p_branch_opt();
			p_branch_final();
		}

		scope.pop();
	}

	private void p_branch_opt()
			throws Exception
	{
		scope.push("<branch_opt>");
		Fragment f = peek(0);
		if (!f.check(lexer.BRANCH, "elseif")) {
			scope.pop();
			return;
		} else {
			lexer.advance();
			consumeDelim("(");
			p_and_expr();
			consumeDelim(")");
			p_block();
			scope.pop();
			p_branch_opt();
		}
	}

	private void p_branch_final()
			throws Exception
	{
		scope.push("<branch_final>");
		Fragment f = peek(0);

		if (f.check(lexer.BRANCH, "else")) {
			lexer.advance();
			p_block();
		}

		scope.pop();
	}

	private void p_loop_expr()
			throws Exception
	{
		scope.push("<loop_expr>");
		Fragment f = peek(0);

		if (f.check(lexer.LOOP, "while")) {
			lexer.advance();
			consumeDelim("(");
			p_and_expr();
			consumeDelim(")");
			p_block();
		}

		scope.pop();
	}

	private void p_block()
			throws Exception
	{
		scope.push("<block>");

		consumeDelim("{");
		p_expr_list();
		consumeDelim("}");

		scope.pop();
	}



	private void consumeDelim(String d)
			throws Exception
	{
		Fragment f = peek(0);
		if (f.token != lexer.DELIM || !f.lex.equals(d)) {
			err(fmt(lexer.DELIM, d), f);
		} else {
			lexer.advance();
		}
	}

	private Boolean checkDelim(String d)
			throws Exception
	{
		Fragment f = peek(0);
		if (f.token == lexer.DELIM && f.lex.equals(d)) {
			return true;
		} else {
			return false;
		}
	}

	// TODO: Ensure mapping is ACCURATE.
	private String map(int token) {
		switch (token) {
			case NanoLexer.ERROR:		return "<ERROR>";
			case NanoLexer.EOF:			return "<EOF>";
			case NanoLexer.DELIM:		return "<DELIM>";
			case NanoLexer.NAME:		return "<NAME>";
			case NanoLexer.LITERAL:		return "<LITERAL>";
			case NanoLexer.MOD_OP:		return "<MOD_OP>";
			case NanoLexer.MUL_OP:		return "<MUL_OP>";
			case NanoLexer.SUM_OP:		return "<SUM_OP>";
			case NanoLexer.CMP_OP:		return "<CMP_OP>";
			case NanoLexer.LOGIC_OP:	return "<LOGIC_OP>";
			case NanoLexer.ASSIGN:		return "<ASSIGN>";
			case NanoLexer.BRANCH:		return "<BRANCH>";
			case NanoLexer.LOOP:		return "<LOOP>";
			case NanoLexer.VAR:			return "<VAR>";
			case NanoLexer.RETURN:		return "<RETURN>";
			default:					return "<???>";
		}
	}

	private String fmt(int type, String specific) {
		return new String("[" + map(type) + ", \"" + specific + "\"]");
	}

	private void err(String expected, Fragment rec)
			throws GrammarException
	{
		throw new GrammarException(lexer.line(), lexer.column(), scope.peek(), expected, fmt(rec.token, rec.lex));
	}

	public void parse()
			throws Exception
	{
		p_program();
	}

	public void printSymbolStack() {
		int indent = 0;
		for(String s : scope) {
			String out = "";
			for (int i = 0; i < indent; ++i) {
				out = out + " ";
			}
			System.out.println(out + s);
			++indent;
		}
	}

	public static void main(String[] args) {
		Parser p = null;

		try {
			p = new Parser(args[0]);
			p.parse();

			// Sanity check -- scope stack should be empty.
			if (!p.scope.isEmpty()) {
				System.out.println("Scope error: Stack not empty.");
			}
		} catch (GrammarException e) {
			System.out.println(e.getMessage());
			p.printSymbolStack();
		} catch (Exception e) {
			System.out.println("Unhandled exception: " + e.getMessage());
		}
	}

	public class GrammarException extends java.lang.Exception {
		public GrammarException(int line, int col, String intermediate, String expected, String received) {
			super("Parse error [line=" + Integer.toString(line) + " col=" + Integer.toString(col) + "] in: " + intermediate + ", expected " + expected + ", got " + received);
		}
	}

	private class Fragment {
		public int		token;
		public String	lex;

		public Fragment() {
			token = 0;
			lex = new String("");
		}

		public Boolean delim(String specific) {
			return check(NanoLexer.DELIM, specific);
		}

		public Boolean check(int type, String specific) {
			if (token == type && lex.equals(specific)) {
				return true;
			} else {
				return false;
			}
		}
	}

	private Fragment peek(int ahead)
			throws IllegalArgumentException
	{
		Fragment out = new Fragment();
		out.token = lexer.peekToken(ahead);
		out.lex = lexer.peekLexeme(ahead);
		return out;
	}
}