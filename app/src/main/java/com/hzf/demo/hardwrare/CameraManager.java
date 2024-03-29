package com.hzf.demo.hardwrare;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;


/**
 * 相机管理类 （singleTon）
 *
 * @author jerry
 * @date 2015-09-01
 */
public class CameraManager implements ICameraHelper {
    public static final String TAG = "CameraManager";

    private static CameraManager mInstance;
    private final ICameraHelper mCameraHelper;
    private Camera mStartTakePhotoCamera;
    private Camera mActivityCamera;

    private FlashLigthStatus mLightStatus;
    private CameraDirection mFlashDirection;

    private Context mContext;

    public static List<FlashLigthStatus> mFlashLightNotSupport = new ArrayList<FlashLigthStatus>();


    public static final int TYPE_PREVIEW = 0;
    public static final int TYPE_PICTURE = 1;

    public static final int ALLOW_PIC_LEN = 2000;       //最大允许的照片尺寸的长度   宽或者高

    //屏蔽默认构造方法
    private CameraManager(Context context) {

        mContext = context;
        mCameraHelper = new CameraHelperGBImpl();
        mLightStatus = FlashLigthStatus.LIGHT_OFF; //默认 自动
        mFlashDirection = CameraDirection.CAMERA_BACK; //默认后置摄像头
    }

    public static CameraManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (CameraManager.class) {
                if (mInstance == null) {
                    mInstance = new CameraManager(context);
                }
            }
        }
        return mInstance;
    }

    public void setStartTakePhotoCamera(Camera mStartTakePhotoCamera) {
        this.mStartTakePhotoCamera = mStartTakePhotoCamera;
    }

    public void setActivityCamera(Camera mActivityCamera) {
        this.mActivityCamera = mActivityCamera;
    }

    public FlashLigthStatus getLightStatus() {
        return mLightStatus;
    }

    public void setLightStatus(FlashLigthStatus mLightStatus) {
        this.mLightStatus = mLightStatus;
    }

    public CameraDirection getCameraDirection() {
        return mFlashDirection;
    }

    public void setCameraDirection(CameraDirection mFlashDirection) {
        this.mFlashDirection = mFlashDirection;
    }


    @Override
    public int getNumberOfCameras() {
        return mCameraHelper.getNumberOfCameras();
    }

    @Override
    public Camera openCameraFacing(int facing) throws Exception {
        Camera camera = mCameraHelper.openCameraFacing(facing);
        mFlashLightNotSupport.clear();
        if (camera != null) {
            List<String> supportFlashModes = camera.getParameters().getSupportedFlashModes();
            if (facing == 0) {
                //某些supportFlashModes  null  不支持
                if (supportFlashModes != null) {
//                    if (!supportFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//                        mFlashLightNotSupport.add(FlashLigthStatus.LIGHT_AUTO);
//                    }
                    if (!supportFlashModes.contains(Camera.Parameters.FLASH_MODE_ON) && !supportFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                        mFlashLightNotSupport.add(FlashLigthStatus.LIGHT_ON);
                    }
                }
            }
        }
        return camera;
    }

    @Override
    public boolean hasCamera(int facing) {
        return mCameraHelper.hasCamera(facing);
    }

    @Override
    public void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo) {
        mCameraHelper.getCameraInfo(cameraId, cameraInfo);
    }

    public boolean hasFrontCamera() {
        return hasCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public boolean hasBackCamera() {
        return hasCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public boolean canSwitch() {
        return hasFrontCamera() && hasBackCamera();
    }


    /**
     * 设置相机拍照的尺寸
     *
     * @param camera
     */
    public void setUpPicSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findBestResolution(camera, 1.0d, TYPE_PICTURE);
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置相机预览的尺寸
     *
     * @param camera
     */
    public void setUpPreviewSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findBestResolution(camera, 1.0d, TYPE_PREVIEW);
            parameters.setPreviewSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Log.i(TAG, "setUpPreviewSize:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpPreviewSizeMin(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        try {
            Camera.Size adapterSize = findMinResolution(camera, TYPE_PREVIEW);

            parameters.setPreviewSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Log.i(TAG, "setUpPreviewSizeMin:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param camera
     * @param bl
     */
    public void setFitPicSize(Camera camera, float bl) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findFitPicResolution(camera, bl);
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Log.e(TAG, "setFitPicSize:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置合适的预览尺寸
     *
     * @param camera
     */
    public void setFitPreSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findFitPreResolution(camera);
            parameters.setPreviewSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Log.e(TAG, "setFitPreSize:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回合适的照片尺寸参数
     *
     * @param camera
     * @param bl
     * @return
     */
    private Camera.Size findFitPicResolution(Camera camera, float bl) throws Exception {
        Camera.Parameters cameraParameters = camera.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes();

        Camera.Size resultSize = null;
        for (Camera.Size size : supportedPicResolutions) {
            if ((float) size.width / size.height == bl && size.width <= ALLOW_PIC_LEN && size.height <= ALLOW_PIC_LEN) {
                if (resultSize == null) {
                    resultSize = size;
                } else if (size.width > resultSize.width) {
                    resultSize = size;
                }
            }
        }
        if (resultSize == null) {
            return supportedPicResolutions.get(0);
        }
        return resultSize;
    }

    /**
     * 返回合适的预览尺寸参数
     *
     * @param camera
     * @return
     */
    private Camera.Size findFitPreResolution(Camera camera) throws Exception {
        Camera.Parameters cameraParameters = camera.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPreviewSizes();

        Camera.Size resultSize = null;
        for (Camera.Size size : supportedPicResolutions) {
            if (size.width <= ALLOW_PIC_LEN) {
                if (resultSize == null) {
                    resultSize = size;
                } else if (size.width > resultSize.width) {
                    resultSize = size;
                }
            }
        }
        if (resultSize == null) {
            return supportedPicResolutions.get(0);
        }
        return resultSize;
    }

    /**
     * 返回最小的预览尺寸
     *
     * @param cameraInst
     * @param type
     * @return
     */
    private Camera.Size findMinResolution(Camera cameraInst, int type) throws Exception {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = type == TYPE_PREVIEW ? cameraParameters.getSupportedPreviewSizes() : cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        if (supportedPicResolutions == null) {
            return null;
        }

        Camera.Size resultSize = supportedPicResolutions.get(0);
        for (Camera.Size size : supportedPicResolutions) {
            if (size.width < resultSize.width) {
                resultSize = size;
            }
        }
        return resultSize;
    }

    /**
     * 找到合适的尺寸
     *
     * @param cameraInst
     * @param maxDistortion 最大允许的宽高比
     * @return
     * @type 尺寸类型 0：preview  1：picture
     */
    public Camera.Size findBestResolution(Camera cameraInst, double maxDistortion, int type) throws Exception {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = type == TYPE_PREVIEW ? cameraParameters.getSupportedPreviewSizes() : cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x')
                    .append(supportedPicResolution.height).append(" ");
        }
        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aRatio = a.width / a.height;
                int bRatio = b.width / a.height;

                if (Math.abs(aRatio - 1) < Math.abs(bRatio - 1)) {
                    return -1;
                } else if (Math.abs(aRatio - 1) == Math.abs(bRatio - 1)) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        //返回最合适的
        return sortedSupportedPicResolutions.get(0);
    }

//    /**
//     * 打开相机界面
//     */
//    public void openCameraActivity(Context context) {
//        Intent intent = new Intent(context, CameraActivity.class);
//        context.startActivity(intent);
//    }


    //控制图像的正确显示方向
    public void setDispaly(Camera camera) {
        int degrees = 90;
        if (Build.VERSION.SDK_INT >= 14) {
            camera.setDisplayOrientation(degrees);
        } else if (Build.VERSION.SDK_INT >= 8) {
            setDisplayOrientation(camera, degrees);
        }
    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }

    public void releaseActivityCamera() {
        if (mActivityCamera != null) {
            try {
                mActivityCamera.stopPreview();
                mActivityCamera.setPreviewCallback(null);
                mActivityCamera.setPreviewCallbackWithBuffer(null);
                mActivityCamera.release();
                mActivityCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseStartTakePhotoCamera() {
        if (mStartTakePhotoCamera != null) {
            try {
                mStartTakePhotoCamera.stopPreview();
                mStartTakePhotoCamera.setPreviewCallback(null);
                mStartTakePhotoCamera.setPreviewCallbackWithBuffer(null);
                mStartTakePhotoCamera.release();
                mStartTakePhotoCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseCamera(Camera camera) {
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.setPreviewCallbackWithBuffer(null);
                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 闪光灯状态
     */
    public enum FlashLigthStatus {
        /**
         * LIGHT_AUTO,
         **/LIGHT_ON, LIGHT_OFF;

        //不断循环的枚举
        public FlashLigthStatus next() {
            int index = ordinal();
            int len = FlashLigthStatus.values().length;
            FlashLigthStatus status = FlashLigthStatus.values()[(index + 1) % len];
            if (!mFlashLightNotSupport.contains(status.name())) {
                return status;
            } else {
                return next();
            }
        }

        public static FlashLigthStatus valueOf(int index) {
            return FlashLigthStatus.values()[index];
        }
    }

    /**
     * 前置还是后置摄像头
     */
    public enum CameraDirection {
        CAMERA_BACK, CAMERA_FRONT;

        //不断循环的枚举
        public CameraDirection next() {
            int index = ordinal();
            int len = CameraDirection.values().length;
            return CameraDirection.values()[(index + 1) % len];
        }

        public static CameraDirection valueOf(int index) {
            return CameraDirection.values()[index];
        }
    }
}