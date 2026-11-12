package mx.edu.utez.judicial.common.model;

import java.time.LocalDate;

public class CalendarDay {
    private LocalDate date;
    private boolean inMonth;
    private boolean today;
    private boolean selected;
    private String state;
    private int count;

    public CalendarDay() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isInMonth() { return inMonth; }
    public void setInMonth(boolean inMonth) { this.inMonth = inMonth; }
    public boolean isToday() { return today; }
    public void setToday(boolean today) { this.today = today; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
