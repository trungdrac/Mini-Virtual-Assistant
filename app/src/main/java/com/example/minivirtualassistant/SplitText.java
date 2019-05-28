package com.example.minivirtualassistant;

public class SplitText {
    public String[] split(String resultText) {
        String[] splitText = resultText.split("\\s");
        return splitText;
    }
}
