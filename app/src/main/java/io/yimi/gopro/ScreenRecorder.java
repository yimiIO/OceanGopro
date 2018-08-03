/*
 * Copyright (c) 2014 Eavn <http://www.Eavn.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yimi.gopro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import io.yimi.gopro.service.ScreenRecorderService;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * @author Eavn
 */
public class ScreenRecorder {
    private Context context;
    private static final String TAG = "ScreenRecorder";
    private static final boolean VERBOSE = false;
    private static final int INVALID_INDEX = -1;
    static final String VIDEO_AVC = MIMETYPE_VIDEO_AVC; // H.264 Advanced Video Coding
    static final String AUDIO_AAC = MIMETYPE_AUDIO_AAC; // H.264 Advanced Audio Coding
    private int mWidth;
    private int mHeight;
    private int mDpi;
    private String mDstPath;
    public boolean isPaused = false;
    private MediaFormat mVideoOutputFormat = null, mAudioOutputFormat = null;
    private int mVideoTrackIndex = INVALID_INDEX, mAudioTrackIndex = INVALID_INDEX;
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted = false;

    private AtomicBoolean mForceQuit = new AtomicBoolean(false);
    private AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private VirtualDisplay mVirtualDisplay;

    private HandlerThread mWorker;

    private Callback mCallback;
    private LinkedList<Integer> mPendingVideoEncoderBufferIndices = new LinkedList<>();
    private LinkedList<Integer> mPendingAudioEncoderBufferIndices = new LinkedList<>();
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderBufferInfos = new LinkedList<>();
    private LinkedList<MediaCodec.BufferInfo> mPendingVideoEncoderBufferInfos = new LinkedList<>();
    private VideoEncodeConfig videoEncodeConfig;
    private AudioEncodeConfig audioEncodeConfig;

    /**
     * @param dpi for {@link VirtualDisplay}
     */
    public ScreenRecorder(Context context ,VideoEncodeConfig video,
                          AudioEncodeConfig audio,
                          int dpi,
                          String dstPath) {
        this.context = context;
        videoEncodeConfig = video;
        audioEncodeConfig = audio;

    }




    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public String getSavedPath() {
        return mDstPath;
    }

    interface Callback {
        void onStop(Throwable error);

        void onStart();

        void onRecording(long presentationTimeUs);
    }



    public void startRecord(final int resultCode, final Intent data){
        final Intent intent = new Intent(context, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_START);
        intent.putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(ScreenRecorderService.EXTRA_VIDEO_CONFIG,videoEncodeConfig);
        intent.putExtra(ScreenRecorderService.EXTRA_AUDIO_CONFIG,audioEncodeConfig);

        intent.putExtras(data);
        context.startService(intent);
    }

    public void pauseRecord(){

            final Intent intent = new Intent(context, ScreenRecorderService.class);
            intent.setAction(ScreenRecorderService.ACTION_PAUSE);
            context.startService(intent);


    }
    public void resumeRecord(){
        final Intent intent = new Intent(context, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_RESUME);
        context.startService(intent);

    }

    public void stopRecord(){
        final Intent intent = new Intent(context, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_STOP);
        context.startService(intent);
    }

}
