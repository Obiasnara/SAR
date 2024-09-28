package abstracts;

public abstract class BaseBrokerAbstract {
    // Abstract methods common to both BrokerAbstract and QueueBrokerAbstract
    public abstract Object accept(int port);  // Can be more specific if needed
    public abstract Object connect(String name, int port);
}
