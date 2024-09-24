package implems;

import abstracts.BrokerAbstract;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerManager {
    // Singleton
    private static BrokerManager instance = null;

    // Map enabling the association of the broker name with the broker object
    private ConcurrentHashMap<String, BrokerAbstract> brokers = new ConcurrentHashMap<String, BrokerAbstract>();

    private BrokerManager() {
    }

    public static BrokerManager getInstance() {
        if (instance == null) {
            instance = new BrokerManager();
        }
        return instance;
    }

    public void addBroker(String name, BrokerAbstract broker) {
        brokers.put(name, broker);
    }

    public BrokerAbstract getBroker(String name) {
        return brokers.get(name);
    }
}
