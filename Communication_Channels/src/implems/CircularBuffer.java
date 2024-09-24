package implems;
/**
 * This circular buffer of bytes can be used to pass bytes between two threads:
 * one thread pushing bytes in the buffer and the other pulling bytes from the
 * buffer. The buffer policy is FIFO: first byte in is the first byte out.
 */
public class CircularBuffer {
    volatile int m_tail, m_head;
    volatile byte m_bytes[];

    public CircularBuffer(int capacity) {
        m_bytes = new byte[capacity];
        m_tail = m_head = 0;
    }

    /**
     * @return true if this buffer is full, false otherwise
     */
    public boolean full() {
        int next = (m_head + 1) % m_bytes.length;
        return (next == m_tail);
    }

    /**
     * @return true if this buffer is empty, false otherwise
     */
    public boolean empty() {
        return (m_tail == m_head);
    }

    /**
     * @param b: the byte to push in the buffer
     * @return the next available byte
     * @throws an IllegalStateException if full.
     */
    public void push(byte b) {
        int next = (m_head + 1) % m_bytes.length;
        if (next == m_tail)
            throw new IllegalStateException();
        m_bytes[m_head] = b;
        m_head = next;
    }

    /**
     * @return the next available byte
     * @throws an IllegalStateException if empty.
     */
    public byte pull() {
        if (m_tail == m_head)
            throw new IllegalStateException();
        int next = (m_tail + 1) % m_bytes.length;
        byte bits = m_bytes[m_tail];
        m_tail = next;
        return bits;
    }

}