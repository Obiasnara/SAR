package tests;



import task1.implems.BrokerManager;
import task3.abstracts.event_queue.QueueChannelAbstract;
import task3.abstracts.event_queue.QueueBrokerAbstract.AcceptListener;
import task3.abstracts.event_queue.QueueBrokerAbstract.ConnectListener;
import task3.abstracts.event_queue.QueueChannelAbstract.Listener;
import task3.implems.event_queue.*;


public class ExtendedSuite {

	public static void main(String[] args) {
		try {
			ExtendedSuite e = new ExtendedSuite();
			EventPump.getInstance().start();
						
			e.testBasicConnection();
			BrokerManager.getInstance().removeAllBrokers();
				
			e.testChannelClosure();
			BrokerManager.getInstance().removeAllBrokers();
					
			e.testConnectionRefusal();
			BrokerManager.getInstance().removeAllBrokers();
						
			e.testMessageExchange();
			BrokerManager.getInstance().removeAllBrokers();
					
			e.testTaskKill();
			BrokerManager.getInstance().removeAllBrokers();
			
			EventPump.getInstance().join();
			System.out.println("Tests ended");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void testBasicConnection() {
	    QueueBroker broker = new QueueBroker("TestBroker");

	    Task task1 = new Task();
	    Task task2 = new Task();

	    Listener listener1 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 1 received: " + new String(msg));
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 1 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 1 closed");
	        }
	    };

	    Listener listener2 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 2 received: " + new String(msg));
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 2 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 2 closed");
	        }
	    };

	    AcceptListener acceptListener = new AcceptListener() {
	        @Override
	        public void accepted(QueueChannelAbstract queue) {
	            System.out.println("Connection accepted");
	            task2.queue = (QueueChannel) queue;
	            task2.queue.setListener(listener2);
	        }
	    };

	    ConnectListener connectListener = new ConnectListener() {
	        @Override
	        public void connected(QueueChannelAbstract queue) {
	            System.out.println("Connected to broker");
	            task1.queue = (QueueChannel) queue;
	            task1.queue.setListener(listener1);

	            // Send a message from task1 to task2
	            Message message = new Message("Hello from Task 1".getBytes());
	            task1.post(new WriteTask(task1.queue, message));
	        }

	        @Override
	        public void refused() {
	            System.out.println("Connection refused");
	        }
	    };

	    task1.post(new ConnectTask(broker, "TestBroker", 8080, connectListener));
	    task2.post(new AcceptTask(broker, 8080, acceptListener));


	}

	public void testConnectionRefusal() {
	    QueueBroker broker = new QueueBroker("TestBroker");

	    Task task1 = new Task();

	    ConnectListener connectListener = new ConnectListener() {
	        @Override
	        public void connected(QueueChannelAbstract queue) {
	            // We do not expect this to be called in this test
	            System.err.println("Should not connect");
	        }

	        @Override
	        public void refused() {
	            System.out.println("Connection refused as expected");
	        }
	    };

	    // We won't accept the connection on the other end
	    task1.post(new ConnectTask(broker, "NonExistingBroker", 8081, connectListener));

	    // Connection should be refused
	    if(task1.queue != null) System.err.println("A queue was created"); // Ensures no queue was created
	}
	
	public void testMessageExchange() {
	    QueueBroker broker = new QueueBroker("TestBroker");

	    Task task1 = new Task();
	    Task task2 = new Task();

	    Listener listener1 = new Listener() {
	        int receivedMessages = 0;

	        @Override
	        public void received(byte[] msg) {
	            receivedMessages++;
	            System.out.println("Task 1 received: " + new String(msg));
	            if (receivedMessages == 5) {
	                task1.queue.close();
	            }
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 1 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 1 closed");
	        }
	    };

	    Listener listener2 = new Listener() {
	        int receivedMessages = 0;

	        @Override
	        public void received(byte[] msg) {
	            receivedMessages++;
	            System.out.println("Task 2 received: " + new String(msg));
	            if (receivedMessages == 5) {
	                task2.queue.close();
	            }
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 2 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 2 closed");
	        }
	    };

	    AcceptListener acceptListener = new AcceptListener() {
	        @Override
	        public void accepted(QueueChannelAbstract queue) {
	            System.out.println("Task 2 accepted connection");
	            task2.queue = (QueueChannel) queue;
	            task2.queue.setListener(listener2);
	        }
	    };

	    ConnectListener connectListener = new ConnectListener() {
	        @Override
	        public void connected(QueueChannelAbstract queue) {
	            System.out.println("Task 1 connected to broker");
	            task1.queue = (QueueChannel) queue;
	            task1.queue.setListener(listener1);

	            // Send multiple messages
	            for (int i = 0; i < 5; i++) {
	                Message message = new Message(("Message " + i).getBytes());
	                task1.post(new WriteTask(task1.queue, message));
	            }
	        }

	        @Override
	        public void refused() {
	            System.err.println("Connection should not be refused");
	        }
	    };

	    task1.post(new ConnectTask(broker, "TestBroker", 8080, connectListener));
	    task2.post(new AcceptTask(broker, 8080, acceptListener));

	}
	
	public void testChannelClosure() {
	    QueueBroker broker = new QueueBroker("TestBroker");

	    Task task1 = new Task();
	    Task task2 = new Task();

	    Listener listener1 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 1 received: " + new String(msg));
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 1 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 1 closed");
	        }
	    };

	    Listener listener2 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 2 received: " + new String(msg));
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 2 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 2 closed");
	        }
	    };

	    AcceptListener acceptListener = new AcceptListener() {
	        @Override
	        public void accepted(QueueChannelAbstract queue) {
	            System.out.println("Task 2 accepted connection");
	            task2.queue = (QueueChannel) queue;
	            task2.queue.setListener(listener2);
	        }
	    };

	    ConnectListener connectListener = new ConnectListener() {
	        @Override
	        public void connected(QueueChannelAbstract queue) {
	            System.out.println("Task 1 connected to broker");
	            task1.queue = (QueueChannel) queue;
	            task1.queue.setListener(listener1);

	            // Close the connection
	            task1.queue.close();
	        }

	        @Override
	        public void refused() {
	            System.err.println("Connection should not be refused");
	        }
	    };

	    task1.post(new ConnectTask(broker, "TestBroker", 8080, connectListener));
	    task2.post(new AcceptTask(broker, 8080, acceptListener));

	}
	
	public void testTaskKill() {
	    QueueBroker broker = new QueueBroker("TestBroker");

	    Task task1 = new Task();
	    Task task2 = new Task();

	    Listener listener1 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 1 received: " + new String(msg));
	            task1.kill(); // Kill task after receiving a message
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 1 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 1 closed");
	        }
	    };

	    Listener listener2 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 2 received: " + new String(msg));
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 2 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 2 closed as a reaction to task 1 kill");
	        }
	    };

	    AcceptListener acceptListener = new AcceptListener() {
	        @Override
	        public void accepted(QueueChannelAbstract queue) {
	            System.out.println("Task 2 accepted connection");
	            task2.queue = (QueueChannel) queue;
	            task2.queue.setListener(listener2);
	        }
	    };

	    ConnectListener connectListener = new ConnectListener() {
	        @Override
	        public void connected(QueueChannelAbstract queue) {
	            System.out.println("Task 1 connected to broker");
	            task1.queue = (QueueChannel) queue;
	            task1.queue.setListener(listener1);

	            // Send a message from task1 to task2
	            Message message = new Message("Hello from Task 1".getBytes());
	            task1.post(new WriteTask(task1.queue, message));
	        }

	        @Override
	        public void refused() {
	            System.err.println("Connection should not be refused");
	        }
	    };

	    task1.post(new ConnectTask(broker, "TestBroker", 8080, connectListener));
	    task2.post(new AcceptTask(broker, 8080, acceptListener));

	}

	public void multipleConnectAndAccept() {
	    QueueBroker broker = new QueueBroker("TestBroker");

	    Task task1 = new Task();
	    Task task2 = new Task();

	    Listener listener1 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 1 received: " + new String(msg));
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 1 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 1 closed");
	        }
	    };

	    Listener listener2 = new Listener() {
	        @Override
	        public void received(byte[] msg) {
	            System.out.println("Task 2 received: " + new String(msg));
	        }

	        @Override
	        public void sent(Message msg) {
	            System.out.println("Task 2 sent: " + new String(msg.getData()));
	        }

	        @Override
	        public void closed() {
	            System.out.println("Task 2 closed as a reaction to task 1 kill");
	        }
	    };

	    AcceptListener acceptListener = new AcceptListener() {
	        @Override
	        public void accepted(QueueChannelAbstract queue) {
	            System.out.println("Task 2 accepted connection");
	        }
	    };

	    ConnectListener connectListener = new ConnectListener() {
	        @Override
	        public void connected(QueueChannelAbstract queue) {
	            System.out.println("Task 1 connected to broker");
	        }

	        @Override
	        public void refused() {
	            System.err.println("Connection should not be refused");
	        }
	    };

	    task1.post(new ConnectTask(broker, "TestBroker", 8080, connectListener));
	    task1.post(new ConnectTask(broker, "TestBroker", 8080, connectListener));
	    task1.post(new ConnectTask(broker, "TestBroker", 8080, connectListener));
	    task2.post(new AcceptTask(broker, 8080, acceptListener));
	    task2.post(new AcceptTask(broker, 8080, acceptListener));
	    task2.post(new AcceptTask(broker, 8080, acceptListener));

	}
	
}
