
import java.util.Arrays;
import java.util.ArrayList;

// TcBranch class ..................
public class TcBranch extends TreeCode {
	private TreeCode	condition;
	private TreeCode[]	body;

	private TcBranch	head;
	private TcBranch	chain;

	private String		exit;
	private String		check;
	private String		invalid;

	public TcBranch(SymbolTable t, TreeCode con, TreeCode[] block) {
		super(t);

		condition = con;
		body = block;
		head = this;
		chain = null;
		exit = null;
		check = null;
		invalid = null;
	}

	public void Link(TcBranch next) {
		chain = next;
		chain.head = this.head;
	}

	// Prepare method generates all the labels for the chain.
	private void Prepare() {
		if (this == head) {
			exit = TreeCode.newLabel();

			// NOTE: HEAD.check is never actually jumped to, but having it be
			//	a valid label makes MASM generation method a bit cleaner.
			check = TreeCode.newLabel();

			TcBranch t0 = this;
			TcBranch t1 = chain;

			while (t1 != null) {
				t1.check = TreeCode.newLabel();
				t0.invalid = t1.check;
				t1.exit = t0.exit;

				t0 = t1;
				t1 = t1.chain;
			}
			t0.invalid = t0.exit;
		} else {
			System.out.println("WARNING: Invoking Prepare(..) on TcBranch that is not HEAD.");
		}
	}

	// NOTE: This method will fail spectacularly if we do not start
	//	MASM generation from the _head_ of a chain.
	@Override
	public String[] MasmCode(boolean tail)
			throws CompilerError
	{
		// TODO: Maybe clean up a bit, optimize code generation for head & tail
		//	branches?

		// If we are generating final code for the HEAD branch, start by
		//	generating the labels for the entire chain.
		if (this == head) {
			Prepare();
		}

		ArrayList<String> output = new ArrayList<>();

		output.add(check + ":");
		output.addAll(Arrays.asList(condition.MasmCode(false)));
		output.add("(GoFalse " + invalid + ")");
		for (TreeCode tc : body) {
			output.addAll(Arrays.asList(tc.MasmCode(false)));
		}
		output.add("(Go " + exit + ")");
		if (chain != null) {
			output.addAll(Arrays.asList(chain.MasmCode(false)));
		} else {
			output.add(exit + ":");
		}

		return output.toArray(new String[0]);
	}
}