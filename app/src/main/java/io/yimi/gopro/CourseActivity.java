package io.yimi.gopro;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;


public class CourseActivity extends AppCompatActivity {

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    File file;
    ScreenRecorder mRecorder;
    VideoEncodeConfig videoEncodeConfig;
    AudioEncodeConfig audioEncodeConfig;
    private Button startBtn;
    private Button pauseBtn;
    private ImageView imageView;
    private int position;
    private List<File> fileList = new ArrayList<>();
    private MediaProjectionManager mMediaProjectionManager;

    private static File getSavingDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "ScreenCaptures");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_course);

        Intent intent = getIntent();
        videoEncodeConfig = (VideoEncodeConfig) intent.getSerializableExtra("video");
        audioEncodeConfig = (AudioEncodeConfig) intent.getSerializableExtra("audio");


        startBtn = (Button) findViewById(R.id.start_record);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mRecorder != null) {
                    toast("保存成功：" + mRecorder.getSavedPath());
                    stopRecorder();
                } else if (hasPermissions()) {
                    startCaptureIntent();
                } else if (Build.VERSION.SDK_INT >= M) {
                    requestPermissions();
                } else {
                    toast("No permission to write sd card");
                }


            }
        });
        pauseBtn = (Button) findViewById(R.id.pause_record);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (null == mRecorder) {
                    toast("Record not start!");
                    return;
                }
                if (mRecorder.isPaused) {
                    mRecorder.isPaused = !mRecorder.isPaused;
                    resumeRecorder();
                } else {
                    mRecorder.isPaused = !mRecorder.isPaused;
                    pauseRecorder();
                }

            }
        });


        imageView = (ImageView) findViewById(R.id.image);
        File root = new File(Environment.getExternalStorageDirectory(), "test");
        File[] files = root.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists() && files[i].isFile()) {
                fileList.add(files[i]);
            }
        }
        if (fileList.size() == 0) {
            Toast.makeText(this, "没有找到文件", Toast.LENGTH_SHORT).show();
            return;
        }
        imageView.setImageURI(Uri.fromFile(fileList.get(position)));

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (position < 0 || position >= fileList.size()) {
                    return false;
                }
                if (event.getX() < v.getWidth() / 2) {
                    onClickLeft();
                } else {
                    onClickRight();
                }
                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            File dir = getSavingDir();
            if (!dir.exists() && !dir.mkdirs()) {
                cancelRecorder();
                return;
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
            final File file = new File(dir, "Screen-" + format.format(new Date())
                    + "-" + videoEncodeConfig.width + "x" + videoEncodeConfig.height + ".mp4");
            Log.d("@@", "Create recorder with :" + videoEncodeConfig + " \n " + audioEncodeConfig + "\n " + file);
            mRecorder = newRecorder(videoEncodeConfig, audioEncodeConfig, file);
            if (hasPermissions()) {
                startRecorder(resultCode, data);
            } else {
                cancelRecorder();
            }
        }
    }

    private void startCaptureIntent() {


        final MediaProjectionManager manager
                = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        final Intent permissionIntent = manager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, REQUEST_MEDIA_PROJECTION);
    }

    private void startRecorder(final int resultCode, final Intent data) {
        if (mRecorder == null) {
            return;
        }
        mRecorder.startRecord(resultCode, data);
        startBtn.setText("停止录制");
    }

    private void pauseRecorder() {
        mRecorder.pauseRecord();
        pauseBtn.setText("继续录制");
        toast("暂停录制");
    }

    private void resumeRecorder() {
        mRecorder.resumeRecord();
        pauseBtn.setText("暂停录制");
        toast("继续录制");
    }

    private void cancelRecorder() {
        if (mRecorder == null) {
            return;
        }
        Toast.makeText(this, "Permission denied! Screen recorder is cancel", Toast.LENGTH_SHORT).show();
        stopRecorder();
    }

    private void stopRecorder() {
        Log.e("OcceanGopro","stopRecoder");


        if (mRecorder != null) {
            mRecorder.stopRecord();
        }
        mRecorder = null;
        startBtn.setText("开始录制");
    }

    private ScreenRecorder newRecorder(VideoEncodeConfig video,
                                       AudioEncodeConfig audio, File output) {
        ScreenRecorder r = new ScreenRecorder(this, video, audio,
                1, output.getAbsolutePath());
        r.setCallback(new ScreenRecorder.Callback() {
            long startTime = 0;

            @Override
            public void onStop(Throwable error) {
                runOnUiThread(() -> stopRecorder());
                if (error != null) {
                    toast("Recorder error ! See logcat for more details");
                    error.printStackTrace();
                    output.delete();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setData(Uri.fromFile(output));
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onRecording(long presentationTimeUs) {
                if (startTime <= 0) {
                    startTime = presentationTimeUs;
                }
                long time = (presentationTimeUs - startTime) / 1000;
            }
        });
        return r;
    }


    private boolean hasPermissions() {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        int granted = pm.checkPermission(RECORD_AUDIO, packageName)
                | pm.checkPermission(WRITE_EXTERNAL_STORAGE, packageName);
        return granted == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(M)
    private void requestPermissions() {
        String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};
        boolean showRationale = false;
        for (String perm : permissions) {
            showRationale |= shouldShowRequestPermissionRationale(perm);
        }
        if (!showRationale) {
            requestPermissions(permissions, REQUEST_PERMISSIONS);
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("Using your mic to record audio and your sd card to save video file")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        requestPermissions(permissions, REQUEST_PERMISSIONS))
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }


    private void toast(String message, Object... args) {
        Toast toast = Toast.makeText(this,
                (args.length == 0) ? message : String.format(Locale.US, message, args),
                Toast.LENGTH_SHORT);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(toast::show);
        } else {
            toast.show();
        }
    }

    private void onClickLeft() {
        position--;
        if (position >= fileList.size() || position < 0) {
            return;
        }
        imageView.setImageURI(Uri.fromFile(fileList.get(position)));
        if (position == fileList.size()) {
            position = 0;
        }
    }

    private void onClickRight() {
        position++;
        if (position >= fileList.size()) {
            return;
        }
        imageView.setImageURI(Uri.fromFile(fileList.get(position)));
        if (position == fileList.size()) {
            position = 0;
        }
    }

    public File getSaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File rootDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "test");
            if (!rootDir.exists()) {
                if (!rootDir.mkdirs()) {
                    return null;
                }
            }
            return rootDir;
        } else {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("OcceanGopro","onDestory");
        super.onDestroy();
        stopRecorder();

    }


}
