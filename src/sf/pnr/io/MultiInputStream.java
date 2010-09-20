package sf.pnr.io;

import java.io.IOException;
import java.io.InputStream;

public class MultiInputStream extends InputStream {

    private InputStream[] inputStreams = new InputStream[0];
    private int index = 0;

    @Override
    public int read() throws IOException {
        if (inputStreams.length <= index) {
            return -1;
        }
        int value = inputStreams[index].read();
        while (value == -1) {
            inputStreams[index].close();
            inputStreams[index] = null;
            index++;
            if (index == inputStreams.length) {
                break;
            }
            value = inputStreams[index].read();
        }
        return value;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        
        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte) c;
        int i = 1;
        try {
            for (; i < len && inputStreams[index].available() > 0; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte) c;
            }
        } catch (IOException e) {
            // we've already read at least one byte, so ignore
        }
        return i;
    }

    public void addInputStream(final InputStream is) {
        final InputStream[] newArray = new InputStream[inputStreams.length + 1];
        System.arraycopy(inputStreams, 0, newArray, 0, inputStreams.length);
        newArray[inputStreams.length] = is;
        inputStreams = newArray;
    }

    @Override
    public int available() throws IOException {
        return index < inputStreams.length? inputStreams[index].available(): 0;
    }

    @Override
    public void close() throws IOException {
        IOException first = null;
        for (InputStream is: inputStreams) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    first = e;
                }
            }
        }
        if (first != null) {
            throw first;
        }
    }
}