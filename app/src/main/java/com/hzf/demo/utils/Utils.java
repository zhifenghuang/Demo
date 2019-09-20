package com.hzf.demo.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hzf.demo.BaseApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Utils {

    public static ContentValues mContentValues = null;

    public final static String GIF_EXTENSION = ".gif";
    public final static String IMAGE_EXTENSION = ".jpg";
    public final static String VIDEO_EXTENSION = ".mp4";

    public final static String VIDEO_CONTENT_URI = "content://media/external/video/media";


    public static String createVideoPath(Context context) {
        String fileName = UUID.randomUUID().toString() + VIDEO_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        return dirPath + "/" + fileName;
    }

    /**
     * @param context
     * @param permission
     * @return
     */
    public static boolean isGrantPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static String createImagePath(Context context) {
        String fileName = UUID.randomUUID().toString() + IMAGE_EXTENSION;
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + fileName;
        return filePath;
    }

    public static String getSaveFilePath(Context context, String fileName) {
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + fileName;
        return filePath;
    }

    /**
     * 保存JPG图片
     *
     * @param bmp
     */
    public static String saveJpegByFileName(Bitmap bmp, String fileName, Context context) {
        String folder = getSaveFilePath(context, fileName);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }

    public static String getMoneyValue(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(value);
    }

    /**
     * 保存JPG图片
     *
     * @param bmp
     */
    public static String saveJpeg(Bitmap bmp, Context context) {
        String folder = createImagePath(context);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return folder;
    }


    /**
     * 复制文件
     *
     * @param oldPath
     * @param newPath
     */
    public static boolean copyFile(String oldPath, String newPath) {
        boolean isSuccessful = false;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                isSuccessful = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isSuccessful = false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (fs != null) {
                    fs.close();
                    fs = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isSuccessful = false;
            }
        }
        return isSuccessful;
    }

    /**
     * @param path
     */
    public static void deleteFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }


    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    @SuppressLint("SimpleDateFormat")
    private static String getTimeStrOnlyHour(long time) {
        SimpleDateFormat mSdf = new SimpleDateFormat("HH:mm");
        Date dt = new Date(time);
        return mSdf.format(dt);
    }

    public static String getDateString(int time, String keyString) {
        return String.format(keyString, time);
    }


    public static String getNewText(int number) {
        return number < 10 ? ("0" + number) : String.valueOf(number);
    }

    /**
     * @param defaultId
     * @param path
     * @param iv
     * @Param File file
     */
    public static void loadImage(File file, int defaultId, String path, ImageView iv) {
        if (file != null && file.exists()) {
            loadImage(defaultId, Uri.fromFile(file), iv);
        } else {
            loadImage(defaultId, path, iv);
        }
    }


    /**
     * @param defaultId
     * @param path
     * @param iv
     * @Param File file
     */
    public static void loadImage(File file, int defaultId, String path, ImageView iv, String fileName) {
        if (file != null && file.exists()) {
            Glide.with(BaseApplication.getAppContext())
                    .load(Uri.fromFile(file))
                    .apply(new RequestOptions()
                            .placeholder(defaultId)
                            .error(defaultId)
                            .centerCrop()//中心切圖, 會填滿
                            .fitCenter()//中心fit, 以原本圖片的長寬為主
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .dontAnimate()
                    )
                    .into(iv);
        } else {
            Glide.with(BaseApplication.getAppContext())
                    .load(path)
                    .apply(new RequestOptions()
                            .placeholder(defaultId)
                            .error(defaultId)
                            .centerCrop()//中心切圖, 會填滿
                            .fitCenter()//中心fit, 以原本圖片的長寬為主
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .dontAnimate()
                    )
                    .into(iv);
        }
    }

    /**
     * @param defaultId
     * @param path
     * @param iv
     */
    public static void loadImage(int defaultId, String path, ImageView iv) {
        Glide.with(BaseApplication.getAppContext())
                .load(path)
                .apply(new RequestOptions()
                        .placeholder(defaultId)
                        .error(defaultId)
                        .centerCrop()//中心切圖, 會填滿
                        .fitCenter()//中心fit, 以原本圖片的長寬為主
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .dontAnimate()
                )
                .into(iv);

    }

    /**
     * @param defaultId
     * @param path
     * @param iv
     */
    public static void loadImage(int defaultId, int path, ImageView iv) {
        Glide.with(BaseApplication.getAppContext())
                .load(path)
                .apply(new RequestOptions()
                        .override(iv.getMeasuredWidth(), iv.getMeasuredHeight())
                        .placeholder(defaultId)
                        .error(defaultId)
                        .centerCrop()//中心切圖, 會填滿
                        .fitCenter()//中心fit, 以原本圖片的長寬為主
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .dontAnimate()
                )
                .into(iv);

    }

    /**
     * @param defaultId
     * @param uri
     * @param iv
     */
    public static void loadImage(int defaultId, Uri uri, ImageView iv) {
        Glide.with(BaseApplication.getAppContext())
                .load(uri)
                .apply(new RequestOptions()
                        .override(iv.getMeasuredWidth(), iv.getMeasuredHeight())
                        .placeholder(defaultId)
                        .error(defaultId)
                        .centerCrop()//中心切圖, 會填滿
                        .fitCenter()//中心fit, 以原本圖片的長寬為主
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .dontAnimate()
                )
                .into(iv);
    }

    /**
     * 从Assets中读取图片
     *
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Resources resources, String fileName) {
        Bitmap image = null;
        InputStream is = null;
        AssetManager am = resources.getAssets();
        try {
            is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return image;
    }


    public static Bitmap rotateBmp(Bitmap bmp, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
        bmp.recycle();
        bmp = null;
        return newBmp;
    }

    /**
     * 判断两个时间是不是同一天时间
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isSameDayTime(long time1, long time2) {
        if (Math.abs(time1 - time2) > 24 * 3600 * 1000) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time1);
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(time2);
        int day2 = calendar.get(Calendar.DAY_OF_MONTH);
        return day1 == day2;
    }

    public static void setSubText(TextView tv, String text, String subText, int textColor, int subTextColor) {
        int index = text.indexOf(subText);
        if (index >= 0) {
            SpannableString ss = new SpannableString(text);
            ss.setSpan(new ForegroundColorSpan(subTextColor), index, index + subText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(ss);
            tv.setTextColor(textColor);
        } else {
            tv.setText(text);
            tv.setTextColor(textColor);
        }
    }

    public final static String getMessageDigest(byte[] buffer) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断设备上是否安装微信
     *
     * @param context
     * @return
     */
    public static boolean isWechatAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String createCachePath(Context context) {
        String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/download";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        return dirPath;
    }

    public static String createAlbumVideoPath() {
        String title = UUID.randomUUID().toString();
        String fileName = title + Utils.VIDEO_EXTENSION;
        String albumPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera";
        File file = new File(albumPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String path = albumPath + "/" + fileName;
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Video.Media.TITLE, title);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, path);
        mContentValues = values;
        return path;
    }


    /**
     * 将录好的视频在系统内注册
     */
    public static void registerVideo(Context context, String path) {
        if (mContentValues != null) {
            Uri videoTable = Uri.parse(Utils.VIDEO_CONTENT_URI);
            mContentValues.put(MediaStore.Video.Media.SIZE, new File(path).length());
            try {
                context.getContentResolver().insert(videoTable, mContentValues);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
            }
            mContentValues = null;
        }
    }

    /**
     * 判断设备上是否安装该应用
     *
     * @param context
     * @return
     */
    public static boolean isAppAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    public static String getStrValue(double value) {
        value += 0.001;
        DecimalFormat decimalFormat = new DecimalFormat("0.0");//构造方法的字符格式这里如果小数不足1位,会以0补足.
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat.format(value);
    }

    public static String getStrValue2(double value) {
        value += 0.001;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return decimalFormat.format(value);
    }

    public static long dateStrToLong(String DateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date time = sdf.parse(DateTime);
            return time.getTime();
        } catch (Exception e) {

        }
        return 0;
    }

    public static String longToDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static long dateStrToLong2(String DateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date time = sdf.parse(DateTime);
            return time.getTime();
        } catch (Exception e) {

        }
        return 0;
    }

    public static boolean isSameDay(long time1, long time2) {
        if (time2 - time1 > 24 * 3600 * 1000) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time1);
        int year1 = calendar.get(Calendar.YEAR);
        int month1 = calendar.get(Calendar.MONTH) + 1;
        int day1 = calendar.get(Calendar.DAY_OF_MONTH);
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(time2);
        int year2 = calendar.get(Calendar.YEAR);
        int month2 = calendar.get(Calendar.MONTH) + 1;
        int day2 = calendar.get(Calendar.DAY_OF_MONTH);
        return year1 == year2 && month1 == month2 && day1 == day2;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeStrOnlyHourBySystem(Context context, boolean is24Format) {
        SimpleDateFormat mSdf;
        if (is24Format) {
            mSdf = new SimpleDateFormat("HH:mm");
        } else {
            mSdf = new SimpleDateFormat("hh:mm");
        }
        Date dt = new Date(System.currentTimeMillis());
        return mSdf.format(dt);
    }

    public static String geAMOrPM() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return hour >= 12 ? "PM" : "AM";
    }

    public static String getWeek() {
        Calendar c = Calendar.getInstance();
        int w = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return Constants.WEEKDAYS[w];
    }

    public static String getMonth() {
        Calendar c = Calendar.getInstance();
        String month = Constants.MONTHS[c.get(Calendar.MONTH)];
        int day = c.get(Calendar.DAY_OF_MONTH);
        return " " + month + " " + (day < 10 ? "0" + day : day);
    }

    /**
     * 如果不是全屏需要获取View位置时，y值减去状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取屏幕宽度的像素
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度度的像素
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * @param st
     * @return
     */
    public static boolean isTextHadChar(String st) {
        try {
            for (int i = 0; i < st.length(); i++) {
                char c = st.charAt(i);
                if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c) && !Character.isWhitespace(c)) {
                    int code = (int) c;
                    if (code >= 0 && code <= 255) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static String createAlbumtImagePath(Context context) {
        String title = UUID.randomUUID().toString();
        String filename = title + Utils.IMAGE_EXTENSION;

        String dirPath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera";
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            file.mkdirs();
        String filePath = dirPath + "/" + filename;
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.ImageColumns.TITLE, title);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.ImageColumns.DATA, filePath);
        mContentValues = values;
        return filePath;
    }

    /**
     * 保存到相册
     *
     * @param bmp
     */
    public static String savePhotoToAlbum(Bitmap bmp, Context context) {
        String folder = createAlbumtImagePath(context);
        FileOutputStream fout = null;
        BufferedOutputStream bos = null;
        try {
            fout = new FileOutputStream(folder);
            bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, bos);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mContentValues);
        mContentValues = null;
        return folder;
    }

    /**
     * 根据图片名字来获取资源Id
     *
     * @param picName
     * @return
     */
    public static int getDrawableIdByName(String picName) {
        int resID = BaseApplication.getAppContext().getResources().getIdentifier(picName,
                "drawable", BaseApplication.getAppContext().getApplicationInfo().packageName);
        return resID;
    }

    /**
     * 将时间转化为时分秒
     *
     * @param time
     * @return
     */
    public static String getHMSTime(long time) {
        if (time < 0) {
            time = 0;
        }
        int second = (int) Math.floor(time / 1000);
        if (second < 60) {
            if (second == 0) {
                second = 1;
            }
            return String.format("00:%02d", second);
        }
        int min = (int) Math.floor(second / 60);
        if (min < 60) {
            return String.format("%02d:%02d", min, second % 60);
        }
        int hour = (int) Math.floor(min / 60);
        if (hour > 100) {
            return String.format("%d:%02d:%02d", hour, min % 60, second % 60);
        }
        return String.format("%02d:%02d:%02d", hour, min % 60, second % 60);
    }

}


