package co.com.segurosbolivar.facturacionelectronica.util;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PaginationUtilTest {

    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_SIZE = 20;

    private List<String> generateItems(int count) {
        return IntStream.range(0, count).mapToObj(i -> "item-" + i).toList();
    }

    @Test
    void emptyList_returnsEmptyContent() {
        PaginatedResponse<String> result = PaginationUtil.paginate(
                Collections.emptyList(), 0, 20, MAX_SIZE, DEFAULT_SIZE);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getCurrentPage());
        assertEquals(20, result.getPageSize());
    }

    @Test
    void pageBeyondRange_returnsEmptyContent() {
        List<String> items = generateItems(10);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 5, 20, MAX_SIZE, DEFAULT_SIZE);

        assertTrue(result.getContent().isEmpty());
        assertEquals(10, result.getTotalElements());
    }

    @Test
    void sizeZero_usesDefaultSize() {
        List<String> items = generateItems(50);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, 0, MAX_SIZE, DEFAULT_SIZE);

        assertEquals(DEFAULT_SIZE, result.getPageSize());
        assertEquals(DEFAULT_SIZE, result.getContent().size());
    }

    @Test
    void negativePage_treatedAsZero() {
        List<String> items = generateItems(10);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, -1, 5, MAX_SIZE, DEFAULT_SIZE);

        assertEquals(0, result.getCurrentPage());
        assertEquals(5, result.getContent().size());
    }

    @Test
    void negativeSize_usesDefaultSize() {
        List<String> items = generateItems(50);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, -5, MAX_SIZE, DEFAULT_SIZE);

        assertEquals(DEFAULT_SIZE, result.getPageSize());
    }

    @Test
    void sizeExceedsMax_clampedToMax() {
        List<String> items = generateItems(200);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, 500, MAX_SIZE, DEFAULT_SIZE);

        assertEquals(MAX_SIZE, result.getPageSize());
        assertEquals(MAX_SIZE, result.getContent().size());
    }

    @Test
    void lastPage_returnsRemainingItems() {
        List<String> items = generateItems(25);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 1, 20, MAX_SIZE, DEFAULT_SIZE);

        assertEquals(5, result.getContent().size());
        assertEquals(1, result.getCurrentPage());
        assertEquals(2, result.getTotalPages());
        assertEquals(25, result.getTotalElements());
    }

    @Test
    void exactPageBoundary_returnsFullPage() {
        List<String> items = generateItems(40);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 1, 20, MAX_SIZE, DEFAULT_SIZE);

        assertEquals(20, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void singleElement_singlePage() {
        List<String> items = List.of("only-item");

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, 10, MAX_SIZE, DEFAULT_SIZE);

        assertEquals(1, result.getContent().size());
        assertEquals("only-item", result.getContent().get(0));
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }
}
