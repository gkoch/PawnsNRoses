package sf.pnr.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Properties;

public class ConfigFileGenerator {

    public static void main(final String[] args) throws IOException {
        final File templateFile = new File(args[0]);
        final String templateFileName = templateFile.getName();
        final String templateName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));
        final File configRootDir = new File(args[1]);

        final Properties template = new Properties();
        template.load(new FileReader(templateFile));
        final File targetDir = new File(configRootDir, templateName);
        targetDir.mkdirs();

        new ConfigGenerator().generateConfigs(template, new ConfigVisitor() {
            private int configCount = 0;

            @Override
            public void visit(final ConfigIdGenerator idGenerator, final Properties config) {
                try {
                    config.store(
                        new FileWriter(new File(targetDir, String.format("%s-%s.ini", templateName, idGenerator.getId()))),
                        "Config #" + configCount);
                    configCount++;
                } catch (IOException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
    }
}