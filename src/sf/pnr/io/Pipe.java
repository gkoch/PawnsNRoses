package sf.pnr.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Pipe {

    private final byte[] buffer;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();
    private final Input input = new Input();
    private final Output output = new Output();
    private volatile int readPos;
    private volatile int writePos;
    private volatile int count;
    private volatile boolean outputClosed;

    public Pipe() {
        this(8192);
    }

    public Pipe(final int bufferSize) {
        buffer = new byte[bufferSize];
        readPos = 0;
        writePos = 0;
        count = 0;
        outputClosed = false;
    }

    public InputStream getOutput() {
        return input;
    }

    public OutputStream getInput() {
        return output;
    }

    private class Input extends InputStream {

        @Override
        public int read() throws IOException {
            lock.lock();
            try {
                while (count == 0 && !outputClosed) {
                    notEmpty.await();
                }
                if (count == 0) {
                    return -1;
                }

                final int value = ((int) buffer[readPos]) & 0xFF;
                readPos++;
                if (readPos == buffer.length) {
                    readPos = 0;
                }
                count--;
                notFull.signal();
                return value;
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting on more input");
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            lock.lock();
            try {
                while (count == 0 && !outputClosed) {
                    notEmpty.await();
                }
                if (count == 0) {
                    return -1;
                }
                final int cnt = Math.min(buffer.length - readPos, Math.min(len, count));
                System.arraycopy(buffer, readPos, b, off, cnt);
                readPos += cnt;
                if (readPos == buffer.length) {
                    readPos = 0;
                }
                count -= cnt;

                final int cnt2;
                if (cnt < len && count > 0) {
                    cnt2 = Math.min(len - cnt, count);
                    System.arraycopy(buffer, readPos, b, off + cnt, cnt2);
                    readPos += cnt2;
                    count -= cnt2;
                } else {
                    cnt2 = 0;
                }
                notFull.signal();
                return cnt + cnt2;
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting on more input");
            } finally {
                lock.unlock();
            }
        }
    }

    private class Output extends OutputStream {
        @Override
        public void write(final int value) throws IOException {
            lock.lock();
            if (outputClosed) {
                throw new IOException("Stream closed");
            }
            try {
                while (count == buffer.length) {
                    notFull.await();
                }
                buffer[writePos] = (byte) value;
                writePos++;
                if (writePos == buffer.length) {
                    writePos = 0;
                }
                count++;
                notEmpty.signal();
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting on more input");
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            lock.lock();
            if (outputClosed) {
                throw new IOException("Stream closed");
            }
            try {
                for (int written = 0; written < len; ) {
                    while (count == buffer.length) {
                        notFull.await();
                    }
                    final int cnt = Math.min(buffer.length - writePos, Math.min(len - written, buffer.length - count));
                    System.arraycopy(b, off + written, buffer, writePos, cnt);
                    writePos += cnt;
                    if (writePos == buffer.length) {
                        writePos = 0;
                    }
                    count += cnt;
                    written += cnt;
                    notEmpty.signal();
                }
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting on more input");
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            lock.lock();
            try {
                outputClosed = true;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}