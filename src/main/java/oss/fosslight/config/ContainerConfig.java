/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.config;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroupDescriptorImpl;
import org.apache.tomcat.util.descriptor.web.TaglibDescriptorImpl;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

import oss.fosslight.common.CoConstDef;

@Configuration
public class ContainerConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		
		 ((TomcatServletWebServerFactory)factory).addContextCustomizers(new TomcatContextCustomizer(){
	            JspConfigDescriptor jspConfigDescriptor = new JspConfigDescriptor() {
	            	// set custom taglib
	                @Override
	                public Collection<TaglibDescriptor> getTaglibs() {
	                    TaglibDescriptor descriptor = new TaglibDescriptorImpl(CoConstDef.COMM_TLD_URI, CoConstDef.COMM_TLD_PATH);
	                    
	                    return Collections.singletonList(descriptor);
	                }
	                
	                // set jsp pattern
	                @Override
	                public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
	                    JspPropertyGroup group = new JspPropertyGroup();
	                    group.addUrlPattern( CoConstDef.JSP_PROPERTY_URL_PATTERN );
	                    group.setPageEncoding("UTF-8");
	                    
	                    JspPropertyGroupDescriptor descriptor = new JspPropertyGroupDescriptorImpl(group);
	                    
	                    return Collections.singletonList(descriptor);
	                }
	            };
	            
	            @Override
	            public void customize(Context context) {
	                context.setJspConfigDescriptor(jspConfigDescriptor);
	            }
	        });
	}
}
