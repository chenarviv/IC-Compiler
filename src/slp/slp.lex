package slp;

import java_cup.runtime.*;

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

%{
  StringBuffer strQT = new StringBuffer();
%}

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%cup
%class Lexer
%type Token
%line
%column
%public
%function next_token
%unicode



/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/

%{
	public int getLineNumber() { return yyline+1 ; }
%}

/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied letter to letter into the Lexer class code.                */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{   
    /*********************************************************************************/
    /* Create a new java_cup.runtime.Symbol with information about the current token */
    /*********************************************************************************/

%}

%eofval{
	if(yystate()==YYCOMMENTS) { 
	
	throw new RuntimeException("unclosed comment. expected: '*/' but got 'EOF' instead at line " + (yyline+1));
		
	}
	if(yystate()==YYQUOTE) { 

	throw new RuntimeException("unclosed string. expected: '\"' but got 'EOF' instead at line " + (yyline+1)); 
	}
	return new Token(yyline, yytext(),SlpSym.EOF); 
%eofval}


/***********************/
/* MACRO DECALARATIONS */
/***********************/

LineTerminator	= \r|\n|\r\n
WhiteSpace		= {LineTerminator} | [ \t\f]
ID = [a-z_][A-Za-z_0-9]*
CLASS_ID = [A-Z][A-Za-z_0-9]*
INTEGER=0 | [1-9][0-9]*


/******************************/
/* NEW STATES */
/******************************/

%state	YYQUOTE
%state  YYLINECOMMENTS
%state  YYCOMMENTS
   
/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/
   
/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {

"+" 		{ return new Token(yyline, yytext(), SlpSym.PLUS); }
"-" 		{ return new Token(yyline, yytext(), SlpSym.MINUS); }
"*" 		{ return new Token(yyline, yytext(), SlpSym.MULTIPLY); }
"/" 		{ return new Token(yyline, yytext(), SlpSym.DIVIDE); }
"(" 		{ return new Token(yyline, yytext(), SlpSym.LP); }
")" 		{ return new Token(yyline, yytext(), SlpSym.RP); }
";" 		{ return new Token(yyline, yytext(), SlpSym.SEMI); }
"=" 		{ return new Token(yyline, yytext(), SlpSym.ASSIGN); }
"<" 		{ return new Token(yyline, yytext(), SlpSym.LT); }
">" 		{ return new Token(yyline, yytext(), SlpSym.GT); }
"<=" 		{ return new Token(yyline, yytext(), SlpSym.LTE); }
">=" 		{ return new Token(yyline, yytext(), SlpSym.GTE); }
"&&" 		{ return new Token(yyline, yytext(), SlpSym.LAND); }
"||" 		{ return new Token(yyline, yytext(), SlpSym.LOR); }
"%" 		{ return new Token(yyline, yytext(), SlpSym.MOD); }
"boolean"	{ return new Token(yyline, yytext(), SlpSym.BOOLEAN); }
"break"		{ return new Token(yyline, yytext(), SlpSym.BREAK); }
"class"		{ return new Token(yyline, yytext(), SlpSym.CLASS); }
"," 		{ return new Token(yyline, yytext(), SlpSym.COMMA); }
"continue"	{ return new Token(yyline, yytext(), SlpSym.CONTINUE); }
"."			{ return new Token(yyline, yytext(), SlpSym.DOT); }
"=="		{ return new Token(yyline, yytext(), SlpSym.EQUAL);}
"extends"	{ return new Token(yyline, yytext(), SlpSym.EXTENDS); }
"else"		{ return new Token(yyline, yytext(), SlpSym.ELSE); }
"false"		{ return new Token(yyline, yytext(), SlpSym.FALSE); }
"if"		{ return new Token(yyline, yytext(), SlpSym.IF); }
"int"		{ return new Token(yyline, yytext(), SlpSym.INT); }
"[" 		{ return new Token(yyline, yytext(), SlpSym.LB); }
"{"			{ return new Token(yyline, yytext(), SlpSym.LCBR); }
"length"	{ return new Token(yyline, yytext(), SlpSym.LENGTH); }
"new"		{ return new Token(yyline, yytext(), SlpSym.NEW); }
"!"			{ return new Token(yyline, yytext(), SlpSym.LNEG); }
"!="		{ return new Token(yyline, yytext(), SlpSym.NEQUAL); }
"null"		{ return new Token(yyline, yytext(), SlpSym.NULL); }
"]"			{ return new Token(yyline, yytext(), SlpSym.RB); }
"}"			{ return new Token(yyline, yytext(), SlpSym.RCBR); }
"return"	{ return new Token(yyline, yytext(), SlpSym.RETURN); }
"static"	{ return new Token(yyline, yytext(), SlpSym.STATIC); }
"this" 		{ return new Token(yyline, yytext(), SlpSym.THIS); }		
"true" 		{ return new Token(yyline, yytext(), SlpSym.TRUE); }		
"void" 		{ return new Token(yyline, yytext(), SlpSym.VOID); }		
"while" 	{ return new Token(yyline, yytext(), SlpSym.WHILE); }
"string"	{ return new Token(yyline, yytext(), SlpSym.STRING);}

"\"" 				{  strQT.delete(0, strQT.length()); strQT.append(yytext()); yybegin(YYQUOTE);  } 


{INTEGER} 	{ return new Token(yyline, "INTEGER", SlpSym.INTEGER, new Integer(yytext())); }
{ID} 		{ return new Token(yyline, "ID", SlpSym.ID, yytext()); }
{WhiteSpace}		{ /* just skip what was found, do nothing */ } 
{CLASS_ID}		{ return new Token(yyline, "CLASS_ID", SlpSym.CLASS_ID, yytext()); } 
[^] 			{ throw new RuntimeException("Illegal character at line " + (yyline+1) + " : '" + yytext() + "'"); }
}

/* Quotes handling state */
 
<YYQUOTE> {

	{LineTerminator} { throw new RuntimeException("unclosed string. expected ' \" ' but got \\n instead at line " + (yyline+1)); }
		
	\"    { strQT.append(yytext()); yybegin(YYINITIAL); return new Token(yyline, "QUOTE", SlpSym.QUOTE, strQT.toString()); }
	
	\\     { strQT.append("\\"); }
	\\t    { strQT.append("\\t"); }
  	\\n    { strQT.append("\\n"); }
 	\\r    { strQT.append("\\r"); }
 	\\\"   { strQT.append("\\\""); } 
  	\\\\   { strQT.append("\\\\"); } 	
  	\\.    { throw new RuntimeException("Illegal character at line " + (yyline+1) + " : '" + yytext() + "'");}
  	[ -~]  { strQT.append( yytext() ); } 
  	 	
	[^]  { throw new RuntimeException("Illegal character at line " + (yyline+1) + " : '" + yytext() + "'");  }
}

/* command handling states */

<YYINITIAL> "//" { yybegin(YYLINECOMMENTS); }
<YYLINECOMMENTS> [^\n] { }
<YYLINECOMMENTS> ["\r"]?["\n"] { yybegin(YYINITIAL); }

<YYINITIAL> "/*" { yybegin(YYCOMMENTS); }
<YYCOMMENTS> {
	"*/"  	{ yybegin(YYINITIAL); }
	[^]		{}
}


[^]			{ throw new RuntimeException("Illegal character at line " + (yyline+1) + " : '" + yytext() + "'"); }
<<EOF>> 	{ return new Token(yyline, "EOF", SlpSym.EOF); }