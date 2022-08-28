package plugin.artimc.common;

import plugin.artimc.ArtimcManager;
import plugin.artimc.ArtimcPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractManager<K, T> implements IManager<K, T> {
    private final ArtimcManager manager;
    private final Map<K, T> hashMap;

    public AbstractManager(ArtimcManager manager) {
        this.manager = manager;
        hashMap = new HashMap<>();
    }

    public ArtimcManager getManager() {
        return manager;
    }

    public ArtimcPlugin getPlugin() {
        return manager.getPlugin();
    }

    public T get(K key) {
        return hashMap.get(key);
    }

    public boolean add(K key, T obj) {
        return hashMap.putIfAbsent(key, obj) == null;
    }

    public boolean set(K key, T obj) {
        return hashMap.put(key, obj) != null;
    }

    public boolean remove(K key) {
        return hashMap.remove(key) != null;
    }

    public boolean contains(K key) {
        return hashMap.containsKey(key);
    }

    public int size() {
        return hashMap.size();
    }

    public Set<T> list() {
        return new HashSet<>(hashMap.values());
    }
}
