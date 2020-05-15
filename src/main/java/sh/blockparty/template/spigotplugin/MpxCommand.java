package sh.blockparty.template.spigotplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;




public class MpxCommand implements CommandExecutor {
    SpigotPlugin plugin;

    public MpxCommand(SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        if (! cmdName.equals("mpx")) {
            return false;
        }

        try {
            final String q = URLEncoder.encode(String.join(" ", args), StandardCharsets.UTF_8.toString());

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    BufferedReader reader = null;
                    try {
                        String uuid = ((Player) sender).getUniqueId().toString();
                        String password = "password";
                        URL url = new URL("http://127.0.0.1:8222/api/minecraft/command?password="+password+"&uuid="+uuid+"&q="+q);
                        reader = new BufferedReader(new InputStreamReader(url.openStream()));
                        StringBuffer buffer = new StringBuffer();
                        int read;
                        char[] chars = new char[1024];
                        while ((read = reader.read(chars)) != -1) {
                            buffer.append(chars, 0, read); 
                        }

                        try {
                            JSONParser jsonParser = new JSONParser();
                            Object parsed = jsonParser.parse(buffer.toString());
                            JSONObject jsonObj = (JSONObject) parsed;

                            String smsg = (String) jsonObj.get("msg");
                            sender.sendMessage(smsg);

                            JSONArray msgs = (JSONArray) jsonObj.get("msgs");
                            if (msgs != null) {

                                for (Object m : msgs) {
                                    JSONObject mobj = (JSONObject) m;

                                    UUID muuid = UUID.fromString(((String) mobj.get("uuid")).replaceFirst(
                                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5" 
                                    ));
                                    String msg = (String) mobj.get("msg");

                                    Player mrecv = Bukkit.getServer().getPlayer(muuid);
                                    if (mrecv != null) {
                                        mrecv.sendMessage(msg);
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            sender.sendMessage("Sorry, error occurred");
                        }
                        // return buffer.toString();
                    } catch (IOException e) {
                        sender.sendMessage("Sorry, error occurred");
                    }

                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        // TODO log that is fucked
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            sender.sendMessage("UnsupportedEncodingException");
        }

        return true;
    }
}

