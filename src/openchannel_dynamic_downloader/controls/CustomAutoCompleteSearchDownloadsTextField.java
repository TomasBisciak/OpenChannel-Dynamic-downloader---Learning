/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import openchannel_dynamic_downloader.model.MainDataModel;
import openchannel_dynamic_downloader.utils.MiscUtils;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author Kofola
 */
public class CustomAutoCompleteSearchDownloadsTextField extends CustomAutoCompleteTextField {

    private static Thread refreshThread;

    public CustomAutoCompleteSearchDownloadsTextField() {
        super();
        init();
    }

    @Override
    public void init() {

        this.setOnKeyPressed((KeyEvent ke) -> {
            switch (ke.getCode()) {
                case ENTER: {
                    System.out.println("pressed enter");
                    //Desktop.getDesktop().browse(null);
                    break;
                }
                default:
                    break;
            }
        });

        refreshThread = new Thread(new Runnable() {

            @Override
            public void run() {
                long milStart = 0;
                long milEnd = 0;
                while (true) {
                    milStart = System.nanoTime();
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            refreshSuggestions();
                        }
                    });
                    milEnd = System.nanoTime();
                    System.out.println("ITERATION TIME IN REFRESH SUGGESTION THREAD:" + (milEnd - milStart));
                    /*
                     try{
                     for(String s:getPossibleSuggestions()){
                     System.out.println("ITEM:"+s+"\n");
                     }
                     }catch(Exception e){
                     e.printStackTrace();
                     }*/
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        refreshThread.setPriority(Thread.MIN_PRIORITY);
        refreshThread.setDaemon(true);
        refreshThread.start();

    }

    public void refreshSuggestions() {
        //TODO maybe not really optimized ,test on 1000 ITEMS, 2000 ITEMS..
        System.out.println("DIRECOTRY:" + MainDataModel.getInstance().loginProfile.getDownloadsDir());
        Path path = Paths.get(MainDataModel.getInstance().loginProfile.getDownloadsDir());
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!attrs.isDirectory()) {
                        if (!getPossibleSuggestions().contains(file.getFileName().toString())) {
                            autoCompletionLearnWord(file.getFileName().toString());
                        }

                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
       // TextFields.bindAutoCompletion(this, getPossibleSuggestions());
        // loadSuggestions();
        //loadSuggestions(names.toArray(new String[names.size()]));
    }

}
