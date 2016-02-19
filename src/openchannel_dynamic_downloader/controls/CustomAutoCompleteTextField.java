/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openchannel_dynamic_downloader.controls;

import java.awt.Desktop;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

/**
 *
 * @author tomas
 */
public abstract class CustomAutoCompleteTextField extends TextField {

    private AutoCompletionBinding<String> autoCompletionBinding;
    private Set<String> possibleSuggestions = new HashSet<>();

    public CustomAutoCompleteTextField() {
        this.setStyle("-fx-background-radius:0;");
         //TextFields.bindAutoCompletion(this,possibleSuggestions);
       // autoCompletionBinding = TextFields.bindAutoCompletion(this, possibleSuggestions);
        
    }

    public abstract void init();

    public void autoCompletionLearnWord(String newWord){

        possibleSuggestions.add(newWord);

         

        // we dispose the old binding and recreate a new binding

        if (autoCompletionBinding != null) {

            autoCompletionBinding.dispose();

        }

        autoCompletionBinding = TextFields.bindAutoCompletion(this, possibleSuggestions);

    }
    
     public void autoCompletionLearnWords(List<String> words){

        possibleSuggestions.addAll(words);

         

        // we dispose the old binding and recreate a new binding

        if (autoCompletionBinding != null) {

            autoCompletionBinding.dispose();

        }

        autoCompletionBinding = TextFields.bindAutoCompletion(this, possibleSuggestions);

    }
    
    
    public final void loadSuggestions(String[] suggestions) {
       // possibleSuggestions = new HashSet<>(Arrays.asList(suggestions));
        // possibleSuggestions.addAll(Arrays.asList(suggestions));

        TextFields.bindAutoCompletion(this, possibleSuggestions);
    }
     public final <T>void loadSuggestions(Collection<T> suggestions) {
       // possibleSuggestions = new HashSet<>(Arrays.asList(suggestions));
        // possibleSuggestions.addAll(Arrays.asList(suggestions));

        TextFields.bindAutoCompletion(this, possibleSuggestions);
    }

    public final void loadSuggestions() {
      // possibleSuggestions.addAll(Arrays.asList(suggestions));
        //  possibleSuggestions=new HashSet<>(possibleSuggestions);
        // TextFields.bindAutoCompletion(this, possibleSuggestions);
    }

    public Set<String> getPossibleSuggestions() {
        return possibleSuggestions;
    }

}
