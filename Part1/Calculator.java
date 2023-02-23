import java.io.InputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.EmptyStackException;

/*
Η LL(1) γραμματική είναι:
    1) expr -> term expr2
    2) expr2 -> ^ term expr2
    3)        | ε
    4) term -> factor term2
    5) term2 -> & factor term2
    6)        | ε
    7) factor -> num 
    8)        | (expr)
    9)        | ε
    10) num -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 

FIRST SETS: (Χάριν ευκολίας γράφω για τα μη τερματικά)
    first(expr2) = {^, ε}
    fisrt(term2) = {&, ε}
    first(num) = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}
    first(factor) = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, (, ε}
    first(term) = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, (, ε, &}  <= (first(factor) + first(term2))
    first(expr) = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, (, ε, &, ^} <= (first(term) + first(expr2))

FOLLOW SETS:
    follow(expr) = { EOF, ) }
    follow(expr2) = { EOF, ) }
    follow(term) = { ^, EOF, ) } <= first(expr2) + follow(expr2)
    follow(term2) = { ^, EOF, ) }  <= follow(term)
    follow(factor) = { &, ^, EOF, ) } <= first(term2) + follow(term2)
    follow(num) = { &, ^, EOF, ) } <= follow(factor)

FIRST+ SETS:
    first+(#1) = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, (, ε, &, ^, EOF, ) }

    first+(#2) = { ^ }
    first+(#3) = { EOF, ), ε}

    first+(#4) = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, (, ε, &, ^, EOF, ) }

    first+(#5) = { & }
    first+(#6) = { ^, EOF, ), ε } 

    first+(#7) = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }
    first+(#8) = { ( }
    first+(#9) = { &, ^, EOF, ), ε }

    first+(#10) = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }

LOOKAHEAD TABLE:
           | 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 |      &       |     ^        |    EOF       |     (        |     )    
    -------|-----------------------------------------------------------------------------------------------------------------
    expr   | term expr2                   | term expr2   | term expr2   | term expr2   | term expr2   | term expr2
    expr2  | parse error                  | parse error  | term expr2   |  ε           | parse error  | ε                    
    term   | factor term2                 | factor term2 | factor term2 | factor term2 | factor term2 | factor term2                                        
    term2  | parse error                  | factor term2 | ε            | ε            | parse error  | ε                    
    factor | num                          | ε            | ε            | ε            | expr)        | ε                        
    num    | consume                      | parse error  | parse error  | parse error  | parse error  | parse error                 
*/

class Calculator {
    private final InputStream in;
    private int lookahead;
    private Stack<Integer> values;  //στοίβα η οποία θα κρατάει τις τιμές που πρέπει να υπολογιστούν
    
    public Calculator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
        this.values = new Stack<Integer>();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol) {
            lookahead = in.read();
        } else {
            throw new ParseError();
        }
    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private boolean isSymbol(int c) {
        return c == '(' || c == ')' || c == '&' || c == '^';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    //βγάζει 2 στοιχεία από τη στοίβα, εκτελεί την bitwise πράξη και αποθηκεύει το αποτέλεσμα πάλι στη στοίβα
    private void bitwise(int c) throws EmptyStackException{
        int a, b, res;
        a = this.values.pop();  //βγάλε από τη στοίβα 2 στοιχεία και κάνε την πράξη
        b = this.values.pop();

        if (c == '^') {
            res = a ^ b;
        } else {
            res = a & b;
        }

        this.values.push(res);  //αποθήκευσε πάλι το αποτέλεσμα
    }

    public int eval() throws IOException, ParseError, EmptyStackException {
        expr();
        if (lookahead != -1 && lookahead != '\n') {
            throw new ParseError();
        }
        return values.pop();
    }

    private void expr() throws IOException, ParseError, EmptyStackException {
        if (isDigit(lookahead) || isSymbol(lookahead) || lookahead == -1 || lookahead == '\n') {
            term();
            expr2();
            return;
        } 
        throw new ParseError();
    }

  private void term() throws IOException, ParseError, EmptyStackException {
        if (isDigit(lookahead) || isSymbol(lookahead) || lookahead == -1 || lookahead == '\n') {
            factor();
            term2();
            return;
        }
        throw new ParseError();
    }

    private void expr2() throws IOException, ParseError, EmptyStackException {
        if (lookahead == '^') {
            consume(lookahead);
            term();
            bitwise('^');   //εκτέλεσε την πράξη και αποθήκευσε την στην στοίβα
            expr2();
            return;
        } 
        else if (lookahead == -1 || lookahead == '\n' || lookahead == ')') {    //expr2->ε
            return;
        } 
        throw new ParseError();
    }

    

    private void term2() throws IOException, ParseError, EmptyStackException {
        int a, b, res;
        if (lookahead == '&') {
            consume(lookahead);
            factor();
            bitwise('&');   //εκτέλεσε την πράξη και αποθήκευσε την στην στοίβα
            term2();
            return;
        } else if (lookahead == '(') {
            throw new ParseError();
        } else if (isSymbol(lookahead) || lookahead == -1 || lookahead == '\n') { //terms -> ε
            return;
        } else {
            throw new ParseError();
        }
    }

    private void factor() throws IOException, ParseError {
        int value;
        if (isDigit(lookahead)) {
            value = evalDigit(lookahead);
            this.values.push(value); //προσθέτουμε την τιμή στη στοίβα
            consume(lookahead);
            return;
        } 
        if (lookahead == '(') {
            consume(lookahead);
            expr();
            consume(')');
            return;
        }
        else if(isSymbol(lookahead) || lookahead == -1 || lookahead == '\n') {   //factor -> ε
            return;
        }
        throw new ParseError();
    }
}