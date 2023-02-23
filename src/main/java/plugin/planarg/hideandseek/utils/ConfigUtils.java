package plugin.planarg.hideandseek.utils;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ConfigUtils {
    private File file;
    private JSONObject json;
    private JSONParser parser = new JSONParser();

    public ConfigUtils(File file) {
        this.file = file;
        reload();
    }

    public String getRawData(String key) {
        return json.get(key).toString();
    }

    public JSONArray getArray(String key) {
        return json.containsKey(key) ? (JSONArray) json.get(key) : new JSONArray();
    }

    public JSONObject Loc2Json(BlockPosition position) {
        JSONObject object = new JSONObject();
        object.put("x", position.getX());
        object.put("y", position.getY());
        object.put("z", position.getZ());
        return object;
    }

    public BlockPosition Json2Loc(JSONObject obj) {
        return new BlockPosition(Integer.parseInt(obj.get("x").toString()),
                Integer.parseInt(obj.get("y").toString()),
                Integer.parseInt(obj.get("z").toString()));
    }

    public List<BlockPosition> getLoc() {
        JSONArray array = getArray("locations");
        List<BlockPosition> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            result.add(Json2Loc((JSONObject)array.get(i)));
        }
        return result;
    }

    public void reload() {
        try {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdir();
            if (!file.exists()) {
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                JSONObject toSave = new JSONObject();
                JSONArray array = new JSONArray();
                array.add(Loc2Json(new BlockPosition(0, 0, 0)));
                toSave.put("locations", array);
                TreeMap<String, Object> treeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                treeMap.putAll(toSave);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String js = gson.toJson(treeMap);
                writer.write(js);
                writer.flush();
                writer.close();
            }
            json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
