package sf.pnr.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class TeeInputStream extends InputStream {

    private final InputStream is;
    private final OutputStream os;

    public TeeInputStream(final InputStream is, final OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public int read() throws IOException {
        final int input = is.read();
        if (input != -1) {
            os.write(input);
        }
        return input;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        final int bytes = is.read(b);
        if (bytes > 0) {
            os.write(b, 0, bytes);
        }
        return bytes;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int bytes = is.read(b, off, len);
        if (bytes > 0) {
            os.write(b, off, bytes);
        }
        return bytes;
    }

    @Override
    public long skip(final long n) throws IOException {
        return is.skip(n);
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public void close() throws IOException {
        is.close();
        os.close();
    }

    @Override
    public void mark(final int readlimit) {
        is.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        is.reset();
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }
}