/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompressUtil {
    public static void compressGZIP(File input, File output, boolean deleteInputFile) throws IOException {
        try (GzipCompressorOutputStream out = new GzipCompressorOutputStream(new FileOutputStream(output))){
            IOUtils.copy(new FileInputStream(input), out);
        }
        
        if(deleteInputFile) {
        	try {
        		input.deleteOnExit();
        	} catch(Exception e) {
        		
        	}
        }
    }

    public static void decompressGZIP(File input, File output, boolean deleteInputFile) {
    	GzipCompressorInputStream zin = null;
    	FileInputStream in = null;
    	FileOutputStream out = null;
    	
        try {
        	in = new FileInputStream(input);
        	zin = new GzipCompressorInputStream(in);
        	out = new FileOutputStream(output);
            IOUtils.copy(in, out);
            
            if(deleteInputFile) {
            	try {
            		input.deleteOnExit();
            	} catch(Exception e) {
            		
            	}
            }
        } catch(Exception e) {
        	log.debug(e.getMessage());
        } finally {
			if(in != null) {
				try {
					in.close();
				} catch (Exception e) {}
			}
			
			if(zin != null) {
				try {
					zin.close();
				} catch (Exception e) {}
			}
			
			if(out != null) {
				try {
					out.close();
				} catch (Exception e2) {}
			}
		}
    }
    
    public static void decompressTarGZ(File tarFile, String dest) throws IOException {
    	File dir = new File(dest);
    	
    	if(!dir.exists()) {
    		dir.mkdirs();
    	}
    	
		TarArchiveInputStream tarIn = null;
		 
		tarIn = new TarArchiveInputStream(
					new GzipCompressorInputStream(
						new BufferedInputStream(
							new FileInputStream(tarFile)
						)
					)
				);
		
		TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
		
		while (tarEntry != null) {
			File destPath = new File(dest, tarEntry.getName());
			
			if (tarEntry.isDirectory()) { // tar.gz의 하위 file이 dir일 경우 dir 생성
				destPath.mkdirs();
			} else { // tar.gz의 하위 file이 dir가 아닐경우 file로 생성
				destPath.createNewFile();
				byte [] btoRead = new byte[1024];
				BufferedOutputStream bout = 
				new BufferedOutputStream(new FileOutputStream(destPath));
				int len = 0;
				 
				while((len = tarIn.read(btoRead)) != -1){
					bout.write(btoRead,0,len);
				}
				 
				bout.close();
				btoRead = null;
				 
			}
			
			tarEntry = tarIn.getNextTarEntry();
		}
		
		tarIn.close();
	}
}
