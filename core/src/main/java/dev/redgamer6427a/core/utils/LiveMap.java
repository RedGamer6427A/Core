package dev.redgamer6427a.core.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class LiveMap<K, V> implements Map<K, V> {


    /**
     * Calls the supplier.
     * @return an up-to-date map.
     */
    private Map<K, V> map(){
        return updateSupplier.get();
    }

    /**
     * Notify all PUT listeners
     * @param key the key.
     * @param value the value.
     */
    private void notifyPut(K key, V value) {
        for (MapChangeListener<K, V> listener : listeners) {
            listener.onPut(key, value);
        }

    }

    /**
     * Notify all REMOVE listeners
     * @param key the key.
     * @param oldValue the removed value.
     */
    private void notifyRemove(Object key, V oldValue) {
        for (MapChangeListener<K, V> listener : listeners) {
            listener.onRemove(key, oldValue);
        }

    }

    private final List<MapChangeListener<K, V>> listeners = new ArrayList<>();
    private final Supplier<Map<K, V>> updateSupplier;


    public LiveMap(Supplier<Map<K, V>> updateSupplier, MapChangeListener<K, V> listener) {
        this.updateSupplier = updateSupplier;
        this.listeners.add(listener);
    }

    public LiveMap(Supplier<Map<K, V>> updateSupplier) {
        this.updateSupplier = updateSupplier;
    }
    
    @Override
    public int size() {
        return map().size();
    }

    @Override
    public boolean isEmpty() {
        return map().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map().get(key);
    }

    @Override
    public @Nullable V put(K key, V value) {
        for (MapChangeListener<K, V> listener : listeners) {
            listener.onPut(key, value);
        }
        return map().put(key, value);
    }



    @Override
    public V remove(Object key) {
        V old = map().remove(key);
        if (old != null) notifyRemove(key, old);
        return old;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            notifyPut(entry.getKey(), entry.getValue());

        }
        map().putAll(m);

    }

    @Override
    public void clear() {
        for (Map.Entry<? extends K, ? extends V> entry : map().entrySet()) {
            notifyRemove(entry.getKey(), entry.getValue());
        }
        map().clear();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return map().keySet();
    }

    @Override
    public @NotNull Collection<V> values() {
        return map().values();
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return map().entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return map().getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {

        map().forEach(action);

    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            // ise thrown from function is not a cme.
            v = function.apply(k, v);

            try {
                entry.setValue(v);
                notifyPut(k, v);
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
    }

    @Override
    public @Nullable V putIfAbsent(K key, V value) {
        V old = get(key);
        if (old == null) {
            V result = map().put(key, value);

            return result;
        }
        return old;
    }


    @Override
    public boolean remove(Object key, Object value) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, value) ||
                (curValue == null && !containsKey(key))) {
            return false;
        }
        remove(key);


        return true;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) ||
                (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);

        return true;
    }

    @Override
    public @Nullable V replace(K key, V value) {
        V curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);

        }
        return curValue;
    }

    @Override
    public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v;
        if ((v = get(key)) == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                put(key, newValue);

                return newValue;
            }
        }

        return v;
    }

    @Override
    public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue;
        if ((oldValue = get(key)) != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);

                return newValue;
            } else {
                remove(key);

                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public V compute(K key, @NotNull BiFunction<? super K, ? super @Nullable V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = get(key);

        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            // delete mapping
            if (oldValue != null || containsKey(key)) {
                // something to remove
                remove(key);

                return null;
            } else {
                // nothing to do. Leave things as they were.
                return null;
            }
        } else {
            // add or replace old mapping
            put(key, newValue);

            return newValue;
        }
    }

    @Override
    public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value :
                remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            remove(key);

        } else {
            put(key, newValue);

        }
        return newValue;
    }

    public interface MapChangeListener<K, V> {
        void onPut(K key, V value);

        void onRemove(Object key, V oldValue);

    }
}
