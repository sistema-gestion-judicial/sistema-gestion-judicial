package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ResultPageTest {

    private final List<Integer> items = IntStream.rangeClosed(1, 23).boxed().toList();

    @Test
    void cutsTheRequestedPage() {
        ResultPage<Integer> page = ResultPage.of(items, 2, 10);

        assertEquals(List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20), page.getItems());
        assertEquals(3, page.getTotalPages());
        assertEquals(11, page.getFirstItem());
        assertEquals(20, page.getLastItem());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
    }

    @Test
    void aNumberOutOfRangeShowsTheClosestPage() {
        assertEquals(1, ResultPage.of(items, 0, 10).getNumber());
        assertEquals(1, ResultPage.of(items, -4, 10).getNumber());
        ResultPage<Integer> last = ResultPage.of(items, 99, 10);
        assertEquals(3, last.getNumber());
        assertEquals(List.of(21, 22, 23), last.getItems());
        assertTrue(last.isLast());
    }

    @Test
    void withoutItemsThereIsOneEmptyPage() {
        ResultPage<Integer> page = ResultPage.of(List.of(), 3, 10);

        assertEquals(1, page.getNumber());
        assertEquals(1, page.getTotalPages());
        assertEquals(0, page.getFirstItem());
        assertEquals(0, page.getLastItem());
        assertTrue(page.isFirst() && page.isLast());
    }
}
