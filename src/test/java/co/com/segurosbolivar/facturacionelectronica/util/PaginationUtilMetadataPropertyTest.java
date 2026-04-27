package co.com.segurosbolivar.facturacionelectronica.util;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Property 8: Pagination Metadata Consistency
 * Validates: Requirements 2.5, 4.6
 */
@Tag("Feature: facturacion-electronica-consulta, Property 8: Pagination Metadata Consistency")
class PaginationUtilMetadataPropertyTest {

    private static final int MAX_SIZE = 100;
    private static final int DEFAULT_SIZE = 20;

    private List<String> generateItems(int count) {
        return IntStream.range(0, count).mapToObj(i -> "item-" + i).toList();
    }

    @Property(tries = 10)
    void totalElementsEqualsListSize(
            @ForAll @IntRange(min = 0, max = 500) int listSize,
            @ForAll @IntRange(min = 0, max = 10) int page,
            @ForAll @IntRange(min = 1, max = 100) int size) {

        List<String> items = generateItems(listSize);
        PaginatedResponse<String> result = PaginationUtil.paginate(items, page, size, MAX_SIZE, DEFAULT_SIZE);

        if (result.getTotalElements() != listSize) {
            throw new AssertionError("totalElements expected " + listSize + " but was " + result.getTotalElements());
        }
    }

    @Property(tries = 10)
    void totalPagesEqualsExpectedCeiling(
            @ForAll @IntRange(min = 0, max = 500) int listSize,
            @ForAll @IntRange(min = 1, max = 100) int size) {

        List<String> items = generateItems(listSize);
        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, size, MAX_SIZE, DEFAULT_SIZE);

        int expectedTotalPages = (int) Math.ceil((double) listSize / result.getPageSize());
        if (result.getTotalPages() != expectedTotalPages) {
            throw new AssertionError("totalPages expected " + expectedTotalPages + " but was " + result.getTotalPages());
        }
    }

    @Property(tries = 10)
    void contentSizeDoesNotExceedPageSize(
            @ForAll @IntRange(min = 0, max = 500) int listSize,
            @ForAll @IntRange(min = 0, max = 10) int page,
            @ForAll @IntRange(min = 1, max = 100) int size) {

        List<String> items = generateItems(listSize);
        PaginatedResponse<String> result = PaginationUtil.paginate(items, page, size, MAX_SIZE, DEFAULT_SIZE);

        if (result.getContent().size() > result.getPageSize()) {
            throw new AssertionError("content.size() " + result.getContent().size()
                    + " exceeds pageSize " + result.getPageSize());
        }
    }

    @Property(tries = 10)
    void emptyContentWhenPageBeyondTotalPages(
            @ForAll @IntRange(min = 1, max = 200) int listSize,
            @ForAll @IntRange(min = 1, max = 50) int size) {

        List<String> items = generateItems(listSize);
        int effectiveSize = Math.min(size, MAX_SIZE);
        int totalPages = (int) Math.ceil((double) listSize / effectiveSize);
        int beyondPage = totalPages + 1;

        PaginatedResponse<String> result = PaginationUtil.paginate(items, beyondPage, size, MAX_SIZE, DEFAULT_SIZE);

        if (!result.getContent().isEmpty()) {
            throw new AssertionError("Expected empty content for page " + beyondPage
                    + " but got " + result.getContent().size() + " elements");
        }
    }
}
