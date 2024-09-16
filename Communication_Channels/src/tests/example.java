package tests;

import implems.*;

/**
 * Cette classe met en place un test simple de communication entre deux tâches via un broker et des canaux.
 * 
 * - Un brokers est créé : `broker1`.
 * - Task 1 (associée à `broker1`) attend une connexion sur le port 8080 et lit les données reçues via un canal.
 * - Task 2 (associée à `broker1`) se connecte à `broker1` via le port 8080, envoie un message, puis se déconnecte.
 * - Les deux tâches sont exécutées simultanément et une fois la communication terminée, un message indique la fin.
 * 
 * Ce test illustre l'utilisation de l'API pour établir une communication bidirectionnelle entre tâches.
 */
public class example {

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

    public static int readMessageSize(ChannelImplem channel) {
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
       
        BrokerImplem broker1 = new BrokerImplem("Broker1");
       
        TaskImplem task1 = new TaskImplem(broker1, new Runnable() {
            @Override
            public void run() { // Implique que le constructeur de TaskImplem démare le thread
                ChannelImplem Channel = (ChannelImplem) broker1.accept(8080); 
                int messageSize = readMessageSize(Channel);
                
                byte[] buffer = new byte[messageSize];
                int bytesRead = 0;
                while (bytesRead < messageSize) {
                    int response = Channel.read(buffer, bytesRead, messageSize - bytesRead);
                    if (response == -1) {
                        System.out.println("Error reading message");
                        return;
                    }
                    bytesRead += response;
                }
                System.out.println("Task 1 received: " + new String(buffer, 0, bytesRead));
                Channel.disconnect();
            }
        });

        TaskImplem task2 = new TaskImplem(broker1, new Runnable() {
            @Override
            public void run() { // Implique que le constructeur de TaskImplem démare le thread
                ChannelImplem Channel = (ChannelImplem) broker1.connect("Broker1", 8080);
                String message = "Hello from Task 2";

                byte[] sizeBytes = getMessageSize(message.length());
                byte[] messageBytes = message.getBytes();
                // Fuse the size and the message into a single byte array
                byte[] buffer = new byte[sizeBytes.length + messageBytes.length];
                System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
                System.arraycopy(messageBytes, 0, buffer, sizeBytes.length, messageBytes.length);

                int bytesWritten = 0;
                while (bytesWritten < buffer.length) {
                    int response = Channel.write(buffer, bytesWritten, buffer.length - bytesWritten);
                    if (response == -1) {
                        System.out.println("Error writing message");
                        return;
                    }
                    bytesWritten += response;
                }

                Channel.disconnect();
            }
        });

        try {
            task1.join();  
            task2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Communication complete.");
    }

}
