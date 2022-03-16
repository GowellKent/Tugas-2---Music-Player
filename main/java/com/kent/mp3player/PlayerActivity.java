package com.kent.mp3player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class PlayerActivity extends AppCompatActivity {

    Button btnPlay, btnNext, btnPrev;
    TextView txtLagu, txtAwal, txtAkhir, txtJudul;
    SeekBar seekBar;
    String judulLagu;
    public static final String EXTRA_NAME = "Judul_lagu";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updtSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnNext = findViewById(R.id.btnNext);
        btnPlay = findViewById(R.id.btnPlay);
        btnPrev = findViewById(R.id.btnPrev);
        txtLagu = findViewById(R.id.txtLagu);
        txtAwal = findViewById(R.id.txtAwal);
        txtAkhir = findViewById(R.id.txtAkhir);
        seekBar = findViewById(R.id.seekBar);
        txtJudul = findViewById(R.id.txtJudul);

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String judul = i.getStringExtra("judul");
        position = bundle.getInt("pos", 0);

        txtLagu.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        judulLagu = mySongs.get(position).getName();
        txtLagu.setText(judulLagu);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        //untuk update seek bar sesuai progress pada lagu yg dimainkan
        updtSeekBar = new Thread(){
            @Override
            public void run() {
                int totDuration = mediaPlayer.getDuration();
                int currPos = 0;

                while (currPos < totDuration){
                    try{
                        sleep(500);
                        currPos = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currPos);
                    }
                    catch(InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updtSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);

        //mengubah progress lagu dengan seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = waktu(mediaPlayer.getDuration());
        txtAkhir.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        //mengupdate waktu yang sedang berjalan
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currTime = waktu(mediaPlayer.getCurrentPosition());
                txtAwal.setText(currTime);
                handler.postDelayed(this, 1000);
            }
        }, delay);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else{
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        //lanjut ke lagu berikutnya
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                judulLagu = mySongs.get(position).getName();
                txtLagu.setText(judulLagu);
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_pause);

                seekBar.setProgress(0);
                seekBar.setMax(mediaPlayer.getDuration());

                String endTime = waktu(mediaPlayer.getDuration());
                txtAkhir.setText(endTime);

            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                judulLagu = mySongs.get(position).getName();
                txtLagu.setText(judulLagu);
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.ic_pause);

                seekBar.setProgress(0);
                seekBar.setMax(mediaPlayer.getDuration());

                String endTime = waktu(mediaPlayer.getDuration());
                txtAkhir.setText(endTime);

            }
        });
    }

    //konversi durasi pada lagu
    public String waktu(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min +":";

        if(sec < 10){
            time += "0";
        }

        time += sec;

        return time;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            mediaPlayer.stop();
        }
    }
}