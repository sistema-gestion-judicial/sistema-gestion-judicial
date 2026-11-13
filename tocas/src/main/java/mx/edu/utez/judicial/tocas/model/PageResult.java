package mx.edu.utez.judicial.tocas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** One page of results plus the data needed to draw the pager. */
public class PageResult<T> {
    private final List<T> items;
    private final int total;
    private final int page;
    private final int pageSize;

    public PageResult(List<T> items, int total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getItems() { return items; }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getPages() { return Math.max(1, (int) Math.ceil(total / (double) pageSize)); }
    public int getFirstIndex() { return total == 0 ? 0 : (page - 1) * pageSize + 1; }
    public int getLastIndex() { return Math.min(total, page * pageSize); }

    /** Page numbers to draw; -1 means an ellipsis. */
    public List<Integer> getWindow() {
        int n = getPages();
        TreeSet<Integer> set = new TreeSet<>();
        set.add(1);
        set.add(n);
        for (int p = page - 1; p <= page + 1; p++) {
            if (p >= 1 && p <= n) set.add(p);
        }
        List<Integer> out = new ArrayList<>();
        int prev = 0;
        for (int p : set) {
            if (prev != 0 && p - prev > 1) out.add(-1);
            out.add(p);
            prev = p;
        }
        return out;
    }
}
