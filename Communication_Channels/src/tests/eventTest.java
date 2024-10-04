package tests;

import abstracts.event_queue.QueueBrokerAbstract.AcceptListener;
import abstracts.event_queue.QueueBrokerAbstract.ConnectListener;
import abstracts.event_queue.QueueChannelAbstract;
import abstracts.event_queue.QueueChannelAbstract.Listener;
import abstracts.event_queue.TaskAbstract;
import implems.event_queue.*;

public class eventTest {

	protected QueueBroker queueBroker;
	
	public static void main(String[] args) {
		EventPump.getInstance().start();
		eventTest evt = new eventTest();
		evt.test1();
	}
	
	protected void test1() {
		QueueBroker queuBroker = new QueueBroker("Broker1");
		
		TaskAbstract t1 = Task.task();
		TaskAbstract t2 = Task.task();
		
		Runnable rAcc = new Runnable() {
			
			QueueChannelAbstract qca;
			
			Listener msgLiss = new Listener() {
				
				@Override
				public void sent(Message msg) {
					System.out.println("Message sent");
				}
				
				@Override
				public void received(byte[] msg) {
					System.out.println("Received " + new String(msg));
					Message msge = new Message(msg);
					qca.send(msge);
				}
				
				@Override
				public void closed() {
					// TODO Auto-generated method stub
					
				}
			};
			
			AcceptListener ls = new AcceptListener() {
				
				@Override
				public void accepted(QueueChannelAbstract queue) {
					System.out.println("Accepted");
					qca = queue;
					queue.setListener(msgLiss);
					Message m = new Message("StringTest".getBytes());
					queue.send(m);
				}
			};
			
			@Override
			public void run() {
				queuBroker.bind(8080, ls);
			}
		};
		
		Runnable rConn = new Runnable() {
			
			QueueChannelAbstract qca;
			
			Listener msgLiss = new Listener() {
				
				@Override
				public void sent(Message msg) {
					// TODO Auto-generated method stub
					System.out.println("Message sent");
				}
				
				@Override
				public void received(byte[] msg) {
					System.out.println("Received " + new String(msg));
					Message msge = new Message(msg);
					qca.send(msge);
				}
				
				@Override
				public void closed() {
					// TODO Auto-generated method stub
					
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
					qca = queue;
					qca.setListener(msgLiss);
				}
			};
			
			@Override
			public void run() {
				queuBroker.connect("Broker1", 8080, cn);
			}
		};
		
		t1.post(rConn);
		t2.post(rAcc);
		
		
	}

}
