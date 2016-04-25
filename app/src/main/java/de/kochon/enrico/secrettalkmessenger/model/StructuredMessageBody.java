package de.kochon.enrico.secrettalkmessenger.model;

import java.util.Arrays;

/**
 * Created by enrico on 31.03.16.
 */
public class StructuredMessageBody {

    public static final int PAYLOAD_LENGTH = Messagekey.KEYBODY_LENGTH - 8;

    // there are 8 bytes for header information, in version 2
    public final byte VERSION = (byte) 2;

    public final byte TYPE_TEXT = (byte) 1;
    public final byte TYPE_IMAGE = (byte) 2;

    // range for all counter is limited at 2 ** 16
    private boolean message_is_image;
    private byte messagenumber_lo;
    private byte messagenumber_hi;
    private byte multipart_total_lo;
    private byte multipart_total_hi;
    private byte multipart_current_lo;
    private byte multipart_current_hi;

    public byte[] payload;

    public StructuredMessageBody(byte[] raw) {
        if (null == raw) {
            throw new IllegalArgumentException("Construction of StructuredMessageBody from raw bytes failed, given array is null!");
        }
        if (raw.length != Messagekey.KEYBODY_LENGTH) {
            throw new IllegalArgumentException(String.format("Construction of StructuredMessageBody from raw bytes failed, target length: %d, actual length: %d",
                    Messagekey.KEYBODY_LENGTH, raw.length));
        }
        if (raw[0] != VERSION) {
            throw new IllegalArgumentException(String.format("Construction of StructuredMessageBody from raw bytes failed, version should be %d.", VERSION));
        }
        if (!(raw[1] == TYPE_TEXT || raw[1] == TYPE_IMAGE)) {
            throw new IllegalArgumentException("Construction of StructuredMessageBody from raw bytes failed, type not defined.");
        }
        if (raw[1] == TYPE_IMAGE) {
            this.message_is_image = true;
        } else {
            this.message_is_image = false;
        }
        this.messagenumber_lo = raw[2];
        this.messagenumber_hi = raw[3];
        this.multipart_total_lo = raw[4];
        this.multipart_total_hi = raw[5];
        this.multipart_current_lo = raw[6];
        this.multipart_current_hi = raw[7];
        this.payload = new byte[PAYLOAD_LENGTH];
        for(int i=0; i<PAYLOAD_LENGTH; i++) {
            payload[i] = raw[i+8];
        }
    }


    public StructuredMessageBody(byte[] payloadbytes, boolean isImage, int currentPartCounter, int partTotal, int messagenumber) {
        this.payload = payloadbytes;
        if (this.payload.length > PAYLOAD_LENGTH) {
            throw new IllegalArgumentException(String.format("Construction of StructuredMessageBody failed! Allowed length: %d, actual length: %d",
                    PAYLOAD_LENGTH, this.payload.length));
        }

        this.message_is_image = isImage;

        int upperlimit = (int) Math.pow(2, 16);

        if (currentPartCounter < 0 || currentPartCounter >= upperlimit) {
            throw new IllegalArgumentException(String.format("Construction of StructuredMessageBody failed! currentPartCounter out of range, allowed: 0 - %d, actual value: %d",
                    upperlimit -1, currentPartCounter));
        }
        multipart_current_lo = new Integer(currentPartCounter % 256).byteValue();
        multipart_current_hi = new Integer(currentPartCounter / 256).byteValue();

        if (partTotal < 0 || partTotal >= upperlimit) {
            throw new IllegalArgumentException(String.format("Construction of StructuredMessageBody failed! partTotal out of range, allowed: 0 - %d, actual value: %d",
                    upperlimit -1, partTotal));
        }
        multipart_total_lo = new Integer(partTotal % 256).byteValue();
        multipart_total_hi = new Integer(partTotal / 256).byteValue();

        if (messagenumber < 0 || messagenumber >= upperlimit) {
            throw new IllegalArgumentException(String.format("Construction of StructuredMessageBody failed! messagenumber out of range, allowed: 0 - %d, actual value: %d",
                    upperlimit -1, messagenumber));
        }
        messagenumber_lo = new Integer(messagenumber % 256).byteValue();
        messagenumber_hi = new Integer(messagenumber / 256).byteValue();
    }


    public byte[] getFullBody() {
        final byte[] copyOfbody = new byte[Messagekey.KEYBODY_LENGTH];
        // header
        copyOfbody[0] = this.VERSION;
        if (message_is_image) {
            copyOfbody[1] = TYPE_IMAGE;
        } else {
            copyOfbody[1] = TYPE_TEXT;
        }
        copyOfbody[2] = this.messagenumber_lo;
        copyOfbody[3] = this.messagenumber_hi;
        copyOfbody[4] = this.multipart_total_lo;
        copyOfbody[5] = this.multipart_total_hi;
        copyOfbody[6] = this.multipart_current_lo;
        copyOfbody[7] = this.multipart_current_hi;

        // payload
        for(int i=0; i<PAYLOAD_LENGTH; i++) {
            byte currentByte = 0;
            if (i<this.payload.length) {
                currentByte = this.payload[i];
            }
            copyOfbody[i+8] = currentByte;
        }
        return copyOfbody;
    }

    public String getPayload() {
        return new String(payload, 0, payload.length);
    }

    public byte[] getRawPayload() {
        return Arrays.copyOfRange(payload, 0, payload.length);
    }


    public int getMessageNumber() {
        return ((int)(0xff & this.messagenumber_lo))+256*((int)(0xff & this.messagenumber_hi));
    }

    public int getTotal() {
        return ((int)(0xff & this.multipart_total_lo))+256*((int)(0xff & this.multipart_total_hi));
    }

    public int getCurrentPart() {
        return ((int)(0xff & this.multipart_current_lo))+256*((int)(0xff & this.multipart_current_hi));
    }

    public boolean isImage() {
        return message_is_image;
    }
}
