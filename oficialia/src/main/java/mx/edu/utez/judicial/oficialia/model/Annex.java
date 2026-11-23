package mx.edu.utez.judicial.oficialia.model;

/**
 * File attached to a demand request when it was registered (writ, ID, certificates). Any type of file is
 * accepted (DFR 3.1). Only its name, size and type are kept, not the file itself.
 */
public class Annex {

    private final String fileName;
    private final int sizeKb;
    private final String contentType;

    public Annex(String fileName, int sizeKb) {
        this(fileName, sizeKb, null);
    }

    public Annex(String fileName, int sizeKb, String contentType) {
        this.fileName = fileName;
        this.sizeKb = sizeKb;
        this.contentType = contentType;
    }

    /** An annex received in the registration; its size is rounded up to whole kilobytes. */
    public static Annex received(String fileName, String contentType, long sizeBytes) {
        return new Annex(fileName, (int) Math.max(1, (sizeBytes + 1023) / 1024), contentType);
    }

    public String getFileName() {
        return fileName;
    }

    public int getSizeKb() {
        return sizeKb;
    }

    public String getContentType() {
        return contentType;
    }

    public String getSizeText() {
        if (sizeKb < 1024) {
            return sizeKb + " KB";
        }
        return String.format("%.1f MB", sizeKb / 1024.0);
    }
}
