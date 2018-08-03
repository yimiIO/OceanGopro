package io.yimi.gopro.util;

import java.io.IOException;

public interface ITexture {
    void release();

    void bind();

    void unbind();

    int getTexTarget();

    int getTexture();

    float[] getTexMatrix();

    void getTexMatrix(float[] var1, int var2);

    int getTexWidth();

    int getTexHeight();

    void loadTexture(String var1) throws NullPointerException, IOException;
}
