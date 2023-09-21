package example.tests;

public class UnusedLocal {
    public static void main(String[] args) {
        int a = 0;
        String b = "aa";
        a += 1;
        System.out.println(a);
    }
}
