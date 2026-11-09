package mx.edu.utez.judicial.oficialia.model;

/**
 * File attached to a demand request when it was registered (writ, ID, certificates).
 */
public class Annex {

    private final String fileName;
    private final int sizeKb;

    public Annex(String fileName, int sizeKb) {
        this.fileName = fileName;
        this.sizeKb = sizeKb;
    }

    public String getFileName() {
        return fileName;
    }

    public int getSizeKb() {
        return sizeKb;
    }

    public String getSizeText() {
        if (sizeKb < 1024) {
            return sizeKb + " KB";
        }
        return String.format("%.1f MB", sizeKb / 1024.0);
    }
}
