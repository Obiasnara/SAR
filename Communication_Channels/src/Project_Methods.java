//abstract class Broker {
//	Broker(String name);
//	Channel accept(int port);
//	Channel connect(String name, int port);
//}
//abstract class Channel {
//	int read(byte[] bytes, int offset, int length);
//	int write(byte[] bytes, int offset, int length);
//	void disconnect();
//	boolean disconnected();
//}
//abstract class Task extends Thread {
//	Task(Broker b, Runnable r);
//	static Broker getBroker();
//}
