package io.yimi.gopro.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class FileUtils {


    private static final String TAG = FileUtils.class.getSimpleName();
    public static String DIR_NAME = "UsbWebCamera";
    private static final SimpleDateFormat mDateTimeFormat;
    public static float FREE_RATIO;
    public static float FREE_SIZE_OFFSET;
    public static float FREE_SIZE;
    public static float FREE_SIZE_MINUTE;
    public static long CHECK_INTERVAL;


    static {
        mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        FREE_RATIO = 0.03F;
        FREE_SIZE_OFFSET = 2.097152E7F;
        FREE_SIZE = 3.145728E8F;
        FREE_SIZE_MINUTE = 4.194304E7F;
        CHECK_INTERVAL = 45000L;
    }
    public FileUtils() {
    }

    public static final File getCaptureFile(Context context, String type, String ext, int save_tree_id) {
        return getCaptureFile(context, type, (String)null, ext, save_tree_id);
    }

    public static final File getCaptureFile(Context context, String type, String prefix, String ext, int save_tree_id) {
        File result = null;
        String file_name = (TextUtils.isEmpty(prefix) ? getDateTimeString() : prefix + getDateTimeString()) + ext;
        if (save_tree_id > 0 && SDUtils.hasStorageAccess(context, save_tree_id)) {
            result = SDUtils.createStorageDir(context, save_tree_id);
            if (result == null || !result.canWrite()) {
                result = null;
            }
        }

        if (result == null) {
            File dir = getCaptureDir(context, type, 0);
            if (dir != null) {
                dir.mkdirs();
                if (dir.canWrite()) {
                    result = dir;
                }
            }
        }

        if (result != null) {
            result = new File(result, file_name);
        }

        return result;
    }

    public static final String getDateTimeString() {
        GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }


    @SuppressLint({"NewApi"})
    public static final File getCaptureDir(Context context, String type, int save_tree_id) {
        File result = null;
        if (save_tree_id > 0 && SDUtils.hasStorageAccess(context, save_tree_id)) {
            result = SDUtils.createStorageDir(context, save_tree_id);
        }

        File dir = result != null ? result : new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        dir.mkdirs();
        return dir.canWrite() ? dir : null;
    }



}
