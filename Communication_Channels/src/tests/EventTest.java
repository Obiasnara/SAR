package tests;

import task3.abstracts.event_queue.QueueChannelAbstract;
import task3.abstracts.event_queue.TaskAbstract;
import task3.abstracts.event_queue.QueueBrokerAbstract.AcceptListener;
import task3.abstracts.event_queue.QueueBrokerAbstract.ConnectListener;
import task3.abstracts.event_queue.QueueChannelAbstract.Listener;
import task3.implems.event_queue.*;

public class EventTest {

	protected QueueBroker queueBroker;
	
	public static void main(String[] args) {
		EventPump.getInstance().start();
		EventTest evt = new EventTest();
		evt.test1();
	}
	
	protected void test1() {
		QueueBroker queuBroker = new QueueBroker("Broker1");
		
		Task t1 = new Task();
		Task t2 = new Task();
	
		Listener lt1 = new Listener() {
			int max = 0;
			
			@Override
			public void received(byte[] msg) {
				if (t1.queue.closed()) return;
				// TODO Auto-generated method stub
				System.out.println("T1 Recieved : " + new String(msg));
				t1.post(new WriteTask(t1.queue, new Message(msg)));
				max++;	
				
				if(max == 10) {
					t1.queue.close();
				}
			}

			@Override
			public void sent(Message msg) {
				// TODO Auto-generated method stub
				System.out.println("T1 Sent");
			}

			@Override
			public void closed() {
				// TODO Auto-generated method stub
				System.out.println("Closed");
			}
			
		};

		Listener lt2 = new Listener() {
			
			
			@Override
			public void received(byte[] msg) {
				if (t2.queue.closed()) return;
				// TODO Auto-generated method stub
				System.out.println("T2 Recieved : " + new String(msg));
				t2.post(new WriteTask(t2.queue, new Message(msg)));
				
			}

			@Override
			public void sent(Message msg) {
				// TODO Auto-generated method stub
				System.out.println("T2 Sent");
			}

			@Override
			public void closed() {
				// TODO Auto-generated method stub
				System.out.println("Closed");
			}
			
		};
		
		ConnectListener cn = new ConnectListener() {
			@Override
			public void refused() {
				System.out.println("Refused");
			}
			
			@Override
			public void connected(QueueChannelAbstract queue) {
				System.out.println("Connected");
				Message msg = new Message("Salut".getBytes());
				t1.queue = (QueueChannel) queue;
				t1.queue.setListener(lt1);
				t1.post(new WriteTask(t1.queue, msg));
			}
		};
		
		AcceptListener al = new AcceptListener() {
			@Override
			public void accepted(QueueChannelAbstract queue) {
				System.out.println("Accepted");
				queue.setListener(lt2);
				t2.queue = (QueueChannel) queue;
			}
		};
		try {
		t1.post(new ConnectTask(queuBroker, "Broker1", 8080, cn));
		t2.post(new AcceptTask(queuBroker, 8080, al));
		} catch (IllegalStateException e) {
			System.out.println("Disconnected successfully");
		}
	}

}
