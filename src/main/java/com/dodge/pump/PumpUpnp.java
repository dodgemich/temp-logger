package com.dodge.pump;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PumpUpnp {

	public void switchWemo(String switchName, String state) throws Exception {
		String auth = System.getProperty("LINKSYS_AUTH");

		List<String> switchIps = new ArrayList<String>();
		{
//
			// Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                    return null;
	                }
	                public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                }
	                public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                }
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
						// TODO Auto-generated method stub
						
					}
					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
							throws CertificateException {
						// TODO Auto-generated method stub
						
					}
	            }
	        };
	 
	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	 
	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };
	 
	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);	

			URL url = new URL("https://192.168.1.1/JNAP/");
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setHostnameVerifier(getHostnameVerifier());
			con.setRequestMethod("POST");
			con.addRequestProperty("X-JNAP-Action", "http://linksys.com/jnap/core/Transaction");
			con.addRequestProperty("X-JNAP-Authorization", auth);
			con.setConnectTimeout(400);
			con.setReadTimeout(10000);

			con.setDoOutput(true);
			String jsonInputString = "[{\"action\":\"http://linksys.com/jnap/devicelist/GetDevices3\",\"request\":{\"sinceRevision\":0}}]";
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
				ObjectMapper mapper = new ObjectMapper();
				JSONObject result = mapper.readValue(response.toString(), JSONObject.class);
				List respArr= (List)(result.get("responses"));
				HashMap o= (HashMap)(respArr.get(0));
				HashMap o2 = (HashMap)o.get("output");
				List<HashMap> devices = (List)o2.get("devices");
				
				for(HashMap device : devices) {
					HashMap model = (HashMap)device.get("model");
					String manufacturer = (String) model.get("manufacturer");
					String type = (String) model.get("deviceType");
					System.out.println(manufacturer + ":" + model+":"+type);
					if(manufacturer != null && manufacturer.contains("Belkin")) {
						List connections = (List)device.get("connections");
						HashMap switchConn = (HashMap) connections.get(0);
						String ip = (String) switchConn.get("ipAddress");
						switchIps.add(ip);
					}
				}
				
				}
			//
		}
		
		

		for (String ip : switchIps) {

			try {
				URL url = new URL("http://" + ip + ":49153/setup.xml");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				con.setConnectTimeout(400);
				con.setReadTimeout(1000);

				String resp = new String(con.getInputStream().readAllBytes());
				String friendlyName = resp.substring(resp.indexOf("<friendlyName>") + 14,
						resp.indexOf("</friendlyName>"));
				//System.out.println(friendlyName);
				System.out.println(ip + ":"+friendlyName);
				try {
					if (switchName.equals(friendlyName)) {
						URL post = new URL("http://" + ip + ":49153/upnp/control/basicevent1");

						HttpURLConnection postCon = (HttpURLConnection) post.openConnection();
						postCon.setRequestMethod("POST");
						postCon.setRequestProperty("SOAPACTION", "\"urn:Belkin:service:basicevent:1#SetBinaryState\"");
						postCon.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
						postCon.setRequestProperty("Accept", "");
						postCon.setDoOutput(true);

						String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
								+ "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
								+ "<s:Body>" + "<u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">"
								+ "<BinaryState>" + state + "</BinaryState>" + "</u:SetBinaryState>" + "</s:Body>"
								+ "</s:Envelope>";

						postCon.getOutputStream().write(body.getBytes());

						String postRes = new String(postCon.getInputStream().readAllBytes());
						System.out.println("resp:"+postRes);
						return;

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				 e.printStackTrace();
			}
		}
	}
	
	private static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        return hostnameVerifier;
    }
}
