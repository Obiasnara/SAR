package tests;

import abstracts.thread_queue.QueueBrokerAbstract;
import abstracts.thread_queue.QueueChannelAbstract;
import implems.Channel;
import implems.thread_queue.QueueBroker;
import implems.thread_queue.Task;

import java.nio.charset.StandardCharsets;

public class queueTests {
	    
    private void validate(byte[] echoBuffer, String message) {
        if(echoBuffer == null) System.err.println("echoBuffer is null, but it shouldn't be.");
        if(!new String(echoBuffer, 0, echoBuffer.length).equals(message)) System.err.println("Message content mismatch.");
    }

    protected static Boolean VERBOSE = false;
    public static void main(String[] args) {
        // Create a new test object
        queueTests test = new queueTests();
        // Run the test
        test.test1();
        
        System.out.println("Program ended");
    }

    protected class EchoServer implements Runnable {
        protected QueueBrokerAbstract broker;
        protected boolean isAccept;
        protected String brokerName;
        protected int port;

        public EchoServer(QueueBrokerAbstract broker2, boolean isAccept, String brokerName, int port) {
            this.broker = broker2;
            this.isAccept = isAccept;
            this.brokerName = brokerName;
            this.port = port;
        }
        @Override
        public void run() {
            String msg = "Message super long de la mort qui tue, bonjour au revoir, c'est qui est à l'appareil ? Bonsoir jhonatan tu as gagné un gâteau.";
                    	
            try {
            	QueueChannelAbstract serverChannel;
                int nbMessages = 0;
                if (isAccept) {
                    serverChannel = (QueueChannelAbstract) broker.accept(this.port);
                
                    while (nbMessages < 10) {
                    	
                    	byte[] recept = serverChannel.receive();
                    	
                        validate(recept, msg);

                    	if (VERBOSE) {
                            System.out.println("Read message: " + new String(recept, 0, recept.length));
                        }
                    	
                    	serverChannel.send(recept, 0, recept.length);
                    	
                        nbMessages++;
                    }

                } else {
                    serverChannel = (QueueChannelAbstract) broker.connect(this.brokerName, this.port);

                    while (nbMessages < 10) {
                    	
                    	serverChannel.send(msg.getBytes(), 0, msg.getBytes().length);
                    	
                    	byte[] recept = serverChannel.receive();
                        
                        validate(recept, msg);

                    	if (VERBOSE) {
                            System.out.println("Read message: " + new String(recept, 0, recept.length));
                        }
                    	
                        nbMessages++;
                    }
                }
                
                serverChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }

    public void test1() {
        QueueBrokerAbstract broker = new QueueBroker("BrokerT1");

        EchoServer ecServ1 = new EchoServer(broker, true, "BrokerT1", 8080);
        EchoServer ecServ2 = new EchoServer(broker, false, "BrokerT1", 8080);
        
        Task serverTask = new Task(broker, ecServ1);
        Task clientTask = new Task(broker, ecServ2);
        
        try {
        	serverTask.join();
        	clientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
