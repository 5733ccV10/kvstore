
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class KVImplementation implements KV {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final WALInterface walInterface;
    public KVImplementation(WALInterface wAlInterface) {
        this.walInterface = wAlInterface;
    }
    @Override
    public boolean setKey(String key, String value) {
        if (key == null || key.isBlank() || value == null || value.isBlank()) return false;
        if (!walInterface.writeIntoWAL("SET " + key + " " + value)) return false;
        store.put(key, value);
        return true;
    }

    @Override
    public String getValue(String key) {
        if (key == null || key.isBlank()) return null;
        return store.get(key);
    }

    @Override
    public boolean deleteKey(String key) {
        if (key == null || key.isBlank()) return false;
        if (!walInterface.writeIntoWAL("DELETE " + key)) return false;
        return store.remove(key) != null;
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public boolean rebuildStore() {
        try {
            List<String> logs = walInterface.replay();
            for (String log : logs) {
                String[] parts = log.split(" ", 3);
                switch (parts[0]) {
                    case "SET" -> store.put(parts[1], parts[2]);
                    case "DELETE" -> store.remove(parts[1]);
                    default -> System.err.println("Unknown WAL operation: " + parts[0]);
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Rebuild failed: " + e.getMessage());
        }
        return false;
    }
}
