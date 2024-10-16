package tests.task1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import task1.implems.Broker;
import task1.implems.BrokerManager;
import task1.implems.Channel;
import task3.implems.event_queue.errors.ConnectionRefused;

public class JUnitTests_Basic_BrokerChannel {
	
	public final static String LOREM_IPSUM = "\n" + //
            "\n" + //
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed id purus eu est viverra euismod ac sit amet est. Duis tincidunt aliquam condimentum. Donec a aliquam arcu, in gravida ligula. Quisque auctor nisi quam, vitae molestie risus consectetur in. Donec lacinia, velit cursus vulputate laoreet, ante magna consequat dolor, in ornare lectus lacus molestie est. Fusce in sodales nisl. Nullam a augue aliquet, accumsan nibh vitae, viverra nulla. Curabitur molestie libero id iaculis aliquet. Nulla facilisi. Integer ut pharetra est. Vestibulum molestie suscipit elementum. Sed interdum dui eros, in euismod erat suscipit eu.\n"
            + //
            "\n" + //
            "In sit amet accumsan justo. Quisque in nunc diam. Fusce non semper massa, non vestibulum mauris. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Vivamus rhoncus viverra tellus. Aliquam tincidunt augue tortor, id lacinia velit luctus in. Donec pretium ligula at posuere interdum. Nullam eros nisl, lacinia tincidunt mauris ac, sollicitudin aliquam ex. Morbi tempor tempor neque sed finibus. Vestibulum eget tortor sollicitudin, vestibulum ex elementum, tristique nunc. Ut congue lacinia lacus, vitae pretium mauris ultricies sit amet. Praesent faucibus venenatis tortor ac interdum. Quisque vel tincidunt neque. Ut eget efficitur massa.\n"
            + //
            "\n" + //
            "Nunc eget libero nec orci mollis convallis maximus et lectus. Donec ut malesuada mauris, at sollicitudin velit. Etiam et ex commodo, interdum enim et, faucibus arcu. Cras mollis mi massa, vitae efficitur mi ultrices vel. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi aliquam lectus vel sem pharetra facilisis. Nullam eget nisl at ex tempor suscipit. Vestibulum viverra orci id sapien luctus, nec faucibus lectus sagittis. Aenean maximus enim at dolor mollis, non tincidunt purus lacinia. Aliquam aliquam nisi felis. Curabitur elit arcu, fringilla non augue vitae, tempus ornare metus. Integer condimentum massa nec odio gravida scelerisque. Integer scelerisque vitae magna nec imperdiet.\n"
            + //
            "\n" + //
            "Suspendisse libero nisi, efficitur vel mauris vel, mollis fermentum diam. Nam accumsan lectus vitae tincidunt finibus. In tempus interdum arcu. Fusce dignissim venenatis ante id scelerisque. Integer finibus tristique lectus in feugiat. In sed sollicitudin metus, in tempor ligula. In commodo, nisi ut pellentesque convallis, arcu ligula ultricies metus, in faucibus odio quam non nisl. In nunc enim, scelerisque sollicitudin mattis id, ullamcorper nec dui. Mauris gravida mollis neque vitae facilisis. Donec nunc mi, condimentum nec metus in, congue hendrerit magna. Cras nunc eros, porttitor ut turpis in, hendrerit congue diam. Etiam id bibendum sem. Donec tempus erat at arcu maximus, id consequat nibh mattis. Cras diam orci, interdum at accumsan ut, fringilla vitae justo. Sed at efficitur eros, in volutpat odio.\n"
            + //
            "\n" + //
            "Pellentesque ornare est at quam rutrum, efficitur vehicula nibh convallis. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam eleifend est viverra odio convallis eleifend. Vivamus nec ipsum vitae nisl efficitur fermentum sit amet nec nisl. Sed eget eros blandit, semper turpis sit amet, faucibus felis. Vestibulum molestie, lacus in blandit lacinia, nulla odio viverra mi, aliquam aliquam est turpis ullamcorper magna. Donec erat diam, vehicula a magna et, finibus commodo velit. Praesent tempus aliquet sem. Curabitur a scelerisque odio. ";

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

        byte[] buffer = new byte[messageSize];
        int bytesRead = 0;

        while (bytesRead < messageSize) {
            int response = channel.read(buffer, bytesRead, messageSize - bytesRead);

            if (response == -1) {
                return null;
            }

            bytesRead += response;
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

        
    }
	
	@BeforeAll
    static void init() {
    	
    }
	Broker broker;
	Broker otherBroker;
    @BeforeEach
    void setUp() {
        broker = new Broker("Broker1");
        otherBroker = new Broker("OtherBroker");
    }

    @AfterEach
    void tearDown() {
    	BrokerManager.getInstance().removeAllBrokers();
    }
    
    
    @RepeatedTest(100)
    @DisplayName("Test connection block of the broker")
    void connectionTest() throws Exception {
        
    	// IMPORTANT : Leave it to 2 for the fail cases to wait for the first connection
        ExecutorService executor = Executors.newFixedThreadPool(2); 

        class Accept implements Runnable {
            int port;
            Broker broker;
            public Accept(Broker broker, int port) {
                this.port = port;
                this.broker = broker;
            }

            @Override
            public void run() {
                try {
                    Channel serverChannel = (Channel) broker.accept(port);
                    assertNotNull(serverChannel);
                } catch (Exception e) {
                    fail("Unexpected exception in Accept: " + e.getMessage());
                }
            }
        }

        class Connect implements Runnable {
            int port;
            Broker broker;
            String name;
            public Connect(Broker broker, int port, String name) {
                this.port = port;
                this.broker = broker;
                this.name = name;
            }

            @Override
            public void run() {
                try {
                    Channel serverChannel = (Channel) broker.connect(name, port);
                    assertNotNull(serverChannel);
                } catch (ConnectionRefused | InterruptedException e) {
                    fail("This connect shouldn't fail: " + e);
                }
            }
        }

        class FailConnect implements Runnable {
            int port;
            Broker broker;
            String name;
            public FailConnect(Broker broker, int port, String name) {
                this.port = port;
                this.broker = broker;
                this.name = name;
            }

            @Override
            public void run() {
                try {
                    broker.connect(name, port);
                    System.err.println("FailConnect connected ?!?");
                    fail("Expected ConnectionRefused but no exception was thrown");
                } catch (ConnectionRefused e) {
                    // Expected exception, test passes
                } catch (Exception e) {
                    fail("Unexpected exception in FailConnect: " + e.getClass().getSimpleName());
                }
            }
        }

        // Two valid tasks executed
        Future<?> serverFuture = executor.submit(new Accept(broker, 8080));
        Future<?> clientFuture = executor.submit(new Connect(broker, 8080, "Broker1"));

        // Two valid tasks executed
        Future<?> serverFuture2 = executor.submit(new Accept(broker, 20));
        Future<?> clientFuture2 = executor.submit(new Connect(broker, 20, "Broker1"));
        
        // Two valid tasks executed
        Future<?> serverFuture3 = executor.submit(new Accept(broker, 40));
        Future<?> clientFuture3 = executor.submit(new Connect(otherBroker, 40, "Broker1"));
        
        
        //  Then fail cases
        Future<?> failFuture1 = executor.submit(new FailConnect(broker, 8080, "Broker1"));
        Future<?> failFuture2 = executor.submit(new FailConnect(broker, 8080, "UnknownBroker"));

        // This will re-throw any exceptions from the threads
        try {
            serverFuture.get(); 
            clientFuture.get();
            
            serverFuture2.get();
            clientFuture2.get();
            
            serverFuture3.get();
            clientFuture3.get();
            
            failFuture1.get();
            failFuture2.get();
        } catch (ExecutionException e) {
            // If any task threw an exception, it's wrapped in an ExecutionException
        	System.out.println(e.getCause());
            throw (Exception) e.getCause(); // Re-throw the original exception
        } finally {
            executor.shutdown(); // Always clean up the executor
        }
    }
    
    protected class EchoServer implements Runnable {
        protected Broker broker;
        protected boolean isAccept;
        protected String brokerName;
        protected int port;

        public EchoServer(Broker broker, boolean isAccept, String brokerName, int port) {
            this.broker = broker;
            this.isAccept = isAccept;
            this.brokerName = brokerName;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Channel serverChannel;
                int nbMessages = 0;
                if (isAccept) {
                    serverChannel = (Channel) this.broker.accept(this.port);
                    while (nbMessages < 10) {
                        byte[] buffer = readSizeAndMessage(serverChannel);

                        writeSizeAndMessage(serverChannel, buffer);

                        nbMessages++;
                    }

                } else {
                    serverChannel = (Channel) this.broker.connect(this.brokerName, this.port);
                    while (nbMessages < 10) {
                        String message = brokerName + " message number " + nbMessages;
                        writeSizeAndMessage(serverChannel, message.getBytes());

                        byte[] echoBuffer = readSizeAndMessage(serverChannel);
                        
                        assertEquals(new String(echoBuffer), message);
                        
                        nbMessages++;
                    }
                }

                serverChannel.disconnect();
            } catch (Exception e) {
                fail("Unexpected exception in EchoServer: " + e.getMessage());
            }
        }
    }

    @RepeatedTest(100)
    @DisplayName("Multiple brokers test")
    public void multipleBrokers() throws Exception {
    	
    	int numberOfLoops = 100;
    	
        ArrayList<Broker> brokers = new ArrayList<>();
        ArrayList<Future<?>> futures = new ArrayList<>();

        // ExecutorService to handle tasks
        ExecutorService executor = Executors.newFixedThreadPool(numberOfLoops * 2); // large pool to accommodate many tasks

        for (int i = 0; i < numberOfLoops; i++) {
            Broker testBroker = new Broker("MultipleBroker" + i);
            brokers.add(testBroker);

            // Submit server and client tasks to the executor
            Future<?> serverFuture = executor.submit(new EchoServer(testBroker, true, "MultipleBroker" + i, i));
            Future<?> clientFuture = executor.submit(new EchoServer(broker, false, "MultipleBroker" + i, i));

            futures.add(serverFuture);
            futures.add(clientFuture);
        }

        // Wait for all tasks to complete and check for exceptions
        try {
            for (Future<?> future : futures) {
                future.get(); // This will throw ExecutionException if an exception occurred in the task
            }
        } catch (ExecutionException e) {
        	System.out.println(e.getCause());
            throw (Exception) e.getCause(); // Re-throw the original exception
        } finally {
            executor.shutdown(); // Clean up the executor
        }
    }
    
    @RepeatedTest(100)
    @DisplayName("One broker multiple clients test")
    public void oneBrokerMultipleClients() throws Exception {
    	
    	int numberOfLoops = 100;
    	
        Broker broker = new Broker("SingleBroker");

        ArrayList<Future<?>> futures = new ArrayList<>();

        // ExecutorService to handle tasks
        ExecutorService executor = Executors.newFixedThreadPool(numberOfLoops * 2); // large pool to accommodate many tasks

        for (int i = 0; i < numberOfLoops; i++) {
        
            // Submit server and client tasks to the executor
            Future<?> serverFuture = executor.submit(new EchoServer(broker, true, "SingleBroker", i));
            Future<?> clientFuture = executor.submit(new EchoServer(broker, false, "SingleBroker", i));

            futures.add(serverFuture);
            futures.add(clientFuture);
        }

        // Wait for all tasks to complete and check for exceptions
        try {
            for (Future<?> future : futures) {
                future.get(); // This will throw ExecutionException if an exception occurred in the task
            }
        } catch (ExecutionException e) {
        	System.out.println(e.getCause());
            throw (Exception) e.getCause(); // Re-throw the original exception
        } finally {
            executor.shutdown(); // Clean up the executor
        }
    }
    
    protected class DisconnectEchoServer implements Runnable {
        protected Broker broker;
        protected boolean isAccept;
        protected String brokerName;
        protected int port;

        public DisconnectEchoServer(Broker broker, boolean isAccept, String brokerName, int port) {
            this.broker = broker;
            this.isAccept = isAccept;
            this.brokerName = brokerName;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Channel serverChannel;
                int nbMessages = 0;
                if (isAccept) {
                    serverChannel = (Channel) this.broker.accept(this.port);
                    while (nbMessages < 10) {
                        byte[] buffer = readSizeAndMessage(serverChannel);

                        writeSizeAndMessage(serverChannel, buffer);
                        
                        nbMessages++;
                    }
                    serverChannel.disconnect();
                    try {
                    	writeSizeAndMessage(serverChannel, "ShouldNotWork".getBytes());
                    	fail("This action should not be possible");
                    } catch (IllegalStateException e) {
                    	// Do nothing, this behavior is expected
                    }
                    
                } else {
                    serverChannel = (Channel) this.broker.connect(this.brokerName, this.port);
                    while (nbMessages < 10) {
                        String message = brokerName + " message number " + nbMessages;
                        writeSizeAndMessage(serverChannel, message.getBytes());
                        
                        byte[] echoBuffer = readSizeAndMessage(serverChannel);
                        
                        assertEquals(new String(echoBuffer), message);
                        
                        nbMessages++;
                    }
                    
                }
            } catch (Exception e) {
                fail("Unexpected exception in EchoServer: " + e.getMessage());
            }
        }
    }
    
    @RepeatedTest(100)
    @DisplayName("One broker channel disconnect propagation test")
    public void oneBrokerDisconnectClients() throws Exception {
    	
    	int numberOfLoops = 100;
    	
        Broker broker = new Broker("SingleBroker");

        ArrayList<Future<?>> futures = new ArrayList<>();

        // ExecutorService to handle tasks
        ExecutorService executor = Executors.newFixedThreadPool(numberOfLoops * 2); // large pool to accommodate many tasks

        for (int i = 0; i < numberOfLoops; i++) {
        
            // Submit server and client tasks to the executor
            Future<?> serverFuture = executor.submit(new DisconnectEchoServer(broker, true, "SingleBroker", i));
            Future<?> clientFuture = executor.submit(new DisconnectEchoServer(broker, false, "SingleBroker", i));

            futures.add(serverFuture);
            futures.add(clientFuture);
        }

        // Wait for all tasks to complete and check for exceptions
        try {
            for (Future<?> future : futures) {
                future.get(); // This will throw ExecutionException if an exception occurred in the task
            }
        } catch (ExecutionException e) {
        	System.out.println(e.getCause());
            throw (Exception) e.getCause(); // Re-throw the original exception
        } finally {
            executor.shutdown(); // Clean up the executor
        }
    }

}

