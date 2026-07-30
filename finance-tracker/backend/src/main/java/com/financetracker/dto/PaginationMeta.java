package com.financetracker.dto;

public class PaginationMeta {
    private int page;
    private int limit;
    private long total;
    private int pages;

    public PaginationMeta(int page, int limit, long total, int pages) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.pages = pages;
    }

    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public long getTotal() { return total; }
    public int getPages() { return pages; }
}
