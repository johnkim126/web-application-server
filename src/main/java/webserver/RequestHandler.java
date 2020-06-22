package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }
    
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	String url = "/index.html";
        	boolean isIndexEntered = checkedStringFromHeader(in, url);
        	
        	byte[] body;
        	if (isIndexEntered) {
        		body = Files.readAllBytes(new File("./webapp" + url).toPath());       
        	} else {
        		body = "Hello World".getBytes();
        	}
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private boolean checkedStringFromHeader(InputStream in, String compareStr) throws IOException {
    	InputStreamReader	isreader;
    	BufferedReader		br;
    	String				buf;
    	String[]			tokens;
    	
    	isreader = new InputStreamReader(in);
    	br = new BufferedReader(isreader);
    	
    	buf = br.readLine();
    	tokens = buf.split(" ");
    	
    	for (int i = 0; i < tokens.length; i++) {
    		if (tokens[i].equals(compareStr)) {
    			return true;
    		}
    	}
    	return false;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
