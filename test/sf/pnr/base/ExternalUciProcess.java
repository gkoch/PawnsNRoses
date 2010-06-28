package sf.pnr.base;

import sf.pnr.io.UciProcess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExternalUciProcess implements UciProcess {

    private Process process;

    public ExternalUciProcess(final String[] command, final File workDir) throws IOException {
        process = Runtime.getRuntime().exec(command, null, workDir);
    }

    @Override
    public OutputStream getOutputStream() {
        return process.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return process.getInputStream();
    }

    @Override
    public void destroy() {
        process.destroy();
    }
}