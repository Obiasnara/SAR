package tests;

import implems.*;

/**
 * Cette classe met en place un test simple de communication entre deux tâches
 * via un broker et des canaux.
 * 
 * - Un brokers est créé : `broker1`.
 * - Task 1 (associée à `broker1`) attend une connexion sur le port 8080 et lit
 * les données reçues via un canal.
 * - Task 2 (associée à `broker1`) se connecte à `broker1` via le port 8080,
 * envoie un message, puis se déconnecte.
 * - Les deux tâches sont exécutées simultanément et une fois la communication
 * terminée, un message indique la fin.
 * 
 * Ce test illustre l'utilisation de l'API pour établir une communication
 * bidirectionnelle entre tâches.
 */
public class example {

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

    public static void main(String[] args) {
        // Create a new test object
        example test = new example();
        // Run the test
        test.test1();
        // test.test2();
    }

    public boolean test1() {
        Broker broker = new Broker("Broker1");

        Task serverTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Listen on port 8080 for incoming connections
                    Channel serverChannel = (Channel) broker.accept(8080);

                    while (true) {
                        // Read the message size
                        int messageSize = readMessageSize(serverChannel);

                        if (messageSize <= 0) {
                            System.out.println("Error: Invalid message size");
                            continue;
                        }

                        System.out.println("Received message size: " + messageSize);

                        byte[] buffer = new byte[messageSize];
                        int bytesRead = 0;

                        // Read the message from the channel
                        while (bytesRead < messageSize) {
                            int response = serverChannel.read(buffer, bytesRead, messageSize - bytesRead);

                            if (response == -1) {
                                System.out.println("Error reading message");
                                serverChannel.disconnect();
                                return;
                            }

                            bytesRead += response;
                        }

                        System.out.println("Received message: " + new String(buffer, 0, bytesRead));

                        byte[] sizeBytes = getMessageSize(buffer.length);
                        byte[] messageBytes = buffer;

                        // Combine size and message into a single byte array
                        byte[] writeBuffer = new byte[sizeBytes.length + messageBytes.length];
                        System.arraycopy(sizeBytes, 0, writeBuffer, 0, sizeBytes.length);
                        System.arraycopy(messageBytes, 0, writeBuffer, sizeBytes.length, messageBytes.length);

                        // Echo the message back to the client
                        int bytesWritten = 0;
                        while (bytesWritten < writeBuffer.length) {
                            int written = serverChannel.write(writeBuffer, bytesWritten,
                                    writeBuffer.length - bytesWritten);

                            if (written == -1) {
                                System.out.println("Error writing message");
                                serverChannel.disconnect();
                                return;
                            }
                            System.out
                                    .println("Server sent mid-echo: " + new String(writeBuffer, bytesWritten, written));
                            bytesWritten += written;
                        }


                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Task clientTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to the server on port 8080
                    Channel clientChannel = (Channel) broker.connect("Broker1", 8080);
                    String message = "Hello from Echo Client";

                    byte[] sizeBytes = getMessageSize(message.length());
                    byte[] messageBytes = message.getBytes();

                    // Combine size and message into a single byte array
                    byte[] buffer = new byte[sizeBytes.length + messageBytes.length];
                    System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
                    System.arraycopy(messageBytes, 0, buffer, sizeBytes.length, messageBytes.length);

                    // Send the message to the server
                    int bytesWritten = 0;
                    while (bytesWritten < buffer.length) {
                        int response = clientChannel.write(buffer, bytesWritten, buffer.length - bytesWritten);
                        if (response == -1) {
                            System.out.println("Error writing message");
                            return;
                        }
                        bytesWritten += response;
                    }

                    // Read the message size
                    int messageSize = readMessageSize(clientChannel);

                    System.out.println("Client received message size: " + messageSize);

                    byte[] echoBuffer = new byte[messageSize];
                    int bytesRead = 0;

                    // Read the message from the channel
                    while (bytesRead < messageSize) {
                        int response = clientChannel.read(echoBuffer, bytesRead, messageSize - bytesRead);

                        if (response == -1) {
                            System.out.println("Error reading message");
                            clientChannel.disconnect();
                            return;
                        }
                        bytesRead += response;
                    }

                    System.out.println("Client received echo: "
                            + new String(echoBuffer, sizeBytes.length, bytesRead - sizeBytes.length));

                    clientChannel.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            serverTask.join();
            clientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean test2() {
        Broker broker = new Broker("Broker1");

        Task serverTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Listen on port 8080 for incoming connections
                    Channel serverChannel = (Channel) broker.accept(8080);

                    while (true) {
                        // Read the message size
                        int messageSize = readMessageSize(serverChannel);

                        if (messageSize <= 0) {
                            System.out.println("Error: Invalid message size");
                            continue;
                        }

                        System.out.println("Received message size: " + messageSize);

                        byte[] buffer = new byte[messageSize];
                        int bytesRead = 0;

                        // Read the message from the channel
                        while (bytesRead < messageSize) {
                            int response = serverChannel.read(buffer, bytesRead, messageSize - bytesRead);

                            if (response == -1) {
                                System.out.println("Error reading message");
                                serverChannel.disconnect();
                                return;
                            }

                            bytesRead += response;
                        }

                        System.out.println("Received message: " + new String(buffer, 0, bytesRead));

                        byte[] sizeBytes = getMessageSize(buffer.length);
                        byte[] messageBytes = buffer;

                        // Combine size and message into a single byte array
                        byte[] writeBuffer = new byte[sizeBytes.length + messageBytes.length];
                        System.arraycopy(sizeBytes, 0, writeBuffer, 0, sizeBytes.length);
                        System.arraycopy(messageBytes, 0, writeBuffer, sizeBytes.length, messageBytes.length);

                        // Echo the message back to the client
                        int bytesWritten = 0;
                        while (bytesWritten < messageSize) {
                            int written = serverChannel.write(writeBuffer, bytesWritten, messageSize - bytesWritten);

                            if (written == -1) {
                                System.out.println("Error writing message");
                                serverChannel.disconnect();
                                return;
                            }

                            bytesWritten += written;
                        }

                        System.out.println("Echoed message: " + new String(writeBuffer, 4, bytesRead));
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Task clientTask = new Task(broker, new Runnable() {
            @Override
            public void run() {
                try {
                    // Connect to the server on port 8080
                    Channel clientChannel = (Channel) broker.connect("Broker1", 8080);
                    String message = LOREM_IPSUM;

                    byte[] sizeBytes = getMessageSize(message.length());
                    byte[] messageBytes = message.getBytes();

                    // Combine size and message into a single byte array
                    byte[] buffer = new byte[sizeBytes.length + messageBytes.length];
                    System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
                    System.arraycopy(messageBytes, 0, buffer, sizeBytes.length, messageBytes.length);

                    // Send the message to the server
                    int bytesWritten = 0;
                    while (bytesWritten < buffer.length) {
                        int response = clientChannel.write(buffer, bytesWritten, buffer.length - bytesWritten);
                        if (response == -1) {
                            System.out.println("Error writing message");
                            return;
                        }
                        bytesWritten += response;
                    }

                    // Read the message size
                    int messageSize = readMessageSize(clientChannel);

                    System.out.println("Client received message size: " + messageSize);

                    byte[] echoBuffer = new byte[messageSize];
                    int bytesRead = 0;

                    // Read the message from the channel
                    while (bytesRead < messageSize) {
                        int response = clientChannel.read(echoBuffer, bytesRead, messageSize - bytesRead);

                        if (response == -1) {
                            System.out.println("Error reading message");
                            clientChannel.disconnect();
                            return;
                        }

                        bytesRead += response;
                    }

                    System.out.println("Client received echo: " + new String(echoBuffer, 4, bytesRead));

                    clientChannel.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            serverTask.join();
            clientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
