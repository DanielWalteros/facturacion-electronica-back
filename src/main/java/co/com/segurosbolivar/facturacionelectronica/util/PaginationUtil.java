package co.com.segurosbolivar.facturacionelectronica.util;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;

import java.util.Collections;
import java.util.List;

public final class PaginationUtil {

    private PaginationUtil() {
        // Utility class — no instantiation
    }

    public static <T> PaginatedResponse<T> paginate(List<T> items, int page, int size, int maxSize, int defaultSize) {
        int effectiveSize = size;
        if (effectiveSize <= 0) {
            effectiveSize = defaultSize;
        }
        if (effectiveSize > maxSize) {
            effectiveSize = maxSize;
        }

        int totalElements = items.size();
        int totalPages = (int) Math.ceil((double) totalElements / effectiveSize);

        if (page < 0) {
            page = 0;
        }

        List<T> content;
        if (page >= totalPages) {
            content = Collections.emptyList();
        } else {
            int fromIndex = page * effectiveSize;
            int toIndex = Math.min(fromIndex + effectiveSize, totalElements);
            content = items.subList(fromIndex, toIndex);
        }

        return PaginatedResponse.<T>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(effectiveSize)
                .build();
    }
}
