package task1.abstracts;

import task3.implems.event_queue.errors.ConnectionRefused;
import task4.abstracts.AcceptListenerAbstract;
import task4.abstracts.ConnectListenerAbstract;

public abstract class BrokerAbstract {
//	public Broker(String name) { };
	public abstract ChannelAbstract accept(int port) throws InterruptedException, ConnectionRefused;
	public abstract ChannelAbstract connect(String name, int port) throws InterruptedException, ConnectionRefused;
}