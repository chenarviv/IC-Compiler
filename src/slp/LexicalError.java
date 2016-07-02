package slp;

/**
 * LexicalError class uses the program to inform the script programmer about lexical errors in his code.
 * Whenever the program encounters a lexical error, the scanner will           
 * throw a LexicalError exception (while creating a new object of this class) and the main method will catch it.
 * 
 * @author      Karin Angel 
 * @author      Lital Biniashvili
 * @author		Maytal Shamir              
 * @since       13-11-2012
 */
public class LexicalError extends Exception
{

	// The class LexicalError must declare a static final serialVersionUID field
	private static final long serialVersionUID = 1L;
	
	//The error message to be returned
	private String errMessage = null;
	
	/*
	 * LexicalError Constructor
	 * (one parameter)
	 * @param  message  A string containing the error message that would be printed on screen            
	 */
	public LexicalError(String message) {

		//Prints the error message to screen
		this.errMessage = message;
	}

	/**
	 * LexicalError Constructor
	 * (3 parameters)
	 * @param  line  	The line number of the current lexical error
	 * @param  message  A string containing the error message that would be printed on screen
	 * @param  text  A string containing the invalid characters            
	 */
		
	public LexicalError(int line, String message, String text){
		if ((message=="unclosed comment")||(message=="unclosed quote")){
			this.errMessage = ++line + ": Lexical error: " + message;
			
		}
		else{
			this.errMessage = ++line + ": Lexical error: " + message + " '" + text +"'";
				
		}
	}
	
	/**
	 * getErrorMessage method
	 * 
	 * @return a string of the current error message for the user           
	 */
	public String getErrorMessage(){
		return this.errMessage;
	}
}
