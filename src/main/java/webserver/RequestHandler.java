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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import model.User;

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
        	process(in, out);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response(OutputStream out, int header, byte[] body) {
    	DataOutputStream	dostream;
    	dostream = new DataOutputStream(out);
    	
    	switch (header) {
    	case 302:
    		response302Header(dostream, body.length);
    	case 200:
    	default:
    		response200Header(dostream, body.length);
    		break;
    	}
        responseBody(dostream, body);
    }
    
    private void process(InputStream in, OutputStream out) throws IOException {
    	InputStreamReader	isreader;
    	BufferedReader		br;
    	String				buf, url;
    	String[]			tokens;
    	byte[]				retBody;
    	
    	retBody = "Hello World".getBytes();
    	
    	isreader = new InputStreamReader(in);
    	br = new BufferedReader(isreader);
    	
    	buf = br.readLine();
    	System.out.println(buf);
  		       
    	tokens = buf.split(" ");
    	if (tokens == null) {
    		response(out, 200, retBody);
    		return;
    	}
   	
    	int status = 200;
    	url = tokens[1];
    	if (url.startsWith("/user/create")) {
    		System.out.println(tokens[1]);
    		int index = url.indexOf("?");
    		
			String requestPath = url.substring(0, index);
			String params = url.substring(index + 1);
			System.out.println("requestPath: " + requestPath);
			System.out.println("params: " + params);
			
			Map<String, String> paramMap = HttpRequestUtils.parseQueryString(params);
			String userId = paramMap.get("userId");
			String password = paramMap.get("password");
    		String userName = paramMap.get("name");
    		String email = paramMap.get("email");
    			
    		User newUser = new User(userId, password, userName, email);
    		retBody = Files.readAllBytes(new File("./webapp" + "/index.html").toPath());
    		status = 302;
    	} else if (!url.equals("/")){
    		retBody = Files.readAllBytes(new File("./webapp" + url).toPath());
   		}
    	response(out, status, retBody);
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
    
    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
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
