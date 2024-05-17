import java.util.HashMap;

public class DNSCache {
    HashMap<DNSQuestion, DNSRecord> cacheData = new HashMap<>();

    DNSRecord queryCache(DNSQuestion question) {
        DNSRecord cachedRecord = cacheData.get(question);

        if (cachedRecord != null && !cachedRecord.isExpired()) {
            cacheData.remove(question);
            return null;
        }

        return cachedRecord;
    }

    void insertRecord(DNSQuestion question, DNSRecord record) {
        cacheData.put(question, record);
    }
}
