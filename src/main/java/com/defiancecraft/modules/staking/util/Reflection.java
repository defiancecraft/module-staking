package com.defiancecraft.modules.staking.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

public class Reflection {

	public static String getVersion() {
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String version = name.substring(name.lastIndexOf('.') + 1) + ".";
		return version;
	}

	public static Class<?> getNMSClass(String className) {
		className = "net.minecraft.server." + getVersion() + className;
		try {
			return Class.forName(className);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Method getMethod(Class<?> clazz, String name) {
		return getMethod(clazz, name, new Class<?>[]{});
	}
	
	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			Method m = clazz.getDeclaredMethod(name, parameterTypes);
			m.setAccessible(true);
			return m;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Field getField(Class<?> clazz, String name) {
		try {
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return f;
		} catch (Exception e) {
			return null;
		}
	}
	
}
