package sf.pnr.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 */
public class TeeOutputStream extends OutputStream {

    private final OutputStream os1;
    private final OutputStream os2;

    public TeeOutputStream(final OutputStream os1, final OutputStream os2) {
        this.os1 = os1;
        this.os2 = os2;
    }

    @Override
    public void write(final int b) throws IOException {
        os1.write(b);
        os2.write(b);
    }

    public void write(final byte[] b) throws IOException {
        os1.write(b);
        os2.write(b);
    }

    public void write(final byte[] bytes, final int offset, final int len) throws IOException {
        os1.write(bytes, offset, len);
        os2.write(bytes, offset, len);
    }

    @Override
    public void flush() throws IOException {
        os1.flush();
        os2.flush();
    }

    @Override
    public void close() throws IOException {
        os1.close();
        os2.close();
    }
}