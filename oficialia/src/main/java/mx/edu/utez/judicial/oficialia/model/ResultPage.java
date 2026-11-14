package mx.edu.utez.judicial.oficialia.model;

import java.util.List;

/**
 * One page of a listing. Pages are numbered from 1; a number out of range shows the closest page.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class ResultPage<T> {

    private final List<T> items;
    private final int number;
    private final int size;
    private final int totalItems;

    private ResultPage(List<T> items, int number, int size, int totalItems) {
        this.items = items;
        this.number = number;
        this.size = size;
        this.totalItems = totalItems;
    }

    /** Cuts the page out of every matching item. */
    public static <T> ResultPage<T> of(List<T> all, int requestedNumber, int size) {
        int totalPages = Math.max(1, Math.ceilDiv(all.size(), size));
        int number = Math.min(Math.max(requestedNumber, 1), totalPages);
        int from = (number - 1) * size;
        return new ResultPage<>(all.subList(from, Math.min(from + size, all.size())), number, size, all.size());
    }

    public List<T> getItems() {
        return items;
    }

    public int getNumber() {
        return number;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return Math.max(1, Math.ceilDiv(totalItems, size));
    }

    /** Position of the first item of the page, counting from 1; 0 when there are no items. */
    public int getFirstItem() {
        return items.isEmpty() ? 0 : (number - 1) * size + 1;
    }

    public int getLastItem() {
        return (number - 1) * size + items.size();
    }

    public boolean isFirst() {
        return number == 1;
    }

    public boolean isLast() {
        return number == getTotalPages();
    }
}
