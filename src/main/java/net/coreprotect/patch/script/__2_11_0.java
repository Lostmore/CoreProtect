package net.coreprotect.patch.script;

import java.sql.Statement;
import net.coreprotect.model.Config;
import org.bukkit.Art;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class __2_11_0 {
   protected static boolean patch(Statement statement) {
      try {
         if ((Integer)Config.config.get("use-mysql") == 1) {
            statement.executeUpdate("START TRANSACTION");
         } else {
            statement.executeUpdate("BEGIN TRANSACTION");
         }

         Art[] var1 = Art.values();
         int var2 = var1.length;

         int var3;
         Integer type;
         String name;
         for(var3 = 0; var3 < var2; ++var3) {
            Art artType = var1[var3];
            type = artType.getId();
            name = artType.toString().toLowerCase();
            statement.executeUpdate("INSERT INTO " + Config.prefix + "art_map (id, art) VALUES ('" + type + "', '" + name + "')");
            Config.art.put(name, type);
            Config.art_reversed.put(type, name);
            if (type > Config.art_id) {
               Config.art_id = type;
            }
         }

         EntityType[] var8 = EntityType.values();
         var2 = var8.length;

         for(var3 = 0; var3 < var2; ++var3) {
            EntityType entityType = var8[var3];
            type = Integer.valueOf(entityType.getTypeId());
            name = entityType.toString().toLowerCase();
            statement.executeUpdate("INSERT INTO " + Config.prefix + "entity_map (id, entity) VALUES ('" + type + "', '" + name + "')");
            Config.entities.put(name, type);
            Config.entities_reversed.put(type, name);
            if (type > Config.entity_id) {
               Config.entity_id = type;
            }
         }

         Material[] var9 = Material.values();
         var2 = var9.length;

         for(var3 = 0; var3 < var2; ++var3) {
            Material material = var9[var3];
            type = material.getId();
            name = material.toString().toLowerCase();
            statement.executeUpdate("INSERT INTO " + Config.prefix + "material_map (id, material) VALUES ('" + type + "', '" + name + "')");
            Config.materials.put(name, type);
            Config.materials_reversed.put(type, name);
            if (type > Config.material_id) {
               Config.material_id = type;
            }
         }

         if ((Integer)Config.config.get("use-mysql") == 1) {
            statement.executeUpdate("COMMIT");
         } else {
            statement.executeUpdate("COMMIT TRANSACTION");
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return true;
   }
}
