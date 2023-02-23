import java.io.IOException;
import java.util.EmptyStackException;

class Main {
    public static void main(String[] args) {
        try {
            int result = (new Calculator(System.in)).eval();
            System.out.println("\n" + result);
        } catch(IOException | ParseError e) {
            System.err.println(e.getMessage());
        } catch (EmptyStackException e) {
            System.err.println("Parse error");
        }
    }
}