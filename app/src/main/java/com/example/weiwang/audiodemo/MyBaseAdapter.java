package com.example.weiwang.audiodemo;

import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Util;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyBaseAdapter extends BaseQuickAdapter<MediaEntity, BaseViewHolder>
        implements AudioController.AudioControlListener, BaseQuickAdapter.OnItemChildClickListener {


    private AudioController mAudioControl;


    public MyBaseAdapter(@Nullable List<MediaEntity> data, AudioController control) {
        super(R.layout.item_audio, data);
        mAudioControl = control;
        mAudioControl.setOnAudioControlListener(this);
        setOnItemChildClickListener(this);
    }

    //
    @Override
    protected void convert(BaseViewHolder helper, MediaEntity item) {
        TextView startTime = helper.getView(R.id.tv_start_time);
        TextView endTime = helper.getView(R.id.tv_end_time);
        TextView title = helper.getView(R.id.text);
        ImageView play = helper.getView(R.id.play);
        ImageView pause = helper.getView(R.id.pause);
        if (item.getPlayStatus()) {
            play.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.VISIBLE);
        } else {
            play.setVisibility(View.VISIBLE);
            pause.setVisibility(View.INVISIBLE);
        }
        helper.addOnClickListener(R.id.play);
        helper.addOnClickListener(R.id.pause);

        startTime.setText(item.getStartTime());
        endTime.setText(item.getEndTime());
        DefaultTimeBar timeBar = helper.getView(R.id.exo_progress);
        timeBar.setDuration(item.getDuration());
        timeBar.setPosition(0L);
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                if (startTime != null) {
                    if (mAudioControl.getPosition() == mData.indexOf(item)) {
                        startTime.setText(Util.getStringForTime(mAudioControl.getFormatBuilder(),
                                mAudioControl

                                        .getFormatter(), position));
                    } else {
                        timeBar.setPosition(0);
                    }
                }
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                if (mAudioControl != null) {
                    if (mAudioControl.getPosition() == mData.indexOf(item)) {
                        mAudioControl.seekToTimeBarPosition(position);
                    } else {
                        timeBar.setPosition(0);
                    }
                }
            }
        });

    }

    @Override
    public void setCurPositionTime(int position, long curPositionTime) {
        if (getViewByPosition(getRecyclerView(), position,
                R.id.exo_progress) != null) {
            DefaultTimeBar timeBar = (DefaultTimeBar) getViewByPosition(getRecyclerView(), position,
                    R.id.exo_progress);
            timeBar.setPosition(curPositionTime);
        }
    }

    @Override
    public void setDurationTime(int position, long durationTime) {
        if (getViewByPosition(getRecyclerView(), position, R.id.exo_progress) != null) {
            DefaultTimeBar timeBar = (DefaultTimeBar) getViewByPosition(getRecyclerView(), position,
                    R.id.exo_progress);
            timeBar.setDuration(durationTime);
        }
    }

    @Override
    public void setBufferedPositionTime(int position, long bufferedPosition) {
        if (getViewByPosition(getRecyclerView(), position,
                R.id.exo_progress) != null) {
            DefaultTimeBar timeBar = (DefaultTimeBar) getViewByPosition(getRecyclerView(), position,
                    R.id.exo_progress);
            timeBar.setBufferedPosition(bufferedPosition);
        }
    }

    @Override
    public void setCurTimeString(int position, String curTimeString) {
        if (getViewByPosition(getRecyclerView(), position, R.id.tv_start_time) != null) {
            TextView startTime = (TextView) getViewByPosition(getRecyclerView(), position,
                    R.id.tv_start_time);
            startTime.setText(curTimeString);
        }
        MediaEntity mediaEntity = mData.get(position);
        mediaEntity.setStartTime(curTimeString);
    }

    @Override
    public void isPlay(int position, boolean isPlay) {
        MediaEntity mediaEntity = mData.get(position);
        mediaEntity.setPlayStatus(isPlay);
        if (getViewByPosition(getRecyclerView(), position, R.id.play) != null
                && getViewByPosition(getRecyclerView(), position, R.id.pause) != null) {
            ImageView play = (ImageView) getViewByPosition(getRecyclerView(), position, R.id.play);
            ImageView pause = (ImageView) getViewByPosition(getRecyclerView(), position,
                    R.id.pause);
            if (isPlay) {
                if (play != null) {
                    play.setVisibility(View.INVISIBLE);
                }
                if (pause != null) {
                    pause.setVisibility(View.VISIBLE);
                }


            } else {
                if (play != null) {
                    play.setVisibility(View.VISIBLE);
                }
                if (pause != null) {
                    pause.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public void setDurationTimeString(int position, String durationTimeString) {
        if (getViewByPosition(getRecyclerView(), position,
                R.id.tv_end_time) != null) {
            TextView endTime = (TextView) getViewByPosition(getRecyclerView(), position,
                    R.id.tv_end_time);
            endTime.setText(durationTimeString);
        }
        MediaEntity mediaEntity = mData.get(position);
        mediaEntity.setEndTime(durationTimeString);
    }


    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        boolean playNClickIsSame = playNClickIsSame(mAudioControl.getPosition(), position);
        switch (view.getId()) {
            case R.id.play:
                initStatus(mAudioControl.getPosition(), position);
                mAudioControl.onPrepare(mData.get(position).getUri());
                mAudioControl.onStart(position);
                break;
            case R.id.pause:
                if (playNClickIsSame) {
                    mAudioControl.onPause();
                }

                break;
        }
    }

    private boolean playNClickIsSame(int playIndex, int clickIndex) {
        return playIndex == clickIndex ? true : false;
    }

    private void initStatus(int playIndex, int clickIndex) {
        MediaEntity oldEntity = mData.get(playIndex);
        oldEntity.setPlayStatus(false);
        oldEntity.setStartTime("00:00");
        if (playIndex >= ((LinearLayoutManager) getRecyclerView().getLayoutManager())
                .findFirstVisibleItemPosition()
                && playIndex <= ((LinearLayoutManager) getRecyclerView().getLayoutManager())
                .findLastVisibleItemPosition()) {
            if (getViewByPosition(getRecyclerView(), playIndex,
                    R.id.exo_progress) != null) {
                DefaultTimeBar timeBar = (DefaultTimeBar) getViewByPosition(getRecyclerView(),
                        playIndex,
                        R.id.exo_progress);
                timeBar.setPosition(0);
                timeBar.setBufferedPosition(0);
            }
            if (getViewByPosition(getRecyclerView(), playIndex,
                    R.id.tv_start_time) != null) {
                TextView startTime = (TextView) getViewByPosition(getRecyclerView(), playIndex,
                        R.id.tv_start_time);
                startTime.setText(oldEntity.getStartTime());
            }
            if (getViewByPosition(getRecyclerView(), playIndex, R.id.play) != null) {
                ImageView oldplay = (ImageView) getViewByPosition(getRecyclerView(), playIndex,
                        R.id.play);
                oldplay.setVisibility(View.VISIBLE);
            }
            if (getViewByPosition(getRecyclerView(), playIndex,
                    R.id.pause) != null) {
                ImageView oldpause = (ImageView) getViewByPosition(getRecyclerView(), playIndex,
                        R.id.pause);
                oldpause.setVisibility(View.INVISIBLE);
            }
            if (getViewByPosition(getRecyclerView(), clickIndex, R.id.play) != null) {
                ImageView newplay = (ImageView) getViewByPosition(getRecyclerView(), clickIndex,
                        R.id.play);
                newplay.setVisibility(View.INVISIBLE);
            }
            if (getViewByPosition(getRecyclerView(), clickIndex, R.id.pause) != null) {
                ImageView onewpause = (ImageView) getViewByPosition(getRecyclerView(), clickIndex,
                        R.id.pause);
                onewpause.setVisibility(View.VISIBLE);
            }
        } else {
            notifyItemChanged(playIndex);
        }
    }
}
