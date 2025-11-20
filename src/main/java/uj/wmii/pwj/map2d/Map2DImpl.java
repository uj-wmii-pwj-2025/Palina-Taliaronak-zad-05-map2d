package uj.wmii.pwj.map2d;

import java.util.*;
import java.util.function.Function;

public class Map2DImpl<R, C, V> implements Map2D<R, C, V> {

    private final Map<R, Map<C, V>> data;
    private int size;

    public Map2DImpl() {
        this.data = new HashMap<>();
        this.size = 0;
    }

    @Override
    public V put(R rowKey, C columnKey, V value) {
        if (rowKey == null || columnKey == null) {
            throw new NullPointerException("Row key and column key cannot be null");
        }

        Map<C, V> row = data.computeIfAbsent(rowKey, k -> new HashMap<>());
        V oldValue = row.put(columnKey, value);

        if (oldValue == null) {
            size++;
        }

        return oldValue;
    }

    @Override
    public V get(R rowKey, C columnKey) {
        Map<C, V> row = data.get(rowKey);
        return row != null ? row.get(columnKey) : null;
    }

    @Override
    public V getOrDefault(R rowKey, C columnKey, V defaultValue) {
        V value = get(rowKey, columnKey);
        return value != null ? value : defaultValue;
    }

    @Override
    public V remove(R rowKey, C columnKey) {
        Map<C, V> row = data.get(rowKey);
        if (row == null) {
            return null;
        }

        V removedValue = row.remove(columnKey);
        if (removedValue != null) {
            size--;
            if (row.isEmpty()) {
                data.remove(rowKey);
            }
        }

        return removedValue;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean nonEmpty() {
        return size > 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        data.clear();
        size = 0;
    }

    @Override
    public Map<C, V> rowView(R rowKey) {
        Map<C, V> row = data.get(rowKey);
        if (row == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<>(row));
    }

    @Override
    public Map<R, V> columnView(C columnKey) {
        Map<R, V> column = new HashMap<>();
        for (Map.Entry<R, Map<C, V>> rowEntry : data.entrySet()) {
            V value = rowEntry.getValue().get(columnKey);
            if (value != null) {
                column.put(rowEntry.getKey(), value);
            }
        }
        return Collections.unmodifiableMap(column);
    }

    @Override
    public boolean containsValue(V value) {
        for (Map<C, V> row : data.values()) {
            if (row.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(R rowKey, C columnKey) {
        Map<C, V> row = data.get(rowKey);
        return row != null && row.containsKey(columnKey);
    }

    @Override
    public boolean containsRow(R rowKey) {
        return data.containsKey(rowKey);
    }

    @Override
    public boolean containsColumn(C columnKey) {
        for (Map<C, V> row : data.values()) {
            if (row.containsKey(columnKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<R, Map<C, V>> rowMapView() {
        Map<R, Map<C, V>> result = new HashMap<>();
        for (Map.Entry<R, Map<C, V>> entry : data.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<C, Map<R, V>> columnMapView() {
        Map<C, Map<R, V>> result = new HashMap<>();

        for (Map.Entry<R, Map<C, V>> rowEntry : data.entrySet()) {
            R rowKey = rowEntry.getKey();
            for (Map.Entry<C, V> colEntry : rowEntry.getValue().entrySet()) {
                C colKey = colEntry.getKey();
                result.computeIfAbsent(colKey, k -> new HashMap<>())
                        .put(rowKey, colEntry.getValue());
            }
        }

        Map<C, Map<R, V>> unmodifiableResult = new HashMap<>();
        for (Map.Entry<C, Map<R, V>> entry : result.entrySet()) {
            unmodifiableResult.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
        }

        return Collections.unmodifiableMap(unmodifiableResult);
    }

    @Override
    public Map2D<R, C, V> fillMapFromRow(Map<? super C, ? super V> target, R rowKey) {
        Map<C, V> row = data.get(rowKey);
        if (row != null) {
            target.putAll(row);
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> fillMapFromColumn(Map<? super R, ? super V> target, C columnKey) {
        for (Map.Entry<R, Map<C, V>> rowEntry : data.entrySet()) {
            V value = rowEntry.getValue().get(columnKey);
            if (value != null) {
                target.put(rowEntry.getKey(), value);
            }
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> putAll(Map2D<? extends R, ? extends C, ? extends V> source) {
        Map<? extends R, ? extends Map<? extends C, ? extends V>> sourceRowMap = source.rowMapView();
        for (Map.Entry<? extends R, ? extends Map<? extends C, ? extends V>> rowEntry : sourceRowMap.entrySet()) {
            R rowKey = rowEntry.getKey();
            for (Map.Entry<? extends C, ? extends V> colEntry : rowEntry.getValue().entrySet()) {
                put(rowKey, colEntry.getKey(), colEntry.getValue());
            }
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToRow(Map<? extends C, ? extends V> source, R rowKey) {
        for (Map.Entry<? extends C, ? extends V> entry : source.entrySet()) {
            put(rowKey, entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public Map2D<R, C, V> putAllToColumn(Map<? extends R, ? extends V> source, C columnKey) {
        for (Map.Entry<? extends R, ? extends V> entry : source.entrySet()) {
            put(entry.getKey(), columnKey, entry.getValue());
        }
        return this;
    }

    @Override
    public <R2, C2, V2> Map2D<R2, C2, V2> copyWithConversion(
            Function<? super R, ? extends R2> rowFunction,
            Function<? super C, ? extends C2> columnFunction,
            Function<? super V, ? extends V2> valueFunction) {

        Map2D<R2, C2, V2> result = new Map2DImpl<>();

        for (Map.Entry<R, Map<C, V>> rowEntry : data.entrySet()) {
            R2 newRowKey = rowFunction.apply(rowEntry.getKey());
            for (Map.Entry<C, V> colEntry : rowEntry.getValue().entrySet()) {
                C2 newColKey = columnFunction.apply(colEntry.getKey());
                V2 newValue = valueFunction.apply(colEntry.getValue());
                result.put(newRowKey, newColKey, newValue);
            }
        }

        return result;
    }

    public static <R, C, V> Map2D<R, C, V> createInstance() {
        return new Map2DImpl<>();
    }
}