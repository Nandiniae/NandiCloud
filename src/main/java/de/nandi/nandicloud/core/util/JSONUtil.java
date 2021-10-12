package de.nandi.nandicloud.core.util;

import de.nandi.nandicloud.core.main.NandiCloud;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONUtil {

	public static void saveJSON(JSONObject json, File file) {
		try {
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e) {
			NandiCloud.printError(e);
		}
		Object object = null;
		JSONParser parser = new JSONParser();

		try {
			object = parser.parse(new FileReader(file));
		} catch (IOException | ParseException ignored) {
		}
		JSONArray array;
		if (object instanceof JSONArray) {
			array = (JSONArray) object;
		} else {
			array = new JSONArray();
		}
		array.add(json);
		saveJSON(array, file);
	}

	public static void saveJSON(JSONArray json, File file) {
		try {
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e) {
			NandiCloud.printError(e);
		}
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(json.toJSONString());
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			NandiCloud.printError(e);
		}
	}

	public static JSONArray getJSON(File file) {
		Object object = null;
		JSONParser parser = new JSONParser();
		try {
			object = parser.parse(new FileReader(file));
		} catch (IOException | ParseException ignored) {
		}
		JSONArray array;
		if (object instanceof JSONArray) {
			array = (JSONArray) object;
		} else {
			array = new JSONArray();
		}

		return array;
	}


}
