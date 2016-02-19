/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.ockeyHook;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import openchannel_dynamic_downloader.downloader.DownloadTask;
import openchannel_dynamic_downloader.downloader.Downloader;
import openchannel_dynamic_downloader.model.MainDataModel;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 *
 * @author Kofola
 */
public class OCKeyHook implements NativeKeyListener {

    public static boolean clearClipboardContent = false;//MIGHT BE SET IN PREFERENCES LATER BY USER TO DIFFERENT BOOLEAN
    
    public static int OC_NKE=NativeKeyEvent.VC_CONTROL_R;

    //CTRL+C to clipboard then, CTRL+SHIFT to quick download ucidim este
    public OCKeyHook() {

    }

    private boolean processEntry() {
        try {
            //URL url, String fileName, String downloadDir, int numberOfConnections, boolean notification, boolean storeItself
            //get contents of the clipboard
            String url = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (clearClipboardContent) {

                Toolkit.getDefaultToolkit()
                        .getSystemClipboard().setContents(new Transferable() {
                            @Override
                            public DataFlavor[] getTransferDataFlavors() {
                                return new DataFlavor[0];
                            }

                            @Override
                            public boolean isDataFlavorSupported(DataFlavor flavor) {
                                return false;
                            }

                            @Override
                            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                                throw new UnsupportedFlavorException(flavor);
                            }
                        },null);

            }
            return Downloader.downloadFile(new URL(url), null, MainDataModel.getInstance().loginProfile.getDownloadsDir(), MainDataModel.getInstance().loginProfile.getNumOfConnectionsPerDownload(), true, true);//TODO storeitself kind of depends on incognito mode etc
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(OCKeyHook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void enable() {
        GlobalScreen.addNativeKeyListener(this);
    }

    public void disable() {
        GlobalScreen.removeNativeKeyListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nke) {

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nke) {
        if (nke.getKeyCode() == OC_NKE) {
            processEntry();
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nke) {
    }

}
