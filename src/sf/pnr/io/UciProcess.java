package sf.pnr.io;

import java.io.InputStream;
import java.io.OutputStream;

public interface UciProcess {

    public OutputStream getOutputStream();

    public InputStream getInputStream();

    public void destroy();
}
