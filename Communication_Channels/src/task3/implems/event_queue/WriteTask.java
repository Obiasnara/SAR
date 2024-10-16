package task3.implems.event_queue;

import task3.abstracts.event_queue.QueueChannelAbstract;

public class WriteTask implements Runnable {
	
	QueueChannelAbstract queue;
	Message msg;
	
	public WriteTask(QueueChannelAbstract queue, Message msg) {
		this.queue = queue;
		this.msg = msg;
	}

	@Override
	public void run() {
		if (!this.queue.send(msg) && !this.queue.closed()) Task.task().post(this);
	}
}
