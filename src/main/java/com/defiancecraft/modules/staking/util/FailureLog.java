package com.defiancecraft.modules.staking.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Instant;

import com.defiancecraft.core.util.FileUtils;

public class FailureLog {

	/**
	 * Writes text to the log with the given level and current date.
	 * 
	 * @param level Level of severity
	 * @param text Text to write
	 * @return Whether the file was written successfully.
	 */
	public static boolean write(Level level, String text) {
		
		StringBuilder failure = new StringBuilder();
		failure.append("[");
		failure.append(level.name());
		failure.append("|");
		failure.append(Instant.now().toString());
		failure.append("]\n");
		failure.append(text);
		failure.append("\n");
		
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(FileUtils.getLogFile("staking.log")));
			out.append(failure.toString());
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	public static enum Level {
		
		INFO,
		WARN,
		SEVERE,
		CRITICAL;
		
	}
	
}
