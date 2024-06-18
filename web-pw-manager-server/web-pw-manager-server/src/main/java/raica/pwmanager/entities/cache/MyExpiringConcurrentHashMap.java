package raica.pwmanager.entities.cache;


import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 自己做的資料結構，基底為ConcurrentHashMap，並為每個鍵值對加上時效性。
 */
public class MyExpiringConcurrentHashMap<K, V> {

    private final ConcurrentHashMap<K, ValueWithTimestamp<V>> dataContainer = new ConcurrentHashMap<>();

    private final long entryExpirationTimeInMillis;

    /**
     * @param entryExpirationTimeInMillis 每個鍵值對的存活時間，精度毫秒的時間戳。
     */
    public MyExpiringConcurrentHashMap(long entryExpirationTimeInMillis) {
        this.entryExpirationTimeInMillis = entryExpirationTimeInMillis;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::removeExpiredEntries, entryExpirationTimeInMillis, entryExpirationTimeInMillis, TimeUnit.MILLISECONDS);
    }

    public void put(K key, V value) {
        dataContainer.put(key, new ValueWithTimestamp<>(value));
    }

    /**
     * @return 若存在對應的entry，則回傳有值的Optional；反之則回傳空Optional。
     */
    public Optional<V> getOpt(K key) {
        ValueWithTimestamp<V> valueWithTimestamp = dataContainer.get(key);

        if (valueWithTimestamp == null) {
            return Optional.empty();
        }

        return Optional.of(valueWithTimestamp.value);
    }

    /**
     * @return 如果移除成功，回傳True；若不存在對應的entry，則回傳False。
     */
    public boolean removeIfPresent(K key) {
        return dataContainer.remove(key) != null;
    }

    public int size() {
        return dataContainer.size();
    }


    private void removeExpiredEntries() {
        long now = Instant.now().toEpochMilli();

        dataContainer.forEach(((key, valueWithTimestamp) -> {
            if (now - valueWithTimestamp.timestamp >= this.entryExpirationTimeInMillis) {
                dataContainer.remove(key);
            }
        }));
    }

    /**
     * 每個鍵值對都有過期時間，此物件即為紀錄一個entry被put的當下時間戳，資料結構有排程定期去掃描，移除過期的entry。
     */
    private static class ValueWithTimestamp<V> {

        private final V value;

        private final long timestamp; //型別不用Instant，節省記憶體

        ValueWithTimestamp(V value) {
            this.value = value;
            this.timestamp = Instant.now().toEpochMilli();
        }
    }


}
