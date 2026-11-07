package com.vhvkhangg.personalprivatevault.drive.internal.files.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveException;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileException;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** S2 admission before parsing; MVC owns async dispatch, with bounded nonblocking servlet bridges. */
public class DriveFileFilter extends OncePerRequestFilter {
    public static final String OPERATION=DriveFileFilter.class.getName()+".operation";
    public static final String CONTENT_TYPE=DriveFileFilter.class.getName()+".contentType";
    private final DriveFileResources resources;
    private final ObjectMapper mapper=new ObjectMapper().findAndRegisterModules();
    public DriveFileFilter(DriveFileResources resources){this.resources=resources;}
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().matches("/api/v1/drive/connections/[^/]+/(root-folder|files|files/[^/]+/download)");
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws IOException,ServletException {
        boolean upload="POST".equals(request.getMethod()) && request.getRequestURI().endsWith("/files");
        DriveFileResources.Operation op;
        try {
            if(upload && request.getContentLengthLong()>DriveFileResources.MAX_REQUEST) throw new DriveFileException("PAYLOAD_TOO_LARGE",413);
            long headers=0;
            for(var names=request.getHeaderNames();names.hasMoreElements();) {
                String name=names.nextElement();
                for(var values=request.getHeaders(name);values.hasMoreElements();) headers+=name.length()+values.nextElement().getBytes(StandardCharsets.UTF_8).length+4;
                if(headers>16*1024) throw DriveFileException.input();
            }
            if(request.getQueryString()!=null && request.getQueryString().length()>4096) throw DriveFileException.input();
            op=resources.open(upload);
        } catch(DriveException error) {error(response,error);return;}
        request.setAttribute(OPERATION,op);request.setAttribute(CONTENT_TYPE,request.getContentType());
        var input=new InputBridge(request.getInputStream(),op);
        var output=new OutputBridge(response.getOutputStream(),op);
        op.onCancel(() -> {input.abort();if(response.isCommitted())output.abort();});
        var wrappedRequest=new HttpServletRequestWrapper(request) {
            @Override public String getContentType(){return upload?"application/octet-stream":super.getContentType();}
            @Override public ServletInputStream getInputStream(){return input;}
            @Override public BufferedReader getReader(){return new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8));}
            @Override public Collection<Part> getParts(){throw DriveFileException.input();}
            @Override public Part getPart(String name){throw DriveFileException.input();}
        };
        var wrappedResponse=new HttpServletResponseWrapper(response) {
            @Override public ServletOutputStream getOutputStream() throws IOException{return request.isAsyncStarted()?output:super.getOutputStream();}
            @Override public PrintWriter getWriter() throws IOException{return request.isAsyncStarted()?new PrintWriter(new OutputStreamWriter(output,StandardCharsets.UTF_8)):super.getWriter();}
            @Override public void flushBuffer() throws IOException{if(request.isAsyncStarted())output.flush();else super.flushBuffer();}
            @Override public void reset(){super.reset();output.reset();}
        };
        try {
            if(upload)op.beginBody();
            // MVC starts async with these wrappers, propagates security and owns task completion/redispatch.
            chain.doFilter(wrappedRequest,wrappedResponse);
        } finally {
            if(request.isAsyncStarted()){
                var async=request.getAsyncContext();
                async.addListener(new AsyncListener() {
                    public void onComplete(AsyncEvent event){op.close();}
                    public void onTimeout(AsyncEvent event){op.cancel();input.abort();if(response.isCommitted())output.abort();}
                    public void onError(AsyncEvent event){op.cancel();input.abort();output.abort();op.close();}
                    public void onStartAsync(AsyncEvent event){}
                });
                if(upload)input.start();output.start();
            }else op.close();
        }
    }
    private void error(HttpServletResponse response,DriveException error) throws IOException {
        if(response.isCommitted())throw new IOException("Drive transfer aborted");
        response.reset();response.setStatus(error.getHttpStatus());response.setContentType("application/json");response.setHeader("Cache-Control","no-store");
        mapper.writeValue(response.getOutputStream(),ApiResponses.error(error.getErrorCode(),error.getMessage()));
    }
    private static class InputBridge extends ServletInputStream implements ReadListener {
        private final ServletInputStream source;
        private final DriveFileResources.Operation op;
        private final ArrayBlockingQueue<byte[]> queue=new ArrayBlockingQueue<>(1);
        private volatile boolean end,aborted;
        private byte[] current;private int position;
        InputBridge(ServletInputStream source,DriveFileResources.Operation op){this.source=source;this.op=op;}
        void start(){source.setReadListener(this);}
        @Override public synchronized void onDataAvailable() throws IOException {
            while(!aborted && source.isReady() && !source.isFinished()) {
                if(queue.remainingCapacity()==0)return;
                byte[] bytes=new byte[DriveFileResources.BUFFER];int count=source.read(bytes);
                if(count<0){end=true;return;}
                if(count==0)return;
                if(count!=bytes.length)bytes=Arrays.copyOf(bytes,count);
                if(!queue.offer(bytes))throw new IOException("Drive input bridge capacity");
            }
        }
        @Override public void onAllDataRead(){end=true;queue.offer(new byte[0]);}
        @Override public void onError(Throwable error){abort();}
        void abort(){aborted=true;end=true;queue.clear();queue.offer(new byte[0]);}
        @Override public int read(byte[] bytes,int offset,int length) throws IOException {
            if(length==0)return 0;
            while(current==null || position==current.length) {
                op.check();if(aborted)throw new IOException("Drive input aborted");
                if(end && queue.isEmpty())return -1;
                try {current=queue.poll(Math.min(op.remaining().toMillis()+1,1000),TimeUnit.MILLISECONDS);}
                catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException("Drive input interrupted");}
                position=0;
                if(current==null)continue;
                if(current.length==0){current=null;continue;}
                // A worker can drain already-ready bytes after freeing the single queue slot; reads remain nonblocking.
                if(!end && !aborted) sourceReady();
            }
            int count=Math.min(length,current.length-position);System.arraycopy(current,position,bytes,offset,count);position+=count;op.progress();return count;
        }
        private void sourceReady() {
            try {onDataAvailable();}catch(IOException error){abort();}
        }
        @Override public int read() throws IOException {byte[] one=new byte[1];return read(one,0,1)<0 ? -1 : one[0]&255;}
        @Override public boolean isFinished(){return end && queue.isEmpty() && (current==null || position==current.length);}
        @Override public boolean isReady(){return true;}
        @Override public void setReadListener(ReadListener listener){throw new IllegalStateException("Bridge has one owner");}
    }
    private static class OutputBridge extends ServletOutputStream implements WriteListener {
        private final ServletOutputStream target;
        private final DriveFileResources.Operation op;
        private final ArrayBlockingQueue<byte[]> queue=new ArrayBlockingQueue<>(1);
        private final Object monitor=new Object();
        private volatile boolean aborted;
        private volatile boolean writing,started;
        OutputBridge(ServletOutputStream target,DriveFileResources.Operation op){this.target=target;this.op=op;}
        void start() throws IOException{target.setWriteListener(this);started=true;drain();}
        @Override public void write(byte[] bytes,int offset,int length) throws IOException {
            while(length>0) {
                int count=Math.min(length,DriveFileResources.BUFFER);byte[] chunk=Arrays.copyOfRange(bytes,offset,offset+count);
                long until=System.nanoTime()+(op.isCancelled() ? TimeUnit.SECONDS.toNanos(1) : op.remaining().toNanos());
                while(!queue.offer(chunk)) {check(until);drain();if(queue.remainingCapacity()==0)pause();}
                offset+=count;length-=count;drain();
            }
        }
        @Override public void write(int value) throws IOException {write(new byte[]{(byte)value},0,1);}
        @Override public void onWritePossible() throws IOException {drain();}
        private void drain() throws IOException {
            synchronized(monitor) {
                if(!started || writing || aborted)return;
                writing=true;
                try {
                    while(target.isReady()) {
                        byte[] bytes=queue.poll();if(bytes==null)break;
                        target.write(bytes);op.progress();
                    }
                    monitor.notifyAll();
                } catch(IOException error){aborted=true;throw error;}
                finally{writing=false;}
            }
        }
        @Override public void onError(Throwable error){abort();op.cancel();}
        void abort(){aborted=true;queue.clear();synchronized(monitor){monitor.notifyAll();}}
        void reset(){queue.clear();aborted=false;}
        private void pause() throws IOException {synchronized(monitor){try{monitor.wait(25);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException("Drive output interrupted");}}}
        private void check(long until) throws IOException {if(aborted || System.nanoTime()>=until)throw new IOException("Drive output aborted");if(!op.isCancelled())op.check();}
        @Override public void flush() throws IOException {
            long until=System.nanoTime()+(op.isCancelled()?TimeUnit.SECONDS.toNanos(1):op.remaining().toNanos());
            while(!queue.isEmpty() || writing){check(until);drain();pause();}
        }
        @Override public void close() throws IOException {flush();}
        @Override public boolean isReady(){return !aborted;}
        @Override public void setWriteListener(WriteListener listener){throw new IllegalStateException("Bridge has one owner");}
    }

    public static void abortTransfer(HttpServletResponse response) throws IOException {
        ServletResponse current=response;
        for(int i=0;i<16;i++){
            if(current.getOutputStream() instanceof OutputBridge output){output.abort();return;}
            if(!(current instanceof ServletResponseWrapper wrapper))break;
            current=wrapper.getResponse();
        }
        throw new IOException("Drive transfer aborted");
    }
}
