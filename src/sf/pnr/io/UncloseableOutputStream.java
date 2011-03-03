package sf.pnr.io;

import java.io.IOException;
import java.io.OutputStream;

public class UncloseableOutputStream extends OutputStream {

    private final OutputStream os;
    private int counter;

    public UncloseableOutputStream(final OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(final int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        os.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        counter--;
        if (counter == 0) {
            os.close();
        }
    }

    public void incCounter() {
        counter++;
    }
}