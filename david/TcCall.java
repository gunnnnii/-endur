
import java.util.Arrays;
import java.util.ArrayList;

// TcCall class ..................
public class TcCall extends TreeCode {
	private String		name;
	private TreeCode[]	args;

	public TcCall(SymbolTable t, String funcName, TreeCode[] funcArgs)
			throws Exception
	{
		super(t);

		if (funcArgs == null) {
			throw new IllegalArgumentException("Function argument TreeCode[] must exist.");
		}

		name = funcName;
		args = funcArgs;
	}

	// The general idea here is that the expr code required to evaluate
	//	the actual arguments is executed in order left-to-right; each
	//	argument result is pushed onto the stack as we go.  When all
	//	N arguments have been evaluated, max(N-1, 0) new values exist
	//	on the stack, the Nth arg is in _acc_, and the actual function
	//	call is made.
	@Override
	public String[] MasmCode(boolean tail)
			throws Exception
	{
		final int N = args.length;
		ArrayList<String> output = new ArrayList<>();

		// Generate the MASM for evaluation of the arguments.
		if (N > 0) {
			output.addAll(Arrays.asList(args[0].MasmCode(false)));
		}
		for (int i = 1; i < N; ++i) {
			output.add("(Push)");
			output.addAll(Arrays.asList(args[i].MasmCode(false)));
		}

		String form = (tail ? "CallR" : "Call");

		output.add("(" + form + " #\"" + name + "[f" + Integer.toString(N) + "]\" " + Integer.toString(N) + ")");

		return output.toArray(new String[0]);
	}
}