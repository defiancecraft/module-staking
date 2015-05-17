package com.defiancecraft.modules.staking.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

public class RawMessaging {

	private static final Class<?> NMS_CHATSERIALIZER = Reflection.getNMSClass("ChatSerializer");
	private static final Class<?> NMS_ICHATBASECOMPONENT = Reflection.getNMSClass("IChatBaseComponent");
	private static final Class<?> NMS_PACKETPLAYOUTCHAT = Reflection.getNMSClass("PacketPlayOutChat");
	private static final Class<?> NMS_PACKETPLAYOUTTITLE = Reflection.getNMSClass("PacketPlayOutTitle");
	private static final Class<?> NMS_ENUMTITLEACTION = Reflection.getNMSClass("EnumTitleAction");
	private static final Class<?> NMS_PACKET = Reflection.getNMSClass("Packet");
	
	private static String getColorName(char color) {
	
		color = Character.toLowerCase(color);
		
		switch (color) {
		case '0': return "black";
		case '1': return "dark_blue";
		case '2': return "dark_green";
		case '3': return "dark_aqua";
		case '4': return "dark_red";
		case '5': return "dark_purple";
		case '6': return "gold";
		case '7': return "gray";
		case '8': return "dark_gray";
		case '9': return "blue";
		case 'a': return "green";
		case 'b': return "aqua";
		case 'c': return "red";
		case 'd': return "light_purple";
		case 'e': return "yellow";
		case 'f': return "white";
		default:
			return "";
		}
		
	}
	
	/**
	 * Sends a raw message to a player with the same formatting and colours
	 * as message (encoded with ampersands), and a click event to run given command.
	 * 
	 * @param p Player to send message to
	 * @param message Message to send
	 * @param command Command to run, prefixed with /
	 */
	public static void sendRawMessage(Player p, String message, String command) {
		
		Pattern pat = Pattern.compile("((?:&[a-fA-FlmnokrLMNOKR0-9])+)([^&]*)|([^&]*)"); // Matches colour codes/pairs of codes and text up to another ampersand
		Matcher m = pat.matcher(message);
		Action action = new Action("run_command", command);
		List<RawJsonMessage> msgs = new ArrayList<RawJsonMessage>();
		
		while (m.find()) {
			
			RawJsonMessage msg = new RawJsonMessage();
			
			if (m.group(1) != null && !m.group(1).isEmpty()) {
				Arrays.stream(m.group(1).split("&"))
					.filter((s) -> s != null && !s.isEmpty())
					.forEach((s) -> {
						
						if (s.matches("[a-fA-F0-9]"))
							msg.color = getColorName(s.charAt(0));
						else if (s.equalsIgnoreCase("k"))
							msg.obfuscated = true;
						else if (s.equalsIgnoreCase("l"))
							msg.bold = true;
						else if (s.equalsIgnoreCase("m"))
							msg.strikethrough = true;
						else if (s.equalsIgnoreCase("n"))
							msg.underlined = true;
						else if (s.equalsIgnoreCase("o"))
							msg.italic = true;
						else if (s.equalsIgnoreCase("r")) {
							msg.color = "white";
							msg.bold = false;
							msg.italic = false;
							msg.obfuscated = false;
							msg.underlined = false;
							msg.strikethrough = false;
						}
							
					});
			}
			
			if (m.group(2) != null && !m.group(2).isEmpty())
				msg.text = m.group(2);
			else if (m.group(3) != null && !m.group(3).isEmpty())
				msg.text = m.group(3);
			else
				continue;
			
			msg.clickEvent = action;
			msgs.add(msg);
			
		}
		
		sendRawMessageJson(p, new Gson().toJson(msgs));
		
	}
	
	private static void sendRawMessageJson(Player player, String json) {
		try {
			
			Object handle = Reflection.getMethod(player.getClass(), "getHandle").invoke(player, new Object[]{});
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object serialized = Reflection.getMethod(NMS_CHATSERIALIZER, "a", String.class).invoke(null, json);
			Object packet = NMS_PACKETPLAYOUTCHAT.getConstructor(NMS_ICHATBASECOMPONENT).newInstance(serialized);
			Reflection.getMethod(connection.getClass(), "sendPacket", NMS_PACKET).invoke(connection, packet);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a 'title' to the player using the Minecraft 1.8 title feature.
	 * <br>
	 * The title will fade in for 5 ticks, stay for 10, and
	 * fade out for 5 ticks (total 20 ticks/1 second).
	 * <br><br>
	 * Titles may have colour codes, which can be given with the {@link org.bukkit.ChatColor}
	 * class.
	 * @param player Player to send title to
	 * @param title The text of the title to send to the player
	 */
	public static void sendTitle(Player player, String title) {
		sendTitle(player, 5, 10, 5, title, null);
	}
	
	/**
	 * Sends a 'title' to the player using the Minecraft 1.8 title
	 * feature.
	 * <br><br>
	 * Titles may have colour codes, which can be given with the {@link org.bukkit.ChatColor}
	 * class.
	 * 
	 * @param player Player to send title to
	 * @param fadeIn The time in <b>ticks</b> for the title to fade in
	 * @param stay The time in <b>ticks</b> for the title to remain at 100% opacity (after fading in)
	 * @param fadeOut The time in <b>ticks</b> for the title to fade out
	 * @param title The text of the title to send to the player
	 */
	public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title) {
		sendTitle(player, fadeIn, stay, fadeOut, title, null);
	}
	
	/**
	 * Sends a 'title' and accompanying subtitle to the player using the
	 * Minecraft 1.8 title feature.
	 * <br><br>
	 * Titles and subtitles may have colour codes, which can be given with
	 * the {@link org.bukkit.ChatColor} class.
	 * 
	 * @param player Player to send title to
	 * @param fadeIn The time in <b>ticks</b> for the title to fade in
	 * @param stay The time in <b>ticks</b> for the title to remain at 100% opacity (after fading in)
	 * @param fadeOut The time in <b>ticks</b> for the title to fade out
	 * @param title The text of the title to send to the player
	 * @param subtitle The text of the subtitle to send to the player at the same time as the title
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle) {
		
		try {
			
			// Get the player's handle and connection.
			Object handle = Reflection.getMethod(player.getClass(), "getHandle").invoke(player, new Object[]{});
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			
			// Send title if it was passed, inputting timing parameters too.
			if (title != null) {
				
				// Serialize text (in JSON format -> IChatBaseComponent)
				Object serialized  = Reflection
						.getMethod(NMS_CHATSERIALIZER, "a", String.class)
						.invoke(null, new Gson().toJson(new BareMessage(title)));
				Object titleEnum   = Enum.valueOf((Class<Enum>)NMS_ENUMTITLEACTION, "TITLE");
				Object titlePacket = NMS_PACKETPLAYOUTTITLE
						.getConstructor(NMS_ENUMTITLEACTION, NMS_ICHATBASECOMPONENT, int.class, int.class, int.class)
						.newInstance(titleEnum, serialized, fadeIn, stay, fadeOut);
				
				// Send packet
				Reflection.getMethod(connection.getClass(), "sendPacket", NMS_PACKET).invoke(connection, titlePacket);
				
			}
			
			// Send subtitle if it was passed to the method
			if (subtitle != null) {
				
				// Serialize text
				Object serialized  = Reflection
						.getMethod(NMS_CHATSERIALIZER, "a", String.class)
						.invoke(null, new Gson().toJson(new BareMessage(subtitle)));
				Object titleEnum   = Enum.valueOf((Class<Enum>)NMS_ENUMTITLEACTION, "SUBTITLE");
				Object titlePacket;
				
				// If timing parameters already sent, only send text
				if (title != null) {
					titlePacket = NMS_PACKETPLAYOUTTITLE
						.getConstructor(NMS_ENUMTITLEACTION, NMS_ICHATBASECOMPONENT)
						.newInstance(titleEnum, serialized);
				} else {
					titlePacket = NMS_PACKETPLAYOUTTITLE
							.getConstructor(NMS_ENUMTITLEACTION, NMS_ICHATBASECOMPONENT, int.class, int.class, int.class)
							.newInstance(titleEnum, serialized, fadeIn, stay, fadeOut);
				}
				
				// Send packet
				Reflection.getMethod(connection.getClass(), "sendPacket", NMS_PACKET).invoke(connection, titlePacket);
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static class RawJsonMessage {
		
		public String text;
		public String color;
		public boolean bold;
		public boolean underlined;
		public boolean italic;
		public boolean strikethrough;
		public boolean obfuscated;
		public Action clickEvent;
		public Action hoverEvent;
		
	}
	
	public static class BareMessage {
		
		public String text;
	
		public BareMessage(String text) {
			this.text = text;
		}
		
	}
	
	public static class Action {
		
		public String action;
		public String value;
		
		public Action(String action, String value) {
			this.action = action;
			this.value = value;
		}
		
	}
	
}
