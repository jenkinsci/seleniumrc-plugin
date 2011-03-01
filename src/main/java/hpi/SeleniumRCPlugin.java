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

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Map;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class SeleniumRCPlugin extends BuildWrapper {
	private String host;
	private int port;
	private String browser;
	private static final SeleniumManagerConnector connector = new SeleniumManagerConnector();
	private String operatingSystem;

	@DataBoundConstructor
	public SeleniumRCPlugin(String host, int port, String browser, String operatingSystem) {
		this.host = host;
		this.port = port;
		this.browser = browser;
		this.operatingSystem = operatingSystem;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public String getBrowser() {
		return browser;
	}
	
	public String getOperatingSystem() {
		return operatingSystem;
	}

	@Override
	public Environment setUp(AbstractBuild build, final Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException 
	{		
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener)
					throws IOException, InterruptedException {
				
				connector.sendStop(host, port, build.getId());
				return true;
			}
		};
	}
	
	@Override
	public void makeBuildVariables(AbstractBuild build, Map<String,String> variables) {
		String url = connector.sendGet(host, port, browser, build.getId(), operatingSystem);
		variables.put(new String("selenium"), url);
    }

    @Override
    public Environment setUp(Build build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return setUp(build, launcher, listener);
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        public DescriptorImpl() {
            super(SeleniumRCPlugin.class);
        }
        
        public FormValidation doCheckHost(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (0 == value.length()) {
            	return FormValidation.error("Empty host");
            }
        	return FormValidation.ok();
        }
        
        public FormValidation doCheckPort(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (0 == value.length()) {
            	return FormValidation.error("Empty port");
            }
            try {
            	int port = Integer.parseInt(value);
            	if (port < 0 || port > 65535) {
            		return FormValidation.error("Wrong port. Should be 0 <= port <= 65535");
            	}
            }
            catch (NumberFormatException e) {
            	return FormValidation.error("Wrong port. Should be numerical value");
			}
        	return FormValidation.ok();
        }
        
        public FormValidation doCheckBrowser(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (0 == value.length()) {
            	return FormValidation.error("Empty browser");
            }
        	return FormValidation.ok();
        }
        
        public FormValidation doCheckOperatingSystem(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (0 == value.length()) {
            	return FormValidation.error("Empty operatingSystem");
            }
        	return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Create Selenium RC instance";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }
}
