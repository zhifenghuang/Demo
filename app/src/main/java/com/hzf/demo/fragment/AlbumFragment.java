package com.hzf.demo.fragment;

import android.Manifest;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hzf.demo.R;
import com.hzf.demo.activity.BaseActivity;
import com.hzf.demo.adapter.MediaFileAdapter;
import com.hzf.demo.adapter.ShowMediasAdapter;
import com.hzf.demo.utils.Constants;
import com.hzf.demo.utils.Utils;
import com.yeemos.yeemos.jni.ExtractDecodeEditEncodeMuxTest;
import com.yeemos.yeemos.jni.ShaderJNILib;
import com.yeemos.yeemos.jni.TextureRender;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 16-6-21.
 */
public class AlbumFragment extends BaseFragment implements View.OnClickListener {

    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;

    private MediaMetadataRetriever mMediaMetadataRetriever;

    public class MediaFileInfo {
        public String mediaLastFileAbsoluteName;
        public ArrayList<MediaInfo> mediaInfoList;
        public String mediaLastFileName;
    }

    private static final String CAMERA_PATH = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM;

    public class MediaInfo {
        public File mediaFile;
        public int mediaType;
        public String mediaTime;
        public int mediaRotateDegree;
        public long mediaAddTime;
    }

    private ArrayList<MediaFileInfo> mMediaFileInfos = new ArrayList<>();
    private Cursor mCursor;
    private ShowMediasAdapter mMediasAdapter;
    private MediaFileAdapter mMediaFileAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_album;
    }

    @Override
    protected void onViewCreated(View view) {
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.alphaView).setVisibility(View.GONE);
        view.findViewById(R.id.llAlbum).setOnClickListener(this);
        view.findViewById(R.id.alphaView).setOnClickListener(this);
        initMedia();
    }


    private void initMedia() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getAlbumMedias(MEDIA_TYPE_IMAGE);
                    getAlbumMedias(MEDIA_TYPE_VIDEO);
                    if (mMediaFileInfos != null && mMediaFileInfos.size() > 1) {
                        Collections.sort(mMediaFileInfos, new SortByFileName());
                    }
                    for (MediaFileInfo mediaFileInfo : mMediaFileInfos) {
                        if (mediaFileInfo.mediaInfoList != null && mediaFileInfo.mediaInfoList.size() > 1) {
                            Collections.sort(mediaFileInfo.mediaInfoList, new SortByMediaAddTime());
                        }
                    }
                } catch (Exception e) {
                    if (mCursor != null && !mCursor.isClosed()) {
                        mCursor.close();
                        mCursor = null;
                    }

                    if (mMediaMetadataRetriever != null) {
                        mMediaMetadataRetriever.release();
                        mMediaMetadataRetriever = null;
                    }
                    if (getActivity() != null) {
                        getActivity().finish();
                        return;
                    }
                }
                if (getView() != null && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                            int initOpenIndex = -1;
                            MediaFileInfo mediaFileInfo;
                            for (int i = 0; i < mMediaFileInfos.size(); ++i) {
                                mediaFileInfo = mMediaFileInfos.get(i);
                                if (mediaFileInfo.mediaLastFileAbsoluteName.contains(CAMERA_PATH + "/Camera") && mediaFileInfo.mediaLastFileName.equalsIgnoreCase("Camera")) {
                                    initOpenIndex = i;
                                    break;
                                }
                            }
                            if (initOpenIndex == -1) {
                                for (int i = 0; i < mMediaFileInfos.size(); ++i) {
                                    mediaFileInfo = mMediaFileInfos.get(i);
                                    if (mediaFileInfo.mediaLastFileAbsoluteName.contains(CAMERA_PATH)) {
                                        initOpenIndex = i;
                                        break;
                                    }
                                }
                            }
                            initOpenIndex = Math.max(initOpenIndex, 0);
                            initGridView(initOpenIndex);
                            initListView(initOpenIndex);
                        }
                    });
                }
            }
        }).start();
    }

    public void onResume() {
        super.onResume();
        if (!Utils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        }

    }

    private void getAlbumMedias(int mediaType) {  //mediaType==0表示image,mediaType==1表示video
        if (getView() == null || getActivity() == null) {
            return;
        }
        // 执行查询，返回一个cursor
        int filePathColumn;
        int bucketNameColumn;
        int fileContentType;
        int dateAdded;
        if (mediaType == MEDIA_TYPE_IMAGE) {
            mCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                            MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATE_ADDED}, null,
                    null, null);
            filePathColumn = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            bucketNameColumn = mCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            fileContentType = mCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
            dateAdded = mCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
        } else {
            mCursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                            MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_ADDED}, null,
                    null, null);
            filePathColumn = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            bucketNameColumn = mCursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            fileContentType = mCursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
            dateAdded = mCursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
        }
        if (mCursor == null) {
            return;
        }
        mCursor.moveToFirst();
        String mediaFilePath, bunketName=null, mediaLastPath;
        String fileType;
        long mediaAddTime;
        int index = 0;
        while (index < mCursor.getCount()) {
            if (mCursor.isLast()) {
                break;
            }
            fileType = mCursor.getString(fileContentType);
            mediaFilePath = mCursor.getString(filePathColumn);
            mediaLastPath = null;
            if (fileType != null) {
                if (fileType.equalsIgnoreCase("image/gif") ||
                        (mediaType == MEDIA_TYPE_VIDEO && (!fileType.equalsIgnoreCase("video/mp4") || mediaFilePath.endsWith(".3gp")))) {
                    mCursor.moveToNext();
                    continue;
                }
            } else {
                mCursor.moveToNext();
                continue;
            }
            try {
                bunketName = mCursor.getString(bucketNameColumn);
                mediaLastPath = mediaFilePath.split("/" + mCursor.getString(bucketNameColumn) + "/")[0] + ("/" + bunketName);
                mediaAddTime = mCursor.getLong(dateAdded);
            } catch (Exception e) {
                mediaAddTime = 0l;
            }
            if (TextUtils.isEmpty(mediaLastPath)) {
                continue;
            }
            addMediaFile(mediaLastPath, mediaFilePath, mediaType, mediaAddTime);
            try {
                mCursor.moveToNext();
            } catch (Exception e) {
                break;
            }
        }
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }

        if (mMediaMetadataRetriever != null) {
            mMediaMetadataRetriever.release();
            mMediaMetadataRetriever = null;
        }
    }

    class SortByFileName implements Comparator<MediaFileInfo> {
        public int compare(MediaFileInfo o1, MediaFileInfo o2) {
            String name1 = o1.mediaLastFileName.toLowerCase();
            String name2 = o2.mediaLastFileName.toLowerCase();
            int compare = name1.compareTo(name2);
            if (compare > 0) {
                return 1;
            } else if (compare == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    class SortByMediaAddTime implements Comparator<MediaInfo> {
        public int compare(MediaInfo o1, MediaInfo o2) {
            long mediaAddTime1 = o1.mediaAddTime;
            long mediaAddTime2 = o2.mediaAddTime;
            if (mediaAddTime1 > mediaAddTime2) {
                return -1;
            } else if (mediaAddTime1 == mediaAddTime2) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private void initListView(int openIndex) {
        final ListView listView = getView().findViewById(R.id.list);
        mMediaFileAdapter = new MediaFileAdapter(getActivity());
        listView.setAdapter(mMediaFileAdapter);
        mMediaFileAdapter.resetMediaFiles(mMediaFileInfos);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaFileInfo mediaFileInfo = mMediaFileInfos.get(position);
                ((TextView) getView().findViewById(R.id.tvAlbum)).setText(mediaFileInfo.mediaLastFileName);
                mMediasAdapter.resetMediaFileInfo(mediaFileInfo);
                listView.setVisibility(View.GONE);
                getView().findViewById(R.id.alphaView).setVisibility(View.GONE);

            }
        });
        ((TextView) getView().findViewById(R.id.tvAlbum)).setText(mMediaFileInfos.isEmpty() ? "" : mMediaFileInfos.get(openIndex).mediaLastFileName);
    }


    private void initGridView(int openIndex) {
        final GridView gridView = getView().findViewById(R.id.gridView);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mMediasAdapter = new ShowMediasAdapter(this);
        gridView.setAdapter(mMediasAdapter);
        mMediasAdapter.resetMediaFileInfo(mMediaFileInfos.isEmpty() ? null : mMediaFileInfos.get(openIndex));
        mMediasAdapter.setItemWidth(dm.widthPixels / 3 - Utils.dip2px(getActivity(), 3));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageView iv = view.findViewById(R.id.iv);
                final MediaInfo info = (MediaInfo) iv.getTag(R.id.media_info);
                if (info != null && iv.getDrawable() != null) {
                    DisplayMetrics dm = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

                    if (info.mediaType == MEDIA_TYPE_IMAGE) {
                        ShaderJNILib.destroySource();
                        String savePath = Utils.createImagePath(getActivity());
                        if (Utils.copyFile(info.mediaFile.getAbsolutePath(), savePath)) {
                            Bundle bundle = new Bundle();
                            bundle.putString(EditPostFragment.SOURCE_PATH, savePath);
                            bundle.putInt(EditPostFragment.FILTER_TYPE, Constants.PIC_SHADER_FILTER);
                            bundle.putBoolean(EditPostFragment.IS_FROM_ALBUM, true);
                            gotoPager(EditPostFragment.class, bundle);
                        }
                    } else {
                        Toast.makeText(getActivity(), "开始读取视频", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            public void run() {
                                int degree = info.mediaRotateDegree % 360;
                                int filterType;
                                if (degree > 45 && degree <= 135) {
                                    filterType = Constants.VIDEO_DEGREE_90;
                                } else if (degree > 135 && degree <= 225) {
                                    filterType = Constants.VIDEO_DEGREE_180;
                                } else if (degree > 225 && degree <= 315) {
                                    filterType = Constants.VIDEO_DEGREE_270;
                                } else {
                                    filterType = Constants.VIDEO_DEGREE_0;
                                }
                                ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                                test.setContext(getActivity());
                                final String savePath = Utils.createVideoPath(getActivity());
                                boolean isSaved = false;
                                try {
                                    test.init(info.mediaFile.getAbsolutePath(), savePath, filterType, TextureRender.USE_FOR_GET_NEW_VIDEO_FROM_ALBUM, null);
                                    test.testExtractDecodeEditEncodeMuxAudioVideo();
                                    isSaved = true;
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                                if (getActivity() == null) {
                                    return;
                                }
                                if (isSaved) {
                                    ShaderJNILib.destroySource();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoadingDialog();
                                            Bundle bundle = new Bundle();
                                            bundle.putString(EditPostFragment.SOURCE_PATH, savePath);
                                            bundle.putInt(EditPostFragment.FILTER_TYPE, Constants.VIDEO_DEGREE_0);
                                            bundle.putBoolean(EditPostFragment.IS_FROM_ALBUM, true);
                                            gotoPager(EditPostFragment.class, bundle);

                                        }
                                    });
                                } else {
                                    Toast.makeText(getActivity(), "读取视频失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).start();
                    }
                }
            }
        });
    }

    private synchronized void addMediaFile(String mediaLastPath, String mediaPath, int mediaType, long mediaAddTime) {
        for (MediaFileInfo imageFileInfo : mMediaFileInfos) {
            if (imageFileInfo == null) {
                continue;
            }
            if (imageFileInfo.mediaLastFileAbsoluteName.equals(mediaLastPath)) {
                File mediaFile = new File(mediaPath);
                if (mediaFile.exists()) {
                    MediaInfo mediaInfo = new MediaInfo();
                    mediaInfo.mediaFile = mediaFile;
                    mediaInfo.mediaType = mediaType;
                    mediaInfo.mediaAddTime = mediaAddTime;
                    if (mMediaMetadataRetriever == null) {
                        mMediaMetadataRetriever = new MediaMetadataRetriever();
                    }
                    try {
                        if (mediaType == MEDIA_TYPE_VIDEO) {
                            mMediaMetadataRetriever.setDataSource(mediaPath);
                            mediaInfo.mediaRotateDegree = Integer.parseInt(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                            mediaInfo.mediaTime = Utils.getHMSTime(Long.parseLong(mMediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)));
                        }
                    } catch (Exception e) {
                    }
                    imageFileInfo.mediaInfoList.add(mediaInfo);
                }
                return;
            }
        }
        File mediaFile = new File(mediaPath);
        if (mediaFile.exists()) {
            ArrayList<MediaInfo> list = new ArrayList<>();
            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.mediaFile = new File(mediaPath);
            mediaInfo.mediaType = mediaType;
            mediaInfo.mediaAddTime = mediaAddTime;
            if (mMediaMetadataRetriever == null) {
                mMediaMetadataRetriever = new MediaMetadataRetriever();
            }
            if (mediaType == MEDIA_TYPE_VIDEO) {
                try {
                    mMediaMetadataRetriever.setDataSource(mediaPath);
                    mediaInfo.mediaRotateDegree = Integer.parseInt(mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                    mediaInfo.mediaTime = Utils.getHMSTime(Long.parseLong(mMediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)));
                } catch (Exception e) {
                    return;
                }
            }
            list.add(mediaInfo);
            MediaFileInfo mediaFileInfo = new MediaFileInfo();
            mediaFileInfo.mediaLastFileAbsoluteName = mediaLastPath;
            mediaFileInfo.mediaInfoList = list;
            String[] fileNames = mediaLastPath.split("/");
            if (fileNames.length > 0) {
                mediaFileInfo.mediaLastFileName = fileNames[fileNames.length - 1];
                mMediaFileInfos.add(mediaFileInfo);
            }
        }
    }


    @Override
    public void updateUIText() {
        setText(R.id.tvChangeFile, "换文件夹");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llAlbum:
                if (!Utils.isGrantPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ((BaseActivity) getActivity()).requestPermission(BaseActivity.ASK_PERMISSION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return;
                }
                if (mMediaFileAdapter == null) {
                    return;
                }
                ListView listView = getView().findViewById(R.id.list);
                if (listView.getVisibility() == View.VISIBLE) {
                    listView.setVisibility(View.GONE);
                    getView().findViewById(R.id.alphaView).setVisibility(View.GONE);
                    break;
                }
                listView.setVisibility(View.VISIBLE);
                if (mMediaFileInfos.size() < 4) {
                    int height = Utils.dip2px(getActivity(), 36) * mMediaFileInfos.size();
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) listView.getLayoutParams();
                    lp.height = height;
                    listView.setLayoutParams(lp);
                }
                getView().findViewById(R.id.alphaView).setVisibility(View.VISIBLE);
                mMediaFileAdapter.notifyDataSetChanged();
                break;
            case R.id.alphaView:
                v.setVisibility(View.GONE);
                getView().findViewById(R.id.list).setVisibility(View.GONE);
                break;
            case R.id.btnBack:
                goBack();
                break;
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            mCursor = null;
        }
    }

}
