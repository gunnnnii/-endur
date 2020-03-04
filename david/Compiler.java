

// 			*NanoMorpho Compiler class.

// Author: David Francis Frigge <dff1@hi.is>, 02/03/2020

// Uses output of nanolexer.jflex -> nanolexer.java process
// Parses a NanoMorpho-esqe grammar, and produces an MASM
//	from the input file.

// NOTE: This class (and the grammer upon which it operates)
//	has been refined from Parser.java -- In between parsing
//	and code generation, it has become clear that a slightly
//	different methodology might be beneficial here.
// Despite the extensive refactor, the core functionality is
//	the same; this uses recursive descent to parse the grammar,
//	and produces intermediate code in the form of TreeCodes,
//	which are then converted to MASM during final code
//	generation.

// EBNF for our variant of NanoMorpho.  Note that this is a 'loose'
//	usage of EBNF, commas separating items have been omitted, and
//	multi-line expansions are present.  This grammar has been
//	optimized for ease of use with respect to TreeCode construction.
/*
	<program>		::=		<function> { <function> }

	<function>		::=		<NAME> '(' [ <NAME> { ',' <NAME> } ] ')'		\
								'{'											\
									{ <decl> ';' }							\
									<expr> ';' { <expr> ';' }				\
								'}'

	<decl>			::=		'var' <NAME> [ ',' <NAME> ]

	<expr>			::=		<and_expr>
						|	<set_expr>
						|	<return_expr>
						|	<branch_expr>
						|	<loop_expr>

	<set_expr>		::=		<NAME> '=' <and_expr>

	<return_expr>	::=		'return' <and_expr>

	<branch_expr>	::=		'if' '(' <and_expr> ')' <block> { <branch_opt> } [ <branch_final> ]

	<branch_opt>	::=		'elseif' '(' <and_expr> ')' <block>

	<branch_final>	::=		'else' <block>

	<loop_expr>		::=		'while' '(' <and_expr> ')' <block>

	<block>			::=		'{' { <expr> ';' } '}'


	<and_expr>		::=		<or_expr> { '&&' <or_expr> }

	<or_expr>		::=		<not_expr> { '||' <not_expr> }

	<not_expr>		::=		[ '!' ] <condition>

	<condition>		::=		<sum_expr> [ <OP_CMP> <sum_expr> ]

	<sum_expr>		::=		<mul_expr> [ '+' <sum_expr> ]
						|	<mul_expr> [ '-' <sum_expr> ]

	<mul_expr>		::=		<rvalue> [ '*' <mul_expr> ]
						|	<rvalue> [ '/' <mul_expr> ]

	<rvalue>		::=		<prefix_expr>
						|	<call_expr>
						|	'(' <and_expr> ')'
						|	<NAME>
						|	<LITERAL>

	<prefix_expr>	::=		'-' <NAME>
						|	'-' <LITERAL>
						|	'--' <NAME>
						|	'++' <NAME>

	<call_expr>		::=		<NAME> '(' [ <and_expr> { ',' <and_expr> } ] ')'
 */

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;


public class Compiler {
	private final NanoLexer lexer;
	private final Stack<String> parseScope;
	private TcProgram tree;

	private String file_nm;
	private String file_masm;
	private String target;

	// ---- Compiler operational methods ----

	// Constructor verifies input and output targets, builds and initializes
	//	the lexer.
	public Compiler(String fin, String fout)
			throws IOException, IllegalArgumentException
	{
		if (!fin.split("\\.")[1].equals("nm")) {
			throw new IllegalArgumentException("Input file must match *.nm");
		} else if (!fout.split("\\.")[1].equals("masm")) {
			throw new IllegalArgumentException("Output file must match *.masm");
		}

		file_nm = fin;
		file_masm = fout;
		target = file_masm.split("\\.")[0];

		parseScope = new Stack<>();
		lexer = new NanoLexer(new FileReader(file_nm));
		lexer.init(2);
	}

	// Build(..) method parses and compiles the input target into the
	//	output target.
	public void Build() {
		FileWriter fw = null;

		try {
			// Parse the input, generating intermediate code as we go.
			tree = p_program();
			System.out.println("Parsed " + file_nm + ", generating code..");

			// Generate final code, and write the .masm file.
			fw = new FileWriter(file_masm);
			String[] lines = tree.MasmCode(false);
			for (String s : lines) {
				fw.write(s + "\n");
			}
			fw.close();

			// Success!
			System.out.println("Compiled " + file_masm);

		} catch (ParseError e) {
			System.out.println(e.getMessage());
			ParseTrace();
		} catch (SymbolError e) {
			System.out.println(e.getMessage());
		} catch (FatalError e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			// Default handler.
			System.out.println(e.getMessage());
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Compiler requires 2 arguments; [input file] [output file]");
			return;
		}

		Compiler c = null;
		try {
			c = new Compiler(args[0], args[1]);
		} catch (IOException e) {
			System.out.println("Failed to construct compiler.");
			System.out.println(e.getMessage());
			return;
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid argument.");
			System.out.println(e.getMessage());
			return;
		}

		c.Build();
	}

	// ---- Parsing methods ----

	// NOTE: Each parsing method p_***** parses a particular syntax element
	//	as defined by the EBNF grammar provided at the head of this file.
	//  The intermediate code tree is generated along side the parsing process,
	//	but bottom-up rather than top-down.

	public TcProgram p_program()
			throws CompilerError
	{
		parseScope.push("<program>");

		TcProgram 			out = null;
		Stack<TcFunction> 	defs = new Stack<>();

		while (Token() != lexer.EOF) {
			defs.push(p_function());
		}
		out = new TcProgram(target, "main", defs.toArray(new TcFunction[0]));

		parseScope.pop();
		return out;
	}

	public TcFunction p_function()
			throws CompilerError
	{
		parseScope.push("<function>");

		// Each function has its own SymbolTable -- all symbols are local
		//	to the function.
		SymbolTable table = new SymbolTable();

		TcFunction 	out = null;
		String		name = "";
		String[]	args = null;
		String[]	decls = null;
		TreeCode[]	exprs = null;

		if (Token() != lexer.NAME) { throw new ParseError(lexer.NAME, Token()); }
		name = Text();
		Advance();

		// Parse argument list.
		if (Token() != lexer.OPEN_PAREN) { throw new ParseError(lexer.OPEN_PAREN, Token()); }
		Advance();

		Stack<String>	argStack = new Stack<>();
		while(Token() == lexer.NAME) {
			argStack.push(Text());
			Advance();
			if (Token() == lexer.COMMA) {
				Advance();
			}
		}
		args = argStack.toArray(new String[0]);
		if (Token() != lexer.CLOSE_PAREN) { throw new ParseError(lexer.CLOSE_PAREN, Token()); }
		Advance();


		if (Token() != lexer.OPEN_BLOCK) { throw new ParseError(lexer.OPEN_BLOCK, Token()); }
		Advance();


		// Parse variable declaration list.
		Stack<String>	declStack = new Stack<>();
		while (Token() == lexer.VAR) {
			Advance();
			if (Token() != lexer.NAME) { throw new ParseError(lexer.NAME, Token()); }
			declStack.push(Text());
			Advance();
			while (Token() == lexer.COMMA) {
				Advance();
				if (Token() != lexer.NAME) { throw new ParseError(lexer.NAME, Token()); }
				declStack.push(Text());
				Advance();
			}
			if (Token() != lexer.SEMICOLON) { throw new ParseError(lexer.SEMICOLON, Token()); }
			Advance();
		}
		decls = declStack.toArray(new String[0]);

		// Parse expression list.
		Stack<TreeCode> exprStack = new Stack<>();
		while (Token() != lexer.CLOSE_BLOCK) {
			exprStack.push(p_expr(table));
			if (Token() != lexer.SEMICOLON) { throw new ParseError(lexer.SEMICOLON, Token()); }
			Advance();
		}
		exprs = exprStack.toArray(new TreeCode[0]);
		if (exprs.length == 0) {
			throw new FatalError("No expressions in function " + name);
		}

		// Note here we need not explicitly check that the next token is '}'
		//	because this is the exit condition of the loop above. Thus we blindly
		//	consume it and move on with our lives.
		Advance();
		out = new TcFunction(table, name, args, decls, exprs);

		parseScope.pop();
		return out;
	}

	public TreeCode p_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<expr>");

		TreeCode out = null;

		// Try and determine what expansion of <expr> to apply by the process
		//	of elimination.
		if (Token() == lexer.RET) {
			Advance();
			out = new TcReturn(table, p_and_expr(table));
		} else if (Token() == lexer.LP_WHILE) {
			out = p_loop_expr(table);
		} else if (Token() == lexer.BRN_IF) {
			out = p_branch_expr(table);
		} else if (Peek() == lexer.OP_SET) {
			if (Token() != lexer.NAME) { throw new ParseError(lexer.NAME, Token()); }
			String name = Text();
			Advance();
			Advance();
			out = new TcAssign(table, name, p_and_expr(table));
		} else {
			out = p_and_expr(table);
		}

		parseScope.pop();
		return out;
	}

	public TcBranch p_branch_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<branch_expr>");

		TcBranch 		out = null;
		TreeCode 		condition = null;
		TreeCode[] 		body = null;
		TcBranch 		t0 = null;
		TcBranch 		t1 = null;

		if (Token() != lexer.BRN_IF) { throw new ParseError(lexer.BRN_IF, Token()); }
		Advance();
		if (Token() != lexer.OPEN_PAREN) { throw new ParseError(lexer.OPEN_PAREN, Token()); }
		Advance();
		condition = p_and_expr(table);
		if (Token() != lexer.CLOSE_PAREN) { throw new ParseError(lexer.CLOSE_PAREN, Token()); }
		Advance();
		body = p_block(table);
		out = new TcBranch(table, condition, body);
		t0 = out;

		// Generate and link any 'elseif' branches.
		while (Token() == lexer.BRN_ELIF) {
			Advance();
			if (Token() != lexer.OPEN_PAREN) { throw new ParseError(lexer.OPEN_PAREN, Token()); }
			Advance();
			condition = p_and_expr(table);
			if (Token() != lexer.CLOSE_PAREN) { throw new ParseError(lexer.CLOSE_PAREN, Token()); }
			Advance();
			body = p_block(table);
			t1 = new TcBranch(table, condition, body);
			t0.Link(t1);
			t0 = t1;
		}

		// Generate and link the final 'else' branch if present.
		if (Token() == lexer.BRN_ELSE) {
			Advance();
			body = p_block(table);
			t1 = new TcBranch(table, new TcLiteral("true"), body);
			t0.Link(t1);
		}

		parseScope.pop();
		return out;
	}

	public TcLoop p_loop_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<loop_expr>");

		TcLoop 			out = null;
		TreeCode		condition = null;
		TreeCode[]		body = null;

		if (Token() != lexer.LP_WHILE) { throw new ParseError(lexer.LP_WHILE, Token()); }
		Advance();
		if (Token() != lexer.OPEN_PAREN) { throw new ParseError(lexer.OPEN_PAREN, Token()); }
		Advance();
		condition = p_and_expr(table);
		if (Token() != lexer.CLOSE_PAREN) { throw new ParseError(lexer.CLOSE_PAREN, Token()); }
		Advance();
		body = p_block(table);
		out = new TcLoop(table, condition, body);

		parseScope.pop();
		return out;
	}

	public TreeCode[] p_block(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<block>");

		TreeCode[]			out = null;
		Stack<TreeCode>		exprStack = new Stack<>();

		if (Token() != lexer.OPEN_BLOCK) { throw new ParseError(lexer.OPEN_BLOCK, Token()); }
		Advance();
		while (Token() != lexer.CLOSE_BLOCK) {
			exprStack.push(p_expr(table));
			if (Token() != lexer.SEMICOLON) { throw new ParseError(lexer.SEMICOLON, Token()); }
			Advance();
		}
		Advance();
		out = exprStack.toArray(new TreeCode[0]);

		parseScope.pop();
		return out;
	}

	public TreeCode p_and_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<and_expr>");

		TreeCode 	out = null;
		TreeCode 	first = p_or_expr(table);

		if (Token() == lexer.LGC_AND) {
			Advance();
			out = new TcAnd(first, p_and_expr(table));
		} else {
			out = first;
		}

		parseScope.pop();
		return out;
	}

	public TreeCode p_or_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<or_expr>");

		TreeCode 	out = null;
		TreeCode 	first = p_not_expr(table);

		if (Token() == lexer.LGC_OR) {
			Advance();
			out = new TcOr(first, p_or_expr(table));
		} else {
			out = first;
		}

		parseScope.pop();
		return out;
	}

	public TreeCode p_not_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<not_expr>");

		TreeCode out = null;

		if (Token() == lexer.LGC_NOT) {
			Advance();
			out = new TcNot(p_condition(table));
		} else {
			out = p_condition(table);
		}

		parseScope.pop();
		return out;
	}

	public TreeCode p_condition(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<condition>");

		TreeCode 	out = null;
		TreeCode 	left = p_sum_expr(table);
		TreeCode 	right = null;
		String		op = "";

		if (Token() == lexer.OP_CMP) {
			op = Text();
			Advance();
			right = p_sum_expr(table);
			out = new TcCall(table, op, new TreeCode[]{left, right});
		} else {
			out = left;
		}

		parseScope.pop();
		return out;
	}

	// NOTE: This method is left-associative -- calculations are grouped
	//	from left to right: 1 + 2 + 3 + 4 = ((1 + 2) + 3) + 4)
	public TreeCode p_sum_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<sum_expr>");

		TreeCode 	out = null;
		TreeCode 	left = p_mul_expr(table);
		TreeCode 	right = null;
		String		op = "";

		while (Token() == lexer.OP_ADD || Token() == lexer.OP_SUB) {
			op = Text();
			Advance();
			right = p_mul_expr(table);
			left = new TcCall(table, op, new TreeCode[]{left, right});
		}
		out = left;

		parseScope.pop();
		return out;
	}

	// This method is also left-associative.
	public TreeCode p_mul_expr(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<mul_expr>");

		TreeCode 	out = null;
		TreeCode 	left = p_rvalue(table);
		TreeCode 	right = null;
		String		op = "";

		while (Token() == lexer.OP_MUL || Token() == lexer.OP_DIV) {
			op = Text();
			Advance();
			right = p_rvalue(table);
			left = new TcCall(table, op, new TreeCode[]{left, right});
		}
		out = left;

		parseScope.pop();
		return out;
	}

	// NOTE: This method is a bit ugly.  May want to refactor it and pretty it
	//	up later.
	public TreeCode p_rvalue(SymbolTable table)
			throws CompilerError
	{
		parseScope.push("<rvalue>");

		TreeCode 	out = null;

		if (Token() == lexer.OP_SUB) {
			Advance();
			if (Token() == lexer.NAME) {
				out = new TcCall(table, "-", new TreeCode[]{new TcValue(table, Text())});
				Advance();
			} else if (Token() == lexer.LITERAL) {
				out = new TcCall(table, "-", new TreeCode[]{new TcLiteral(Text())});
				Advance();
			} else {
				// TODO: implement ParseError constructor for multiple 'expected' tokens.
				throw new ParseError(lexer.NAME, Token());
			}
		} else if (Token() == lexer.OP_INC) {
			// NOTE: Increment and decrement operators implemented in terms of
			//	binary + and -.
			Advance();
			if (Token() != lexer.NAME) { throw new ParseError(lexer.NAME, Token()); }

			String		var = Text();
			TreeCode	inc = new TcCall(table, "+", new TreeCode[]{new TcValue(table, var), new TcLiteral("1")});

			out = new TcAssign(table, var, inc);
			Advance();
		} else if (Token() == lexer.OP_DEC) {
			Advance();
			if (Token() != lexer.NAME) { throw new ParseError(lexer.NAME, Token()); }

			String		var = Text();
			TreeCode	inc = new TcCall(table, "-", new TreeCode[]{new TcValue(table, var), new TcLiteral("1")});

			out = new TcAssign(table, var, inc);
			Advance();
		} else if (Token() == lexer.OPEN_PAREN) {
			Advance();
			out = p_and_expr(table);
			if (Token() != lexer.CLOSE_PAREN) { throw new ParseError(lexer.CLOSE_PAREN, Token()); }
			Advance();
		} else if (Token() == lexer.LITERAL) {
			out = new TcLiteral(Text());
			Advance();
		} else if (Token() == lexer.NAME) {
			// Here we have an ambiguity -- could be <call_expr> or just <NAME>.
			if (Peek() == lexer.OPEN_PAREN) {
				// <call_expr>
				String				name = Text();
				TreeCode[]			args = null;
				Stack<TreeCode>		argStack = new Stack<>();

				Advance();
				Advance();
				if (Token() != lexer.CLOSE_PAREN) {
					argStack.push(p_and_expr(table));
					while (Token() == lexer.COMMA) {
						Advance();
						argStack.push(p_and_expr(table));
					}
				}
				if (Token() != lexer.CLOSE_PAREN) { throw new ParseError(lexer.CLOSE_PAREN, Token()); }
				Advance();
				args = argStack.toArray(new TreeCode[0]);
				out = new TcCall(table, name, args);
			} else {
				// <NAME>
				out = new TcValue(table, Text());
				Advance();
			}
		} else {
			throw new ParseError(-2, Token());
		}

		parseScope.pop();
		return out;
	}

	// NOTE: The following four methods are meant to WRAP the interface to
	//	the Lexer, though it is unlikely that its interface will change.  They
	//	also serve to catch lexer exceptions which may bubble up.

	// Token(..) method returns the next token.
	public int Token() {
		try {
			return lexer.peekToken(0);
		} catch (IllegalArgumentException e) {
			System.out.println("Bad argument to lexer.peekToken(..)");
			return -1;
		}
	}

	// Text(..) method returns the next lexeme.
	// NOTE: For some tokens, the lexeme is not defined/available, and
	//	the empty string will be returned instead.
	public String Text() {
		try {
			return lexer.peekLexeme(0);
		} catch (IllegalArgumentException e) {
			System.out.println("Bad argument to lexer.peekLexeme(..)");
			return "";
		}
	}

	// Peek(..) method returns the token AHEAD of the next token.
	public int Peek() {
		try {
			return lexer.peekToken(1);
		} catch (IllegalArgumentException e) {
			System.out.println("Bad argument to lexer.peekToken(..)");
			return -1;
		}
	}

	public void Advance()
			throws CompilerError
	{
		// NOTE: It may not be sensible to catch the IOException in this
		//	method -- we can't really deal with it here, and if it is being
		//	raised by advancing the lexer, then it is unlikely that any
		//	further execution of the program will succeed.
		// TODO: Move exception handling to a more sensible place.
		try {
			lexer.advance();
		} catch (IOException e) {
			throw new FatalError("IOException in lexer.advance(..)");
		}
	}

	// ---- Error-handling & Debugging methods/classes ----

	// ParseTrace(..) method prints out a trace of the parsing stack,
	//	in its current state.  Used during error handling, and for debugging.
	public void ParseTrace() {
		String prefix = " ";
		String[] stack = parseScope.toArray(new String[0]);
		for (int i = 0; i < stack.length; ++i) {
			String write = "";
			for (int j = 0; j < i; ++j) {
				write = write + prefix;
			}
			write = write + stack[i];
			System.out.println(write);
		}
	}

	// Map(..) function maps an integer token to an associated string.
	//	This method is provided here to provide a single clearinghouse
	//	for convenience -- when we're parsing and a grammar error is
	//	detected, we can simply create a ParseError with the associated
	//	tokens and not worry about the int<->string mapping there.

	// NOTE: This method NEEDS to be updated if the token list changes.
	private static String Map(int token) {
		switch (token) {
			case NanoLexer.ERROR:			return "<ERROR>";
			case NanoLexer.EOF:				return "<EOF>";
			case NanoLexer.SEMICOLON:		return ";";
			case NanoLexer.COMMA:			return ",";
			case NanoLexer.OPEN_PAREN:		return "(";
			case NanoLexer.CLOSE_PAREN:		return ")";
			case NanoLexer.OPEN_BLOCK:		return "{";
			case NanoLexer.CLOSE_BLOCK:		return "}";
			case NanoLexer.OPEN_BRACKET:	return "[";
			case NanoLexer.CLOSE_BRACKET:	return "]";
			case NanoLexer.NAME:			return "<NAME>";
			case NanoLexer.LITERAL:			return "<LITERAL>";
			case NanoLexer.OP_INC:			return "++";
			case NanoLexer.OP_DEC:			return "--";
			case NanoLexer.OP_MUL:			return "*";
			case NanoLexer.OP_DIV:			return "/";
			case NanoLexer.OP_ADD:			return "+";
			case NanoLexer.OP_SUB:			return "-";
			case NanoLexer.OP_LIST:			return ":";
			case NanoLexer.LGC_AND:			return "&&";
			case NanoLexer.LGC_OR:			return "||";
			case NanoLexer.LGC_NOT:			return "!";
			case NanoLexer.OP_SET:			return "=";
			case NanoLexer.OP_CMP:			return "<OP_CMP>";
			case NanoLexer.BRN_IF:			return "if";
			case NanoLexer.BRN_ELIF:		return "elseif";
			case NanoLexer.BRN_ELSE:		return "else";
			case NanoLexer.LP_WHILE:		return "while";
			case NanoLexer.VAR:				return "var";
			case NanoLexer.RET:				return "return";
			default:						return "<???>";
		}
	}

	public class ParseError extends CompilerError {
		public ParseError(int expected, int received) {
			super("Parse error in " + parseScope.peek() + " (L:" + Integer.toString(lexer.line()) + " C:" + Integer.toString(lexer.column()) + ") expected " + Map(expected) + ", got " + Map(received));
		}
	}
}