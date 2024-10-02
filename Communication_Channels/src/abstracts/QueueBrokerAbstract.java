package abstracts;

public abstract class QueueBrokerAbstract extends BaseBrokerAbstract {
    //	public QueueBroker(String name) { };
	public abstract QueueChannelAbstract accept(int port);
    public abstract QueueChannelAbstract connect(String name, int port);
    public abstract String name();
}
