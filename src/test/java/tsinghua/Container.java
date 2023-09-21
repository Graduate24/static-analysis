package tsinghua;

import edu.tsinghua.Item;

public class Container {
    private edu.tsinghua.Item item = new edu.tsinghua.Item();

    void setItem(edu.tsinghua.Item item) {
        this.item = item;
    }

    Item getItem() {
        return this.item;
    }
}

