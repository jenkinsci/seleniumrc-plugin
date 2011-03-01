/*Copyright (c) 2010, Parallels-NSU lab. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided 
that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions 
    * and the following disclaimer.
    
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
    * and the following disclaimer in the documentation and/or other materials provided with 
    * the distribution.
    
    * Neither the name of the Parallels-NSU lab nor the names of its contributors may be used to endorse 
    * or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package hpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class SeleniumManagerConnector {
	private volatile Hashtable <String, Integer> hashcodes;
	
	public SeleniumManagerConnector() {
		this.hashcodes = new Hashtable<String, Integer>();
	}
	
	public String sendGet(String host, int port, String browser, String id, String operatingSystem) {
		String url = "Connection failed";
		
		try {
			Socket seleniumManager = null;
			
			try {
				seleniumManager = new Socket(host, port);
				
				PrintWriter writer = new PrintWriter(
						seleniumManager.getOutputStream(), true);
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						seleniumManager.getInputStream()));
				
				String data = new String("getSelenium."+browser+"."+operatingSystem);
				
				writer.println(data);
				
				String answer = reader.readLine();
				
				if (!answer.equals("Connection failed")) {
					StringTokenizer tokenizer = new StringTokenizer(answer, " ");
				
					int hashcode = Integer.parseInt(tokenizer.nextToken());
				
					hashcodes.put(id, hashcode);
				
					url = tokenizer.nextToken();
				}
			} catch (IOException e) {
				url = "Connection failed";
			} finally {
				seleniumManager.close();
			}
			
			return url;
			
		} catch (Exception e) {
			return url;
		}
	}
	
	public void sendStop(String host, int port, String id) {
		try {
			Integer hashcode = hashcodes.remove(id);

			if (null != hashcode) {

				Socket seleniumManager = new Socket(host, port);

				PrintWriter writer = new PrintWriter(seleniumManager
						.getOutputStream(), true);

				String data = new String("stopSelenium." + hashcode);

				writer.println(data);

				seleniumManager.close();
			}
		} catch (Exception e) {
		}
	}
}
