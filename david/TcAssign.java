
import java.util.Arrays;
import java.util.ArrayList;

// TcAssign class ..................
public class TcAssign extends TreeCode {
	private String 		name;
	private TreeCode	valueExpr;

	public TcAssign(SymbolTable t, String varName, TreeCode varValue)
			throws Exception
	{
		super(t);

		if (varValue == null) {
			throw new IllegalArgumentException("Assign value TreeCode must exist.");
		}

		name = varName;
		valueExpr = varValue;
	}

	// If an assignment operation is the tail operation of a function,
	//	then we don't even need to perform the assignment!  Instead we
	//	just leave the value that would be assigned in the acc register,
	//	and it becomes our return value.
	// NOTE: The implicit assumption here is that whatever the result of
	//	evaluating 'valueExpr', it will be in _acc_ when the evaluation
	//	is complete.
	@Override
	public String[] MasmCode(boolean tail)
			throws Exception
	{
		ArrayList<String>	output = new ArrayList<>();
		output.addAll(Arrays.asList(valueExpr.MasmCode(false)));
		if (!tail) {
			int loc = table.translate(name);
			output.add("(Store " + Integer.toString(loc) + ")");
		}

		return output.toArray(new String[0]);
	}
}