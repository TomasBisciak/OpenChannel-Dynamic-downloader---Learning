/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.AutoCompletionBinding;

/**
 *
 * @author Kofola
 */
public class CustomAutoCompleteSearchTableTextField extends CustomAutoCompleteTextField{
    
    
  
    
    public CustomAutoCompleteSearchTableTextField(){
        super();
    }

    @Override
    public void init() {
          this.setOnKeyPressed((KeyEvent ke) -> {
            switch (ke.getCode()) {
                case ENTER:{
                    System.out.println("pressed enter");
                    //Desktop.getDesktop().browse(null);
                    break;
                }
                default:
                    break;
            }
        });
    }
    
}
