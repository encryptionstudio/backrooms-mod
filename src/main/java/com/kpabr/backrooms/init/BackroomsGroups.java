package com.kpabr.backrooms.init;

import java.util.Set;

import com.kpabr.backrooms.BackroomsMod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BackroomsGroups {

	public static final RegistryKey<ItemGroup> ITEMS_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), BackroomsMod.id("items"));
	public static final ItemGroup ITEMS_GROUP = FabricItemGroup.builder()
		.icon(() -> BackroomsBlocks.PATTERNED_WALLPAPER.asItem().getDefaultStack())
		.displayName(Text.translatable("itemGroup.backrooms.items"))
		.build();

		public static void init() {
			Registry.register(Registries.ITEM_GROUP, ITEMS_GROUP_KEY, ITEMS_GROUP);
			
			ItemGroupEvents.modifyEntriesEvent(ITEMS_GROUP_KEY).register(itemGroup -> {
				for (Identifier entry : Registries.ITEM.getIds()) {
					if (entry.getNamespace().contains("backrooms")) {
						itemGroup.add(Registries.ITEM.get(entry));
					}
				}
			});
		}
}
