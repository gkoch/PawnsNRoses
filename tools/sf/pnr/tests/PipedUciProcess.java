package sf.pnr.tests;

import sf.pnr.io.UCI;
import sf.pnr.io.UciProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PipedUciProcess implements UciProcess {

    private PipedOutputStream fromEngineOut;
    private PipedInputStream toEngineIn;
    private Future<Boolean> future;

    private void create() throws IOException {
        if (future == null) {
            final PipedOutputStream toEngineOut = new PipedOutputStream();
            toEngineIn = new PipedInputStream(toEngineOut);
            final PipedInputStream fromEngineIn = new PipedInputStream();
            fromEngineOut = new PipedOutputStream(fromEngineIn);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            future = executor.submit(new UciCallable(fromEngineIn, toEngineOut));
            executor.shutdown();
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        create();
        return fromEngineOut;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        create();
        return toEngineIn;
    }

    @Override
    public void restart() throws IOException {
        destroy();
        create();
    }

    @Override
    public void destroy() throws IOException {
        if (future != null) {
            final PrintStream printStream = new PrintStream(getOutputStream());
            printStream.print("\r\nquit\r\n");
            printStream.flush();
            printStream.close();
            try {
                future.get();
            } catch (InterruptedException e) {
                // no need to rethrow it at this point
                e.printStackTrace();
            } catch (ExecutionException e) {
                // no need to rethrow it at this point
                e.printStackTrace();
            }
            future = null;
        }
    }

    @Override
    public UciProcess duplicate() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private static class UciCallable implements Callable<Boolean> {
        private final UCI uci;

        public UciCallable(final PipedInputStream in, final PipedOutputStream out) {
            uci = new UCI(in, out);
        }

        @Override
        public Boolean call() throws Exception {
            uci.run();
            uci.destroy();
            return Boolean.TRUE;
        }
    }
}