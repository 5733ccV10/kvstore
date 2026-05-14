
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Main {

    static void testConcurrency(KV kvStore) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 10000000; i++) {
            int temp = i;
            String key = "user" + String.valueOf(temp); 
            Thread virtualThread = Thread.ofVirtual().start(() -> {
                String value = String.valueOf(temp);
                kvStore.setKey(key, value);
            });

            threads.add(virtualThread);
        }

        for (Thread t: threads) t.join();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Ready to go");
        KV kvStore = new KVImplementation(new WALFile());
        kvStore.rebuildStore();
        Scanner s = new Scanner(System.in);
        // Runtime rt = Runtime.getRuntime();
        // long usedBefore = rt.totalMemory() - rt.freeMemory();
        // long startTime = System.nanoTime();
        // testConcurrency(kvStore);
        // long endTime = System.nanoTime();
        // long usedAfter = rt.totalMemory() - rt.freeMemory();
        // System.out.println("Keys in store: " + kvStore.size() + " / 1000");
        // long totalTime = endTime - startTime;
        // long totalMemory = usedAfter - usedBefore;
        // System.out.println("Time taken: " + totalTime + " and memory: " + totalMemory);
        while (true) {
            System.out.println("Enter operation: SET <key> <value> | GET <key> | DELETE <key> | EXIT");
            String input = s.nextLine().trim();
            String[] parts = input.split(" ", 3);

            switch (parts[0].toUpperCase()) {
                case "SET" -> {
                    if (parts.length < 3) {
                        System.out.println("Usage: SET <key> <value>");
                        break;
                    }
                    boolean ok = kvStore.setKey(parts[1], parts[2]);
                    System.out.println(ok ? "OK" : "Failed — key or value cannot be blank");
                }
                case "GET" -> {
                    if (parts.length < 2) {
                        System.out.println("Usage: GET <key>");
                        break;
                    }
                    String value = kvStore.getValue(parts[1]);
                    System.out.println(value != null ? value : "NOT FOUND");
                }
                case "DELETE" -> {
                    if (parts.length < 2) {
                        System.out.println("Usage: DELETE <key>");
                        break;
                    }
                    boolean deleted = kvStore.deleteKey(parts[1]);
                    System.out.println(deleted ? "DELETED" : "NOT FOUND");
                }
                case "EXIT" -> {
                    System.out.println("Terminating");
                    return;
                }
                default -> System.out.println("Unknown command. Use SET, GET, DELETE or EXIT");
            }
        }
    }
}
