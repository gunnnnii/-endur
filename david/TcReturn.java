
import java.util.Arrays;
import java.util.ArrayList;

// TcReturn class ..................
public class TcReturn extends TreeCode {

	public TcReturn(SymbolTable t, TreeCode expr)
			throws Exception
	{
		super(t);

		if (expr == null) {
			throw new IllegalArgumentException("Return TreeCode must exist.");
		}

		result = expr;
	}

	// NOTE: Current implementation produces a superfluous (Return) code
	//	after the MASM for evaluating result, if result can be tail-
	//	optimized into a ____R MASM.
	// TODO: Add generic CanTailOpt(..) to TreeCode super-class, so that
	//	we can 'look ahead' where necessary and optimize final code
	//	generation.
	@Override
	public String[] MasmCode(boolean tail)
			throws Exception
	{
		ArrayList<String> output = new ArrayList<>();
		output.addAll(Arrays.asList(result.MasmCode(true)));
		output.add("(Return)");
		return output.toArray(new String[0]);
	}

	private TreeCode	result;
}