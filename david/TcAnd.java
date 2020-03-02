
import java.util.Arrays;
import java.util.ArrayList;

// TcAnd class ..................
public class TcAnd extends TreeCode {
	private TreeCode	left;
	private TreeCode	right;

	public TcAnd(TreeCode leftExpr, TreeCode rightExpr) {
		super(null);

		left = leftExpr;
		right = rightExpr;
	}

	// The desired behaviour of the && operator is the short-circuit;
	//	if the left expression evaluates as false, then the right expression
	//	is never evaluated.
	@Override
	public String[] MasmCode(boolean tail)
			throws CompilerError
	{
		String 				skip = TreeCode.newLabel();
		ArrayList<String> 	output = new ArrayList<>();

		output.addAll(Arrays.asList(left.MasmCode(false)));
		output.add("(GoFalse " + skip + ")");
		output.addAll(Arrays.asList(right.MasmCode(false)));
		output.add(skip + ":");

		return output.toArray(new String[0]);
	}
}