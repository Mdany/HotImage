package com.chenyu.monster.hotimage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chenyu on 16/6/22.
 */
public class FileUtils {
    /**
     * 关闭输入流
     *
     * @param is
     */
    public static void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
