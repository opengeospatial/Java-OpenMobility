package com.augtech.geoapi.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;


/** An Internet TCP/IP request using the DefaultHttpClient and a Get request.
 * The request can be built slowly adding parameters as key-value pairs, and then issued
 * to retrieve an InputStream.
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class WebRequest extends HashMap<String, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String USER_AGENT = "support@aug-tech.co.uk";
	
	protected String SERVER_URL;
	private String encodedPostAuth = null;

	/** Create a new HTTP get request. 
	 * 
	 * @param serverUrl The base server url, such as www.awila.co.uk
	 */
	public WebRequest(String serverUrl) {
		this.SERVER_URL = serverUrl;
	}

	
	@Override
	public String toString() {
		return getBuiltURL();
	}

	/** Get the full URL as a String
	 * 
	 * @return A string that could be copied to a browser
	 */
	public String getBuiltURL() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.SERVER_URL.replace(" ", "%20"));
		int i=0;
		for (Entry<String, Object> tmp : this.entrySet()) {
			if (i==0 && this.SERVER_URL.contains("?")==false) {
				sb.append("?");
			} else {
				sb.append("&");	
			}
			sb.append(tmp.getKey())
			.append("=")
			.append(tmp.getValue());
			i++;
		}
		return sb.toString();
	}
	/** Gets a new un-zipped InputStream from the currently compiled URL. If the response
	 * is known to be a string it is far more efficient to use {@link #openGetForString()}
	 * 
	 * @return A new InputStream
	 * @throws IOException 
	 */
	public InputStream openConnection() throws IOException {

		HttpEntity entity = null;
		try {
			entity = openGetForResponse().getEntity();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException( e.getMessage(), e.getCause() );
		}

		if (entity!=null) {
			return entity.getContent();
		} else {
			throw new IOException("Entity Null!");
		}
		
	}
	/** Gets a String from the currently compiled URL.<p>
	 * Obviously the request should be expected to return a String. If in doubt, 
	 * use {@link #openConnectionForStream()}
	 * 
	 * @return A new String containing the response or Null if there was an error
	 * @throws ParseException
	 * @throws IOException
	 */
	public String openGetForString() throws IOException {
		String ret = "";

		try {
			HttpEntity entity = openGetForResponse().getEntity();

			//ret = FileIO.getStreamToString( entity.getContent() );
			ret = EntityUtils.toString(entity);
					
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage(), e.getCause());
		}
		
		return ret;
	}
	/** Open the connection to the currently compiled URL and get a HttpResponse.<p>
	 * Response is automatically un-zipped if required.
	 * 
	 * @return A new unzipped response
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	private HttpResponse openGetForResponse() throws Exception  {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet( getBuiltURL() );
		getRequest.addHeader("Accept-Encoding", "gzip");
		getRequest.addHeader(HTTP.USER_AGENT, USER_AGENT);
		
		// TODO: Proper authentication passing with Base64
		if (get("username")!=null) {
			String encoded = "";
			getRequest.setHeader("Authorization", "Basic " + encoded);
			}

		// Add an 'Interceptor' to automatically unzip the content
		httpClient.addResponseInterceptor(
				new HttpResponseInterceptor() {
					public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							Header ceheader = entity.getContentEncoding();
							if (ceheader != null) {
								HeaderElement[] codecs = ceheader.getElements();
								for (int i = 0; i < codecs.length; i++) {
									if (codecs[i].getName().equalsIgnoreCase("gzip")) {
										response.setEntity( new GzipDecompressingEntity(response.getEntity()) );
										return;
									}
								}
							}
						}
					}
				});

		HttpResponse response = httpClient.execute(getRequest);	
		
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new Exception("Failed : HTTP error code : "
			   + response.getStatusLine().getReasonPhrase() );
		}

		return response;
	}
	/** Get a String via a post request as HTTPS
	 * 
	 * @param requestContent
	 * @return
	 * @throws Exception
	 */
	public String openHttpsPostForString(String requestContent) throws Exception {
		URL uri = new URL(SERVER_URL);

		HttpsURLConnection httpConnection = (HttpsURLConnection) uri.openConnection();

		httpConnection.setDoInput(true);
		httpConnection.setDoOutput(true);
		httpConnection.setUseCaches(false);
		// add request header
		httpConnection.setRequestMethod("POST");
		for (Map.Entry<String, Object> e : entrySet()) {
			
			if (e.getKey().toLowerCase().equals("authorization"))
				throw new Exception("Use setPostAuthorization() instead of a parameter");
			
			httpConnection.setRequestProperty( e.getKey(), e.getValue().toString() );
			
		}
		if (!containsKey("User-Agent")) httpConnection.setRequestProperty("User-Agent", USER_AGENT);
		if (!containsKey("Accept-Language")) httpConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (encodedPostAuth!=null) httpConnection.setRequestProperty("Authorization", encodedPostAuth);
		

		// Send post request
		DataOutputStream wr = new DataOutputStream(	httpConnection.getOutputStream() );
		if (requestContent!=null && !requestContent.equals("") ) wr.writeBytes(requestContent);
		wr.flush();
		wr.close();

		String result = null;

		BufferedReader in = new BufferedReader(new InputStreamReader(
				httpConnection.getInputStream()));
		
		if (in != null) {
			String inputLine;

			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
		
			in.close();

			result = response.toString();
		}
		
		return result;

	}

	/** Set the authorisation to use in {@link #openHttpsPostForString(String)}
	 * 
	 * @param type Authorisation type (i.e. Basic)
	 * @param value The value
	 * @return True if successfully set
	 */
	public boolean setPostAuthorization(String type, String value) {
		try {
			encodedPostAuth = type + " " +Base64.encodeToString( value.getBytes("UTF-8"), false );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	/** Set parameters for a WFS getCapabilities request
	 * 
	 * @param service
	 * @param version
	 */
	public void setGetCapabilities(String service, String version) {
		this.put("SERVICE", service);
		this.put("REQUEST", "GetCapabilities");
		this.put("Version", version);
	}
	/**
	 * {@link HttpEntityWrapper} for handling gzip Content Coded responses.
	 *
	 * @since 4.1
	 */
	class GzipDecompressingEntity extends DecompressingEntity {

	    /**
	     * Creates a new {@link GzipDecompressingEntity} which will wrap the specified
	     * {@link HttpEntity}.
	     *
	     * @param entity
	     *            the non-null {@link HttpEntity} to be wrapped
	     */
	    public GzipDecompressingEntity(final HttpEntity entity) {
	        super(entity);
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public InputStream getContent() throws IOException, IllegalStateException {

	        // the wrapped entity's getContent() decides about repeatability
	        InputStream wrappedin = wrappedEntity.getContent();

	        return new GZIPInputStream(wrappedin);
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public Header getContentEncoding() {

	        /* This HttpEntityWrapper has dealt with the Content-Encoding. */
	        return null;
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public long getContentLength() {

	        /* length of ungzipped content is not known */
	        return -1;
	    }

	}
	/**
	 * Common base class for decompressing {@link HttpEntity} implementations.
	 *
	 * @since 4.1
	 */
	abstract class DecompressingEntity extends HttpEntityWrapper {

	    /**
	     * Default buffer size.
	     */
	    private static final int BUFFER_SIZE = 1024 * 2;

	    /**
	     * Creates a new {@link DecompressingEntity}.
	     *
	     * @param wrapped
	     *            the non-null {@link HttpEntity} to be wrapped
	     */
	    public DecompressingEntity(final HttpEntity wrapped) {
	        super(wrapped);
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void writeTo(OutputStream outstream) throws IOException {
	        if (outstream == null) {
	            throw new IllegalArgumentException("Output stream may not be null");
	        }

	        InputStream instream = getContent();

	        byte[] buffer = new byte[BUFFER_SIZE];

	        int l;

	        while ((l = instream.read(buffer)) != -1) {
	            outstream.write(buffer, 0, l);
	        }
	    }
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((SERVER_URL == null) ? 0 : SERVER_URL.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebRequest other = (WebRequest) obj;
		if (SERVER_URL == null) {
			if (other.SERVER_URL != null)
				return false;
		} else if (!SERVER_URL.equals(other.SERVER_URL))
			return false;
		return true;
	}

	
}
