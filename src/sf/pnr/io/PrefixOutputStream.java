package sf.pnr.io;

import java.io.IOException;
import java.io.OutputStream;

public class PrefixOutputStream extends OutputStream {

    private final OutputStream os;
    private final byte trigger;
    private final byte[] prefix;
    private boolean pending = false;

    public PrefixOutputStream(final OutputStream os, final char triggerChar, final String prefix) {
        this.os = os;
        this.trigger = (byte) (triggerChar & 0xFF);
        this.prefix = prefix.getBytes();
    }

    @Override
    public void write(final int b) throws IOException {
        if (b == trigger) {
            pending = true;
        } else if (pending) {
            os.write(prefix);
            pending = false;
        }
        os.write(b);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}