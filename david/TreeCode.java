
// TreeCode class bundles a tree-like intermediate code structure,
//	with the methods required to construct it piecewise, rather than
//	all at once.
// TreeCode is intended to be used by Parser.java -- this is the intermediate
//	code generation step.  Final code is generated from a valid TreeCode
//	object by MasmCode virtual method.
// NOTE: The implementation (internal data structures etc.) here is a naive
//	first pass; any and all performance considerations have been ignored
//	at this time.
public class TreeCode {
	protected final SymbolTable table;

	public TreeCode(SymbolTable t) {
		/*	Turns out that SymbolTable _can_ be NULL!  Some TreeCodes will not
			 require any access to the symbol table, such as TcLiteral, or
			 TcProgram (which holds multiple TcFunction codes, each with
			 their own SymbolTable).

		if (t == null) {
			throw new IllegalArgumentException("SymbolTable cannot be NULL.");
		}
		 */
		table = t;
	}

	// Default TreeCode cannot produce any sensible MASM.
	public String[] MasmCode(boolean tail)
			throws CompilerError
	{
		throw new FatalError("Default TreeCode cannot generate MASM.");
	}

	// Default behaviour for a TreeCode if it is asked whether tail-optimization
	//	is possible is to respond with a firm no.  Specific TreeCodes that do
	//	offer tail-optimization must override this method.
	public boolean CanTailOpt() {
		return false;
	}

	// Static newLabel(..) method provided to generate unique labels as jump
	//	targets.  Intended to be used by TreeCode subclasses, method is
	//	currently invisible to outside objects.
	protected static String newLabel() {
		return "_tcl" + Integer.toString(lCounter++);
	}

	private static int lCounter = 0;
}