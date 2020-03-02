
// TreeCode class bundles a tree-like intermediate code structure,
//	with the methods required to construct it piecewise, rather than
//	all at once.
// TreeCode is intended to be used by Parser.java -- this is the intermediate
//	code generation step.  Final code is generated from a valid TreeCode
//	object by MasmCode class; see MasmCode.java
// NOTE: The implementation (internal data structures etc.) here is a naive
//	first pass; any and all performance considerations have been ignored
//	at this time.
public class TreeCode {
	protected final SymbolTable table;

	public TreeCode(SymbolTable t) {
		if (t == null) {
			throw new IllegalArgumentException("SymbolTable cannot be NULL.");
		}
		table = t;
	}

	public String[] MasmCode(boolean tail)
			throws Exception
	{
		throw new UnsupportedOperationException("Default TreeCode cannot generate MASM.");
	}

	public static int newLabel() {
		return lCounter++;
	}

	private static int lCounter = 0;
}