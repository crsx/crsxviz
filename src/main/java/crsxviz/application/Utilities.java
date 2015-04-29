package crsxviz.application;

import javafx.collections.ObservableList;
import javafx.scene.text.Text;

public class Utilities {

    /**
     * Check if the list of Text objects contains the string property of
     * str
     *
     * @param list list of Text objects to iterate over
     * @param str  the String object to search for
     * @return true if str is in list, false otherwise
     */
    public static boolean contains(ObservableList<Text> list, String str) {
        return list.stream().anyMatch((text) -> (text.getText().equals(str)));
    }
}
