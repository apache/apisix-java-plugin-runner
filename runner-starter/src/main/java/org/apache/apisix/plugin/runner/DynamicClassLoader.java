package org.apache.apisix.plugin.runner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

public class DynamicClassLoader extends ClassLoader {
    private HashSet<String> allFilters;
    private String dir;
    private String packageName;

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
        allFilters = new HashSet<>();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        if(!allFilters.contains(name)) {
            return super.loadClass(name);
        }
        try {
            String packagePath = packageName.replaceAll("\\.", "/");
            String url = "file:" + dir + "/" + packagePath + "/" + name + ".class";
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            String fullyQualifiedName = packageName + "." + name;
            return defineClass(fullyQualifiedName,
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setDir(String path) {
        dir = path;
    }

    public void setFilters(HashSet<String> filters) {
        allFilters = filters;
    }

    public void setPackageName(String name) {
        packageName = name;
    }
}
