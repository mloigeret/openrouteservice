/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.servlet.requests;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
 
public class ByteStreamResponseWrapper extends HttpServletResponseWrapper
{
    private ByteArrayOutputStream byteStream;
 
    public ByteStreamResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }
 
    @Override
    public ServletOutputStream getOutputStream()
    {
        ServletOutputStreamImpl outputStream = null;
 
        this.byteStream =  (null == this.byteStream)
            ? new ByteArrayOutputStream() : this.byteStream;
        outputStream = new ServletOutputStreamImpl(this.byteStream);
 
        return (outputStream);
    }
 
    @Override
    public PrintWriter getWriter()
    {
        PrintWriter printWriter = null;
 
        this.byteStream =  (null == this.byteStream)
            ? new ByteArrayOutputStream() : this.byteStream;
        printWriter = new PrintWriter(this.byteStream);
 
        return (printWriter);
    }
 
    @Override
    public String toString()
    {
        return ((null == this.byteStream)
                ? null : new String(this.byteStream.toByteArray()));
    }
 
    public byte[] toBytes()
    {
        return ((null == this.byteStream)
                ? null : this.byteStream.toByteArray());
    }
    
	public class ServletOutputStreamImpl extends ServletOutputStream {
		private OutputStream _out;
		private byte[] _buffer;

		public ServletOutputStreamImpl(OutputStream out) {
			_out = out;
		}


		/**
		 * Writes a byte to the output stream.
		 */
		public final void write(int b) throws IOException {
			_out.write(b);
		}

		/**
		 * Writes a byte buffer to the output stream.
		 */
		public final void write(byte[] buf, int offset, int len)
				throws IOException {
			_out.write(buf, offset, len);
		}

		/**
		 * Prints a string to the stream. Note, this method does not properly
		 * handle character encoding.
		 * 
		 * @param s
		 *            the string to write.
		 */
		@Override
		public void print(String s) throws IOException {
			if (s == null)
				s = "null";

			OutputStream out = _out;

			{
				int length = s.length();

				if (_buffer == null)
					_buffer = new byte[128];

				byte[] buffer = _buffer;

				// server/0810
				int offset = 0;

				while (length > 0) {
					int sublen = buffer.length;
					if (length < sublen)
						sublen = length;

					for (int i = 0; i < sublen; i++) {
						buffer[i] = (byte) s.charAt(i + offset);
					}

					out.write(buffer, 0, sublen);

					length -= sublen;
					offset += sublen;
				}
			}
		}

		public final void flush() throws IOException {
			_out.flush();
		}

		public final void close() throws IOException {
		}

		public String toString() {
			return getClass().getSimpleName() + "[" + _out + "]";
		}


		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return true;
		}


		@Override
		public void setWriteListener(WriteListener writeListener) {
			// TODO Auto-generated method stub
			
		}
	}
}