import java.io.File;
import java.nio.file.Files;
import java.lang.reflect.Method;

public class ForgeMain {
    public static void main(String[] args) throws Exception {
        File classFile = new File("hotdeploy/PluginAlgorithm.class");
        long lastModified = 0;
        Object pluginInstance = null;
        Method processMethod = null;

        System.out.println("Zero Downtime Hot Swap Engine Start");
        System.out.println("Monitoring hotdeploy directory for class changes");

        while (true) {
            if (classFile.exists() && classFile.lastModified() > lastModified) {
                lastModified = classFile.lastModified();
                System.out.println("New class detected Hot swapping at runtime");

                byte[] classBytes = Files.readAllBytes(classFile.toPath());

                ClassLoader hotLoader = new ClassLoader() {
                    @Override
                    protected Class<?> findClass(String name) {
                        return defineClass(name, classBytes, 0, classBytes.length);
                    }
                };

                Class<?> pluginClass = hotLoader.loadClass("PluginAlgorithm");
                pluginInstance = pluginClass.getDeclaredConstructor().newInstance();
                processMethod = pluginClass.getMethod("process", long.class);

                System.out.println("Hot Swap Complete Resuming traffic processing");
            }

            if (pluginInstance != null && processMethod != null) {
                long mockTraffic = System.currentTimeMillis() % 100;
                long result = (long) processMethod.invoke(pluginInstance, mockTraffic);
                System.out.println("Traffic In " + mockTraffic + " Processed Out " + result);
            } else {
                System.out.println("Waiting for initial PluginAlgorithm class deployment");
            }

            Thread.sleep(2000);
        }
    }
}