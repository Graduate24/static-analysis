package example.helloworld;

public class LiveVariable {

    public static void main(String[] args) {
        int x = 3;
        int y = 6;
        int z = 0;
        if (args.length > 1) {
            x = y;
        } else {
            z = 2;
        }
        int b = y;
        int a = z;
        System.out.println(a + b + x);
    }
}
