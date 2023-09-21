package tsinghua;

import edu.tsinghua.Container;
import edu.tsinghua.Item;

public class Test {

    public void go() {
        edu.tsinghua.Container c1 = new edu.tsinghua.Container();
        edu.tsinghua.Item i1 = new edu.tsinghua.Item();
        c1.setItem(i1);
        edu.tsinghua.Container c2 = new edu.tsinghua.Container();
        edu.tsinghua.Item i2 = new Item();
        c2.setItem(i2);
        Container c3 = c2;
    }

    public static void main(String[] args) {
       new Test().go();
    }
}
