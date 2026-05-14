interface KV {
    boolean setKey(String key, String value);
    String getValue(String key);
    boolean deleteKey(String key);
    int size();
    boolean rebuildStore();
}
