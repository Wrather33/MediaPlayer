package com.example.mediaplayer;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaPlayerApp extends Application {
    private final FileChooser fileChooser = new FileChooser();
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private MediaFile mediaFile;
    private MediaFile libraryFile;
    private final MediaList mediaList = new MediaList();
    private final BooleanProperty completedProperty = new SimpleBooleanProperty();
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font font = new Font(20);
        primaryStage.setTitle("MediaPlayer");
        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        final Button openButton = new Button("Add Media");
        openButton.setFont(font);
        Label fileName = new Label();
        fileName.setFont(font);
        HBox hBox = new HBox();
        Button rep = new Button("Repeat");
        VBox timeBox = new VBox();
        HBox time = new HBox();
        timeBox.setAlignment(Pos.TOP_CENTER);
        Button auto = new Button("Repeat: Enable");
        time.setAlignment(Pos.TOP_CENTER);
        hBox.setAlignment(Pos.TOP_CENTER);
        Label videoDuration = new Label();
        Label slash = new Label("/");
        Label currentDuration = new Label(convertDurationMillis(0));
        Button controller = new Button("Play");
        Button stop = new Button("Stop");
        VBox boxVol = new VBox();
        Slider volume = new Slider(0.0, 1.0, 0.5);
        Slider progress = new Slider();
        boxVol.setAlignment(Pos.TOP_CENTER);
        Button volreg = new Button("Mute");
        Label sound = new Label(String.valueOf((int)(volume.getValue()*100))+"%");
        AtomicBoolean repeat = new AtomicBoolean(false);
        AtomicBoolean changing = new AtomicBoolean(false);
        ObservableList<MediaFile> files = FXCollections.observableArrayList(mediaList.getMediaFiles());
        ListView<MediaFile> fileListView = new ListView<>(files);
        Button remove = new Button("Remove Media");
        Button next = new Button("Next");
        Button prev = new Button("Prev");
        Button autoplay = new Button("AutoPlay: enable");
        AtomicBoolean autoboolean = new AtomicBoolean(false);
        AtomicBoolean mute = new AtomicBoolean(false);
        remove.setFont(font);
        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file;
                        if(libraryFile == null) {
                            file = fileChooser.showOpenDialog(primaryStage);
                        }
                        else {
                            file = libraryFile.getFile();
                            libraryFile = null;
                        }
                        if (file != null) {
                            try {
                                mediaFile = new MediaFile(file, file.getName());
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            if(mediaPlayer != null){
                                stop.fire();
                            }
                            if (mediaFile.setMedia()) {
                                mediaPlayer = new MediaPlayer(mediaFile.getMedia());
                                if (mediaView != null) {
                                    root.getChildren().remove(remove);
                                    timeBox.getChildren().clear();
                                    time.getChildren().clear();
                                    hBox.getChildren().clear();
                                    boxVol.getChildren().clear();
                                    root.getChildren().remove(mediaView);
                                    root.getChildren().remove(hBox);
                                    root.getChildren().remove(timeBox);
                                    root.getChildren().remove(boxVol);
                                    root.getChildren().remove(fileListView);
                                }
                                root.getChildren().add(1, remove);
                                fileName.setText(mediaFile.getFile().getName());
                                mediaPlayer.setVolume(volume.getValue());
                                mediaView = new MediaView(mediaPlayer);
                                mediaView.setFitWidth(primaryStage.getWidth() / 2);
                                mediaView.setFitHeight(primaryStage.getHeight() / 2);
                                root.getChildren().add(mediaView);
                                hBox.getChildren().addAll(prev, controller, stop, next, auto, rep, autoplay);
                                boxVol.getChildren().addAll(volreg, sound, volume);
                                volume.setMaxWidth(primaryStage.getWidth()/2);
                                progress.setMaxWidth(primaryStage.getWidth()/2);
                                time.getChildren().addAll(currentDuration, slash, videoDuration);
                                timeBox.getChildren().addAll(time, progress);
                                root.getChildren().addAll(hBox, timeBox, boxVol, fileListView);
                                completedProperty.setValue(true);
                                volume.valueProperty().addListener((observable, oldValue, newValue) -> {
                                    sound.setText(String.valueOf((int) (newValue.doubleValue()*100))+"%");
                                    mediaPlayer.setVolume(newValue.doubleValue());

                                });
                                autoplay.setOnAction(event -> {
                                    if(autoplay.getText().equals("AutoPlay: enable")){
                                        autoboolean.set(true);
                                        autoplay.setText("AutoPlay: disable");
                                    } else if (autoplay.getText().equals("AutoPlay: disable")) {
                                        autoboolean.set(false);
                                        autoplay.setText("AutoPlay: enable");
                                    }
                                });

                                remove.setOnAction(event -> {
                                    if(mediaList.remove(fileListView.getSelectionModel().getSelectedItem())){
                                        files.remove(mediaFile);
                                        if(!files.isEmpty()){
                                            fileListView.getSelectionModel().select(0);
                                            fileListView.fireEvent(
                                                    new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                                                            0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                                                            true, true, true, true, true, true, null)
                                            );
                                        }
                                        else {
                                            stop.fire();
                                            fileName.setText("");
                                            root.getChildren().remove(remove);
                                            timeBox.getChildren().clear();
                                            time.getChildren().clear();
                                            hBox.getChildren().clear();
                                            boxVol.getChildren().clear();
                                            root.getChildren().remove(mediaView);
                                            root.getChildren().remove(hBox);
                                            root.getChildren().remove(timeBox);
                                            root.getChildren().remove(boxVol);
                                            root.getChildren().remove(fileListView);
                                        }
                                    }
                                });
                                rep.setOnAction(event -> {
                                    mediaPlayer.seek(Duration.ZERO);
                                });
                                volreg.setOnAction(event -> {
                                    if(volreg.getText().equals("Mute")){
                                        mediaPlayer.muteProperty().setValue(true);
                                        mute.set(true);
                                        volreg.setText("Unmute");
                                    }
                                    else if(volreg.getText().equals("Unmute")) {
                                        mediaPlayer.muteProperty().setValue(false);
                                        mute.set(false);
                                        volreg.setText("Mute");
                                    }
                                });
                                progress.valueChangingProperty().addListener((observableValue, aBoolean, t1) -> {
                                    changing.set(t1);
                                });
                                progress.valueProperty().addListener((observableValue, number, t1) -> {
                                    if (changing.get()) {
                                        mediaPlayer.seek(Duration.seconds(t1.intValue()));
                                    }
                                });
                                mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                                            currentDuration.setText(convertDurationMillis((int) newValue.toMillis()));
                                    if (!changing.get()) {
                                        progress.setValue(newValue.toSeconds());
                                    }
                                });
                                prev.setOnAction(event -> {
                                    if(fileListView.getSelectionModel().getSelectedIndex() == 0){
                                        fileListView.getSelectionModel().selectLast();
                                    }
                                    else {
                                        fileListView.getSelectionModel().selectPrevious();
                                    }
                                    libraryFile = fileListView.getSelectionModel().getSelectedItem();
                                    stop.fire();
                                    openButton.fire();
                                });
                                next.setOnAction(event -> {
                                    System.out.println(fileListView.getSelectionModel().getSelectedIndex());
                                    if(fileListView.getSelectionModel().getSelectedIndex() ==
                                    files.size()-1){
                                        fileListView.getSelectionModel().selectFirst();
                                    }
                                    else {
                                        fileListView.getSelectionModel().selectNext();
                                    }
                                    libraryFile = fileListView.getSelectionModel().getSelectedItem();
                                    stop.fire();
                                    openButton.fire();
                                });

                                controller.setOnAction(event -> {
                                    if(controller.getText().equals("Play")) {
                                        mediaPlayer.play();
                                        controller.setText("Pause");
                                    }
                                    else if(controller.getText().equals("Pause")){
                                        mediaPlayer.pause();
                                        controller.setText("Play");
                                    }
                                });
                                mediaPlayer.setOnStopped(new Runnable() {
                                    @Override
                                    public void run() {
                                        mediaPlayer.seek(Duration.ZERO);
                                        if(controller.getText().equals("Pause")){
                                            controller.setText("Play");
                                        }

                                    }
                                });
                                stop.setOnAction(event -> {
                                    mediaPlayer.stop();
                                });
                                auto.setOnAction(event -> {
                                    if(auto.getText().equals("Repeat: Enable")){
                                        auto.setText("Repeat: Disable");
                                        repeat.set(true);
                                    }
                                    else {
                                        auto.setText("Repeat: Enable");
                                        repeat.set(false);
                                    }
                                });
                                mediaView.setOnMouseClicked(event -> {
                                    controller.fire();
                                });
                                mediaPlayer.setOnEndOfMedia(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(repeat.get()){
                                            mediaPlayer.seek(Duration.ZERO);
                                            mediaPlayer.play();
                                        }
                                        else {
                                            if(fileListView.getSelectionModel().getSelectedIndex() ==
                                                    files.size()-1){
                                                fileListView.getSelectionModel().selectFirst();
                                            }
                                            else {
                                                fileListView.getSelectionModel().selectNext();
                                            }
                                            libraryFile = fileListView.getSelectionModel().getSelectedItem();
                                            stop.fire();
                                            openButton.fire();
                                        }


                                    }
                                });
                                fileListView.setOnMouseClicked(event -> {
                                    libraryFile = fileListView.getSelectionModel().getSelectedItem();
                                    stop.fire();
                                    openButton.fire();
                                });
                                mediaPlayer.statusProperty().addListener((ob, oldVal, newVal) -> {
                                    if (newVal.equals(MediaPlayer.Status.READY) && mediaFile.getDuration() == null) {
                                        mediaFile.setDuration(mediaFile.getMedia().getDuration());
                                        videoDuration.setText(convertDurationMillis((int) mediaFile.getDuration().toMillis()));
                                        progress.setMax(mediaFile.getDuration().toSeconds());
                                        if(mediaList.add(mediaFile)){
                                            files.add(mediaFile);
                                            fileListView.getSelectionModel().select(mediaFile);
                                        }
                                        if(autoboolean.get()){
                                            controller.fire();
                                        }
                                        mediaPlayer.muteProperty().setValue(mute.get());
                                    }
                                });

                            } else {
                                if(files.isEmpty()) {
                                    fileName.setText("Wrong file format");
                                    timeBox.getChildren().clear();
                                    time.getChildren().clear();
                                    hBox.getChildren().clear();
                                    boxVol.getChildren().clear();
                                    root.getChildren().remove(mediaView);
                                    root.getChildren().remove(hBox);
                                    root.getChildren().remove(timeBox);
                                    root.getChildren().remove(boxVol);
                                }
                                else {
                                    fileListView.fireEvent(
                                            new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                                                    0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                                                    true, true, true, true, true, true, null)
                                    );
                                }
                            }
                        }
                    }
                });

        primaryStage.widthProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(mediaView!= null){
                    mediaView.setFitWidth(primaryStage.getWidth()/2);
                    mediaView.setFitHeight(primaryStage.getHeight()/2);
                    volume.setMaxWidth(primaryStage.getWidth()/2);
                    progress.setMaxWidth(primaryStage.getWidth()/2);
                }

            }
        });
        root.getChildren().addAll(openButton, fileName);
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.setFullScreen(true);
        primaryStage.show();
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
