/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 *
 * @author tomas
 */
public class FileUtils {

    private FileUtils() {
    }

    public static final long getDiskSpace(Path fileStore) throws IOException {
        long diskSpace = -1;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            if (fileStore.equals(root)) {
                diskSpace += Files.getFileStore(fileStore).getUsableSpace();
            }
        }
        return diskSpace;
    }

    public static final int getNumOfDisk() {
        int c = 0;
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            c++;
        }
        return c;
    }

    public static final long getAllDiskSpace() {

        long diskSpace = 0;

        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            try {
                FileStore store = Files.getFileStore(root);
                diskSpace += store.getTotalSpace();
            } catch (Exception ex) {

            }
        }
        return diskSpace;

    }

    public static final long getAllUsableDiskSpace() {
        long diskSpace = 0;

        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            try {
                FileStore store = Files.getFileStore(root);
                diskSpace += store.getUsableSpace();
            } catch (Exception ex) {

            }
        }
        return diskSpace;
    }

    //readingResources
    public static final String readResource(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        try (Scanner scan = new Scanner(stream)) {

            while (scan.hasNextLine()) {
                sb.append(scan.nextLine()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static final void openWebpage(URI uri) {
        //if desktop mode is supported get desktop
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if ((desktop != null) && (desktop.isSupported(Desktop.Action.BROWSE))) {
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //clears anything that is inside the file
    public static void ereaseFile(File file) {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.print("");
            System.out.println("File: " + file.toString() + " was ereased.File is now empty");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getFilesInDir(String dir) {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                System.out.println("File " + listOfFile.getName());
            } 
        }
    }

}
