package com.example.mediaplayer;
import javafx.scene.media.Media;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MediaFile {
    private Media media;
    private String name;
    private File file;
    private double size;
    private String fileType;
    private Duration duration;

    public MediaFile(File file, String name) throws IOException {
        this.name = name;
        this.file = file;
        this.size = (double) Files.size(file.toPath()) /1024/1024;
        this.fileType = Files.probeContentType(file.toPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash( name, file, size, fileType, duration);
    }

    public void setSize(long size) {
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean setMedia() {
            try {
                this.media = new Media(file.toURI().toString());
                return true;
            }
            catch (Exception e){
                return false;
            }
    }

    public Media getMedia() {
        return media;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return String.format("Name: %s Size: %.2f Mb Duration: %s Type: %s", getName(), getSize(), convertDurationMillis((int) media.getDuration().toMillis()),
                getFileType());
    }

    public String getFileType() {
        return fileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaFile mediaFile = (MediaFile) o;
        return size == mediaFile.size && Objects.equals(name, mediaFile.name) && Objects.equals(file, mediaFile.file) && Objects.equals(fileType, mediaFile.fileType) && Objects.equals(duration, mediaFile.duration);
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String convertDurationMillis(Integer getDurationInMillis){
        int getDurationMillis = getDurationInMillis;

        String convertHours = String.format("%02d", TimeUnit.MILLISECONDS.toHours(getDurationMillis));
        String convertMinutes = String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(getDurationMillis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(getDurationMillis))); //I needed to add this part.
        String convertSeconds = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(getDurationMillis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getDurationMillis)));


        String getDuration = convertHours + ":" + convertMinutes + ":" + convertSeconds;

        return getDuration;

    }
}
