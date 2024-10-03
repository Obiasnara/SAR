package abstracts;

public abstract class BrokerAbstract {
//	public Broker(String name) { };
	public abstract ChannelAbstract accept(int port) throws InterruptedException;
	public abstract ChannelAbstract connect(String name, int port) throws InterruptedException;
}