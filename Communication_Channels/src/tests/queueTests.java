package tests;

import abstracts.BaseBrokerAbstract;
import abstracts.QueueBrokerAbstract;
import abstracts.QueueChannelAbstract;
import implems.Channel;
import implems.QueueBroker;
import implems.Task;
import java.nio.charset.StandardCharsets;

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

    public static int readMessageSize(Channel channel) {
        byte[] sizeBytes = new byte[4];
        // We need to use a While loop to make sure we read all 4 bytes
        int bytesRead = 0;
        int response = 0;
        while (bytesRead < 4) {
            response = channel.read(sizeBytes, bytesRead, 4 - bytesRead);
            if (response == -1) {
                return -1;
            }
            bytesRead += response;
        }
        return getSizeFromMessage(sizeBytes);
    }

    public static byte[] readSizeAndMessage(Channel channel) {
        int messageSize = readMessageSize(channel);
        if (messageSize <= 0) {
            return null;
        }
    
        // Log message size for debugging
        if (VERBOSE) {
            System.out.println("Expected message size: " + messageSize);
        }
    
        byte[] buffer = new byte[messageSize];
        int bytesRead = 0;
    
        while (bytesRead < messageSize) {
            int response = channel.read(buffer, bytesRead, messageSize - bytesRead);
            if (response == -1) {
                return null;
            }
    
            bytesRead += response;
        }
    
        if (VERBOSE) {
            System.out.println("Received message: " + new String(buffer, 0, buffer.length, StandardCharsets.UTF_8));
        }
    
        return buffer;
    }
    
    public static void writeSizeAndMessage(Channel channel, byte[] message) {
        byte[] sizeBytes = getMessageSize(message.length);
        byte[] buffer = new byte[sizeBytes.length + message.length];
        System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
        System.arraycopy(message, 0, buffer, sizeBytes.length, message.length);
    
        int bytesWritten = 0;
        while (bytesWritten < buffer.length) {
            int response = channel.write(buffer, bytesWritten, buffer.length - bytesWritten);
            if (response == -1) {
                return;
            }
            bytesWritten += response;
        }
    
        if (VERBOSE) {
            System.out.println("Sent message: " + new String(message, 0, message.length, StandardCharsets.UTF_8));
        }
    }
    

    private void validate(byte[] echoBuffer, String message) {
        if(echoBuffer == null) System.err.println("echoBuffer is null, but it shouldn't be.");
        if(!new String(echoBuffer, 0, echoBuffer.length).equals(message)) System.err.println("Message content mismatch.");
        if(echoBuffer.length != message.length()) System.err.println("Message length mismatch.");
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
