package mx.edu.utez.judicial.tocas.model;

import java.time.LocalDate;

public class CaseDocument {
    private int id;
    private int appealId;
    private String name;
    private int pages;
    private LocalDate date;
    private String instance;
    private String court;

    public CaseDocument() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAppealId() { return appealId; }
    public void setAppealId(int appealId) { this.appealId = appealId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getInstance() { return instance; }
    public void setInstance(String instance) { this.instance = instance; }
    public String getCourt() { return court; }
    public void setCourt(String court) { this.court = court; }
}
