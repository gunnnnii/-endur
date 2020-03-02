
import java.util.ArrayList;

// TcLiteral class ..................
// NOTE: This class can only represent a LEAF in the TreeCode schema.
public class TcLiteral extends TreeCode {

	public TcLiteral(SymbolTable t, String literal)
			throws Exception
	{
		super(t);

		value = literal;
	}

	// This TreeCode cannot be tail optimized.
	@Override
	public String[] MasmCode(boolean tail) {
		ArrayList<String> output = new ArrayList<>();
		output.add("(MakeVal " + value + ")");
		return output.toArray(new String[0]);
	}

	private String		value;
}