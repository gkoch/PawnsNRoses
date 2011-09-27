package sf.pnr.io;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class PipeTest extends TestCase {

    public void testNormal() throws IOException, InterruptedException {
        final Pipe pipe = new Pipe(32);
        final OutputStream input = pipe.getInput();
        final InputStream output = pipe.getOutput();
        final Recorder sink = new Recorder(output);
        sink.start();
        final String content = "12345678901234567890";
        input.write(content.getBytes());
        input.close();
        while (sink.isAlive()) {
            Thread.sleep(20);
        }
        assertEquals(content, sink.getContent());
    }

    public void testSmallBuffer() throws IOException, InterruptedException {
        final Pipe pipe = new Pipe(16);
        final OutputStream input = pipe.getInput();
        final InputStream output = pipe.getOutput();
        final Recorder sink = new Recorder(output);
        sink.start();
        final String content = "1234567890123456789012345678901234567890";
        input.write(content.getBytes());
        input.close();
        while (sink.isAlive()) {
            Thread.sleep(20);
        }
        assertEquals(content, sink.getContent());
    }

    public void testClose() throws IOException, InterruptedException {
        final Pipe pipe = new Pipe(32);
        final OutputStream input = pipe.getInput();
        final InputStream output = pipe.getOutput();
        final Recorder sink = new Recorder(output);
        sink.start();
        final String content = "12345678901234567890";
        input.write(content.getBytes());
        input.close();
        try {
            input.write('a');
            fail();
        } catch (IOException e) {
            // expected
        }
        try {
            input.write(content.getBytes());
            fail();
        } catch (IOException e) {
            // expected
        }
    }

    private static class Recorder extends Thread {
        private final ByteArrayOutputStream baos;
        private final InputStream output;

        public Recorder(final InputStream output) {
            this.output = output;
            baos = new ByteArrayOutputStream(64);
        }

        @Override
        public void run() {
            try {
                for (int value = output.read(); value >= 0; value = output.read()) {
                    baos.write(value);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public String getContent() {
            return baos.toString();
        }
    }
}
