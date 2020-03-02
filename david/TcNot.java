
import java.util.Arrays;
import java.util.ArrayList;

// TcNot class ..................
public class TcNot extends TreeCode {
	private TreeCode	inner;

	public TcNot(TreeCode expr) {
		super(null);

		this.inner = expr;
	}

	@Override
	public String[] MasmCode(boolean tail)
			throws CompilerError
	{
		ArrayList<String> 	output = new ArrayList<>();

		output.addAll(Arrays.asList(inner.MasmCode(false)));
		output.add("(Not)");

		return output.toArray(new String[0]);
	}
}