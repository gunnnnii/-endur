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

		NOTE: Parsing rolled into p_arg_list(..)
	<arg_tail>		::=		\eps
							',' <NAME> <arg_tail>

		NOTE: Parsing rolled into p_function(..)
	<func_body>		::=		'{' <decl_list> <expr_list> '}'

	<decl_list>		::=		\eps
							<decl> ';' <decl_list>

	<expr_list>		::=		<expr> ';' <expr_tail>

		NOTE: Parsing rolled into p_expr_list(..)
	<expr_tail>		::=		\eps
							<expr> ';' <expr_tail>

	<decl>			::=		'var' <NAME> <decl_tail>

		NOTE: Parsing rolled into p_decl(..)
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

		NOTE: Parsing rolled into p_value_list(..)
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
import java.io.FileWriter;
import java.util.Stack;


public class Parser {
	private final NanoLexer			lexer;
	private final Stack<String>		scope;
	private TcProgram				tree;

	// Constructor builds and initializes lexer.
	public Parser(String file)
			throws Exception
	{
		scope = new Stack<>();
		lexer = new NanoLexer(new FileReader(file));
		lexer.init(5);
	}




	// WIP: TreeCode
	private void p_program()
			throws Exception
	{
		scope.push("<program>");

		Stack<TcFunction> defs = new Stack<>();
		SymbolTable dummy = new SymbolTable(); 	// We don't need this one, but TcProgram
												//	requires a non-null table.  Maybe change?

		while (lexer.peekToken(0) != lexer.EOF) {
			defs.push(p_function());
		}

		tree = new TcProgram(dummy, "code", "main", defs.toArray(new TcFunction[0]));

		scope.pop();
	}

	// WIP: TreeCode
	private TcFunction p_function()
			throws Exception
	{
		scope.push("<function>");
		Fragment f = peek(0);

		// Each function has its own Symbol Table.
		SymbolTable 		table = new SymbolTable();
		String				name = "";
		String[]			args = null;
		String[]			decls = null;
		TreeCode[]			expr = null;

		if (f.token != lexer.NAME) {
			err(map(lexer.NAME), f);
		} else {
			name = f.lex;
			lexer.advance();
		}

		consumeDelim("(");

		args = p_arg_list();

		consumeDelim(")");
		consumeDelim("{");

		decls = p_decl_list(null);

		expr = p_expr_list(table);

		consumeDelim("}");

		TcFunction code = new TcFunction(table, name, args, decls, expr);

		scope.pop();
		return code;
	}

	// WIP: TreeCode
	private String[] p_arg_list()
			throws Exception
	{
		scope.push("<arg_list>");
		Fragment f = peek(0);

		Stack<String> args = new Stack<>();

		if (f.token != lexer.NAME) {
			scope.pop();
			return new String[0];
		} else {
			args.push(f.lex);
			lexer.advance();
		}

		f = peek(0);
		while (f.check(lexer.DELIM, ",")) {
			lexer.advance();
			f = peek(0);
			if (f.token != lexer.NAME) {
				err(map(lexer.NAME), f);
			} else {
				args.push(f.lex);
				lexer.advance();
				f = peek(0);
			}
		}

		scope.pop();
		return args.toArray(new String[0]);
	}

	// WIP: TreeCode
	private String[] p_decl_list(Stack<String> current)
			throws Exception
	{
		scope.push("<decl_list>");

		Stack<String> decls = (current != null ? current : new Stack<String>());

		// Declaration list could be empty or not, so we must check FIRST(<decl>) in
		//	order to know whether to expand or end.
		// FIRST(<decl>) = { <VAR> }
		Fragment f = peek(0);
		if (f.token != lexer.VAR) {
			scope.pop();
			return decls.toArray(new String[0]);
		} else {
			p_decl(decls);
			consumeDelim(";");
			scope.pop();
			return p_decl_list(decls);
		}
	}

	// WIP: TreeCode
	private void p_decl(Stack<String> decls)
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
			decls.push(f.lex);
			lexer.advance();
		}

		f = peek(0);
		while (f.check(lexer.DELIM, ",")) {
			lexer.advance();
			f = peek(0);
			if (f.token != lexer.NAME) {
				err(map(lexer.NAME), f);
			} else {
				decls.push(f.lex);
				lexer.advance();
				f = peek(0);
			}
		}

		scope.pop();
	}

	// WIP: TreeCode
	private TreeCode[] p_expr_list(SymbolTable table)
			throws Exception
	{
		scope.push("<expr_list>");

		Stack<TreeCode> exprs = new Stack<>();

		exprs.push(p_expr(table));
		consumeDelim(";");

		while (!checkDelim("}")) {
			exprs.push(p_expr(table));
			consumeDelim(";");
		}

		scope.pop();

		return exprs.toArray(new TreeCode[0]);
	}

	// WIP: TreeCode
	private TreeCode p_expr(SymbolTable table)
			throws Exception
	{
		scope.push("<expr>");

		TreeCode out = null;

		Fragment f = peek(0);
		if (f.token == lexer.LOOP) {
			p_loop_expr();
			// TODO: Implement Loop TreeCode.
		} else if (f.token == lexer.BRANCH) {
			p_branch_expr();
			// TODO: Implement Branch TreeCode.
		} else if (f.token == lexer.RETURN) {
			lexer.advance();
			TreeCode result = p_expr(table);
			out = new TcReturn(table, result);
		} else {
			// Here we need to check the next token further:
			Fragment f2 = peek(1);
			if (f2.token == lexer.ASSIGN) {
				// <expr> -> <NAME> '=' <expr>
				if (f.token != lexer.NAME) {
					err(map(lexer.NAME), f);
				} else {
					String name = f.lex;
					lexer.advance();
					lexer.advance();	// Already checked 2nd token is '='
					TreeCode value = p_expr(table);
					out = new TcAssign(table, name, value);
				}
			} else if (f2.delim("(")) {
				// <expr> -> <NAME> '(' <value_list> ')'
				if (f.token != lexer.NAME) {
					err(map(lexer.NAME), f);
				} else {
					String funcName = f.lex;
					lexer.advance();
					consumeDelim("(");
					TreeCode[] args = p_value_list(table);
					consumeDelim(")");
					out = new TcCall(table, funcName, args);
				}
			} else {
				out = p_and_expr(table);
			}
		}

		scope.pop();
		return out;
	}

	// WIP: TreeCode
	// NOTE: Implementation is BROKEN pass-through.
	private TreeCode p_and_expr(SymbolTable table)
			throws Exception
	{
		scope.push("<and_expr>");

		TreeCode out = p_or_expr(table);
		scope.pop();
		return out;

		/*
		Fragment f = peek(0);
		if (!f.check(lexer.LOGIC_OP, "&&")) {
			scope.pop();
			return null;
		} else {
			lexer.advance();
			scope.pop();
			p_and_expr(table);
		}
		 */
	}

	// WIP: TreeCode
	// NOTE: Implementation is BROKEN pass-through.
	private TreeCode p_or_expr(SymbolTable table)
			throws Exception
	{
		scope.push("<or_expr>");

		TreeCode out =  p_not_expr(table);
		scope.pop();
		return out;

		/*
		Fragment f = peek(0);
		if (!f.check(lexer.LOGIC_OP, "||")) {
			scope.pop();
			return null;
		} else {
			lexer.advance();
			scope.pop();
			p_or_expr(table);
		}
		 */
	}

	// WIP: TreeCode
	// NOTE: Implementation is BROKEN pass-through.
	private TreeCode p_not_expr(SymbolTable table)
			throws Exception
	{
		scope.push("<not_expr>");
		Fragment f = peek(0);
		if (f.check(lexer.LOGIC_OP, "!")) {
			lexer.advance();
		}
		TreeCode out =  p_cmp_expr(table);
		scope.pop();
		return out;
	}

	// WIP: TreeCode
	// NOTE: Implementation is BROKEN pass-through.
	private TreeCode p_cmp_expr(SymbolTable table)
			throws Exception
	{
		scope.push("<cmp_expr>");

		TreeCode out = p_sum_expr(table);
		scope.pop();
		return out;

		/*
		Fragment f = peek(0);
		if (f.token == lexer.CMP_OP) {
			lexer.advance();
			p_sum_expr(table);
		}

		scope.pop();
		 */
	}

	// WIP: TreeCode
	private TreeCode p_sum_expr(SymbolTable table)
			throws Exception
	{
		TreeCode out = null;
		TreeCode[] args = new TreeCode[2];
		String name = "";

		scope.push("<sum_expr>");

		args[0] = p_mul_expr(table);

		Fragment f = peek(0);
		if (f.token == lexer.SUM_OP) {
			name = f.lex;
			lexer.advance();
			args[1] = p_sum_expr(table);
			out = new TcCall(table, name, args);
		} else {
			out = args[0];
		}

		scope.pop();
		return out;
	}

	// WIP: TreeCode
	private TreeCode p_mul_expr(SymbolTable table)
			throws Exception
	{
		TreeCode out = null;
		TreeCode[] args = new TreeCode[2];
		String name = "";

		scope.push("<mul_expr>");

		args[0] = p_value_expr(table);

		Fragment f = peek(0);
		if (f.token == lexer.MUL_OP) {
			name = f.lex;
			lexer.advance();
			args[1] = p_mul_expr(table);
			out = new TcCall(table, name, args);
		} else {
			out = args[0];
		}

		scope.pop();
		return out;
	}

	// WIP: TreeCode
	// NOTE: Implementation is BROKEN pass-through; only processes
	//	<NAME> or <LITERAL> into TreeCode.
	private TreeCode p_value_expr(SymbolTable table)
			throws Exception
	{
		TreeCode out = null;

		scope.push("<value_expr>");
		Fragment f = peek(0);
		Fragment f2 = peek(1);

		if (f2.token == lexer.MOD_OP) {
			p_postfix_expr();
		} else if (f.token == lexer.MOD_OP || f.check(lexer.SUM_OP, "-")) {
			p_prefix_expr();
		} else if (f.check(lexer.DELIM, "(")) {
			lexer.advance();
			out = p_expr(table);
			consumeDelim(")");
		} else if (f.token == lexer.NAME) {
			out = new TcValue(table, f.lex);
			lexer.advance();
		} else if (f.token == lexer.LITERAL) {
			out = new TcLiteral(table, f.lex);
			lexer.advance();
		} else {
			err(map(-2), f);
		}

		scope.pop();
		return out;
	}

	// WIP: TreeCode
	private TreeCode[] p_value_list(SymbolTable table)
			throws Exception
	{
		Stack<TreeCode> list = new Stack<>();
		// FOLLOW(<value_list>) is only { ")" }, so we check against this
		//	to determine whether to expand or epsilon.
		scope.push("<value_list>");
		Fragment f = peek(0);
		if (f.check(lexer.DELIM, ")")) {
			scope.pop();
			return new TreeCode[0];
		} else {
			list.push(p_value_expr(table));

			f = peek(0);
			while (f.check(lexer.DELIM, ",")) {
				lexer.advance();
				list.push(p_value_expr(table));
				f = peek(0);
			}
		}

		scope.pop();
		return list.toArray(new TreeCode[0]);
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
			p_and_expr(null);
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
			p_and_expr(null);
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
			p_and_expr(null);
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
		p_expr_list(null);
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
		System.out.println("Parse Complete, generating code..");

		FileWriter out = new FileWriter("code.masm");
		String[] lines = tree.MasmCode(false);
		for (String s : lines) {
			out.write(s + "\n");
		}
		out.close();
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
			p.printSymbolStack();
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