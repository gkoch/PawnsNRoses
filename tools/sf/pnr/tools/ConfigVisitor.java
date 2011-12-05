package sf.pnr.tools;

import java.util.Properties;

public interface ConfigVisitor {
    public void visit(final ConfigIdGenerator idGenerator, Properties config);
}
