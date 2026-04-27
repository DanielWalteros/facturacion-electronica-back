package co.com.segurosbolivar.facturacionelectronica.util;

import co.com.segurosbolivar.facturacionelectronica.dto.response.PaginatedResponse;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Property 7: Pagination Size Clamping
 * Validates: Requirements 2.4, 4.5
 */
@Tag("Feature: facturacion-electronica-consulta, Property 7: Pagination Size Clamping")
class PaginationUtilSizeClampingPropertyTest {

    private static final int DEFAULT_SIZE = 20;

    private List<String> generateItems(int count) {
        return IntStream.range(0, count).mapToObj(i -> "item-" + i).toList();
    }

    @Property(tries = 10)
    void sizeGreaterThanMaxIsClampedToMax_tracker(
            @ForAll @IntRange(min = 101, max = 10000) int size) {
        int maxSize = 100;
        List<String> items = generateItems(200);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, size, maxSize, DEFAULT_SIZE);

        Assertions.assertThat(result.getPageSize()).isEqualTo(maxSize);
        Assertions.assertThat(result.getContent().size()).isLessThanOrEqualTo(maxSize);
    }

    @Property(tries = 10)
    void sizeGreaterThanMaxIsClampedToMax_logs(
            @ForAll @IntRange(min = 201, max = 10000) int size) {
        int maxSize = 200;
        List<String> items = generateItems(400);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, size, maxSize, DEFAULT_SIZE);

        Assertions.assertThat(result.getPageSize()).isEqualTo(maxSize);
        Assertions.assertThat(result.getContent().size()).isLessThanOrEqualTo(maxSize);
    }

    @Property(tries = 10)
    void sizeLessThanOrEqualToZeroUsesDefault(
            @ForAll @IntRange(min = -1000, max = 0) int size) {
        int maxSize = 100;
        List<String> items = generateItems(50);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, size, maxSize, DEFAULT_SIZE);

        Assertions.assertThat(result.getPageSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Property(tries = 10)
    void sizeInValidRangePassesThrough_tracker(
            @ForAll @IntRange(min = 1, max = 100) int size) {
        int maxSize = 100;
        List<String> items = generateItems(200);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, size, maxSize, DEFAULT_SIZE);

        Assertions.assertThat(result.getPageSize()).isEqualTo(size);
    }

    @Property(tries = 10)
    void sizeInValidRangePassesThrough_logs(
            @ForAll @IntRange(min = 1, max = 200) int size) {
        int maxSize = 200;
        List<String> items = generateItems(400);

        PaginatedResponse<String> result = PaginationUtil.paginate(items, 0, size, maxSize, DEFAULT_SIZE);

        Assertions.assertThat(result.getPageSize()).isEqualTo(size);
    }

    /**
     * Simple assertion helper to avoid importing external assertion libraries.
     */
    private static class Assertions {
        static IntAssert assertThat(int actual) {
            return new IntAssert(actual);
        }

        static class IntAssert {
            private final int actual;
            IntAssert(int actual) { this.actual = actual; }

            IntAssert isEqualTo(int expected) {
                if (actual != expected) {
                    throw new AssertionError("Expected " + expected + " but was " + actual);
                }
                return this;
            }

            IntAssert isLessThanOrEqualTo(int expected) {
                if (actual > expected) {
                    throw new AssertionError("Expected <= " + expected + " but was " + actual);
                }
                return this;
            }
        }
    }
}
