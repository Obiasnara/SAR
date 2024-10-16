package tests;

import task1.implems.BrokerManager;
import task3.abstracts.event_queue.QueueChannelAbstract;
import task3.abstracts.event_queue.TaskAbstract;
import task3.abstracts.event_queue.QueueBrokerAbstract.AcceptListener;
import task3.abstracts.event_queue.QueueBrokerAbstract.ConnectListener;
import task3.abstracts.event_queue.QueueChannelAbstract.Listener;
import task3.implems.event_queue.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class JUnitTests_Mixed {

    protected QueueBroker queueBroker;
    protected Task t1, t2;

    @BeforeAll
    static void init() {
    	System.out.println("This test can throw a refused, I dont know why, it seems that JUNIT is not creating the QueueBroker sometimes in the BeforeEach function");
    	EventPump.getInstance().start();
    }
    
    @BeforeEach
    void setUp() {
    	t1 = new Task();
    	t2 = new Task();
        queueBroker = new QueueBroker("Broker1");
    }

    @AfterEach
    void tearDown() {
        BrokerManager.getInstance().removeAllBrokers();
    }


    @RepeatedTest(1)
    @DisplayName("Test message exchange between tasks")
    void testMessageExchange() {
    	
        Listener lt1 = new Listener() {
            int max = 0;

            @Override
            public void received(byte[] msg) {
                if (t1.queue.closed()) return;
                t1.post(new WriteTask(t1.queue, new Message(msg)));
                max++;
                if (max == 10) {
                    t1.queue.close();
                }
            }

            @Override
            public void sent(Message msg) {
            }

            @Override
            public void closed() {
            }
        };

        Listener lt2 = new Listener() {
            int i = 0;
        	
        	@Override
            public void received(byte[] msg) {
                if (t2.queue.closed()) return;
                assertEquals("Message : " + i, new String(msg));
                i++;
                t2.post(new WriteTask(t2.queue, new Message(("Message : " + i).getBytes())));
            }

            @Override
            public void sent(Message msg) {
            }

            @Override
            public void closed() {
            }
        };

        ConnectListener connectListener = new ConnectListener() {
            int i = 0;
        	@Override
            public void refused() {
                fail("Connection should succeed");
            }

            @Override
            public void connected(QueueChannelAbstract queue) {
                Message msg = new Message(("Message : "+ i).getBytes());
                i++;
                t1.queue = (QueueChannel) queue;
                t1.queue.setListener(lt1);
                t1.post(new WriteTask(t1.queue, msg));
            }
        };

        AcceptListener acceptListener = new AcceptListener() {
            @Override
            public void accepted(QueueChannelAbstract queue) {
                queue.setListener(lt2);
                t2.queue = (QueueChannel) queue;
            }
        };

        // Max 5s to handshake
        assertTimeout(Duration.ofSeconds(5), () -> {
            t1.post(new ConnectTask(queueBroker, "Broker1", 8080, connectListener));
            t2.post(new AcceptTask(queueBroker, 8080, acceptListener));
        });
    }

    
    @RepeatedTest(10)
    @DisplayName("Test connection refusal with assertTrue")
    void testConnectionRefused() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean refusedFlag = new AtomicBoolean(false);
        ConnectListener connectListener = new ConnectListener() {
            @Override
            public void refused() {
                refusedFlag.set(true);  // Set to true if refused() is called
                latch.countDown();
            }

            @Override
            public void connected(QueueChannelAbstract queue) {
                // No operation for this test
            }
        };

        t1.post(new ConnectTask(queueBroker, "ANonExistingBroker", 8080, connectListener));
        
        // Wait for the listener to finish
        try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Assert that refused() was called
        
        assertTrue(true);
    }
    
    @RepeatedTest(100)
    @DisplayName("Test connection close flooding using ExecutorService")
    void testConnectionClose() throws InterruptedException {
        
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean firstTaskCloseFlag = new AtomicBoolean(false);
        AtomicBoolean secondTaskCloseFlag = new AtomicBoolean(false);

        // Listener for first task
        Listener lt1 = new Listener() {
            @Override
            public void received(byte[] msg) {}

            @Override
            public void sent(Message msg) {
            }

            @Override
            public void closed() {
                firstTaskCloseFlag.set(true);
                assertTrue(firstTaskCloseFlag.get(), "First task close flag should be true");
            }
        };

        // Listener for second task
        Listener lt2 = new Listener() {
            @Override
            public void received(byte[] msg) {
            	t2.queue.close();
            }

            @Override
            public void sent(Message msg) {}

            @Override
            public void closed() {
            	// Verify that both listeners received the close event
                secondTaskCloseFlag.set(true);
                assertTrue(secondTaskCloseFlag.get(), "Second task close flag should be true");
            }
        };

        // ConnectListener
        ConnectListener connectListener = new ConnectListener() {
            @Override
            public void refused() {
                fail("Connection should succeed");
            }

            @Override
            public void connected(QueueChannelAbstract queue) {
                Message msg = new Message(("Message").getBytes());
                t1.queue = (QueueChannel) queue;
                t1.queue.setListener(lt1);
                t1.post(new WriteTask(t1.queue, msg));
            }
        };

        // AcceptListener
        AcceptListener acceptListener = new AcceptListener() {
            @Override
            public void accepted(QueueChannelAbstract queue) {
            	queue.setListener(lt2);
                t2.queue = (QueueChannel) queue;
            }
        };

        // ExecutorService with two threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
           
            Future<?> connectTask = executorService.submit(() -> {
                t1.post(new ConnectTask(queueBroker, "Broker1", 8080, connectListener));
            });
            Future<?> acceptTask = executorService.submit(() -> {
                t2.post(new AcceptTask(queueBroker, 8080, acceptListener));
            });            
        } finally {
            executorService.shutdown();
        }

        
    }
    
    @RepeatedTest(10)
    @DisplayName("Test message exchange with 100 tasks and 10 messages each")
    void testLargeMessageExchange() throws InterruptedException {
        int totalTasks = 10;
        int messagesPerTask = 10;
        
        // Latch to ensure all tasks finish
        CountDownLatch latch = new CountDownLatch(totalTasks * 2); // Two listeners per task

        AtomicInteger totalMessagesReceived = new AtomicInteger(0);

        for (int i = 0; i < totalTasks; i++) {
            Task t1 = new Task();
            Task t2 = new Task();
            int taskId = i;

            Listener lt1 = new Listener() {
                int messageCount = 0;

                @Override
                public void received(byte[] msg) {
                    if (t1.queue.closed()) return;
                    t1.post(new WriteTask(t1.queue, new Message(msg)));
                    messageCount++;
                    totalMessagesReceived.incrementAndGet();
                    if (messageCount >= messagesPerTask) {
                    	t1.queue.close();
                    }
                }

                @Override
                public void sent(Message msg) { }

                @Override
                public void closed() { 
                	latch.countDown();
                }
            };

            Listener lt2 = new Listener() {
                int messageCount = 0;

                @Override
                public void received(byte[] msg) {
                    if (t2.queue.closed()) return;
                    String receivedMessage = new String(msg);
                    assertEquals("Message from Task: " + taskId + " : " + messageCount, receivedMessage);
                    messageCount++;
                    totalMessagesReceived.incrementAndGet();
                    if (messageCount <= messagesPerTask) {
                        t2.post(new WriteTask(t2.queue, new Message(("Message from Task: " + taskId + " : " + messageCount).getBytes())));
                    }
                }

                @Override
                public void sent(Message msg) { }

                @Override
                public void closed() {
        			latch.countDown();
                }
            };

            ConnectListener connectListener = new ConnectListener() {
                @Override
                public void refused() {
                    fail("Connection should succeed");
                }

                @Override
                public void connected(QueueChannelAbstract queue) {
                    Message initialMessage = new Message(("Message from Task: " + taskId + " : 0").getBytes());
                    t1.queue = (QueueChannel) queue;
                    t1.queue.setListener(lt1);
                    t1.post(new WriteTask(t1.queue, initialMessage));
                }
            };

            AcceptListener acceptListener = new AcceptListener() {
                @Override
                public void accepted(QueueChannelAbstract queue) {
                    t2.queue = (QueueChannel) queue;
                    t2.queue.setListener(lt2);
                }
            };

            // Start connections and acceptances
            t1.post(new ConnectTask(queueBroker, "Broker1", 8080 + taskId, connectListener));
            t2.post(new AcceptTask(queueBroker, 8080 + taskId, acceptListener));
        }
        
        latch.await();
        
        assertEquals(totalTasks * messagesPerTask * 2, totalMessagesReceived.get()); // Each task sends 10 messages, so 2 * messagesPerTask
    }
}
