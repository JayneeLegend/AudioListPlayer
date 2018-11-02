package com.example.weiwang.audiodemo;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class AudioController {


    private Context context;
    private StringBuilder formatBuilder;
    private Formatter formatter;
    private long[] adGroupTimesMs;
    private boolean[] playedAdGroups;
    private DefaultControlDispatcher controlDispatcher;
    private Timeline.Window window;
    private Timeline.Period period;
    private SimpleExoPlayer simpleExoPlayer;
    List<MediaSource> sourceList = new ArrayList<>();
    private DefaultDataSourceFactory defaultDataSourceFactory;
    Handler handler;
    private AudioControlListener listener;
    private String curUri;
    private int position;

    public AudioController(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        init();
    }

    public AudioController(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        if (handler == null) {
            handler = new Handler();
        }
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        adGroupTimesMs = new long[0];
        playedAdGroups = new boolean[0];
        controlDispatcher = new com.google.android.exoplayer2.DefaultControlDispatcher();
        period = new Timeline.Period();
        window = new Timeline.Window();
        //获取player的一个实例，大多数情况可以直接使用 DefaultTrackSelector
        // DefaultTrackSelector 该类可以对当前播放的音视频进行操作，比如设置音轨，设置约束曲目选择，禁用渲染器
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        defaultDataSourceFactory = new DefaultDataSourceFactory(context, "audio/mpeg");
        //创建一个媒体连接源
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        concatenatingMediaSource.addMediaSources(sourceList);
        simpleExoPlayer.setPlayWhenReady(false);
        initListener();
    }

    private void initListener() {
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest,
                    int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups,
                    TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                if (isLoading) {
                    handler.post(loadStatusRunable);
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
    }

    public StringBuilder getFormatBuilder() {
        return formatBuilder;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void onStart(int position) {
        this.position = position;
        if (controlDispatcher != null && simpleExoPlayer != null) {
            if (simpleExoPlayer.getPlaybackState() == Player.STATE_IDLE) {

            } else if (simpleExoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                //重新播放
                controlDispatcher
                        .dispatchSeekTo(simpleExoPlayer, simpleExoPlayer.getCurrentWindowIndex(),
                                C.TIME_UNSET);
            }
            controlDispatcher.dispatchSetPlayWhenReady(simpleExoPlayer, true);
            if (listener != null) {
                listener.isPlay(position, true);
            }
        }
    }

    public void onPause() {
        if (controlDispatcher != null && simpleExoPlayer != null) {
            controlDispatcher.dispatchSetPlayWhenReady(simpleExoPlayer, false);
            if (listener != null) {
                listener.isPlay(position, false);
            }
        }
    }

    public void onPrepare(String uri) {
        if (uri.equals(curUri)) {
            return;
        }
        curUri = uri;
        final ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(
                defaultDataSourceFactory)
                //创建一个播放数据源
                .createMediaSource(Uri.parse(uri));
        if (simpleExoPlayer != null) {
            simpleExoPlayer.prepare(mediaSource);
        }
    }


    Runnable loadStatusRunable = new Runnable() {
        @Override
        public void run() {
            long durationUs = 0;
            int adGroupCount = 0;
            long currentWindowTimeBarOffsetMs = 0;
            Timeline currentTimeline = simpleExoPlayer.getCurrentTimeline();
            if (!currentTimeline.isEmpty()) {
                int currentWindowIndex = simpleExoPlayer.getCurrentWindowIndex();
                int firstWindowIndex = currentWindowIndex;
                int lastWindowIndex = currentWindowIndex;
                for (int i = firstWindowIndex; i <= lastWindowIndex; i++) {
                    if (i == currentWindowIndex) {
                        currentWindowTimeBarOffsetMs = C.usToMs(durationUs);
                    }
                    currentTimeline.getWindow(i, window);
                    if (window.durationUs == C.TIME_UNSET) {
//                       /**/ Assertions.checkState(!multiWindowTimeBar);
                        break;
                    }
                    for (int j = window.firstPeriodIndex; j <= window.lastPeriodIndex; j++) {
                        currentTimeline.getPeriod(j, period);
                        int periodAdGroupCount = period.getAdGroupCount();
                        for (int adGroupIndex = 0; adGroupIndex < periodAdGroupCount;
                                adGroupIndex++) {
                            long adGroupTimeInPeriodUs = period.getAdGroupTimeUs(adGroupIndex);
                            if (adGroupTimeInPeriodUs == C.TIME_END_OF_SOURCE) {
                                if (period.durationUs == C.TIME_UNSET) {
                                    // Don't show ad markers for postrolls in periods with
                                    // unknown duration.
                                    continue;
                                }
                                adGroupTimeInPeriodUs = period.durationUs;
                            }
                            long adGroupTimeInWindowUs = adGroupTimeInPeriodUs + period
                                    .getPositionInWindowUs();
                            if (adGroupTimeInWindowUs >= 0
                                    && adGroupTimeInWindowUs <= window.durationUs) {
                                if (adGroupCount == adGroupTimesMs.length) {
                                    int newLength = adGroupTimesMs.length == 0 ? 1
                                            : adGroupTimesMs.length * 2;
                                    adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, newLength);
                                    playedAdGroups = Arrays.copyOf(playedAdGroups, newLength);
                                }
                                adGroupTimesMs[adGroupCount] = C
                                        .usToMs(durationUs + adGroupTimeInWindowUs);
                                playedAdGroups[adGroupCount] = period
                                        .hasPlayedAdGroup(adGroupIndex);
                                adGroupCount++;
                            }
                        }
                    }
                    durationUs += window.durationUs;
                }
            }

            durationUs = C.usToMs(window.durationUs);
            long curtime = currentWindowTimeBarOffsetMs + simpleExoPlayer.getContentPosition();
            long bufferedPosition = currentWindowTimeBarOffsetMs + simpleExoPlayer
                    .getContentBufferedPosition();
            if (listener != null) {
                listener.setCurTimeString(position,
                        "" + Util.getStringForTime(formatBuilder, formatter, curtime));
                listener.setDurationTimeString(position,
                        "" + Util.getStringForTime(formatBuilder, formatter, durationUs));
                listener.setBufferedPositionTime(position, bufferedPosition);
                listener.setCurPositionTime(position, curtime);
                listener.setDurationTime(position, durationUs);
            }
            handler.removeCallbacks(loadStatusRunable);
            int playbackState = simpleExoPlayer == null ? Player.STATE_IDLE
                    : simpleExoPlayer.getPlaybackState();
            //播放器未开始播放后者播放器播放结束
            if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                long delayMs = 0;
                //当正在播放状态时
                if (simpleExoPlayer.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                    float playBackSpeed = simpleExoPlayer.getPlaybackParameters().speed;
                    if (playBackSpeed <= 0.1f) {
                        delayMs = 1000;
                    } else if (playBackSpeed <= 5f) {
                        //中间更新周期时间
                        long mediaTimeUpdatePeriodMs = 1000 / Math
                                .max(1, Math.round(1 / playBackSpeed));
                        //当前进度时间与中间更新周期之间的多出的不足一个中间更新周期时长的时间
                        long surplusTimeMs = curtime % mediaTimeUpdatePeriodMs;
                        //播放延迟时间
                        long mediaTimeDelayMs = mediaTimeUpdatePeriodMs - surplusTimeMs;
                        if (mediaTimeDelayMs < (mediaTimeUpdatePeriodMs / 5)) {
                            mediaTimeDelayMs += mediaTimeUpdatePeriodMs;
                        }
                        delayMs = playBackSpeed == 1 ? mediaTimeDelayMs
                                : (long) (mediaTimeDelayMs / playBackSpeed);
                        Log.e("AUDIO_CONTROL", "playBackSpeed<=5:" + delayMs);
                    } else {
                        delayMs = 200;
                    }
                } else {
                    //当暂停状态时
                    delayMs = 1000;
                }
                handler.postDelayed(this, delayMs);
            } else {
                if (listener != null) {
                    //播放完结
                    listener.isPlay(position, false);
                }
            }
        }
    };

    public void setOnAudioControlListener(AudioControlListener listener) {
        this.listener = listener;
    }

    public int getPosition() {
        return position;
    }

    public void seekToTimeBarPosition(long positionMs) {
        Timeline timeline = simpleExoPlayer.getCurrentTimeline();
        int windowIndex;
        if (!timeline.isEmpty()) {
            int windowCount = timeline.getWindowCount();
            windowIndex = 0;
            while (true) {
                long windowDurationMs = timeline.getWindow(windowIndex, window).getDurationMs();
                if (positionMs < windowDurationMs) {
                    break;
                } else if (windowIndex == windowCount - 1) {
                    // Seeking past the end of the last window should seek to the end of the
                    // timeline.
                    positionMs = windowDurationMs;
                    break;
                }
                positionMs -= windowDurationMs;
                windowIndex++;
            }
        } else {
            windowIndex = simpleExoPlayer.getCurrentWindowIndex();
        }
        boolean dispatched = controlDispatcher
                .dispatchSeekTo(simpleExoPlayer, windowIndex, positionMs);
        if (!dispatched) {
            handler.post(loadStatusRunable);
        }
    }

    public void release() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release();
        }
    }

    public interface AudioControlListener {

        void setCurPositionTime(int position, long curPositionTime);
        void setDurationTime(int position, long durationTime);
        void setBufferedPositionTime(int position, long bufferedPosition);
        void setCurTimeString(int position, String curTimeString);
        void isPlay(int position, boolean isPlay);
        void setDurationTimeString(int position, String durationTimeString);
    }
}
