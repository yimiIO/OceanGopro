package io.yimi.gopro.util;

import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AssetsHelper {
    public AssetsHelper() {
    }

    public static String loadString(AssetManager assets, String name) throws IOException {
        StringBuffer sb = new StringBuffer();
        char[] buf = new char[1024];
        BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(name)));
        int r = reader.read(buf);

        while(r > 0) {
            sb.append(buf, 0, r);
        }

        return sb.toString();
    }
}
