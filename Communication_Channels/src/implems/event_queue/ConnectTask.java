package implems.event_queue;

import abstracts.event_queue.QueueBrokerAbstract.ConnectListener;

public class ConnectTask implements Runnable {
	
	QueueBroker qb;
	String name; 
	int port; 
	ConnectListener cn;
	
	public ConnectTask(QueueBroker qb, String name, int port, ConnectListener cn) {
		this.qb = qb;
		this.name = name;
		this.port = port;
		this.cn = cn;
	}
	
	@Override
	public void run() {
		qb.connect(name, port, cn);
	}

}
