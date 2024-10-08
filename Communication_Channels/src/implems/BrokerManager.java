package implems;

import abstracts.BrokerAbstract;
import java.util.HashMap;

public class BrokerManager {
    // Singleton
    private static final BrokerManager instance;

    // Map enabling the association of the broker name with the broker object
    private final HashMap<String, BrokerAbstract> brokers;

    // Runs on class loading from the JVM
    static {
        instance = new BrokerManager();
    }

    private BrokerManager() {
        this.brokers = new HashMap<>();
    }

    public static BrokerManager getInstance() {
        return instance;
    }

    public synchronized void addBroker(String name, BrokerAbstract broker) {
        BrokerAbstract existingBroker = brokers.get(name);
        if (existingBroker != null) {
            throw new IllegalArgumentException("Broker with name " + name + " already exists");
        }
        brokers.put(name, broker);
    }

    public synchronized BrokerAbstract getBroker(String name) {
        return brokers.get(name);
    }

    public synchronized BrokerAbstract removeBroker(String name) {
        return brokers.remove(name);
    }
}
