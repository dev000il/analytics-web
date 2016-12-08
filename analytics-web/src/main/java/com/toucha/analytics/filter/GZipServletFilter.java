package com.toucha.analytics.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Servlet Filter implementation class GZipServletFilter
 */
public class GZipServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
	public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ( acceptsGZipEncoding(httpRequest) ) {
            httpResponse.addHeader("Content-Encoding", "gzip");
            GZipServletResponseWrapper gzipResponse =
                new GZipServletResponseWrapper(httpResponse);
            chain.doFilter(request, gzipResponse);
            gzipResponse.close();
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean acceptsGZipEncoding(HttpServletRequest httpRequest) {
        String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
        return acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1;
    }
    
    class GZipServletResponseWrapper extends HttpServletResponseWrapper {

        private GZipServletOutputStream gzipOutputStream = null;
        private PrintWriter             printWriter      = null;

        public GZipServletResponseWrapper(HttpServletResponse response)
                throws IOException {
            super(response);
        }

        public void close() throws IOException {

            //PrintWriter.close does not throw exceptions. Thus, the call does not need
            //be inside a try-catch block.
            if (this.printWriter != null) {
                this.printWriter.close();
            }

            if (this.gzipOutputStream != null) {
                this.gzipOutputStream.close();
            }
        }


        /**
         * Flush OutputStream or PrintWriter
         *
         * @throws IOException
         */

        @Override
        public void flushBuffer() throws IOException {

            //PrintWriter.flush() does not throw exception
            if(this.printWriter != null) {
                this.printWriter.flush();
            }

            IOException exception1 = null;
            try{
                if(this.gzipOutputStream != null) {
                    this.gzipOutputStream.flush();
                }
            } catch(IOException e) {
                exception1 = e;
            }

            IOException exception2 = null;
            try {
                super.flushBuffer();
            } catch(IOException e){
                exception2 = e;
            }

            if(exception1 != null) throw exception1;
            if(exception2 != null) throw exception2;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (this.printWriter != null) {
                throw new IllegalStateException(
                    "PrintWriter obtained already - cannot get OutputStream");
            }
            if (this.gzipOutputStream == null) {
                this.gzipOutputStream = new GZipServletOutputStream(
                    getResponse().getOutputStream());
            }
            return this.gzipOutputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (this.printWriter == null && this.gzipOutputStream != null) {
                throw new IllegalStateException(
                    "OutputStream obtained already - cannot get PrintWriter");
            }
            if (this.printWriter == null) {
                this.gzipOutputStream = new GZipServletOutputStream(
                    getResponse().getOutputStream());
                this.printWriter      = new PrintWriter(new OutputStreamWriter(
                    this.gzipOutputStream, getResponse().getCharacterEncoding()));
            }
            return this.printWriter;
        }


        @Override
        public void setContentLength(int len) {
            //ignore, since content length of zipped content
            //does not match content length of unzipped content.
        }
    }
    
    class GZipServletOutputStream extends ServletOutputStream {
        private GZIPOutputStream    gzipOutputStream = null;

        public GZipServletOutputStream(OutputStream output)
                throws IOException {
            super();
            this.gzipOutputStream = new GZIPOutputStream(output);
        }

        @Override
        public void close() throws IOException {
            this.gzipOutputStream.close();
        }

        @Override
        public void flush() throws IOException {
            this.gzipOutputStream.flush();
        }

        @Override
        public void write(byte b[]) throws IOException {
            this.gzipOutputStream.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            this.gzipOutputStream.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            this.gzipOutputStream.write(b);
        }
        
        @Override
        public boolean isReady() {
            // TODO: always return true here as isReady is 3.x Servlet async support
            // which we do not use for now
            return true;
        }
        
        @Override
        public void setWriteListener(WriteListener writeListener) {
            // TODO: do nothing here, also 3.x Servlet async
        }
    }   

}    

