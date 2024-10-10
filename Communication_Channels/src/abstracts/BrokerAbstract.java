package abstracts;

import implems.event_queue.errors.ConnectionRefused;

public abstract class BrokerAbstract {
//	public Broker(String name) { };
	public abstract ChannelAbstract accept(int port) throws InterruptedException, ConnectionRefused;
	public abstract ChannelAbstract connect(String name, int port) throws InterruptedException, ConnectionRefused;
}