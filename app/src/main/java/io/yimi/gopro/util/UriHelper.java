package io.yimi.gopro.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import java.io.File;

public final class UriHelper {
    private static final String TAG = UriHelper.class.getSimpleName();

    public UriHelper() {
    }

    public static String getAbsolutePath(ContentResolver cr, Uri uri) {
        String path = null;

        try {
            String[] columns = new String[]{"_data"};
            Cursor cursor = cr.query(uri, columns, (String)null, (String[])null, (String)null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        path = cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception var9) {
            ;
        }

        return path;
    }

    public static String getPath(Context context, Uri uri) {
        Log.i(TAG, "getPath:uri=" + uri);
        if (BuildCheck.isKitKat() && DocumentsContract.isDocumentUri(context, uri)) {
            Log.i(TAG, "getPath:isDocumentUri,getAuthority=" + uri.getAuthority());
            String docId;
            String[] split;
            String type;
            if (isExternalStorageDocument(uri)) {
                docId = DocumentsContract.getDocumentId(uri);
                Log.i(TAG, "getPath:isDocumentUri,docId=" + docId);
                Log.i(TAG, "getPath:isDocumentUri,getTreeDocumentId=" + DocumentsContract.getTreeDocumentId(uri));
                split = docId.split(":");
                type = split[0];
                Log.i(TAG, "getPath:type=" + type);
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                String primary = Environment.getExternalStorageDirectory().getAbsolutePath();
                Log.i(TAG, "getPath:primary=" + primary);
                File[] dirs = context.getExternalFilesDirs((String)null);
                int n = dirs != null ? dirs.length : 0;
                StringBuilder sb = new StringBuilder();

                for(int i = 0; i < n; ++i) {
                    File dir = dirs[i];
                    Log.i(TAG, "getPath:" + i + ")dir=" + dir);
                    if (dir == null || !dir.getAbsolutePath().startsWith(primary)) {
                        String dir_path = dir.getAbsolutePath();
                        String[] dir_elements = dir_path.split("/");
                        int m = dir_elements != null ? dir_elements.length : 0;
                        if (m > 1 && "storage".equalsIgnoreCase(dir_elements[1])) {
                            boolean found = false;
                            sb.setLength(0);
                            sb.append('/').append(dir_elements[1]);

                            for(int j = 2; j < m; ++j) {
                                if ("Android".equalsIgnoreCase(dir_elements[j])) {
                                    found = true;
                                    break;
                                }

                                sb.append('/').append(dir_elements[j]);
                            }

                            if (found) {
                                File path = new File(new File(sb.toString()), split[1]);
                                Log.i(TAG, "getPath:path=" + path);
                                if (path.exists() && path.canWrite()) {
                                    return path.getAbsolutePath();
                                }
                            }
                        }
                    }
                }
            } else {
                if (isDownloadsDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    return getDataColumn(context, contentUri, (String)null, (String[])null);
                }

                if (isMediaDocument(uri)) {
                    docId = DocumentsContract.getDocumentId(uri);
                    split = docId.split(":");
                    type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, "_id=?", selectionArgs);
                }
            }
        } else {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }

                return getDataColumn(context, uri, (String)null, (String[])null);
            }

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{"_data"};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String)null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow("_data");
                String var8 = cursor.getString(column_index);
                return var8;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
