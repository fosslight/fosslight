/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;

@Slf4j
public class ShellCommander extends CoTopComponent {
	
	public static void shellCommandWaitFor(String command) throws Exception {
		Process process = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try {
			process = Runtime.getRuntime().exec(command);
			is = process.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String line;
			
			while((line = br.readLine()) != null) {
				log.debug("ShellCommander : "+line);
			}
		} finally {
			if(br != null) { 
				try {br.close();} catch (Exception e) {}
			}
			
			if(isr != null) {
				try {isr.close();} catch (Exception e) {}
			}
			
			if(is != null) {
				try {is.close();} catch (Exception e) {}
			}
			
			if(process != null) {
				try {process.destroy();} catch (Exception e) {}
			}
		}
	}
	
	public static void shellCommandWaitFor(String[] command) throws Exception {
		Process process = null;
		
		try {
			process = Runtime.getRuntime().exec(command);
			process.waitFor(); 
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			throw new Exception(e);
		} finally {
			if (process != null) {
				try {
					process.destroy();
				} catch (Exception e2) {
				}
			}
		}
	}
	
}
