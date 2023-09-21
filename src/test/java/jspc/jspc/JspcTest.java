package jspc.jspc;

import jspc.compile.JspCompiler;
import org.junit.Test;

public class JspcTest {

    @Test
    public void test1() {
        JspCompiler jspCompiler = new JspCompiler();
        jspCompiler.compile("/home/ran/Documents/work/thusa/cbfe","./compiledjsp");
    }
}
