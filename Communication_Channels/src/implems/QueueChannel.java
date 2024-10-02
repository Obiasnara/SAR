package implems;

import abstracts.ChannelAbstract;
import abstracts.QueueChannelAbstract;

public class QueueChannel extends QueueChannelAbstract {

	ChannelAbstract connectedChannel;
	
	public QueueChannel(ChannelAbstract connectedChannel) {
		this.connectedChannel = connectedChannel;
	}
	
	public static int getSizeFromMessage(byte[] sizeBytes) {
        return ((sizeBytes[0] & 0xFF) << 24) |
               ((sizeBytes[1] & 0xFF) << 16) |
               ((sizeBytes[2] & 0xFF) << 8)  |
               (sizeBytes[3] & 0xFF);
    }
	
	public static int readMessageSize(ChannelAbstract channel) {
        byte[] sizeBytes = new byte[4];

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
	
	public static byte[] getMessageSize(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value
        };
    }
    

	@Override
	public void send(byte[] bytes, int offset, int length) {

		byte[] sizeBytes = getMessageSize(length);
        byte[] buffer = new byte[sizeBytes.length + length];
        System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
        System.arraycopy(bytes, 0, buffer, sizeBytes.length, length);
		
		int sentBytes = 0;
		while (sentBytes != buffer.length) {
			sentBytes += connectedChannel.write(buffer, offset, buffer.length - sentBytes);
		}
	}

	@Override
	public byte[] receive() {
		int messageSize = readMessageSize(connectedChannel);
        if (messageSize <= 0) {
            return null;
        }

        byte[] buffer = new byte[messageSize];
        int bytesRead = 0;

        while (bytesRead < messageSize) {
            int response = connectedChannel.read(buffer, bytesRead, messageSize - bytesRead);

            bytesRead += response;
        }
        return buffer;
	}

	@Override
	public void close() {
		connectedChannel.disconnect();
	}

	@Override
	public boolean closed() {
		return connectedChannel.disconnected();
	}
	
}