package implems.event_queue;

import abstracts.ChannelAbstract;
import abstracts.event_queue.QueueBrokerAbstract;
import abstracts.event_queue.QueueChannelAbstract;
import implems.Broker;
import implems.BrokerManager;
import implems.Channel;

public class QueueBroker extends QueueBrokerAbstract {

	String name;
	Broker br;
	
	public QueueBroker(String name) {
		this.br = new Broker(name);
		this.name = name;
	}
	
	@Override
	public boolean bind(int port, AcceptListener listener) {
		Runnable goToThreadedWorld = new Runnable() {
			@Override
		    public void run() {
		
				try {
					ChannelAbstract ch = br.accept(port);
					QueueChannelAbstract queueChannel = new QueueChannel(ch);
					Task.task().post(new Runnable() {	
						@Override
						public void run() {
							listener.accepted(queueChannel);
						}
					});
					
				} catch (InterruptedException e) {
					System.out.println("Timed out");
				} 
			}
		};
		new implems.thread_queue.Task(this.br, goToThreadedWorld);
		return true;
	}

	@Override
	public boolean unbind(int port) {
		if(this.br.requestList.get(port).poll() == null) return false;
		return true;
		
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) {
		if (!BrokerManager.getInstance().brokerExists(name)) {
			listener.refused();
			return false;
		}
		Runnable goToThreadedWorld = new Runnable() {
			@Override
		    public void run() {
				try {
				ChannelAbstract ch = br.connect(name, port);
				QueueChannelAbstract queueChannel = new QueueChannel(ch);
				// Go back to event pump thread
				Task.task().post(new Runnable() {	
					@Override
					public void run() {
						listener.connected(queueChannel);
					}
				});
				} catch(InterruptedException e) {
					System.out.println("Timed out");
				}
			}
		};
		new implems.thread_queue.Task(this.br, goToThreadedWorld);
		return true;
	}

}
