package uj.wmii.pwj.map2d;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.insecure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Map2DRandomTest {

    private static final int SIZE = 1000;
    String[] rowKeys = new String[SIZE];
    String[] colKeys = new String[SIZE];
    String[][] values = new String[SIZE][SIZE];
    private final Map2D<String, String, String> map = Map2D.createInstance();

    @BeforeAll
    void setup() {
        for (int i = 0; i < SIZE; i++) {
            rowKeys[i] = insecure().nextAlphanumeric(15, 25);
            colKeys[i] = insecure().nextAlphanumeric(15, 25);
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                values[i][j] = insecure().nextAlphanumeric(10, 20);
                map.put(rowKeys[i], colKeys[j], values[i][j]);
            }
        }
    }

    @Test
    void size() {
        assertEquals(SIZE * SIZE, map.size());
    }

    @Test
    void get() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                assertEquals(values[i][j], map.get(rowKeys[i], colKeys[j]));
            }
        }
    }

    @Test
    void containsKey() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                assertTrue(map.containsKey(rowKeys[i], colKeys[j]));
            }
        }
    }

    @Test
    void containsRow() {
        for (int i = 0; i < SIZE; i++) {
            assertTrue(map.containsRow(rowKeys[i]));
        }
    }

    @Test
    void containsColumn() {
        for (int i = 0; i < SIZE; i++) {
            assertTrue(map.containsColumn(colKeys[i]));
        }
    }

    @Test
    void columnView() {
        for (int i = 0; i < SIZE; i++) {
            var view = map.columnView(colKeys[i]);
            for (int j = 0; j < SIZE; j++) {
                var value = view.get(rowKeys[j]);
                assertEquals(values[j][i], value);
            }
        }
    }

    @Test
    void rowView() {
        for (int i = 0; i < SIZE; i++) {
            var view = map.rowView(rowKeys[i]);
            for (int j = 0; j < SIZE; j++) {
                var value = view.get(colKeys[j]);
                assertEquals(values[i][j], value);
            }
        }
    }

    @Test
    void columnMapView() {
        var view = map.columnMapView();
        for (int i = 0; i < SIZE; i++) {
            var rowMap = view.get(colKeys[i]);
            for (int j = 0; j < SIZE; j++) {
                var value = rowMap.get(rowKeys[j]);
                assertEquals(values[j][i], value);
            }
        }
    }

    @Test
    void rowMapView() {
        var view = map.rowMapView();
        for (int i = 0; i < SIZE; i++) {
            var colMap = view.get(rowKeys[i]);
            for (int j = 0; j < SIZE; j++) {
                var value = colMap.get(colKeys[j]);
                assertEquals(values[i][j], value);
            }
        }
    }

    @Test
    void put() {
        Map2D<String, String, String> m = Map2D.createInstance();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                m.put(rowKeys[i], colKeys[j], values[i][j]);
            }
        }
        assertEquals(SIZE * SIZE, m.size());
    }

    @Test
    void putToColumn() {
        Map2D<String, String, String> m = Map2D.createInstance();
        for (int i = 0; i < SIZE; i++) {
            Map<String, String> toPut = new HashMap<>();
            for (int j = 0; j < SIZE; j++) {
                toPut.put(rowKeys[j], values[i][j]);
            }
            m.putAllToColumn(toPut, colKeys[i]);
        }
        assertEquals(SIZE * SIZE, m.size());
    }

    @Test
    void putToRow() {
        Map2D<String, String, String> m = Map2D.createInstance();
        for (int i = 0; i < SIZE; i++) {
            Map<String, String> toPut = new HashMap<>();
            for (int j = 0; j < SIZE; j++) {
                toPut.put(colKeys[j], values[j][i]);
            }
            m.putAllToRow(toPut, rowKeys[i]);
        }
        assertEquals(SIZE * SIZE, m.size());
    }

    @Test
    void putAll() {
        Map2D<String, String, String> m = Map2D.createInstance();
        m.putAll(map);
        assertEquals(SIZE * SIZE, m.size());
    }

}
