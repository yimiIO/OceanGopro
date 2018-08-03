package io.yimi.gopro.util;

import android.opengl.GLES20;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLDrawer2D implements IDrawer2D {
    private static final float[] VERTICES = new float[]{1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, -1.0F};
    private static final float[] TEXCOORD = new float[]{1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F};
    private static final int FLOAT_SZ = 4;
    private static final int VERTEX_NUM = 4;
    private static final int VERTEX_SZ = 8;
    private final FloatBuffer pVertex;
    private final FloatBuffer pTexCoord;
    private final int mTexTarget;
    private int hProgram;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    private final float[] mMvpMatrix = new float[16];

    public GLDrawer2D(boolean isOES) {
        this.mTexTarget = isOES ? '赥' : 3553;
        this.pVertex = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pVertex.put(VERTICES);
        this.pVertex.flip();
        this.pTexCoord = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.pTexCoord.put(TEXCOORD);
        this.pTexCoord.flip();
        if (isOES) {
            this.hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        } else {
            this.hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\nprecision mediump float;\nuniform sampler2D sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        }

        Matrix.setIdentityM(this.mMvpMatrix, 0);
        this.init();
    }

    @Override
    public void release() {
        if (this.hProgram >= 0) {
            GLES20.glDeleteProgram(this.hProgram);
        }

        this.hProgram = -1;
    }

    @Override
    public float[] getMvpMatrix() {
        return this.mMvpMatrix;
    }

    @Override
    public IDrawer2D setMvpMatrix(float[] matrix, int offset) {
        System.arraycopy(matrix, offset, this.mMvpMatrix, 0, 16);
        return this;
    }

    @Override
    public void getMvpMatrix(float[] matrix, int offset) {
        System.arraycopy(this.mMvpMatrix, 0, matrix, offset, 16);
    }

    @Override
    public synchronized void draw(int texId, float[] tex_matrix, int offset) {
        if (this.hProgram >= 0) {
            GLES20.glUseProgram(this.hProgram);
            if (tex_matrix != null) {
                GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, tex_matrix, offset);
            }

            GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(this.mTexTarget, texId);
            GLES20.glDrawArrays(5, 0, 4);
            GLES20.glBindTexture(this.mTexTarget, 0);
            GLES20.glUseProgram(0);
        }
    }

    @Override
    public void draw(ITexture texture) {
        this.draw(texture.getTexture(), texture.getTexMatrix(), 0);
    }

    public void draw(TextureOffscreen offscreen) {
        this.draw(offscreen.getTexture(), offscreen.getTexMatrix(), 0);
    }

    public int initTex() {
        return GLHelper.initTex(this.mTexTarget, 9728);
    }

    public void deleteTex(int hTex) {
        GLHelper.deleteTex(hTex);
    }

    public synchronized void updateShader(String vs, String fs) {
        this.release();
        this.hProgram = GLHelper.loadShader(vs, fs);
        this.init();
    }

    public void updateShader(String fs) {
        this.updateShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", fs);
    }

    public int glGetAttribLocation(String name) {
        GLES20.glUseProgram(this.hProgram);
        return GLES20.glGetAttribLocation(this.hProgram, name);
    }

    public int glGetUniformLocation(String name) {
        GLES20.glUseProgram(this.hProgram);
        return GLES20.glGetUniformLocation(this.hProgram, name);
    }

    public void glUseProgram() {
        GLES20.glUseProgram(this.hProgram);
    }

    private void init() {
        GLES20.glUseProgram(this.hProgram);
        this.maPositionLoc = GLES20.glGetAttribLocation(this.hProgram, "aPosition");
        this.maTextureCoordLoc = GLES20.glGetAttribLocation(this.hProgram, "aTextureCoord");
        this.muMVPMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uMVPMatrix");
        this.muTexMatrixLoc = GLES20.glGetUniformLocation(this.hProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(this.muMVPMatrixLoc, 1, false, this.mMvpMatrix, 0);
        GLES20.glUniformMatrix4fv(this.muTexMatrixLoc, 1, false, this.mMvpMatrix, 0);
        GLES20.glVertexAttribPointer(this.maPositionLoc, 2, 5126, false, 8, this.pVertex);
        GLES20.glVertexAttribPointer(this.maTextureCoordLoc, 2, 5126, false, 8, this.pTexCoord);
        GLES20.glEnableVertexAttribArray(this.maPositionLoc);
        GLES20.glEnableVertexAttribArray(this.maTextureCoordLoc);
    }
}
