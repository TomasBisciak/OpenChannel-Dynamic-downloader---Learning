/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.utils;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author tomas
 */
public class FileUtils {

    private FileUtils() {
    }

    public static final long getAllDiskSpace() {

        long diskSpace = 0;

        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            try {
                System.out.println(root);
                FileStore store = Files.getFileStore(root);
                System.out.println("All disk space "+store.getTotalSpace());
                diskSpace += store.getTotalSpace();
            } catch (Exception  ex) {
                ex.printStackTrace();
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
                ex.printStackTrace();
            }
        }
        return diskSpace;
    }

}
