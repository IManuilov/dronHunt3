package ilovke.compil;

import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class MainRun {

    public interface My {
        String sayHello(String str);
    }


    public static void main(String[] args) throws Exception {



        new MainRun().run();

    }


    //@SneakyThrows
    public void run() throws Exception {
        // Создаем временный файл с исходным кодом
        String className = "MyClass";
        String sourceCode = """
            import ilovke.compil.MainRun.My;
            
            public class MyClass implements My {
                public String sayHello(String str) {
                    System.out.println("Hello from dynamically compiled class! " + str);
                    return "" + Math.random();
                }
            }
            """;



        // Сохраняем исходный код в файл
        File sourceFile = new File(className + ".java");
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(sourceCode);
        }

        // Компилируем
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(java.util.Collections.singletonList(sourceFile));

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, null, null, compilationUnits);

        boolean success = task.call();
        fileManager.close();

        if (success) {
            // Загружаем класс
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { new File(".").toURI().toURL() });
            Class<?> loadedClass = classLoader.loadClass(className);

            // Создаем объект и вызываем метод
            Object instance = loadedClass.getDeclaredConstructor().newInstance();
            My my = (My) instance;
            //Method method = loadedClass.getMethod("sayHello", String.class);
            //String res = (String) method.invoke(instance, "123");
            //System.out.println(res);

            my.sayHello("432");

            classLoader.close();
            classLoader = null;

        } else {
            System.out.println("Compilation failed:");
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                System.out.println(diagnostic);
            }
        }


        // Очищаем файлы
        sourceFile.delete();
        new File(className + ".class").delete();
    }

}
