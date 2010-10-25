package sf.pnr.base;

import sf.pnr.io.UciProcess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class ExternalUciProcess implements UciProcess {

    private Process process;
    private String[] command;
    private File workDir;

    public ExternalUciProcess(final String executable) throws IOException {
        command = new String[]{"\"" + executable + "\""};
        workDir = new File(executable).getParentFile();
    }

    private void createProcess() throws IOException {
        if (process == null) {
            process = Runtime.getRuntime().exec(this.command, null, this.workDir);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        createProcess();
        return process.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        createProcess();
        return process.getInputStream();
    }

    @Override
    public void restart() throws IOException {
        destroy();
        createProcess();
    }

    @Override
    public void destroy() {
        if (process != null) {
            try {
                final PrintStream printStream = new PrintStream(getOutputStream());
                printStream.print("\r\nquit\r\n");
                printStream.flush();
                printStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                // carry on
            }
            process.destroy();
            process = null;
        }
    }
}