package org.wit.placemark.models;

import com.google.gson.annotations.SerializedName;


public class Quote {

    @SerializedName("quote")
    private String shakespeareQuote;


    public Quote(String quote) {
        this.shakespeareQuote = quote;
    }

    public String getQuote() {
        return shakespeareQuote;
    }
}
