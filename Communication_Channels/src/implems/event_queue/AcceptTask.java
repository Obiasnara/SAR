package implems.event_queue;

import abstracts.event_queue.QueueBrokerAbstract.AcceptListener;

public class AcceptTask implements Runnable {
	QueueBroker qb;
	int port; 
	AcceptListener cn;
	
	public AcceptTask(QueueBroker qb, int port, AcceptListener cn) {
		this.qb = qb;
		this.port = port;
		this.cn = cn;
	}
	
	@Override
	public void run() {
		qb.bind(port, cn);
	}
}
