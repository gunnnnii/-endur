
import java.util.Arrays;
import java.util.ArrayList;

// TcFunction class ..................
public class TcFunction extends TreeCode {

	public TcFunction(SymbolTable t, String funcName, String[] args, String[] decls, TreeCode[] expr)
			throws Exception
	{
		super(t);

		if (expr == null) {
			throw new IllegalArgumentException("Function body TreeCode[] must exist.");
		}

		// Register the arguments and declared variables with the symbol table.
		for (String a : args) {
			table.register(a);
		}

		for (String d : decls) {
			table.register(d);
		}

		name = funcName;
		argCount = args.length;
		declCount = decls.length;
		body = expr;
	}

	// NOTE: This method will generate code for the final expression of the function
	//	as a tail expression.  This may or may not be a good idea!  Must experiment
	//	further.
	@Override
	public String[] MasmCode(boolean tail)
			throws Exception
	{
		final int 			N = body.length;
		ArrayList<String>	output = new ArrayList<>();

		// Start by writing out the function label, and open the MASM block:
		output.add("#\"" + name + "[f" + Integer.toString(argCount) + "]\" =");
		output.add("[");

		// Allocate stack space for the local variables.
		output.add("(MakeVal 0)");
		for (int i = 0; i < declCount; i++) {
			output.add("(Push)");
		}

		// Inject the MASM for the expressions.
		for (int i = 0; i < (N - 1); i++) {
			output.addAll(Arrays.asList(body[i].MasmCode(false)));
		}

		// Final expression is called in tail format.
		output.addAll(Arrays.asList(body[N - 1].MasmCode(true)));

		// Close the block.
		output.add("];");
		return output.toArray(new String[0]);
	}

	private String		name;
	private int 		argCount;
	private int 		declCount;
	private TreeCode[]	body;
}