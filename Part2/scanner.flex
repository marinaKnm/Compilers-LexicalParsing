import java_cup.runtime.*;

%% 
/* ----------------- Options and Declarations Section----------------- */

/*
   The name of the class JFlex will create will be Scanner.
   Will write the code to the file Scanner.java.
*/
%class Scanner

/*
  The current line number can be accessed with the variable yyline
  and the current column number with the variable yycolumn.
*/
%line
%column

/*
   Will switch to a CUP compatibility mode to interface with a CUP
   generated parser.
*/
%cup  /* I want to communicate that I want my scanner in the parser */
%unicode

/*
  Declarations

  Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions.
*/

%{
    /**
        The following two methods create java_cup.runtime.Symbol objects
    **/
    StringBuffer stringBuffer = new StringBuffer();
    private Symbol symbol(int type) {
       return new Symbol(type, yyline, yycolumn);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

/*
  Macro Declarations

  These declarations are regular expressions that will be used latter
  in the Lexical Rules Section.
*/

/* A line terminator is a \r (carriage return), \n (line feed), or
   \r\n. */
LineTerminator = \r|\n|\r\n

/* White space is a line terminator, space, tab, or line feed. */
WhiteSpace     = {LineTerminator} | [ \t\f]

Keywords = if | else | prefix | reverse
Identifier = [a-zA-Z][a-zA-Z0-9_]*
String = \"(\\.|[^\"])*\"
fOpener = [)]{WhiteSpace}*[{]
%%
/* ------------------------Lexical Rules Section---------------------- */

{Keywords}     {  /* these keywords are terminals and have no value */
                  String s = yytext();
                  if (s.equals("if")) {
                     return symbol(sym.IF);
                  } else if (s.equals("else")) {
                     return symbol(sym.ELSE);
                  } else if (s.equals("prefix")) {
                     return symbol(sym.PREFIX);
                  } else if (s.equals("reverse")) {
                     return symbol(sym.REVERSE);
                  }
               }
"+"            { return symbol(sym.CONCAT); }
"("            { return symbol(sym.LPAREN); }
")"            { return symbol(sym.RPAREN); }
{fOpener}      { return symbol(sym.FDECL); }
"}"            { return symbol(sym.RBRACKET); }
","            { return symbol(sym.COMMA); }

{Identifier}   { //System.out.println("identifier found: " + yytext()); 
                 //System.out.println("Returning identifier = " + yytext());
                 return symbol(sym.IDENTIFIER, yytext());
               }
{String}       {  
                  String s = yytext();    // yytext() is sth like => "string", we need to remove the ","
                  s = s.replace("\"", "");
                  //System.out.println("Returning string = " + s);
                  return symbol(sym.STRING_LITERAL, s); 
               }

{WhiteSpace} { /* just skip what was found, do nothing */ }

/* No token was found for the input so through an error.  Print out an
   Illegal character message with the illegal character that was found. */
[^]   { throw new Error("Illegal character <"+yytext()+">"); }
