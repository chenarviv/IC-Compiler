package slp;

import java.io.*;

import java_cup.runtime.*;
import SymbolTable.*;
import Semantic.*;
import lir.*;

/**
 * The entry point of the SLP (Straight Line Program) application.
 * 
 */
public class Main {
	/**
	 * Reads an SLP and pretty-prints it.
	 * 
	 * @param args
	 *            Should be the name of the file containing an SLP.
	 */
	public static void main(String[] args) {
		LibraryParser library_parser;
		Parser Program_Parser; //ic language parser
		FileReader libFile, programFile;//ic 'Library' parser
		Symbol Library_tree;
		Symbol Prog_tree;
		String library_location = "src/slp/libic.sig";
		Program root=null; 

		if (args.length == 0) {
			System.out.println("path to file not given!");
			return;
		}

		try {
			programFile = new FileReader(args[0]);
			Lexer Program_scanner = new Lexer(programFile);
			Program_Parser = new Parser(Program_scanner);
			Prog_tree = Program_Parser.parse();
			root=(Program) Prog_tree.value;
			try {
				libFile = new FileReader(library_location);
				} 
			catch (Exception e1) {
					libFile = null;
					System.out.println("IO Error: no library signature file was found.");
					System.exit(-1);
				}
			try {
					Liblexer Library_scanner = new Liblexer(libFile);
					library_parser = new LibraryParser(Library_scanner);
					Library_tree = library_parser.parse();
					ICClass lib_root = (ICClass) Library_tree.value;
					root.addLibraryClass(lib_root);
					System.out.println("Parsed " + library_location+ " successfully!");
				} 
			catch (Exception e) {
					System.out.println("Exception in parsing library file "+ e.getMessage());
					//e.printStackTrace();
				}

			BuilderSymbolTable symTableCreate = new BuilderSymbolTable(args[0]);
			SymbolTableProgram glbTable = (SymbolTableProgram) root.accept(symTableCreate, null);
			SemanticChecker semanticCheckV = new SemanticChecker(glbTable);
			root.accept(semanticCheckV);
			System.out.println("Parsed " + args[0] + " successfully!\n");
			
			TranslatorICToLIR trLIR = new TranslatorICToLIR(glbTable);
			String strLIR = (String) root.accept(trLIR, 1);

			BufferedWriter out = new BufferedWriter(new FileWriter(args[0].substring(0, args[0].length()-2) + "lir"));
			out.write(strLIR);

			out.close();
			System.out.print("Translation to LIR ended successfuly!");

		} catch (SemanticError e1) {
			e1.getErrorMessage();
		} catch (Exception e) {
			System.out.println("Exception: "+e.getMessage());
			e.printStackTrace();
		}
	}
}