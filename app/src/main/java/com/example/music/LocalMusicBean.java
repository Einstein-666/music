package com.example.music;

import android.net.Uri;

public class LocalMusicBean {

    private String Id; //歌曲id
    private String song; //歌曲名称
    private String singer; //歌手名称
    private String uri; //歌曲路径

    public LocalMusicBean() {
    }

    public LocalMusicBean(String id, String song, String singer, String uri) {
        this.Id = id;
        this.song = song;
        this.singer = singer;
        this.uri = uri;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getUri() { return uri; }

    public void setUri(String Uri) { this.uri = uri; }
}
