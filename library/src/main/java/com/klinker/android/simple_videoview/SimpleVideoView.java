/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package com.klinker.android.simple_videoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * VideoView implementation that simplifies things, fixes aspect ratio, and allows 
 * you to specify whether or not you want to overtake the system audio.
 */
public class SimpleVideoView extends RelativeLayout {

    private MediaPlayer mediaPlayer;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private LinearLayout progressBar;

    private VideoPlaybackErrorTracker errorTracker;

    private boolean loop = false;
    private boolean stopSystemAudio = false;
    private boolean muted = false;

    private Uri videoUri = null;

    /**
     * Default constructor
     * @param context context for the activity
     */
    public SimpleVideoView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor for XML layout
     * @param context activity context
     * @param attrs xml attributes
     */
    public SimpleVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleVideoView, 0, 0);
        loop = a.getBoolean(R.styleable.SimpleVideoView_loop, false);
        stopSystemAudio = a.getBoolean(R.styleable.SimpleVideoView_stopSystemAudio, false);
        muted = a.getBoolean(R.styleable.SimpleVideoView_muted, false);
        a.recycle();

        init();
    }

    /**
     * Initialize the layout for the SimpleVideoView.
     */
    private void init() {
        // add a progress spinner
        progressBar = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.progress_bar, this, false);
        addView(progressBar);

        setGravity(Gravity.CENTER);
    }

    /**
     * Add the SurfaceView to the layout.
     */
    private void addSurfaceView() {

        if (getChildCount() == 0) {
            // ensure that we have the progress spinner added.
            // Can happen if you are recycling views on a list
            addView(progressBar);
        }

        // initialize the media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mediaPlayer) {
                scalePlayer();

                if (stopSystemAudio) {
                    AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                    am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                }

                if (muted) {
                    mediaPlayer.setVolume(0, 0);
                }

                progressBar.setVisibility(View.GONE);

                mediaPlayer.setDisplay(surfaceHolder);
                mediaPlayer.setLooping(loop);
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            /**
             * Called to indicate an error.
             *
             * @param mp      the MediaPlayer the error pertains to
             * @param what    the type of error that has occurred:
             * <ul>
             * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_UNKNOWN}
             * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_SERVER_DIED}
             * </ul>
             * @param extra an extra code, specific to the error. Typically
             * implementation dependent.
             * <ul>
             * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_IO}
             * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_MALFORMED}
             * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_UNSUPPORTED}
             * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_TIMED_OUT}
             * <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
             * </ul>
             */
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (errorTracker != null) {
                    errorTracker.onPlaybackError(
                            new RuntimeException("Error playing video! what code: " + what + ", extra code: " + extra)
                    );
                }
                return true;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Some devices (Samsung) don't respond to the MediaPlayer#setLooping value
                // for whatever reason. So this manually restarts it.
                if (loop) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            }
        });

        final RelativeLayout.LayoutParams surfaceViewParams =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView = new SurfaceView(getContext());
        surfaceView.setLayoutParams(surfaceViewParams);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) { }
            @Override public void surfaceDestroyed(SurfaceHolder surfaceHolder) { }
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // this needs to be run on a background thread.
                            // set data source can take upwards of 1-2 seconds
                            mediaPlayer.setDataSource(getContext(), videoUri);
                            mediaPlayer.prepare();
                        } catch (Exception e) {
                            if (errorTracker != null) {
                                errorTracker.onPlaybackError(e);
                            }
                        }
                    }
                }).start();

            }
        });

        addView(surfaceView, 0);

    }

    /**
     * Adjust the size of the player so it fits on the screen.
     */
    private void scalePlayer() {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;

        float screenProportion = (float) getWidth() / (float) getHeight();
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = getWidth();
            lp.height = (int) ((float) getWidth() / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) getHeight());
            lp.height = getHeight();
        }

        surfaceView.setLayoutParams(lp);
    }

    /**
     * Load the video into the player and initialize the layouts
     *
     * @param videoUrl String url to the video
     */
    public void start(String videoUrl) {
        start(Uri.parse(videoUrl));
    }

    /**
     * Load the video into the player and initialize the layouts.
     *
     * @param videoUri uri to the video.
     */
    public void start(Uri videoUri) {
        this.videoUri = videoUri;

        // You HAVE TO RELEASE the old video or you will have terrible performance issues.
        if (mediaPlayer != null) {
            throw new RuntimeException("You need to release the old video first!");
        }

        // we will not load the surface view or anything else until we are given a video.
        // That way, if, say, you wanted to add the simple video view on a list or something,
        // it won't be as intensive. ( == Better performance.)
        addSurfaceView();
    }

    /**
     * Start video playback. Called automatically with the SimpleVideoPlayer#start method
     */
    public void play() {
        if (!mediaPlayer.isPlaying())
            mediaPlayer.start();
    }

    /**
     * Pause video playback
     */
    public void pause() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    /**
     * Release the video to stop playback immediately.
     *
     * Should be called when you are leaving the playback activity
     */
    public void release() {
        removeAllViews();

        try {
            mediaPlayer.release();
        } catch (Exception e) { }

        mediaPlayer = null;
    }

    /**
     * Whether you want the video to loop or not
     *
     * @param shouldLoop
     */
    public void setShouldLoop(boolean shouldLoop) {
        this.loop = shouldLoop;
    }

    /**
     * Whether you want the app to stop the currently playing audio when you start the video
     *
     * @param stopSystemAudio
     */
    public void setStopSystemAudio(boolean stopSystemAudio) {
        this.stopSystemAudio = stopSystemAudio;
    }

    /**
     * Get whether or not the video is playing
     *
     * @return true if the video is playing, false otherwise
     */
    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Will return a result if there is an error playing the video
     *
     * @param tracker
     */
    public void setErrorTracker(VideoPlaybackErrorTracker tracker) {
        this.errorTracker = tracker;
    }

    public interface VideoPlaybackErrorTracker {
        void onPlaybackError(Exception e);
    }
}
