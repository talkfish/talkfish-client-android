package de.kochon.enrico.secrettalkmessenger.model;

/**
 * Created by enrico on 31.03.16.
 */
public class StructuredMessageBody {

    // there are 7 bytes for header information, in version 1
    public final byte VERSION=1;

    // range for all counter is limited at 2 ** 16
    public byte messagenumber_lo;
    public byte messagenumber_hi;
    public byte multipart_total_lo;
    public byte multipart_total_hi;
    public byte multipart_current_lo;
    public byte multipart_current_hi;

    public byte[] payload;
}
