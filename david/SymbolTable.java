
import java.util.HashMap;

// SymbolTable class contains an implementation of a simple Symbol Table to be
//	used in conjunction with TreeCodes to generate a morpho assembly from valid
//	nano-Morpho code.
// Current implementation allows only:
//	- Translation from string to unique integer.  Note that the current model
//		gives ascending integers to unique labels in the order they are
//		translated.  This behaviour may be modified later.
public class SymbolTable {
	public int translate(String name)
			throws Exception
	{
		if (!exists(name)) {
			throw new SymbolException("Invalid symbol - '" + name + "' is not defined.");
		} else {
			return table.get(name);
		}
	}

	public boolean exists(String name) {
		return table.containsKey(name);
	}

	public int register(String name)
			throws Exception
	{
		if (exists(name)) {
			throw new SymbolException("Symbol collision - '" + name + "' already defined.");
		} else {
			table.put(name, acc++);
			return acc - 1;
		}
	}

	private final HashMap<String, Integer> table = new HashMap<>();
	private int acc = 0;

	public class SymbolException extends Exception
	{
		public SymbolException(String msg) {
			super(msg);
		}
	}
}