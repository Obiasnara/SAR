package task3.implems.event_queue;

import java.util.concurrent.atomic.AtomicBoolean;

import task1.abstracts.ChannelAbstract;
import task1.implems.Broker;
import task1.implems.BrokerManager;
import task3.abstracts.event_queue.QueueBrokerAbstract;
import task3.abstracts.event_queue.QueueChannelAbstract;
import task3.implems.event_queue.Task;
import task3.implems.event_queue.errors.ConnectionRefused;

public class QueueBroker extends QueueBrokerAbstract {

	String name;
	Broker br;
	
	public QueueBroker(String name) {
		this.br = new Broker(name);
		this.name = name;
	}
	
	@Override
	public boolean bind(int port, AcceptListener listener) {
		AtomicBoolean success = new AtomicBoolean(true);  // Will hold the result
		
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
					
				} catch (ConnectionRefused | InterruptedException e) {
					// Silently drop
					success.set(false);
				} 
			}
		};
		new task2.implems.thread_queue.Task(this.br, goToThreadedWorld);
		return success.get();
	}

	@Override
	public boolean unbind(int port) {
		this.br.requestList.remove(port); 
		return true;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) {
		if(listener == null) throw new IllegalStateException("Connect listener can't be null");
		if (!BrokerManager.getInstance().brokerExists(name)) {
			listener.refused();
			return false;
		} 
		AtomicBoolean success = new AtomicBoolean(true);  // Will hold the result
		
		Runnable goToThreadedWorld = new Runnable() {
			@Override
		    public void run() {
				
				ChannelAbstract ch;
				try {
					ch = br.connect(name, port);
					QueueChannelAbstract queueChannel = new QueueChannel(ch);
					// Go back to event pump thread
					Task.task().post(new Runnable() {	
						@Override
						public void run() {
							listener.connected(queueChannel);
						}
					});
				} catch (ConnectionRefused | InterruptedException e) {
					success.set(false);
					Task.task().post(new Runnable() {	
						@Override
						public void run() {
							listener.refused();
						}
					});
				}
			}
		};
		new task2.implems.thread_queue.Task(this.br, goToThreadedWorld);
		return success.get();
	}

}
