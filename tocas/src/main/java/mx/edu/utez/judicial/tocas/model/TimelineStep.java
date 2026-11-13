package mx.edu.utez.judicial.tocas.model;


public class TimelineStep {
    private String label;
    private String dateText;
    private String state;
    private String badge;

    public TimelineStep() {}

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDateText() { return dateText; }
    public void setDateText(String dateText) { this.dateText = dateText; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
}
