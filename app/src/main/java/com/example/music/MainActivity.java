package com.example.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static int UPDATE = 0x101;
    ImageView nextIv,playIv,lastIv,modeIv;
    TextView singerTv,songTv;
    RecyclerView musicRv;

    boolean isPlaying = false;  //播放状态
    private static SeekBar mSeekBar = null;  //进度条
    public int playPattern = 0;              //播放模式
    public final int PLAY_IN_ORDER = 0;  //顺序播放
    public final int PLAY_RANDOM = 1;    //随机播放
    public final int PLAY_SINGLE = 2;    //单曲循环
    private boolean isUserTouchProgressBar = false;     //判断手是否触摸进度条的状态

    //数据源
    List<LocalMusicBean> mDates;
    private LocalMusicAdapter adapter;

    //记录当前正在播放的音乐的位置
    int currentPlayPosition = -1;
    //记录暂停时进度条的位置
    int currentPausePositionInSong = 0;
    private static MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mediaPlayer = new MediaPlayer();
        mDates = new ArrayList<>();
        //创建适配器对象
        adapter = new LocalMusicAdapter(this, mDates);
        musicRv.setAdapter(adapter);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);
        //加载本地数据源
        loadLocalMusicData();
        //设置点击事件
        setEventListener();
    }

    private void setEventListener() {
        /*设置每一项的点击事件*/
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener(){
            @Override
            public void OnItemClick(View view, int position) {
                currentPlayPosition = position;
                LocalMusicBean musicBean = mDates.get(position);
                playMusicInMusicBean(musicBean);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void initView(){
        /*初始化空间布局*/
        nextIv = findViewById(R.id.local_music_bottom_iv_next);
        playIv = findViewById(R.id.local_music_bottom_iv_play);
        lastIv = findViewById(R.id.local_music_bottom_iv_last);
        modeIv = findViewById(R.id.play_mode);
        singerTv = findViewById(R.id.local_music_bottom_tv_singer);
        songTv = findViewById(R.id.local_music_bottom_tv_song);
        musicRv = findViewById(R.id.local_music_rv);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setEnabled(true);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserTouchProgressBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int touchProgress= seekBar.getProgress();
                if(mediaPlayer != null) {
                    int musicMax = mediaPlayer.getDuration();
                    int seekBarMax = seekBar.getMax();
                    mediaPlayer.seekTo(touchProgress*musicMax/seekBarMax);
                }
                isUserTouchProgressBar = false;
            }
        });

        Handler seekBarHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                int mMax = mediaPlayer.getDuration();
//                if(msg.what == UPDATE){
//                    try{
//                        mSeekBar.setProgress(msg.arg1);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }else{
//                    mSeekBar.setProgress(0);
//                }
                switch (msg.what){
                    case PLAY_IN_ORDER:
                        //循环播放
                        if(msg.arg1 == mSeekBar.getMax()-1){
                            mSeekBar.setProgress(0);
                            playNext();
                            break;
                        } else{
                            try{
                                mSeekBar.setProgress(msg.arg1);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        break;
                    case PLAY_RANDOM:
                        //随机播放
                        if(msg.arg1 == mSeekBar.getMax()-1){
                            mSeekBar.setProgress(0);
                            playRandom();
                            break;
                        }else{
                            try{
                                mSeekBar.setProgress(msg.arg1);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        break;
                    case PLAY_SINGLE:
                        //单曲循环
                        if(msg.arg1 == mSeekBar.getMax()-1){
                            mSeekBar.setProgress(0);
                            playAgain();
                            break;
                        } else{
                            try {
                                mSeekBar.setProgress(msg.arg1);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                }
                return false;
            }
        });
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                int position, mMax, sMax;   //当前播放秒数、最大秒数、进度条最大值
                while (!Thread.currentThread().isInterrupted()){
                    if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                        position = mediaPlayer.getCurrentPosition();
                        mMax = mediaPlayer.getDuration();
                        sMax = mSeekBar.getMax();
                        Message msg = seekBarHandler.obtainMessage();      //获取msg
                        msg.arg1 = position * sMax / mMax;
                        msg.arg2 = position;
                        msg.what = playPattern;
                        seekBarHandler.sendMessage(msg);
                        try{
                            Thread.sleep(1000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        Thread seekBarThread = new Thread(null, runnable1, "seekBarThread");
        seekBarThread.start();

        mSeekBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
        modeIv.setOnClickListener(this);
        this.playPattern = PLAY_IN_ORDER;
    }

    public void onClick(View v){
        switch(v.getId()) {
            case R.id.local_music_bottom_iv_last:
                if (currentPlayPosition == 0) {
                    Toast.makeText(this, "已经是第一首了，没有上一曲！", Toast.LENGTH_SHORT).show();
                    return;
                }
                playLast();
                break;
            case R.id.local_music_bottom_iv_next:
                if (currentPlayPosition == mDates.size() - 1) {
                    Toast.makeText(this, "已经是最后一首了，没有下一曲！", Toast.LENGTH_SHORT).show();
                    return;
                }
                playNext();
                break;
            case R.id.local_music_bottom_iv_play:
                if(currentPlayPosition == -1){
                    //没有选中歌曲
                    Toast.makeText(this, "请选择要播放的音乐", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mediaPlayer.isPlaying()){
                    //播放 转 暂停
                    pauseMusic();
                } else {
                    //暂停 转 播放
                    try {
                        playMusic(mediaPlayer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.play_mode:
                playPattern = (playPattern + 1) % 3;
                if(playPattern == 0){
                    modeIv.setBackgroundResource(R.drawable.xunhuanbofang);
                    playPattern = PLAY_IN_ORDER;
                } else if(playPattern == 1){
                    modeIv.setBackgroundResource(R.drawable.suijibofang);
                    playPattern = PLAY_RANDOM;
                } else if (playPattern == 2) {
                    modeIv.setBackgroundResource(R.drawable.danquxunhuan);
                    playPattern = PLAY_SINGLE;
                }
        }
    }

    private void playMusicInMusicBean(LocalMusicBean musicBean) {
        singerTv.setText(musicBean.getSinger());
        songTv.setText(musicBean.getSong());
        stopMusic();
        //重置音乐播放器
        mediaPlayer.reset();
        //设置新路径
        try {
            switch (musicBean.getId()){
                case "1":mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.a);break;
                case "2":mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.b);break;
                case "3":mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.c);break;
                case "4":mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.d);break;
                default:
                    break;
            }
            playMusic(mediaPlayer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLocalMusicData(){
        LocalMusicBean bean1 = new LocalMusicBean("1","入海","毛不易","");
        LocalMusicBean bean2 = new LocalMusicBean("2","奇迹再现","毛华锋","");
        LocalMusicBean bean3 = new LocalMusicBean("3", "这世界那么多人", "莫文蔚", "");
        LocalMusicBean bean4 = new LocalMusicBean("4","漠河舞厅","柳爽","");
        mDates.add(bean1);
        mDates.add(bean2);
        mDates.add(bean3);
        mDates.add(bean4);
    }

    private void playMusic(MediaPlayer mediaPlayer) throws IOException {
        /*播放音乐*/
        if(mediaPlayer!=null && !mediaPlayer.isPlaying()){
            if(currentPausePositionInSong == 0){
//                mediaPlayer.prepare();
//                mediaPlayer = MediaPlayer.create(this, a);
                mediaPlayer.start();
                isPlaying = true;
            } else {
                //从暂停继续播放
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }
            playIv.setImageResource(R.mipmap.icon_pause);
        }
    }

    public void playLast(){
        currentPlayPosition = currentPlayPosition - 1;
        LocalMusicBean lastBean = mDates.get(currentPlayPosition);
        playMusicInMusicBean(lastBean);
    }

    public void playAgain(){
        LocalMusicBean curBean = mDates.get(currentPlayPosition);
        playMusicInMusicBean(curBean);
    }

    public void playNext(){
        currentPlayPosition = (currentPlayPosition+1)%4;        //实现列表循环
        LocalMusicBean nextBean = mDates.get(currentPlayPosition);
        playMusicInMusicBean(nextBean);
    }

    public void playRandom(){
        Random random = new Random();
        currentPlayPosition = random.nextInt(mDates.size()-1);  //随机生成 0 ~ 列表长度-1 的整数
        LocalMusicBean randomBean = mDates.get(currentPlayPosition);
        playMusicInMusicBean(randomBean);
    }

    private void pauseMusic(){
        /*暂停*/
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            isPlaying = false;
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }

    private void stopMusic() {
        /*停止音乐播放*/
        if(mediaPlayer != null){
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }

}