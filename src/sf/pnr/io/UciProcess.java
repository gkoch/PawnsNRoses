package sf.pnr.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface UciProcess {

    public OutputStream getOutputStream() throws IOException;

    public InputStream getInputStream() throws IOException;

    public void restart() throws IOException;

    public void destroy() throws IOException;

    public UciProcess duplicate();
}
