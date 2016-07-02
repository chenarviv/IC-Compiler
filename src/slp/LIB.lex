/* FIRST PART */
package slp;
import java_cup.runtime.Symbol;


%%
/* SECOND PART */

%class Liblexer
%public
%function next_token
%type Token
%scanerror LexicalError
%unicode
%cup
%line
%column
%{
  StringBuffer string = new StringBuffer();
  int templine = 0, tempcol = 0;
%}

%eofval{
	if(yystate()==COMMENTS) { 
		throw new LexicalError((yyline + 1) +":" + (yycolumn + 1) + ": lexical error; unclosed comment. expected: '*/' but found 'EOF'"); 
	}
	if(yystate()==STRING) { 
		throw new LexicalError((yyline + 1) +":" + (yycolumn + 1) + ": lexical error; unclosed string. expected: '\"' but found 'EOF'");
	}
  	return new Token((yyline + 1),"EOF", LibrarySym.EOF);
%eofval}

alpha = [a-z]
ALPHA = [A-Z]
Letters = {alpha}|{ALPHA}
Digits = [0-9]

/* whitespaces */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

/* identifiers */
Identifier = {alpha}({Letters}|{Digits}|_)*
ClassIdentifier =  {ALPHA}({Letters}|{Digits}|_)*

%state STRING 
%state COMMENTS 
%state LINECOMMENTS
 
%%
/* THIRD PART */

 /* keywords */
 
<YYINITIAL> "class"		{ return new Token((yyline + 1), yytext(), LibrarySym.CLASS); }
<YYINITIAL> "static"	{ return new Token((yyline + 1), yytext(), LibrarySym.STATIC); }
<YYINITIAL> "int"		{ return new Token((yyline + 1), yytext(), LibrarySym.INT); }
<YYINITIAL> "boolean"	{ return new Token((yyline + 1), yytext(), LibrarySym.BOOLEAN); }
<YYINITIAL> "string"	{ return new Token((yyline + 1), yytext(), LibrarySym.STRING); }
<YYINITIAL> "void"		{ return new Token((yyline + 1), yytext(), LibrarySym.VOID); }
<YYINITIAL> "(" 		{ return new Token((yyline + 1), yytext(), LibrarySym.LP); }
<YYINITIAL> ")" 		{ return new Token((yyline + 1), yytext(), LibrarySym.RP); }
<YYINITIAL> "{" 		{ return new Token((yyline + 1), yytext(), LibrarySym.LCBR); }
<YYINITIAL> "}" 		{ return new Token((yyline + 1), yytext(), LibrarySym.RCBR); }
<YYINITIAL> "[" 		{ return new Token((yyline + 1), yytext(), LibrarySym.LB); }
<YYINITIAL> "]" 		{ return new Token((yyline + 1), yytext(), LibrarySym.RB); }
<YYINITIAL> "," 		{ return new Token((yyline + 1), yytext(), LibrarySym.COMMA); }
<YYINITIAL> ";" 		{ return new Token((yyline + 1), yytext(), LibrarySym.SEMI); }
<YYINITIAL> "Library" 	{ return new Token((yyline + 1), yytext(), LibrarySym.LIBRARY); }


 <YYINITIAL> {Identifier}			{ return new Token((yyline + 1),"ID",LibrarySym.ID,yytext()); }
  <YYINITIAL> {ClassIdentifier}		{ return new Token((yyline + 1),"CLASS_ID",LibrarySym.CLASSIDENT,yytext()); }
 <YYINITIAL> "\""                   { 
 	tempcol = yycolumn + 1;
 	templine = yyline + 1; 
 	string.setLength(0); 
 	yybegin(STRING); 
 }
 <YYINITIAL> {WhiteSpace}           { /* ignore */ }
<STRING> {
  {LineTerminator}				{ throw new LexicalError((yyline + 1) +":" + (yycolumn + 1) + ": lexical error; unclosed string. expected: '\"' but found '\\n'"); }
  \\							{ throw new LexicalError((yyline + 1) +":" + (yycolumn + 1) + ": lexical error; illegal character '\\'"); }
  \"                            { yybegin(YYINITIAL); return new Token((yyline + 1), yytext(), LibrarySym.STRING_LITERAL); }
  \\t                           { string.append("\t"); }
  \\n                           { string.append("\n"); }
  \\r                           { string.append("\r"); }
  \\\"                          { string.append("\\"); }
  \\\\	                        { string.append("\\"); }
  [ -~]							{ string.append( yytext() ); }
  [^]							{ throw new LexicalError((yyline + 1) +":" + (yycolumn + 1) + ": lexical error; illegal character '" +yytext() + "'"); }
  
}

<YYINITIAL> "//" { yybegin(LINECOMMENTS); }
<LINECOMMENTS> [^\n] { }
<LINECOMMENTS> ["\r"]?["\n"] { yybegin(YYINITIAL); }

<YYINITIAL> "/*" {templine = (yyline + 1); tempcol = (yycolumn + 1); yybegin(COMMENTS); }
<COMMENTS> {
	"*/"  	{ yybegin(YYINITIAL); }
	[^]		{}
}


 /* error fallback */
[^]   { throw new LexicalError((yyline + 1) +":" + (yycolumn + 1) + ": lexical error; illegal character '" +yytext() + "'"); }



