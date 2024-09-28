package tests;

import abstracts.BaseBrokerAbstract;
import abstracts.ChannelAbstract;
import abstracts.QueueBrokerAbstract;
import abstracts.QueueChannelAbstract;
import implems.Broker;
import implems.Channel;
import implems.QueueBroker;
import implems.Task;

public class queueTests {
	
	public static byte[] getMessageSize(int size) {
        byte[] sizeBytes = new byte[4];
        sizeBytes[0] = (byte) (size >> 24);
        sizeBytes[1] = (byte) (size >> 16);
        sizeBytes[2] = (byte) (size >> 8);
        sizeBytes[3] = (byte) size;
        return sizeBytes;
    }

    public static int getSizeFromMessage(byte[] sizeBytes) {
        return (sizeBytes[0] << 24) | (sizeBytes[1] << 16) | (sizeBytes[2] << 8) | sizeBytes[3];
    }

    protected static Boolean VERBOSE = true;
    public static void main(String[] args) {
        // Create a new test object
        queueTests test = new queueTests();
        // Run the test
        test.test1();
    }

    protected class EchoServer implements Runnable {
        protected BaseBrokerAbstract broker;
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
            try {
            	QueueChannelAbstract serverChannel;
                int nbMessages = 0;
                if (isAccept) {
                    serverChannel = (QueueChannelAbstract) broker.accept(this.port);
                
                    while (nbMessages < 10) {
                    	
                    	byte[] recept = serverChannel.receive();
                    	
                    	if (VERBOSE) {
                            System.out.println("Read message: " + new String(recept, 0, recept.length));
                        }
                    	
                    	serverChannel.send(recept, 0, recept.length);
                    	
                        nbMessages++;
                    }

                } else {
                    serverChannel = (QueueChannelAbstract) broker.connect(this.brokerName, this.port);

                    while (nbMessages < 10) {
                    	
                    	String msg = "Message super long de la mort qui tue, bonjour au revoir, c'est qio à l'appareil ? Bonsoir jhonatan tu as gagné un gâteau." + nbMessages;
                    	
                    	serverChannel.send(msg.getBytes(), 0, msg.getBytes().length);
                    	
                    	byte[] recept = serverChannel.receive();
                        
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
        QueueBrokerAbstract broker = new QueueBroker("Broker1");

        EchoServer ecServ1 = new EchoServer(broker, true, "Broker1", 8080);
        EchoServer ecServ2 = new EchoServer(broker, false, "Broker1", 8080);
        
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
