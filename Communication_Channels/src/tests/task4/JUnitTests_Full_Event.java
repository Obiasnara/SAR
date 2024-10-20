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

import task4.abstracts.ChannelAbstract;
import task4.abstracts.TaskAbstract;
import task4.implems.Broker;
import task4.implems.Broker.AcceptListener;
import task4.implems.Broker.ConnectListener;
import task4.implems.BrokerManager;
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
	
	@RepeatedTest(1)
	@DisplayName("Multiple connect and disconnect test")
    public void connect_disconnect() {
		final int MAX_TRIES = 10000;
        try {
            TaskAbstract t1 = Task.task();
            
            
            // Countdown for accept, connect, and refused
            CountDownLatch latch = new CountDownLatch(MAX_TRIES * 3);
            
            // Use boolean flags to assert specific events
            final boolean[] acceptEvent = new boolean[MAX_TRIES];
            final boolean[] connectEvent = new boolean[MAX_TRIES];
            final boolean[] disconnectedEvent = new boolean[MAX_TRIES];
            
            
            // Listener for connection refused and connected
            ConnectListener cl = new ConnectListener() {
            	int nbConnect = 0;
            	int nbDisconnect = 0;
                @Override
                public void refused() { fail("Should not be refused");}

                @Override
                public void connected(ChannelAbstract queue) {
                	connectEvent[nbConnect++] = true; // Track that connect was called
                    latch.countDown();      // Decrement latch
                }
                
                @Override 
                public void disconnected() {
                	disconnectedEvent[nbDisconnect++] = true;
                	latch.countDown();
                }
            };
            
            // Listener for connection accepted
            AcceptListener ac = new AcceptListener() {
            	int nbAccept = 0;
                @Override
                public void accepted(ChannelAbstract queue) {
                	acceptEvent[nbAccept++] = true; // Track that accept was called
                    latch.countDown();     // Decrement latch
                }
            };
            
            // Post events to simulate accept, connect, and refused
            t1.post(() -> testBroker.accept(8080, ac), "Accept");
            
            // This loop simulates multiple clients connecting and disconnecting (one after the other due to one thread)
            for(int i = 0 ; i < MAX_TRIES ; i++) {
            	  TaskAbstract t2 = Task.task();
            	  t2.post(() -> testBroker.connect(8080, "Broker1", cl), "Connect : " + i);
                  t2.post(() -> testBroker.disconnect(8080, "Broker1", cl), "Disconnect : " + i);
            }
            
            latch.await();
            // Wait for events to occur or timeout after 5 seconds
            //boolean completed = latch.await(5, TimeUnit.SECONDS);
            
            // Assert that all the events were completed successfully
            //assertTrue(completed, "Not all events completed before the timeout.");
            
            
            for (int i = 1; i < MAX_TRIES; i++) {
            	assertTrue(acceptEvent[0], "Accept event failed for attempt " + i);
                assertTrue(connectEvent[i], "Connect event failed for attempt " + i);
                assertTrue(disconnectedEvent[i], "Disconnect event failed for attempt " + i);
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
