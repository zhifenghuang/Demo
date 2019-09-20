package com.hzf.demo.record;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.util.Log;

import com.hzf.demo.manager.DataManager;
import com.hzf.demo.utils.Constants;
import com.yeemos.yeemos.jni.ShaderJNILib;

import javax.microedition.khronos.opengles.GL10;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class ShaderViewDraw {

    public static final String NO_FILTER_VERTEX_SHADER = ""
            + "attribute vec4 position;\n"
            + "attribute vec2 inputTextureCoordinate;\n" + " \n"
            + "varying vec2 textureCoordinate;\n" + " \n" + "void main()\n"
            + "{\n" + "    gl_Position = position;\n"
            + "    textureCoordinate = inputTextureCoordinate;\n" + "}";
    public static final String NO_FILTER_FRAGMENT_SHADER = ""
            + "varying highp vec2 textureCoordinate;\n"
            + " \n"
            + "uniform sampler2D inputImageTexture;\n"
            + " \n"
            + "void main()\n"
            + "{\n"
            + "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
            + "}";

    private FloatBuffer vertexBuffer, vertexBuffer2, textureVerticesBuffer, textureVerticesBuffer2;
    private ShortBuffer drawListBuffer;
    private int mProgram, mProgram2;
    private int mGLAttribPosition, mGLAttribPosition2;
    private int mGLAttribTextureCoordinate, mGLAttribTextureCoordinate2;
    private int mGLUniformTexture, mGLUniformTexture2;
    private int mSingleStepOffsetLocation;

    private short drawOrder[] = {0, 1, 2, 2, 0, 3};

    private final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4;

    float squareCoords[];
    float squareCoords2[];

    float textureVertices[];
    float textureVertices2[];

    private int mTextureId, mTextureId2;

    private int mTextureType;

    private void resetBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);


        bb = ByteBuffer.allocateDirect(squareCoords2.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer2 = bb.asFloatBuffer();
        vertexBuffer2.put(squareCoords2);
        vertexBuffer2.position(0);

        bb = ByteBuffer.allocateDirect(textureVertices2.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textureVerticesBuffer2 = bb.asFloatBuffer();
        textureVerticesBuffer2.put(textureVertices2);
        textureVerticesBuffer2.position(0);
    }

    private void initShader() {
        int vertexShader;
        int fragmentShader;
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                ShaderJNILib.getVertexSource());
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                ShaderJNILib.getFragmentSource());
        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        mGLAttribPosition = GLES20.glGetAttribLocation(mProgram, "position");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mProgram,
                "inputTextureCoordinate");
        mGLUniformTexture = GLES20.glGetUniformLocation(mProgram,
                "inputImageTexture");
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(mProgram,
                "singleStepOffset");


        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, NO_FILTER_VERTEX_SHADER);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                NO_FILTER_FRAGMENT_SHADER);
        mProgram2 = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram2, vertexShader);
        GLES20.glAttachShader(mProgram2, fragmentShader);
        GLES20.glLinkProgram(mProgram2);
        mGLAttribPosition2 = GLES20.glGetAttribLocation(mProgram2, "position");
        mGLAttribTextureCoordinate2 = GLES20.glGetAttribLocation(mProgram2,
                "inputTextureCoordinate");
        mGLUniformTexture2 = GLES20.glGetUniformLocation(mProgram2,
                "inputImageTexture");
        mTextureId2 = -1;
        resetNewTextureId();
    }

    private void initBuffer(int textureType) {
        squareCoords = new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f}/*new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f}*/;
//        if (isBackCamera) {
//            textureVertices = new float[]{0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f};
//        } else {
//            textureVertices = new float[]{1f, 1f, 0f, 1f, 0f, 0f, 1f, 0f}/*new float[]{0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f}*/;
//        }

        if (textureType == Constants.VIDEO_DEGREE_0 || textureType == Constants.PIC_SHADER_FILTER) {
            textureVertices = new float[]{0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f};
        } else if (textureType == Constants.VIDEO_DEGREE_90) {
            textureVertices = new float[]{0f, 1f, 1f, 1f, 1f, 0f, 0f, 0f};
        } else if (textureType == Constants.VIDEO_DEGREE_180) {
            textureVertices = new float[]{1f, 1f, 1f, 0f, 0f, 0f, 0f, 1f};
        } else {
            textureVertices = new float[]{1f, 1f, 0f, 1f, 0f, 0f, 1f, 0f};
        }

        squareCoords2 = new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f};
        textureVertices2 = new float[]{0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f};

        resetBuffer();
        ByteBuffer bb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        bb.order(ByteOrder.nativeOrder());
        drawListBuffer = bb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    public ShaderViewDraw() {
        initShader();
    }

    public void resetTextureID(int textureId, int textureType, int viewWidth, int viewHeight) {
        this.mTextureId = textureId;
        mTextureType = textureType;
        initBuffer(textureType);
        setTextureWH(viewWidth, viewHeight);
    }

    public void setTextureWH(int width, int height) {
        GLES20.glUniform2fv(mSingleStepOffsetLocation, 1, FloatBuffer.wrap(new float[]{2.0f / width,
                2.0f / height}));
    }


    public void draw() {
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GL10.GL_BLEND);

        draw(mProgram, vertexBuffer, textureVerticesBuffer,
                mGLAttribPosition, mGLAttribTextureCoordinate,
                mGLUniformTexture, mTextureId, mTextureType == Constants.PIC_SHADER_FILTER);

        resetNewTextureId();
        if (mTextureId2 > 0) {
            draw(mProgram2, vertexBuffer2, textureVerticesBuffer2,
                    mGLAttribPosition2, mGLAttribTextureCoordinate2,
                    mGLUniformTexture2, mTextureId2, true);
        }
    }

    private void draw(int program, FloatBuffer vertexBuffer,
                      FloatBuffer textureVerticesBuffer, int mGLAttribPosition,
                      int mGLAttribTextureCoordinate, int mGLUniformTexture, int textureId, boolean isPic) {
        GLES20.glUseProgram(program);


        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureVerticesBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate,
                COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride,
                textureVerticesBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(isPic ? GLES20.GL_TEXTURE_2D : GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(mGLUniformTexture, 0);

        drawListBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(isPic ? GLES20.GL_TEXTURE_2D : GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    /**
     * terminatinng, this should be called in GL context
     */
    public void release() {
        if (mProgram >= 0)
            GLES20.glDeleteProgram(mProgram);
        if (mProgram2 >= 0)
            GLES20.glDeleteProgram(mProgram2);
        mProgram = -1;
        mProgram2 = -1;
    }

    private void resetNewTextureId() {
        Object obj = DataManager.getInstance().getObject();
        if (obj instanceof Bitmap) {
            Bitmap bmp = (Bitmap) obj;
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            bmp.recycle();
            bmp = null;
            mTextureId2 = textures[0];
            DataManager.getInstance().setObject(null);
        }
    }
}
