package com.example.weiwang.audiodemo;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.TimeBar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    boolean show;
    private TextView tvShow;
    private TextView tvShow2;
    TimeBar timeBar;
    private boolean scrubbing;
    List<MediaEntity> mediaEntityList = new ArrayList<>();
    List<MediaSource> sourceList = new ArrayList<>();
    AudioController control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaEntityList.add(new MediaEntity(
                "http://5.595818.com/2015/ring/000/140/6731c71dfb5c4c09a80901b65528168b.mp3",
                "00:00",
                "00:49", false, 49000));
        mediaEntityList.add(new MediaEntity(
                "http://file.kuyinyun.com/group1/M00/90/B7/rBBGdFPXJNeAM-nhABeMElAM6bY151.mp3",
                "00:00",
                "00:22", false, 22000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=557581476.mp3",
                        "00:00",
                        "04:05", false, 245000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=1318235595.mp3",
                        "00:00",
                        "04:01", false, 241000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=1293886117.mp3",
                        "00:00",
                        "04:39", false, 279000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=1318458131.mp3",
                        "00:00",
                        "03:28", false, 208000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=439139814.mp3",
                        "00:00",
                        "03:38", false, 218000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=863046037.mp3",
                        "00:00",
                        "03:34", false, 214000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=449818741.mp3",
                        "00:00",
                        "03:55", false, 235000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=1318284264.mp3",
                        "00:00",
                        "03:54", false, 234000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=1305186462.mp3",
                        "00:00",
                        "04:02", false, 242000));
        mediaEntityList
                .add(new MediaEntity("http://music.163.com/song/media/outer/url?id=1311974383.mp3",
                        "00:00",
                        "04:04", false, 244000));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        AudioController audioControl = new AudioController(this);
        MyBaseAdapter myBaseAdapter = new MyBaseAdapter(mediaEntityList, audioControl);
        myBaseAdapter.bindToRecyclerView(recyclerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        control.release();
    }

}
