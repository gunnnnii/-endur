
import java.util.Arrays;
import java.util.ArrayList;

// TcFunction class ..................
public class TcFunction extends TreeCode {

	public TcFunction(SymbolTable t, String funcName, String[] args_in, String[] decls_in, TreeCode[] expr) {
		super(t);

		name = funcName;
		args = args_in;
		decls = decls_in;
		body = expr;
	}

	// NOTE: This method will generate code for the final expression of the function
	//	as a tail expression.  This may or may not be a good idea!  Must experiment
	//	further.
	@Override
	public String[] MasmCode(boolean tail)
			throws CompilerError
	{
		// Register the arguments and declared variables with the symbol table.
		for (String a : args) {
			table.register(a);
		}

		for (String d : decls) {
			table.register(d);
		}

		final int			argCount = args.length;
		final int			declCount = decls.length;
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

		// HACK: This is a bit of an ugly workaround here.  Rather than
		//	checking semantically whether the function will definitely
		//	return at some point, we simply insert a failsafe (Return)
		//	code at the end of every function, just in case.  This will
		//	sometimes result in superfluous MASM, but ensures that that
		//	calling a particular function which does not explicitely
		//	return (or end on an expression which can be tail optimized)
		//	will not OVERRUN straight into the next defined function.
		output.add("(Return)");

		// Close the block.
		output.add("];");
		return output.toArray(new String[0]);
	}

	private String		name;
	private String[]	args;
	private String[]	decls;
	private TreeCode[]	body;
}