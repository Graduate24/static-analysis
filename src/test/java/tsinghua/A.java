package tsinghua;

import com.google.gson.Gson;
import edu.tsinghua.Main;

public class A {
    public void m1() {
        Gson gson = new Gson();
        String s = "hello";
        String a = afoo(s);
        int x = 3;
        String b = a + x;
        if (b.length() > 3) {
            x = abar(x);
        }
        edu.tsinghua.Main main = new edu.tsinghua.Main();
        gson.toJson(main.foo() + x);
    }

    public String afoo(String s) {

        return new Main().zoo()+s;
    }

    public int abar(int x) {
        return x + 1;
    }
}
