package com.hzf.demo.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hzf.demo.R;
import com.hzf.demo.fragment.AlbumFragment;
import com.hzf.demo.fragment.EditPostFragment;
import com.hzf.demo.hardwrare.CameraManager;
import com.hzf.demo.hardwrare.SensorControler;
import com.hzf.demo.record.OnCameraUseListener;
import com.hzf.demo.utils.Constants;
import com.hzf.demo.utils.Utils;
import com.hzf.demo.view.CameraGLSurfaceView;
import com.hzf.demo.view.CircleButton;
import com.hzf.demo.view.SquareCameraContainer;

public class MainActivity extends BaseActivity implements View.OnTouchListener {

    private CameraManager mCameraManager;
    private SquareCameraContainer mCameraContainer;

    private boolean mUsingCamera;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewsOnClickListener(R.id.btnTakePhotoOrRecord, R.id.btnFlashlight, R.id.btnSwitchCamera, R.id.btnAlbum, R.id.btnClose);
    }


    @Override
    public void onResume() {
        super.onResume();
        mCameraManager = CameraManager.getInstance(this);
        mCameraManager.setCameraDirection(CameraManager.CameraDirection.CAMERA_BACK);
        initCameraLayout();
        findViewById(R.id.btnTakePhotoOrRecord).setVisibility(View.VISIBLE);
        ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
        setViewVisible(R.id.recorder_flashlight_parent1, R.id.btnAlbum);
        showOrHideAllBtn(true);
        mUsingCamera = false;
        mIsToFilterFragment = false;
        findViewById(R.id.btnTakePhotoOrRecord).setOnTouchListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraContainer != null) {
            if (mCameraContainer.getParent() != null) {
                ((ViewGroup) mCameraContainer.getParent()).removeAllViews();
            }
            mCameraContainer.onStop();
        } else {
            if (mCameraManager != null) {
                mCameraManager.releaseActivityCamera();
            }
        }
        mUsingCamera = false;
        mCameraContainer = null;
    }

    private void initCameraLayout() {
        RelativeLayout topLayout = findViewById(R.id.recorder_surface_parent);
        topLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.focusView).setOnTouchListener(this);
        if (topLayout.getChildCount() > 0)
            topLayout.removeAllViews();

        if (mCameraContainer == null) {
            if (topLayout.getChildCount() > 0)
                topLayout.removeAllViews();
            mCameraContainer = new SquareCameraContainer(this);
        }
        mCameraContainer.onStart();
        mCameraContainer.bindActivity(this);
        if (mCameraContainer.getParent() == null) {
            RelativeLayout.LayoutParams layoutParam1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParam1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            topLayout.addView(mCameraContainer, layoutParam1);
        }

        showSwitchCameraIcon();
    }

    private void showOrHideAllBtn(final boolean isShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShow) {
                    setViewVisible(R.id.recorder_flashlight_parent1, R.id.btnAlbum);
                } else {
                    setViewGone(R.id.recorder_flashlight_parent1, R.id.btnAlbum);
                }
            }
        });
    }

    private long mTapTime, mStartRecordingTime, mTouchDownTime;
    private boolean mIsStartTimer, mIsFingerUp;
    private Object mLockObject = new Object();
    private int mTouchType; //0表示什么都么做，1表示拍照或录制视频
    private boolean mIsToFilterFragment;
    private Bitmap mTakeBmp;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mUsingCamera || mCameraContainer == null) {
            return true;
        }
        if (v.getId() == R.id.focusView) {
            mCameraContainer.onTouchEvent(event);
            return true;
        }
        if (v.getId() != R.id.btnTakePhotoOrRecord) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownTime = System.currentTimeMillis();
                mTouchType = 0;
                ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).startScaleAnim();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchType == 0 && System.currentTimeMillis() - mTouchDownTime > 200) {
                    mTouchType = 1;
                    if (!initTakeOrRecord(true)) {
                        mTouchType = 3;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchType == 0) {
                    mTouchType = 1;
                    if (!initTakeOrRecord(false)) {
                        mTouchType = 3;
                    }
                }
                if (mTouchType == 1) {
                    mIsFingerUp = true;
                    synchronized (mLockObject) {
                        mIsStartTimer = false;
                        if (mStartRecordingTime > 0) {
                            if (!mCameraContainer.isRecording()) {
                                return true;   //在此之前就已经结束
                            }
                            stopRecording();
                        } else {
                            int x = (int) event.getRawX();
                            int y = (int) event.getRawY();
                            int[] location = new int[2];
                            v.getLocationOnScreen(location);
                            if ((x > location[0] && x < location[0] + v.getWidth()) && (y > location[1] && y < location[1] + v.getHeight())) {
                                findViewById(R.id.btnTakePhotoOrRecord).setVisibility(View.INVISIBLE);
                                mUsingCamera = true;
                                boolean isSuccessful = mCameraContainer.takePicture(new OnCameraUseListener() {
                                    @Override
                                    public void takePicture(final Bitmap bmp) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTakeBmp = bmp;
                                                if (bmp != null) {
                                                    mCameraContainer.stopPreview();
                                                    ImageView showPic = findViewById(R.id.showPic);
                                                    showPic.setVisibility(View.VISIBLE);
                                                    showPic.setImageBitmap(bmp);
                                                    String srcPath = Utils.saveJpeg(bmp, MainActivity.this);
                                                    toEditPostFragment(Constants.PIC_SHADER_FILTER, srcPath);
                                                } else {
                                                    SensorControler.getInstance(MainActivity.this).unlockFocus();
                                                    mUsingCamera = false;
                                                    mCameraContainer.startPreview();
                                                    ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                                                    showOrHideAllBtn(true);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void recordingEnd(String videoPath) {

                                    }
                                });
                                if (!isSuccessful) {
                                    mUsingCamera = false;
                                    mCameraContainer.startPreview();
                                    ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                                    showOrHideAllBtn(true);
                                }
                            } else {
                                mUsingCamera = false;
                                mCameraContainer.startPreview();
                                ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                                showOrHideAllBtn(true);
                            }
                        }
                    }
                } else if (mTouchType == 3) {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
                    ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                    showOrHideAllBtn(true);
                }
                break;
            default:
                break;
        }
        return true;
    }


    private boolean initTakeOrRecord(boolean isRecord) {
        if (isRecord) {
            if (!Utils.isGrantPermission(this, Manifest.permission.CAMERA)
                    || !Utils.isGrantPermission(this, Manifest.permission.RECORD_AUDIO)
                    || !Utils.isGrantPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermission(0, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                return false;
            }
        } else {
            if (!Utils.isGrantPermission(this, Manifest.permission.CAMERA)
                    || !Utils.isGrantPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermission(0, Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                return false;
            }
        }
        mTapTime = System.currentTimeMillis();
        mStartRecordingTime = 0;
        mIsFingerUp = false;
        if (isRecord) {
            startTimer();
        }
        return true;
    }

    private void startTimer() {
        if (mIsStartTimer) {
            return;
        }
        mIsStartTimer = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsStartTimer) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mStartRecordingTime == 0 && System.currentTimeMillis() - mTapTime > 500 && !mIsFingerUp) {   //当按下时间超过0.5s时,默认为开始录制视频
                        synchronized (mLockObject) {
                            showOrHideAllBtn(false);
                            mCameraContainer.startRecording();
                            mStartRecordingTime = System.currentTimeMillis();
                        }
                    }
                    if (mIsFingerUp) {
                        mIsStartTimer = false;
                        break;
                    }
                    if (mCameraContainer.isRecording()) {
                        long recordTime = System.currentTimeMillis() - mStartRecordingTime;
                        ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetArcAngle(recordTime, CameraGLSurfaceView.MAX_DURATION);
                        if (recordTime >= CameraGLSurfaceView.MAX_DURATION) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopRecording();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    private void stopRecording() {
        mIsStartTimer = false;
        mUsingCamera = true;
        findViewById(R.id.btnTakePhotoOrRecord).setVisibility(View.INVISIBLE);
        mCameraContainer.stopRecording(new OnCameraUseListener() {
            @Override
            public void takePicture(Bitmap bmp) {

            }

            @Override
            public void recordingEnd(String videoPath) {
                if (System.currentTimeMillis() - mStartRecordingTime > 1100) {
                    mCameraContainer.stopPreview();
                    toEditPostFragment(Constants.VIDEO_DEGREE_0, videoPath);
                    findViewById(R.id.btnTakePhotoOrRecord).setOnTouchListener(null);
                } else {
                    mUsingCamera = false;
                    mCameraContainer.startPreview();
                    ((CircleButton) findViewById(R.id.btnTakePhotoOrRecord)).resetCircleButton();
                    showOrHideAllBtn(true);
                }
            }
        });
    }


    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.btnFlashlight) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                return;
            }
            mCameraManager.setLightStatus(mCameraManager.getLightStatus().next());
            showFlashIcon();
        } else if (id == R.id.btnSwitchCamera) {
            mCameraManager.setCameraDirection(mCameraManager.getCameraDirection().next());
            v.setClickable(false);
            mCameraContainer.switchCamera();
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.setClickable(true);
                }
            }, 500);
            showSwitchCameraIcon();
        } else if (id == R.id.btnAlbum) {
         gotoPager(AlbumFragment.class, null);
        } else if (id == R.id.btnClose) {
            goBack();
        }
    }

    private void showSwitchCameraIcon() {
        if (mCameraManager.getCameraDirection() == CameraManager.CameraDirection.CAMERA_FRONT) {
            findViewById(R.id.btnFlashlight).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.btnFlashlight).setVisibility(View.VISIBLE);
            showFlashIcon();
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            findViewById(R.id.btnSwitchCamera).setVisibility(View.VISIBLE);
        }
    }


    private void showFlashIcon() {
        if (mCameraManager.getLightStatus() == CameraManager.FlashLigthStatus.LIGHT_ON) {
            ((ImageButton) findViewById(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashon);
        } else {
            ((ImageButton) findViewById(R.id.btnFlashlight)).setImageResource(R.drawable.camera_flashoff);
        }
    }

    private synchronized void toEditPostFragment(int shaderFilter, String srcPath) {
        if (mIsToFilterFragment) {
            return;
        }
        mIsToFilterFragment = true;
        Bundle bundle = new Bundle();
        bundle.putString(EditPostFragment.SOURCE_PATH, srcPath);
        bundle.putInt(EditPostFragment.FILTER_TYPE, shaderFilter);
        bundle.putBoolean(EditPostFragment.IS_FROM_ALBUM, false);
        gotoPager(EditPostFragment.class, bundle);


    }

    @Override
    public void onStop() {
        super.onStop();
        ImageView showPic = findViewById(R.id.showPic);
        showPic.setImageBitmap(null);
        if (mTakeBmp != null && !mTakeBmp.isRecycled()) {
            mTakeBmp.recycle();
        }
        mTakeBmp = null;
    }
}
