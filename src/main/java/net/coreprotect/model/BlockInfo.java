package net.coreprotect.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;

public class BlockInfo {
   public static List<Material> track_any;
   public static List<Material> track_top;
   public static List<Material> track_side;
   public static List<Material> shulker_boxes;
   public static List<Material> containers;
   public static List<Material> falling_block_types;
   public static List<Material> non_attachable;
   public static List<Material> natural_blocks;
   public static Map<String, Integer> legacy_block_ids;
   public static Map<Integer, String> legacy_block_names;

   protected static void loadData() {
      Map<Integer, String> n = legacy_block_names;
      Map<String, Integer> i = legacy_block_ids;
      n.put(0, "minecraft:air");
      n.put(1, "minecraft:stone");
      n.put(2, "minecraft:grass");
      n.put(3, "minecraft:dirt");
      n.put(4, "minecraft:cobblestone");
      n.put(5, "minecraft:planks");
      n.put(6, "minecraft:sapling");
      n.put(7, "minecraft:bedrock");
      n.put(8, "minecraft:flowing_water");
      n.put(9, "minecraft:water");
      n.put(10, "minecraft:flowing_lava");
      n.put(11, "minecraft:lava");
      n.put(12, "minecraft:sand");
      n.put(13, "minecraft:gravel");
      n.put(14, "minecraft:gold_ore");
      n.put(15, "minecraft:iron_ore");
      n.put(16, "minecraft:coal_ore");
      n.put(17, "minecraft:log");
      n.put(18, "minecraft:leaves");
      n.put(19, "minecraft:sponge");
      n.put(20, "minecraft:glass");
      n.put(21, "minecraft:lapis_ore");
      n.put(22, "minecraft:lapis_block");
      n.put(23, "minecraft:dispenser");
      n.put(24, "minecraft:sandstone");
      n.put(25, "minecraft:noteblock");
      n.put(26, "minecraft:bed");
      n.put(27, "minecraft:golden_rail");
      n.put(28, "minecraft:detector_rail");
      n.put(29, "minecraft:sticky_piston");
      n.put(30, "minecraft:web");
      n.put(31, "minecraft:tallgrass");
      n.put(32, "minecraft:deadbush");
      n.put(33, "minecraft:piston");
      n.put(34, "minecraft:piston_head");
      n.put(35, "minecraft:wool");
      n.put(36, "minecraft:piston_extension");
      n.put(37, "minecraft:yellow_flower");
      n.put(38, "minecraft:red_flower");
      n.put(39, "minecraft:brown_mushroom");
      n.put(40, "minecraft:red_mushroom");
      n.put(41, "minecraft:gold_block");
      n.put(42, "minecraft:iron_block");
      n.put(43, "minecraft:double_stone_slab");
      n.put(44, "minecraft:stone_slab");
      n.put(45, "minecraft:brick_block");
      n.put(46, "minecraft:tnt");
      n.put(47, "minecraft:bookshelf");
      n.put(48, "minecraft:mossy_cobblestone");
      n.put(49, "minecraft:obsidian");
      n.put(50, "minecraft:torch");
      n.put(51, "minecraft:fire");
      n.put(52, "minecraft:mob_spawner");
      n.put(53, "minecraft:oak_stairs");
      n.put(54, "minecraft:chest");
      n.put(55, "minecraft:redstone_wire");
      n.put(56, "minecraft:diamond_ore");
      n.put(57, "minecraft:diamond_block");
      n.put(58, "minecraft:crafting_table");
      n.put(59, "minecraft:wheat");
      n.put(60, "minecraft:farmland");
      n.put(61, "minecraft:furnace");
      n.put(62, "minecraft:lit_furnace");
      n.put(63, "minecraft:standing_sign");
      n.put(64, "minecraft:wooden_door");
      n.put(65, "minecraft:ladder");
      n.put(66, "minecraft:rail");
      n.put(67, "minecraft:stone_stairs");
      n.put(68, "minecraft:wall_sign");
      n.put(69, "minecraft:lever");
      n.put(70, "minecraft:stone_pressure_plate");
      n.put(71, "minecraft:iron_door");
      n.put(72, "minecraft:wooden_pressure_plate");
      n.put(73, "minecraft:redstone_ore");
      n.put(74, "minecraft:lit_redstone_ore");
      n.put(75, "minecraft:unlit_redstone_torch");
      n.put(76, "minecraft:redstone_torch");
      n.put(77, "minecraft:stone_button");
      n.put(78, "minecraft:snow_layer");
      n.put(79, "minecraft:ice");
      n.put(80, "minecraft:snow");
      n.put(81, "minecraft:cactus");
      n.put(82, "minecraft:clay");
      n.put(83, "minecraft:reeds");
      n.put(84, "minecraft:jukebox");
      n.put(85, "minecraft:fence");
      n.put(86, "minecraft:pumpkin");
      n.put(87, "minecraft:netherrack");
      n.put(88, "minecraft:soul_sand");
      n.put(89, "minecraft:glowstone");
      n.put(90, "minecraft:portal");
      n.put(91, "minecraft:lit_pumpkin");
      n.put(92, "minecraft:cake");
      n.put(93, "minecraft:unpowered_repeater");
      n.put(94, "minecraft:powered_repeater");
      n.put(95, "minecraft:stained_glass");
      n.put(96, "minecraft:trapdoor");
      n.put(97, "minecraft:monster_egg");
      n.put(98, "minecraft:stonebrick");
      n.put(99, "minecraft:brown_mushroom_block");
      n.put(100, "minecraft:red_mushroom_block");
      n.put(101, "minecraft:iron_bars");
      n.put(102, "minecraft:glass_pane");
      n.put(103, "minecraft:melon_block");
      n.put(104, "minecraft:pumpkin_stem");
      n.put(105, "minecraft:melon_stem");
      n.put(106, "minecraft:vine");
      n.put(107, "minecraft:fence_gate");
      n.put(108, "minecraft:brick_stairs");
      n.put(109, "minecraft:stone_brick_stairs");
      n.put(110, "minecraft:mycelium");
      n.put(111, "minecraft:waterlily");
      n.put(112, "minecraft:nether_brick");
      n.put(113, "minecraft:nether_brick_fence");
      n.put(114, "minecraft:nether_brick_stairs");
      n.put(115, "minecraft:nether_wart");
      n.put(116, "minecraft:enchanting_table");
      n.put(117, "minecraft:brewing_stand");
      n.put(118, "minecraft:cauldron");
      n.put(119, "minecraft:end_portal");
      n.put(120, "minecraft:end_portal_frame");
      n.put(121, "minecraft:end_stone");
      n.put(122, "minecraft:dragon_egg");
      n.put(123, "minecraft:redstone_lamp");
      n.put(124, "minecraft:lit_redstone_lamp");
      n.put(125, "minecraft:double_wooden_slab");
      n.put(126, "minecraft:wooden_slab");
      n.put(127, "minecraft:cocoa");
      n.put(128, "minecraft:sandstone_stairs");
      n.put(129, "minecraft:emerald_ore");
      n.put(130, "minecraft:ender_chest");
      n.put(131, "minecraft:tripwire_hook");
      n.put(132, "minecraft:tripwire");
      n.put(133, "minecraft:emerald_block");
      n.put(134, "minecraft:spruce_stairs");
      n.put(135, "minecraft:birch_stairs");
      n.put(136, "minecraft:jungle_stairs");
      n.put(137, "minecraft:command_block");
      n.put(138, "minecraft:beacon");
      n.put(139, "minecraft:cobblestone_wall");
      n.put(140, "minecraft:flower_pot");
      n.put(141, "minecraft:carrots");
      n.put(142, "minecraft:potatoes");
      n.put(143, "minecraft:wooden_button");
      n.put(144, "minecraft:skull");
      n.put(145, "minecraft:anvil");
      n.put(146, "minecraft:trapped_chest");
      n.put(147, "minecraft:light_weighted_pressure_plate");
      n.put(148, "minecraft:heavy_weighted_pressure_plate");
      n.put(149, "minecraft:unpowered_comparator");
      n.put(150, "minecraft:powered_comparator");
      n.put(151, "minecraft:daylight_detector");
      n.put(152, "minecraft:redstone_block");
      n.put(153, "minecraft:quartz_ore");
      n.put(154, "minecraft:hopper");
      n.put(155, "minecraft:quartz_block");
      n.put(156, "minecraft:quartz_stairs");
      n.put(157, "minecraft:activator_rail");
      n.put(158, "minecraft:dropper");
      n.put(159, "minecraft:stained_hardened_clay");
      n.put(160, "minecraft:stained_glass_pane");
      n.put(161, "minecraft:leaves2");
      n.put(162, "minecraft:log2");
      n.put(163, "minecraft:acacia_stairs");
      n.put(164, "minecraft:dark_oak_stairs");
      n.put(165, "minecraft:slime");
      n.put(166, "minecraft:barrier");
      n.put(167, "minecraft:iron_trapdoor");
      n.put(168, "minecraft:prismarine");
      n.put(169, "minecraft:sea_lantern");
      n.put(170, "minecraft:hay_block");
      n.put(171, "minecraft:carpet");
      n.put(172, "minecraft:hardened_clay");
      n.put(173, "minecraft:coal_block");
      n.put(174, "minecraft:packed_ice");
      n.put(175, "minecraft:double_plant");
      n.put(176, "minecraft:standing_banner");
      n.put(177, "minecraft:wall_banner");
      n.put(178, "minecraft:daylight_detector_inverted");
      n.put(179, "minecraft:red_sandstone");
      n.put(180, "minecraft:red_sandstone_stairs");
      n.put(181, "minecraft:double_stone_slab2");
      n.put(182, "minecraft:stone_slab2");
      n.put(183, "minecraft:spruce_fence_gate");
      n.put(184, "minecraft:birch_fence_gate");
      n.put(185, "minecraft:jungle_fence_gate");
      n.put(186, "minecraft:dark_oak_fence_gate");
      n.put(187, "minecraft:acacia_fence_gate");
      n.put(188, "minecraft:spruce_fence");
      n.put(189, "minecraft:birch_fence");
      n.put(190, "minecraft:jungle_fence");
      n.put(191, "minecraft:dark_oak_fence");
      n.put(192, "minecraft:acacia_fence");
      n.put(193, "minecraft:spruce_door");
      n.put(194, "minecraft:birch_door");
      n.put(195, "minecraft:jungle_door");
      n.put(196, "minecraft:acacia_door");
      n.put(197, "minecraft:dark_oak_door");
      n.put(198, "minecraft:end_rod");
      n.put(199, "minecraft:chorus_plant");
      n.put(200, "minecraft:chorus_flower");
      n.put(201, "minecraft:purpur_block");
      n.put(202, "minecraft:purpur_pillar");
      n.put(203, "minecraft:purpur_stairs");
      n.put(204, "minecraft:purpur_double_slab");
      n.put(205, "minecraft:purpur_slab");
      n.put(206, "minecraft:end_bricks");
      n.put(207, "minecraft:beetroots");
      n.put(208, "minecraft:grass_path");
      n.put(209, "minecraft:end_gateway");
      n.put(210, "minecraft:repeating_command_block");
      n.put(211, "minecraft:chain_command_block");
      n.put(212, "minecraft:frosted_ice");
      n.put(213, "minecraft:magma");
      n.put(214, "minecraft:nether_wart_block");
      n.put(215, "minecraft:red_nether_brick");
      n.put(216, "minecraft:bone_block");
      n.put(217, "minecraft:structure_void");
      n.put(218, "minecraft:observer");
      n.put(219, "minecraft:white_shulker_box");
      n.put(220, "minecraft:orange_shulker_box");
      n.put(221, "minecraft:magenta_shulker_box");
      n.put(222, "minecraft:light_blue_shulker_box");
      n.put(223, "minecraft:yellow_shulker_box");
      n.put(224, "minecraft:lime_shulker_box");
      n.put(225, "minecraft:pink_shulker_box");
      n.put(226, "minecraft:gray_shulker_box");
      n.put(227, "minecraft:light_gray_shulker_box");
      n.put(228, "minecraft:cyan_shulker_box");
      n.put(229, "minecraft:purple_shulker_box");
      n.put(230, "minecraft:blue_shulker_box");
      n.put(231, "minecraft:brown_shulker_box");
      n.put(232, "minecraft:green_shulker_box");
      n.put(233, "minecraft:red_shulker_box");
      n.put(234, "minecraft:black_shulker_box");
      n.put(255, "minecraft:structure_block");
      n.put(256, "minecraft:iron_shovel");
      n.put(257, "minecraft:iron_pickaxe");
      n.put(258, "minecraft:iron_axe");
      n.put(259, "minecraft:flint_and_steel");
      n.put(260, "minecraft:apple");
      n.put(261, "minecraft:bow");
      n.put(262, "minecraft:arrow");
      n.put(263, "minecraft:coal");
      n.put(264, "minecraft:diamond");
      n.put(265, "minecraft:iron_ingot");
      n.put(266, "minecraft:gold_ingot");
      n.put(267, "minecraft:iron_sword");
      n.put(268, "minecraft:wooden_sword");
      n.put(269, "minecraft:wooden_shovel");
      n.put(270, "minecraft:wooden_pickaxe");
      n.put(271, "minecraft:wooden_axe");
      n.put(272, "minecraft:stone_sword");
      n.put(273, "minecraft:stone_shovel");
      n.put(274, "minecraft:stone_pickaxe");
      n.put(275, "minecraft:stone_axe");
      n.put(276, "minecraft:diamond_sword");
      n.put(277, "minecraft:diamond_shovel");
      n.put(278, "minecraft:diamond_pickaxe");
      n.put(279, "minecraft:diamond_axe");
      n.put(280, "minecraft:stick");
      n.put(281, "minecraft:bowl");
      n.put(282, "minecraft:mushroom_stew");
      n.put(283, "minecraft:golden_sword");
      n.put(284, "minecraft:golden_shovel");
      n.put(285, "minecraft:golden_pickaxe");
      n.put(286, "minecraft:golden_axe");
      n.put(287, "minecraft:string");
      n.put(288, "minecraft:feather");
      n.put(289, "minecraft:gunpowder");
      n.put(290, "minecraft:wooden_hoe");
      n.put(291, "minecraft:stone_hoe");
      n.put(292, "minecraft:iron_hoe");
      n.put(293, "minecraft:diamond_hoe");
      n.put(294, "minecraft:golden_hoe");
      n.put(295, "minecraft:wheat_seeds");
      n.put(296, "minecraft:wheat");
      n.put(297, "minecraft:bread");
      n.put(298, "minecraft:leather_helmet");
      n.put(299, "minecraft:leather_chestplate");
      n.put(300, "minecraft:leather_leggings");
      n.put(301, "minecraft:leather_boots");
      n.put(302, "minecraft:chainmail_helmet");
      n.put(303, "minecraft:chainmail_chestplate");
      n.put(304, "minecraft:chainmail_leggings");
      n.put(305, "minecraft:chainmail_boots");
      n.put(306, "minecraft:iron_helmet");
      n.put(307, "minecraft:iron_chestplate");
      n.put(308, "minecraft:iron_leggings");
      n.put(309, "minecraft:iron_boots");
      n.put(310, "minecraft:diamond_helmet");
      n.put(311, "minecraft:diamond_chestplate");
      n.put(312, "minecraft:diamond_leggings");
      n.put(313, "minecraft:diamond_boots");
      n.put(314, "minecraft:golden_helmet");
      n.put(315, "minecraft:golden_chestplate");
      n.put(316, "minecraft:golden_leggings");
      n.put(317, "minecraft:golden_boots");
      n.put(318, "minecraft:flint");
      n.put(319, "minecraft:porkchop");
      n.put(320, "minecraft:cooked_porkchop");
      n.put(321, "minecraft:painting");
      n.put(322, "minecraft:golden_apple");
      n.put(323, "minecraft:sign");
      n.put(324, "minecraft:wooden_door");
      n.put(325, "minecraft:bucket");
      n.put(326, "minecraft:water_bucket");
      n.put(327, "minecraft:lava_bucket");
      n.put(328, "minecraft:minecart");
      n.put(329, "minecraft:saddle");
      n.put(330, "minecraft:iron_door");
      n.put(331, "minecraft:redstone");
      n.put(332, "minecraft:snowball");
      n.put(333, "minecraft:boat");
      n.put(334, "minecraft:leather");
      n.put(335, "minecraft:milk_bucket");
      n.put(336, "minecraft:brick");
      n.put(337, "minecraft:clay_ball");
      n.put(338, "minecraft:reeds");
      n.put(339, "minecraft:paper");
      n.put(340, "minecraft:book");
      n.put(341, "minecraft:slime_ball");
      n.put(342, "minecraft:chest_minecart");
      n.put(343, "minecraft:furnace_minecart");
      n.put(344, "minecraft:egg");
      n.put(345, "minecraft:compass");
      n.put(346, "minecraft:fishing_rod");
      n.put(347, "minecraft:clock");
      n.put(348, "minecraft:glowstone_dust");
      n.put(349, "minecraft:fish");
      n.put(350, "minecraft:cooked_fish");
      n.put(351, "minecraft:dye");
      n.put(352, "minecraft:bone");
      n.put(353, "minecraft:sugar");
      n.put(354, "minecraft:cake");
      n.put(355, "minecraft:bed");
      n.put(356, "minecraft:repeater");
      n.put(357, "minecraft:cookie");
      n.put(358, "minecraft:filled_map");
      n.put(359, "minecraft:shears");
      n.put(360, "minecraft:melon");
      n.put(361, "minecraft:pumpkin_seeds");
      n.put(362, "minecraft:melon_seeds");
      n.put(363, "minecraft:beef");
      n.put(364, "minecraft:cooked_beef");
      n.put(365, "minecraft:chicken");
      n.put(366, "minecraft:cooked_chicken");
      n.put(367, "minecraft:rotten_flesh");
      n.put(368, "minecraft:ender_pearl");
      n.put(369, "minecraft:blaze_rod");
      n.put(370, "minecraft:ghast_tear");
      n.put(371, "minecraft:gold_nugget");
      n.put(372, "minecraft:nether_wart");
      n.put(373, "minecraft:potion");
      n.put(374, "minecraft:glass_bottle");
      n.put(375, "minecraft:spider_eye");
      n.put(376, "minecraft:fermented_spider_eye");
      n.put(377, "minecraft:blaze_powder");
      n.put(378, "minecraft:magma_cream");
      n.put(379, "minecraft:brewing_stand");
      n.put(380, "minecraft:cauldron");
      n.put(381, "minecraft:ender_eye");
      n.put(382, "minecraft:speckled_melon");
      n.put(383, "minecraft:spawn_egg");
      n.put(384, "minecraft:experience_bottle");
      n.put(385, "minecraft:fire_charge");
      n.put(386, "minecraft:writable_book");
      n.put(387, "minecraft:written_book");
      n.put(388, "minecraft:emerald");
      n.put(389, "minecraft:item_frame");
      n.put(390, "minecraft:flower_pot");
      n.put(391, "minecraft:carrot");
      n.put(392, "minecraft:potato");
      n.put(393, "minecraft:baked_potato");
      n.put(394, "minecraft:poisonous_potato");
      n.put(395, "minecraft:map");
      n.put(396, "minecraft:golden_carrot");
      n.put(397, "minecraft:skull_item");
      n.put(398, "minecraft:carrot_on_a_stick");
      n.put(399, "minecraft:nether_star");
      n.put(400, "minecraft:pumpkin_pie");
      n.put(401, "minecraft:fireworks");
      n.put(402, "minecraft:firework_charge");
      n.put(403, "minecraft:enchanted_book");
      n.put(404, "minecraft:comparator");
      n.put(405, "minecraft:netherbrick");
      n.put(406, "minecraft:quartz");
      n.put(407, "minecraft:tnt_minecart");
      n.put(408, "minecraft:hopper_minecart");
      n.put(409, "minecraft:prismarine_shard");
      n.put(410, "minecraft:prismarine_crystals");
      n.put(411, "minecraft:rabbit");
      n.put(412, "minecraft:cooked_rabbit");
      n.put(413, "minecraft:rabbit_stew");
      n.put(414, "minecraft:rabbit_foot");
      n.put(415, "minecraft:rabbit_hide");
      n.put(416, "minecraft:armor_stand");
      n.put(417, "minecraft:iron_horse_armor");
      n.put(418, "minecraft:golden_horse_armor");
      n.put(419, "minecraft:diamond_horse_armor");
      n.put(420, "minecraft:lead");
      n.put(421, "minecraft:name_tag");
      n.put(422, "minecraft:command_block_minecart");
      n.put(423, "minecraft:mutton");
      n.put(424, "minecraft:cooked_mutton");
      n.put(425, "minecraft:banner");
      n.put(426, "minecraft:end_crystal");
      n.put(427, "minecraft:spruce_door");
      n.put(428, "minecraft:birch_door");
      n.put(429, "minecraft:jungle_door");
      n.put(430, "minecraft:acacia_door");
      n.put(431, "minecraft:dark_oak_door");
      n.put(432, "minecraft:chorus_fruit");
      n.put(433, "minecraft:chorus_fruit_popped");
      n.put(434, "minecraft:beetroot");
      n.put(435, "minecraft:beetroot_seeds");
      n.put(436, "minecraft:beetroot_soup");
      n.put(437, "minecraft:dragon_breath");
      n.put(438, "minecraft:splash_potion");
      n.put(439, "minecraft:spectral_arrow");
      n.put(440, "minecraft:tipped_arrow");
      n.put(441, "minecraft:lingering_potion");
      n.put(442, "minecraft:shield");
      n.put(443, "minecraft:elytra");
      n.put(444, "minecraft:spruce_boat");
      n.put(445, "minecraft:birch_boat");
      n.put(446, "minecraft:jungle_boat");
      n.put(447, "minecraft:acacia_boat");
      n.put(448, "minecraft:dark_oak_boat");
      n.put(449, "minecraft:totem_of_undying");
      n.put(450, "minecraft:shulker_shell");
      n.put(451, "");
      n.put(452, "");
      n.put(453, "");
      n.put(454, "");
      n.put(455, "");
      n.put(456, "");
      n.put(457, "");
      n.put(458, "");
      n.put(459, "");
      n.put(460, "");
      n.put(461, "");
      n.put(462, "");
      n.put(463, "");
      n.put(464, "");
      n.put(465, "");
      n.put(2256, "minecraft:record_13");
      n.put(2257, "minecraft:record_cat");
      n.put(2258, "minecraft:record_blocks");
      n.put(2259, "minecraft:record_chirp");
      n.put(2260, "minecraft:record_far");
      n.put(2261, "minecraft:record_mall");
      n.put(2262, "minecraft:record_mellohi");
      n.put(2263, "minecraft:record_stal");
      n.put(2264, "minecraft:record_strad");
      n.put(2265, "minecraft:record_ward");
      n.put(2266, "minecraft:record_11");
      n.put(2267, "minecraft:record_wait");
      n.put(2268, "");
      n.put(2269, "");
      n.put(2270, "");
      n.put(2271, "");
      Iterator var2 = n.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Integer, String> entry = (Entry)var2.next();
         Integer type = entry.getKey();
         String name = (entry.getValue()).toLowerCase();
         i.put(name, type);
      }

   }

   static {
      track_any = Arrays.asList(Material.PISTON_EXTENSION, Material.TORCH, Material.LEVER, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON);
      track_top = Arrays.asList(Material.SAPLING, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.REDSTONE_WIRE, Material.CROPS, Material.SIGN_POST, Material.STANDING_BANNER, Material.WOODEN_DOOR, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.RAILS, Material.STONE_PLATE, Material.WOOD_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.SNOW, Material.CACTUS, Material.SUGAR_CANE_BLOCK, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.FLOWER_POT, Material.CARROT, Material.POTATO, Material.GOLD_PLATE, Material.IRON_PLATE, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON, Material.ACTIVATOR_RAIL, Material.CARPET, Material.DOUBLE_PLANT, Material.NETHER_WARTS);
      track_side = Arrays.asList(Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL, Material.WALL_BANNER, Material.BED_BLOCK, Material.LADDER, Material.WALL_SIGN, Material.STONE_BUTTON, Material.TRAP_DOOR, Material.IRON_TRAPDOOR, Material.VINE, Material.COCOA, Material.TRIPWIRE_HOOK, Material.WOOD_BUTTON);
      shulker_boxes = Arrays.asList(Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SILVER_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);
      containers = Arrays.asList(Material.DISPENSER, Material.CHEST, Material.FURNACE, Material.BURNING_FURNACE, Material.BREWING_STAND, Material.TRAPPED_CHEST, Material.HOPPER, Material.DROPPER, Material.ARMOR_STAND, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SILVER_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);
      falling_block_types = Arrays.asList(Material.SAND, Material.GRAVEL, Material.ANVIL);
      non_attachable = Arrays.asList(Material.AIR, Material.SAPLING, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE, Material.REDSTONE_WIRE, Material.LADDER, Material.RAILS, Material.LEVER, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.STONE_BUTTON, Material.SNOW, Material.SUGAR_CANE_BLOCK, Material.PORTAL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON);
      natural_blocks = Arrays.asList(Material.STONE, Material.GRASS, Material.DIRT, Material.SAND, Material.GRAVEL, Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE, Material.LOG, Material.LEAVES, Material.LAPIS_ORE, Material.SANDSTONE, Material.WEB, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.OBSIDIAN, Material.DIAMOND_ORE, Material.CROPS, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE, Material.SNOW, Material.ICE, Material.CACTUS, Material.CLAY, Material.SUGAR_CANE_BLOCK, Material.PUMPKIN, Material.NETHERRACK, Material.SOUL_SAND, Material.MELON_BLOCK, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.VINE, Material.MYCEL, Material.WATER_LILY, Material.NETHER_WARTS, Material.ENDER_STONE, Material.EMERALD_ORE, Material.CARROT, Material.POTATO);
      legacy_block_ids = Collections.synchronizedMap(new HashMap());
      legacy_block_names = Collections.synchronizedMap(new HashMap());
   }
}
