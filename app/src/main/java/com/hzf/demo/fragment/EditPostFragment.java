package com.hzf.demo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hzf.demo.R;
import com.hzf.demo.manager.DataManager;
import com.hzf.demo.utils.AnimatedGifEncoder;
import com.hzf.demo.utils.Constants;
import com.hzf.demo.utils.Utils;
import com.hzf.demo.view.DrawCommentView;
import com.hzf.demo.view.GLShaderJNIView;
import com.hzf.demo.view.HandleRelativeLayout;
import com.yeemos.yeemos.jni.ExtractDecodeEditEncodeMuxTest;
import com.yeemos.yeemos.jni.ExtractMpegFramesTest;
import com.yeemos.yeemos.jni.ShaderJNILib;
import com.yeemos.yeemos.jni.TextureRender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by gigabud on 15-12-11.
 */
public class EditPostFragment extends BaseFragment implements View.OnClickListener {

    public static final String SOURCE_PATH = "source_path";
    public static final String FILTER_TYPE = "filter_type";
    public static final String IS_FROM_ALBUM = "isFromAlbum";

    private GLShaderJNIView mSurfaceView;
    protected int mShaderFilter;
    private String mSrcPath;
    private boolean isDeleteSrc = true;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_edit_post;
    }

    @Override
    protected void onViewCreated(View view) {
        init(view);
    }


    protected void init(final View view) {
        mSurfaceView = view.findViewById(R.id.glView);
        Bundle bundle = getArguments();
        if (bundle == null) {
            getActivity().finish();
            return;
        }
        mShaderFilter = bundle.getInt(FILTER_TYPE, Constants.PIC_SHADER_FILTER);
        mSrcPath = bundle.getString(SOURCE_PATH);
        if (mShaderFilter != Constants.ONLY_TEXT) {
            ShaderJNILib.setShaderType(mShaderFilter);
            ShaderJNILib.setPlatform(Constants.PLATFORM_ANDROID);
            mSurfaceView.setShaderFilterType(mShaderFilter, mSrcPath);
        }

        getDrawCommentView().setParentFragment(this);
        getHandleRelativeLayout().initViewPager();
        getHandleRelativeLayout().setOnHandleRelativeLayoutEvent(new HandleRelativeLayout.OnHandleRelativeLayoutEvent() {
            @Override
            public void onScroll(int pageNumber, float xOffset) {
                ShaderJNILib.resetXOffset(xOffset, pageNumber);
                mSurfaceView.requestRender();
            }

            @Override
            public void onClick() {
                if (!getDrawCommentView().isPostEditViewVisibility()) {
                    getDrawCommentView().findViewById(R.id.btnWartMark).performClick();
                }
            }
        });
        view.findViewById(R.id.btnSave).setOnClickListener(this);
    }

    public void onStart() {
        super.onStart();
        if (!TextUtils.isEmpty(mSrcPath)) {
            if (!new File(mSrcPath).exists()) {
                getActivity().finish();
            }
        }
    }


    public void onResume() {
        super.onResume();
        ShaderJNILib.resetXOffset(0f, getHandleRelativeLayout().getViewPager().getCurrentItem());
        if (mSurfaceView != null) {
            mSurfaceView.setVisibility(View.VISIBLE);
            mSurfaceView.onMediaResume();
        }
        getDrawCommentView().setVisibility(View.VISIBLE);
    }

    public void onPause() {
        super.onPause();
        if (mSurfaceView != null) {
            mSurfaceView.onMediaPause();
            mSurfaceView.setVisibility(View.GONE);
        }
        getDrawCommentView().setVisibility(View.GONE);
        ShaderJNILib.destroySource();
    }

    public void onDestroyView() {
        super.onDestroyView();
        mIsSavedVideo = true;
        getHandleRelativeLayout().destroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (isDeleteSrc && !TextUtils.isEmpty(mSrcPath)) {
            new File(mSrcPath).delete();
        }
        if (mSurfaceView != null) {
            mSurfaceView.releaseMediaPlayer();
        }
    }

    @Override
    public void updateUIText() {

    }


    private boolean mIsSavedVideo;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                DataManager.getInstance().setObject(null);
                if (mShaderFilter == Constants.PIC_SHADER_FILTER) {
            //        showLoadingDialog();
                    Toast.makeText(getActivity(), "开始保存", Toast.LENGTH_SHORT).show();
                    if (getDrawCommentView().isHadGifSticker()) {  //当贴有gif时，图片要转为视频
                        mIsSavedVideo = false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                final String savePath = Utils.createAlbumVideoPath();
                                mSurfaceView.startRecording(savePath);
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mSurfaceView.stopRecording(null);
                                mIsSavedVideo = true;
                                Utils.registerVideo(getActivity(), savePath);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                           //             hideLoadingDialog();
                                        Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).start();
                        startGetBmpInVideo();
                    } else {
                        mSurfaceView.startGetGLBmp(new GLShaderJNIView.OnSaveBmp() {
                            @Override
                            public void getGLViewBmp(final Bitmap bmp) {
                                getDrawCommentView().savePhotoToAlbum(bmp, getHandleRelativeLayout());
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideLoadingDialog();
                                        Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }


                } else {
             //       showLoadingDialog();
                    Toast.makeText(getActivity(), "开始保存", Toast.LENGTH_SHORT).show();
                    mIsSavedVideo = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                            test.setContext(getActivity());
                            String savePath = Utils.createAlbumVideoPath();
                            try {
                                test.init(mSrcPath, savePath, mShaderFilter, TextureRender.USE_FOR_UPLOAD_SAVE_POST, null);
                                DataManager.getInstance().setObject(getDrawCommentView().getBmpInVideo(getHandleRelativeLayout()));
                                test.testExtractDecodeEditEncodeMuxAudioVideo();
                                Utils.registerVideo(getActivity(), savePath);
                                mIsSavedVideo = true;
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            mIsSavedVideo = true;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
           //                         hideLoadingDialog();
                                    Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                    startGetBmpInVideo();
                }
                break;
        }
    }


    public HandleRelativeLayout getHandleRelativeLayout() {
        return (HandleRelativeLayout) getView().findViewById(R.id.handleView);
    }

    private DrawCommentView getDrawCommentView() {
        return (DrawCommentView) getView().findViewById(R.id.drawCommentView);
    }


    private void startGetBmpInVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mIsSavedVideo) {
                    DataManager.getInstance().setObject(
                            getDrawCommentView().getBmpInVideo(getHandleRelativeLayout()));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private String convertVideoTopFiveFragmeToGif(String videoPath) {
        String saveFile = Utils.createCachePath(getActivity());
        File frameFile = new File(saveFile, "frame");
        String gifPath = null;
        if (!frameFile.exists() || !frameFile.isDirectory()) {
            frameFile.mkdirs();
        }
        try {
            ExtractMpegFramesTest test = new ExtractMpegFramesTest();
            //     test.setContext(getActivity());
            try {
                test.testExtractMpegFrames(videoPath, frameFile);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            OutputStream os = null;
            gifPath = saveFile + "/" + UUID.randomUUID().toString() + Utils.GIF_EXTENSION;
            os = new FileOutputStream(gifPath);
            Bitmap bitmap;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.setFrameRate(1000f);
            encoder.setRepeat(Integer.MAX_VALUE);
            encoder.start(bos);
            for (int i = 0; i < 9; ++i) {
                if (i <= 4) {
                    bitmap = BitmapFactory.decodeFile(frameFile + "/" + i + Utils.IMAGE_EXTENSION);
                } else {
                    bitmap = BitmapFactory.decodeFile(frameFile + "/" + (8 - i) + Utils.IMAGE_EXTENSION);
                }
                if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                    continue;
                }
                encoder.addFrame(bitmap);
                bitmap.recycle();
            }
            encoder.finish();
            os.write(bos.toByteArray());
            bos.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < 5; ++i) {
                new File(frameFile + "/" + i + Utils.IMAGE_EXTENSION).delete();
            }
        }
        return gifPath;
    }


    @Override
    public void goBack() {
        getActivity().finish();
    }

}
