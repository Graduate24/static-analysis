package core;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import utils.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LibProjectTest {

    @Test
    public void test1() {
        //FileUtility.flatExtractJar("D:\\jsp-demo.zip", "libtest/");
        FileUtil.flatExtractJar("libtest/sa-compile.jar", "libtest/");
    }

    @Test
    public void test2(){
        System.out.println(FilenameUtils.removeExtension(Paths.get("libtest/sa-compile.jar").toString()));
    }

}
