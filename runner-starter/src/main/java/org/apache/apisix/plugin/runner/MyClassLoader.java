package org.apache.apisix.plugin.runner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        if(!"org.apache.apisix.plugin.runner.filter.RewriteRequestDemoFilter".equals(name)){
            return super.loadClass(name);
        }
        try {
            String url = "file:/home/parallels/Documents/apisix-java-plugin-runner/runner-plugin/target/classes/org/apache/apisix/plugin/runner/filter/RewriteRequestDemoFilter.class";
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

            return defineClass("org.apache.apisix.plugin.runner.filter.RewriteRequestDemoFilter",
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}


