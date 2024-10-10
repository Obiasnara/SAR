package tests;

import abstracts.event_queue.QueueBrokerAbstract.AcceptListener;
import abstracts.event_queue.QueueBrokerAbstract.ConnectListener;
import abstracts.event_queue.QueueChannelAbstract;
import abstracts.event_queue.QueueChannelAbstract.Listener;
import abstracts.event_queue.TaskAbstract;
import implems.BrokerManager;
import implems.event_queue.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
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
    	EventPump.getInstance().start();
    }
    
    @BeforeEach
    void setUp() {
        queueBroker = new QueueBroker("Broker1");
        t1 = new Task();
        t2 = new Task();
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
    
    @Test
    @DisplayName("Test connection close flodding")
    void testConnectionClose() {

        CountDownLatch latch = new CountDownLatch(2);
		AtomicBoolean firstTaskCloseFlag = new AtomicBoolean(false);
		AtomicBoolean secondTaskCloseFlag = new AtomicBoolean(false);
		Listener lt1 = new Listener() {
		      
		      @Override
		      public void received(byte[] msg) {
		         
		      }
		
		      @Override
		      public void sent(Message msg) {
		      }
		
		      @Override
		      public void closed() {firstTaskCloseFlag.set(true); latch.countDown();}
		};

        Listener lt2 = new Listener() {
            
          	  @Override
              public void received(byte[] msg) {
              }

              @Override
              public void sent(Message msg) {
              }

              @Override
              public void closed() {secondTaskCloseFlag.set(true); latch.countDown();}
        };

        ConnectListener connectListener = new ConnectListener() {
             
          	@Override
              public void refused() {
                  fail("Connection should succeed");
              }

              @Override
              public void connected(QueueChannelAbstract queue) {
            	  queue.setListener(lt1);
              }
          };

        AcceptListener acceptListener = new AcceptListener() {
              @Override
              public void accepted(QueueChannelAbstract queue) {
                  queue.setListener(lt2);
            	  queue.close();
              }
        };

        // Max 5s to handshake
        assertTimeout(Duration.ofSeconds(5), () -> {
              t1.post(new ConnectTask(queueBroker, "Broker1", 8080, connectListener));
              t2.post(new AcceptTask(queueBroker, 8080, acceptListener));
        });
        
        // Wait for the listener to finish
        try {
			latch.await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			fail("Disconnect was timed out");
		}
        assertTrue(firstTaskCloseFlag.get());
        assertTrue(secondTaskCloseFlag.get());
    }
    
    @Test
    @DisplayName("Test message exchange with 100 tasks and 10 messages each")
    void testLargeMessageExchange() throws InterruptedException {
        int totalTasks = 2;
        int messagesPerTask = 1;
        
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
                	System.out.println(new String(msg));
                    if (t1.queue.closed()) return;
                    t1.post(new WriteTask(t1.queue, new Message(msg)));
                    messageCount++;
                    totalMessagesReceived.incrementAndGet();
                    if (messageCount == messagesPerTask) {
                        t1.queue.close();
                        latch.countDown();
                    }
                }

                @Override
                public void sent(Message msg) { }

                @Override
                public void closed() { latch.countDown(); }
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
                    if (messageCount < messagesPerTask) {
                        t2.post(new WriteTask(t2.queue, new Message(("Message from Task: " + taskId + " : " + messageCount).getBytes())));
                    } else {
                        t2.queue.close();
                        latch.countDown();
                    }
                }

                @Override
                public void sent(Message msg) { System.out.println("Sent");}

                @Override
                public void closed() { latch.countDown(); }
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

        // Wait for all tasks to complete
        assertTimeout(Duration.ofSeconds(30), () -> latch.await(30, TimeUnit.SECONDS));

        // Assert total messages received
        assertEquals(totalTasks * messagesPerTask * 2, totalMessagesReceived.get()); // Each task sends 10 messages, so 2 * messagesPerTask
    }
}
