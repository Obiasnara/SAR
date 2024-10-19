package task4.abstracts;

import task4.implems.Broker.AcceptListener;
import task4.implems.Broker.ConnectListener;

public abstract class BrokerAbstract {
//	public Broker(String name) { };
	public abstract boolean accept(int port, AcceptListener acl);
	public abstract boolean connect(int port, String name, ConnectListener cnl);
	public abstract boolean disconnect(int port, String name, ConnectListener cnl);
}