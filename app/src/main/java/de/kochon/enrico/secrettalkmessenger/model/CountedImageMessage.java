package de.kochon.enrico.secrettalkmessenger.model;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by enrico on 25.04.16.
 */
public class CountedImageMessage extends CountedMessage {

    protected byte[] imagedata;

    public CountedImageMessage(boolean isReceived, int localmessagenumber, int transmittedmessagenumber, byte[] imagedata, Date localtime) {
        super(isReceived, localmessagenumber, transmittedmessagenumber, "", localtime);
        this.imagedata = imagedata;
    }

    public byte[] getImagedata() {
        return Arrays.copyOfRange(imagedata, 0, imagedata.length);
    }
}