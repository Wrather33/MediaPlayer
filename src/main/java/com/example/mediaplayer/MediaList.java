package com.example.mediaplayer;

import java.util.ArrayList;

public class MediaList{
    private ArrayList<MediaFile> mediaFiles;
    public MediaList(){
        mediaFiles = new ArrayList<>();
    }
    public boolean contains(MediaFile mediaFile){
        for (MediaFile med: getMediaFiles()
             ) {
            if (mediaFile.equals(med)){
                return true;
            }
        }
        return false;
    }
    public boolean add(MediaFile mediaFile) {
        if(!contains(mediaFile)) {
            return mediaFiles.add(mediaFile);
        }
        return false;
    }

    public boolean remove(MediaFile mediaFile) {
        try {
            return mediaFiles.remove(mediaFile);
        }
        catch (Exception e){
            return false;
        }
    }

    public ArrayList<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }
}
