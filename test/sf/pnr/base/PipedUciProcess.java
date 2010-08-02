package sf.pnr.base;

import sf.pnr.io.UCI;
import sf.pnr.io.UciProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PipedUciProcess implements UciProcess {

    private final PipedOutputStream fromEngineOut;
    private final PipedInputStream toEngineIn;
    private final ExecutorService executor;
    private Future<Boolean> future;

    public PipedUciProcess() throws IOException, ExecutionException, InterruptedException {
        final PipedOutputStream toEngineOut = new PipedOutputStream();
        toEngineIn = new PipedInputStream(toEngineOut);
        final PipedInputStream fromEngineIn = new PipedInputStream();
        fromEngineOut = new PipedOutputStream(fromEngineIn);
        executor = Executors.newSingleThreadExecutor();
        future = executor.submit(new UciCallable(fromEngineIn, toEngineOut));
        executor.shutdown();
    }

    @Override
    public OutputStream getOutputStream() {
        return fromEngineOut;
    }

    @Override
    public InputStream getInputStream() {
        return toEngineIn;
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
        try {
            future.get();
        } catch (InterruptedException e) {
            // no need to rethrow it at this point
            e.printStackTrace();
        } catch (ExecutionException e) {
            // no need to rethrow it at this point
            e.printStackTrace();
        }
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