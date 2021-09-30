package com.zzkk.community;

import java.io.File;

/**
 * @author zzkk
 * @ClassName TestFile
 * @Description Todo
 **/
public class TestFile {
    public static void main(String[] args) {
        File file = new File("d:/zhuangshaotest/kk");
        boolean mkdir = file.mkdirs();
        System.out.println(mkdir);
    }
}
