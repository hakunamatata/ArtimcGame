package plugin.artimc.common;

import plugin.artimc.ArtimcPlugin;

import java.util.Set;

public interface IManager<K, T> {
    ArtimcPlugin getPlugin();

    T get(K key);

    boolean add(K key, T obj);

    boolean set(K key, T obj);

    boolean remove(K key);

    boolean contains(K key);

    int size();

    Set<T> list();


}
