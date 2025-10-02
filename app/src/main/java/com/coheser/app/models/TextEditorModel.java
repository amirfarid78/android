package com.coheser.app.models;

import java.io.Serializable;

public class TextEditorModel implements Serializable {
    public FontModel selectedFont;
    public int direction = 1;
    public int colorCode;
    public String text = "";
}
