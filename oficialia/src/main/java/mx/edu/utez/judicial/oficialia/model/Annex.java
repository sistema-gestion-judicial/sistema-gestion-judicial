package mx.edu.utez.judicial.oficialia.model;

/**
 * File attached to a demand request when it was registered (writ, ID, certificates). Any type of file is
 * accepted (DFR 3.1). The content is only present while the request is being saved: the lists read back
 * the name, the size and the type, not the bytes.
 */
public class Annex {

    private final String fileName;
    private final int sizeKb;
    private final String contentType;
    private final byte[] content;

    public Annex(String fileName, int sizeKb) {
        this(fileName, sizeKb, null, null);
    }

    /** An annex received in the registration, with its bytes; its size is rounded up to whole kilobytes. */
    public Annex(String fileName, String contentType, byte[] content) {
        this(fileName, Math.max(1, (content.length + 1023) / 1024), contentType, content.clone());
    }

    /** An annex read back from storage, with its type but without its bytes. */
    public Annex(String fileName, int sizeKb, String contentType) {
        this(fileName, sizeKb, contentType, null);
    }

    private Annex(String fileName, int sizeKb, String contentType, byte[] content) {
        this.fileName = fileName;
        this.sizeKb = sizeKb;
        this.contentType = contentType;
        this.content = content;
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

    /** A copy of the bytes, or null when the annex was read back without them. */
    public byte[] getContent() {
        return content == null ? null : content.clone();
    }

    public String getSizeText() {
        if (sizeKb < 1024) {
            return sizeKb + " KB";
        }
        return String.format("%.1f MB", sizeKb / 1024.0);
    }
}
