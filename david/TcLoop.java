
import java.util.Arrays;
import java.util.ArrayList;

// TcLoop class ..................
public class TcLoop extends TreeCode {
	private TreeCode	condition;
	private TreeCode[]	body;

	public TcLoop(SymbolTable t, TreeCode con, TreeCode[] block) {
		super(t);

		condition = con;
		body = block;
	}

	@Override
	public String[] MasmCode(boolean tail)
			throws CompilerError
	{
		// Each loop requires 2 labels.
		String check = TreeCode.newLabel();
		String exit = TreeCode.newLabel();

		ArrayList<String> output = new ArrayList<>();

		output.add(check + ":");
		output.addAll(Arrays.asList(condition.MasmCode(false)));
		output.add("(GoFalse " + exit + ")");
		for (TreeCode tc : body) {
			output.addAll(Arrays.asList(tc.MasmCode(false)));
		}
		output.add("(Go " + check + ")");
		output.add(exit + ":");

		return output.toArray(new String[0]);
	}
}