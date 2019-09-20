/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yeemos.yeemos.jni;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.hzf.demo.manager.DataManager;
import com.hzf.demo.utils.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Code for rendering a texture onto a surface using OpenGL ES 2.0.
 */
public class TextureRender {
    private static final String TAG = "TextureRender";

    public static final int USE_FOR_UPLOAD_SAVE_POST = 0;  //用于上传和保存post

    public static final int USE_FOR_ADD_WARTMARK = 1;   //用于给视频加水印图片

    public static final int USE_FOR_GET_NEW_VIDEO_FROM_ALBUM = 2;   //用于从相册中获取新的视频

    private int mUserType;


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

    public static final String NO_FILTER_FRAGMENT_SHADER_2 = ""
            + "#extension GL_OES_EGL_image_external : require\n"
            + "varying highp vec2 textureCoordinate;\n"
            + " \n"
            + "uniform samplerExternalOES inputImageTexture;\n"
            + " \n"
            + "void main()\n"
            + "{\n"
            + "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
            + "}";

    float squareCoords1[];
    float squareCoords2[];

    float textureVertices1[];
    float textureVertices2[];
    private FloatBuffer vertexBuffer1, vertexBuffer2, textureVerticesBuffer1,
            textureVerticesBuffer2;
    private short drawOrder[] = {0, 1, 2, 2, 0, 3};
    private final int COORDS_PER_VERTEX = 2;
    private final int vertexStride = COORDS_PER_VERTEX * 4;

    private ShortBuffer drawListBuffer;

    private int mTextureId1, mTextureId2;

    private int mProgram1, mProgram2;
    private int mGLAttribPosition1, mGLAttribPosition2, mWidthFactor, mHeightFactor;
    private int mGLAttribTextureCoordinate1, mGLAttribTextureCoordinate2;
    private int mGLUniformTexture1, mGLUniformTexture2;
    private float mWidthFactorValue, mHeightFactorValue;

    public TextureRender(int videoType, int userType, float xOffset, float yOffset) {
        mUserType = userType;
        initBuffer(videoType, xOffset, yOffset);
    }


    private void resetBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords1.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer1 = bb.asFloatBuffer();
        vertexBuffer1.put(squareCoords1);
        vertexBuffer1.position(0);

        bb = ByteBuffer.allocateDirect(textureVertices1.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textureVerticesBuffer1 = bb.asFloatBuffer();
        textureVerticesBuffer1.put(textureVertices1);
        textureVerticesBuffer1.position(0);

        if (mUserType != USE_FOR_GET_NEW_VIDEO_FROM_ALBUM) {
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
    }

    public int getTextureId() {
        return mTextureId1;
    }

    private void initBuffer(int videoType, float xOffset, float yOffset) {
        squareCoords1 = new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f};
        if (videoType == Constants.VIDEO_DEGREE_0) {
            textureVertices1 = new float[]{xOffset, yOffset, xOffset, 1f - yOffset, 1f - xOffset, 1f - yOffset, 1f - xOffset, yOffset};
        } else if (videoType == Constants.VIDEO_DEGREE_90) {
            textureVertices1 = new float[]{yOffset, 1f - xOffset, 1f - yOffset, 1f - xOffset, 1f - yOffset, xOffset, yOffset, xOffset};
        } else if (videoType == Constants.VIDEO_DEGREE_180) {
            textureVertices1 = new float[]{1f - xOffset, 1f - yOffset, 1f - xOffset, yOffset, xOffset, yOffset, xOffset, 1f - yOffset};
        } else {
            textureVertices1 = new float[]{1f - yOffset, xOffset, yOffset, xOffset, yOffset, 1f - xOffset, 1f - yOffset, 1f - xOffset};
        }

        if (mUserType != USE_FOR_GET_NEW_VIDEO_FROM_ALBUM) {
            squareCoords2 = new float[]{-1f, 1f, -1f, -1f, 1f, -1f, 1f, 1f};
            textureVertices2 = new float[]{0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f};
        }

        resetBuffer();
        ByteBuffer bb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        bb.order(ByteOrder.nativeOrder());
        drawListBuffer = bb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    public void drawFrame(SurfaceTexture st) {
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GL10.GL_BLEND);
        draw(mProgram1, vertexBuffer1, textureVerticesBuffer1,
                mGLAttribPosition1, mGLAttribTextureCoordinate1,
                mGLUniformTexture1, mWidthFactor, mHeightFactor, false);

        if (mUserType != USE_FOR_GET_NEW_VIDEO_FROM_ALBUM) {
            resetNewTextureId();
            if (mTextureId2 > 0) {
                draw(mProgram2, vertexBuffer2, textureVerticesBuffer2,
                        mGLAttribPosition2, mGLAttribTextureCoordinate2,
                        mGLUniformTexture2, -1, -1, true);
            }
        }
    }

    private void draw(int program, FloatBuffer vertexBuffer,
                      FloatBuffer textureVerticesBuffer, int mGLAttribPosition,
                      int mGLAttribTextureCoordinate, int mGLUniformTexture, int widthFactor, int heightFactor, boolean isPic) {
        GLES20.glUseProgram(program);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        textureVerticesBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate,
                COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride,
                textureVerticesBuffer);
        drawListBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        if (mUserType == USE_FOR_UPLOAD_SAVE_POST) {
            if (widthFactor >= 0) {
                GLES20.glUniform1f(widthFactor, mWidthFactorValue);
            }
            if (heightFactor >= 0) {
                GLES20.glUniform1f(heightFactor, mHeightFactorValue);
            }
        }

        if (isPic) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId2);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);

    }

    /**
     * Initializes GL state. Call this after the EGL surface has been created
     * and made current.
     */
    public void surfaceCreated() {
        int vertexShader;
        int fragmentShader;
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                mUserType == USE_FOR_UPLOAD_SAVE_POST ? ShaderJNILib.getVertexSource() : NO_FILTER_VERTEX_SHADER);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                mUserType == USE_FOR_UPLOAD_SAVE_POST ? ShaderJNILib.getFragmentSource() : NO_FILTER_FRAGMENT_SHADER_2);
        mProgram1 = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram1, vertexShader);
        GLES20.glAttachShader(mProgram1, fragmentShader);
        GLES20.glLinkProgram(mProgram1);
        mGLAttribPosition1 = GLES20.glGetAttribLocation(mProgram1, "position");
        mGLAttribTextureCoordinate1 = GLES20.glGetAttribLocation(mProgram1,
                "inputTextureCoordinate");
        mGLUniformTexture1 = GLES20.glGetUniformLocation(mProgram1,
                "inputImageTexture");
        if (mUserType == USE_FOR_UPLOAD_SAVE_POST) {
            mWidthFactor = GLES20.glGetUniformLocation(mProgram1, "imageWidthFactor");
            mHeightFactor = GLES20.glGetUniformLocation(mProgram1, "imageHeightFactor");
        }

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mTextureId1 = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId1);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        mTextureId2 = -1;
        if (mUserType != USE_FOR_GET_NEW_VIDEO_FROM_ALBUM) {
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
            resetNewTextureId();
        }
    }

    private void resetNewTextureId() {
        Object obj = DataManager.getInstance().getObject();
        if (obj instanceof Bitmap) {
            Bitmap bmp = (Bitmap) obj;
            mWidthFactorValue = 1f / bmp.getWidth();
            mHeightFactorValue = 1f / bmp.getHeight();
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

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


}
