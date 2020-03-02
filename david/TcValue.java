
import java.util.ArrayList;

// TcValue class ..................
// NOTE: This class can only represent a LEAF in the TreeCode schema.
public class TcValue extends TreeCode {

	public TcValue(SymbolTable t, String varName)
			throws Exception
	{
		super(t);

		name = varName;
	}

	// This TreeCode cannot be tail optimized.
	@Override
	public String[] MasmCode(boolean tail)
			throws Exception
	{
		ArrayList<String> output = new ArrayList<>();
		output.add("(Fetch " + Integer.toString(table.translate(name)) + ")");
		return output.toArray(new String[0]);
	}

	private String		name;
}