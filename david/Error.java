

// This file contains a limited Exception hierarchy used by the compiler.

class CompilerError extends Exception {
	public CompilerError(String msg) {
		super(msg);
	}
}

class FatalError extends CompilerError {
	public FatalError(String msg) {
		super("FATAL ERROR: " + msg);
	}
}

class SymbolError extends CompilerError {
	public SymbolError(String msg) {
		super(msg);
	}
}