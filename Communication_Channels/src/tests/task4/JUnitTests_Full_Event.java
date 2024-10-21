package tests.task4;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import task4.abstracts.ChannelAbstract;
import task4.abstracts.TaskAbstract;
import task4.implems.Broker;
import task4.implems.Broker.AcceptListener;
import task4.implems.Broker.ConnectListener;
import task4.implems.BrokerManager;
import task4.implems.Channel.ReadListener;
import task4.implems.Channel.WriteListener;
import tests.external.Utils;
import task4.implems.EventPump;
import task4.implems.Task;

public class JUnitTests_Full_Event {
	
	protected Broker testBroker;
	
	@BeforeAll
	static void init(){
		EventPump.getInstance().start();
	}
	
	@BeforeEach
	void cleanUp() {
		BrokerManager.getInstance().removeAllBrokers();
		testBroker = new Broker("Broker1");
	}
	
	@RepeatedTest(10)
    @DisplayName("Basic connection accept and refused test")
	public void connectionTest() {
        try {
            TaskAbstract t1 = Task.task();
            TaskAbstract t2 = Task.task();
            
            // Countdown for accept, connect, and refused
            CountDownLatch latch = new CountDownLatch(3);
            
            // Use boolean flags to assert specific events
            final boolean[] acceptEvent = {false};
            final boolean[] connectEvent = {false};
            final boolean[] refusedEvent = {false};
            
            // Listener for connection refused and connected
            ConnectListener cl = new ConnectListener() {
                @Override
                public void refused() {
                    refusedEvent[0] = true; // Track that refuse was called
                    latch.countDown();      // Decrement latch
                }

                @Override
                public void connected(ChannelAbstract queue) {
                	connectEvent[0] = true; // Track that connect was called
                    latch.countDown();      // Decrement latch
                }
                public void disconnected() { fail("Sould not disconnect");}
            };
            
            // Listener for connection accepted
            AcceptListener ac = new AcceptListener() {
                @Override
                public void accepted(ChannelAbstract queue) {
                	acceptEvent[0] = true; // Track that accept was called
                    latch.countDown();     // Decrement latch
                }
            };
            
            // Post events to simulate accept, connect, and refused
            t1.post(() -> testBroker.accept(8080, ac), "Accept");
            t2.post(() -> testBroker.connect(8080, "Broker1", cl), "Connect1");
            t2.post(() -> testBroker.connect(8080, "Broker100", cl), "Connect2");
            
            // Wait for events to occur or timeout after 5 seconds
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            
            // Assert that all the events were completed successfully
            assertTrue(completed, "Not all events completed before the timeout.");
            assertTrue(acceptEvent[0], "The accept event was not triggered.");
            assertTrue(connectEvent[0], "The connect event was not triggered.");
            assertTrue(refusedEvent[0], "The refused event was not triggered.");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
	
	@ParameterizedTest
	// @ValueSource(ints = {100, 500, 1000, 5000, 10000, 20000, 30000, 50000, 100000, 200000, 300000, 500000, 1000000})
	@ValueSource(ints = {100, 500, 1000, 5000, 10000, 20000, 30000, 50000, 100000})
	@DisplayName("Multiple connect and accept test")
	public void connect_disconnect(int MAX_TRIES) {
	    long startTime = System.nanoTime(); // Start time tracking
	    
	    try {
	        TaskAbstract t1 = Task.task();
	        CountDownLatch latch = new CountDownLatch(MAX_TRIES * 2);
	        final boolean[] acceptEvent = new boolean[MAX_TRIES];
	        final boolean[] connectEvent = new boolean[MAX_TRIES];

	        ConnectListener cl = new ConnectListener() {
	            int nbConnect = 0;
	            @Override
	            public void refused() { fail("Should not be refused"); }

	            @Override
	            public void connected(ChannelAbstract queue) {
	                connectEvent[nbConnect++] = true;
	                latch.countDown();
	            }
	        };

	        AcceptListener ac = new AcceptListener() {
	            int nbAccept = 0;
	            @Override
	            public void accepted(ChannelAbstract queue) {
	                acceptEvent[nbAccept++] = true;
	                latch.countDown();
	            }
	        };

	        TaskAbstract t2 = Task.task();
	        for (int i = 0; i < MAX_TRIES; i++) {
	            t1.post(() -> testBroker.accept(8080, ac), "Accept");
	            t2.post(() -> testBroker.connect(8080, "Broker1", cl), "Connect: " + i);
	        }

	        latch.await();

	        // Assertions to check if events occurred
	        for (int i = 1; i < MAX_TRIES; i++) {
	            assertTrue(acceptEvent[0], "Accept event failed for attempt " + i);
	            assertTrue(connectEvent[i], "Connect event failed for attempt " + i);
	        }

	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	    } finally {
	        long endTime = System.nanoTime(); // End time tracking
	        long executionTime = endTime - startTime;
	        //System.out.println("Execution time for MAX_TRIES = " + MAX_TRIES + ": " + executionTime / 1_000_000 + " ms");
	    }
	}
	
	@RepeatedTest(10)
    @DisplayName("Basic read write on channel test")
	public void readWriteTest() {
        try {
            TaskAbstract t1 = Task.task();
            TaskAbstract t2 = Task.task();
            
            // Countdown for accept, connect
            CountDownLatch latch = new CountDownLatch(4);
            
            // Use boolean flags to assert specific events
            final boolean[] acceptEvent = {false};
            final boolean[] connectEvent = {false};
            final boolean[] writeEvent = {false};
            final boolean[] readEvent = {false};
            
            WriteListener wl = new WriteListener() {
				
				@Override
				public void written(int byteWrote) {
					writeEvent[0] = true;
					latch.countDown();
				}
			};
            
            // Listener for connection refused and connected
            ConnectListener cl = new ConnectListener() {
                @Override
                public void connected(ChannelAbstract queue) {
                	connectEvent[0] = true; // Track that connect was called
                    latch.countDown();      // Decrement latch
                    
                    String stringMessage = "Hello world";
                    byte[] message = stringMessage.getBytes();
                    byte[] sizeBytes = Utils.getMessageSize(message.length);
                    
                    byte[] buffer = new byte[sizeBytes.length + message.length];
                    System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
                    System.arraycopy(message, 0, buffer, sizeBytes.length, message.length);
                    
                    queue.write(buffer, 0, buffer.length, wl);
                }
				@Override
				public void refused() {fail("Should not be refused");}
            };
            
            ReadListener rl = new ReadListener() {
				
				@Override
				public void read(byte[] bytes) {
					// TODO Auto-generated method stub
					readEvent[0] = true;
					latch.countDown();
				}
			};
            
            // Listener for connection accepted
            AcceptListener ac = new AcceptListener() {
                @Override
                public void accepted(ChannelAbstract queue) {
                	acceptEvent[0] = true; // Track that accept was called
                    latch.countDown();     // Decrement latch
                    
                    queue.setListener(rl);
                    
                    byte[] message_size = new byte[4];
                    
                    queue.read(message_size, 0, message_size.length);
                }
            };
            
            // Post events to simulate accept, connect, and refused
            t1.post(() -> testBroker.accept(8080, ac), "Accept");
            t2.post(() -> testBroker.connect(8080, "Broker1", cl), "Connect1");
            
            latch.await();
            
            assertTrue(acceptEvent[0], "The accept event was not triggered.");
            assertTrue(connectEvent[0], "The connect event was not triggered.");
            assertTrue(readEvent[0], "The read event was not triggered.");
            assertTrue(writeEvent[0], "The write event was not triggered.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
