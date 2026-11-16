package com.sms.model;

import java.util.Collections;
import java.util.List;

/**
 * Immutable pagination container for large-scale record viewing.
 *
 * @param <T> Item element type
 */
public class Page<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final int totalElements;
    private final int totalPages;

    public Page(List<T> content, int pageNumber, int pageSize, int totalElements) {
        this.content = content != null ? Collections.unmodifiableList(content) : Collections.emptyList();
        this.pageNumber = Math.max(1, pageNumber);
        this.pageSize = Math.max(1, pageSize);
        this.totalElements = Math.max(0, totalElements);
        this.totalPages = (int) Math.ceil((double) this.totalElements / this.pageSize);
    }

    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasPrevious() {
        return pageNumber > 1;
    }

    public boolean hasNext() {
        return pageNumber < totalPages;
    }
}
