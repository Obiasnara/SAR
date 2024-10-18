package task4.abstracts;

public abstract class BrokerAbstract {
//	public Broker(String name) { };
	public abstract boolean accept(int port, AcceptListenerAbstract acl);
	public abstract boolean connect(String name, int port, ConnectListenerAbstract cnl);
}