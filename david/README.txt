    ----    SubMorpho Compiler README 04/03/2020    ----

The SubMorpho compiler accepts as input a .nm file containing valid
 SubMorpho code (EBNF* described in Compiler.java).  It produces as
 output a viable .masm assembly, which can be compiled by the morpho
 library into a valid .mexe file.


  --  USAGE:  -------------------------------------------------------

	(from within the directory):
(build compiler)	make build
(compile .nm)		java -cp . Compiler _source_.nm _target_.masm
(assemble)		java -jar Morpho.jar -c _target_.masm
(run)			java -jar Morpho.jar _target_

	(ALTERNATE usage via MAKE):
(all steps)		make in=_source_

Note that the alternate method via MAKE requires that _source_ be the
 name of an existing source file, sans extension.  The actual file that
 is compiled is _source_.nm, producing _source_.masm.  This is then
 assembled into _source_.mexe, and run via Morpho.jar.


  --  TESTING:  -----------------------------------------------------

Provided with this distribution are three SubMorpho programs, which
 demonstrate most of the core functionality of the compiler.  These
 are nm_branch.nm, nm_loop.nm, and nm_recursion.nm.  I encourage you
 to compile and run them all, verifying that they work as expected.
As a by-product, this will generate a heap of .masm which can be
 checked by hand to verify that the compiler is working correctly.
An example command that would build & run nm_loop is (again from
 the compiler directory):
	make in=nm_loop


  --  NOTES:  -------------------------------------------------------

A comprehensive description of the grammar recognized as SubMorpho is
 provided at the head of the file Compiler.java, in a loose EBNF
 notation.

There are a number of features not implemented in this compiler. A
 (non comprehensive) list of them is:

 - full tail-optimization (partial tail optimizations exist for
	TcCall and TcAssign).
 - Type checking.
 - List operations.
 - String concatenation (and likely other string functionality,
	not personally familiar with the extent which morpho
	offers).
 - @, $, & operators.
 - Nested functions, and closures in general.
 - Global variables.
 - Classes/Structures.
 - Compilation of modules (as opposed to .mexe targets).
 - Advanced linking (beyond just the BASIS module).
 - Constant variables.
 - Semantic checks (do all functions return? etc.)
 - Structural optimization (superfluous variables, expressions, etc.)
 - MASM optimization (removal of unnecessary operations, compile time
	calculation where possible, rearrangement of control flow, etc.)