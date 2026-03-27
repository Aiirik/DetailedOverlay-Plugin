/*
 * Copyright (c) 2026, Aiirik
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.aiirik.detailedoverlay;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("detailedoverlay")
public interface DetailedOverlayConfig extends Config
{
	enum ItemPosition
	{
		TOP_LEFT("Top Left"),
		TOP_RIGHT("Top Right"),
		BOTTOM_LEFT("Bottom Left"),
		BOTTOM_RIGHT("Bottom Right"),
		CENTER("Center");

		private final String name;

		ItemPosition(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	@ConfigSection(name = "Potions", description = "Settings for potion doses", position = 0)
	String potionSection = "potionSection";

	@ConfigItem(keyName = "showPotions", name = "Show Doses", description = "Show doses left on potions", position = 1, section = potionSection)
	default boolean showPotions()
	{
		return true;
	}

	@ConfigItem(keyName = "potionColor", name = "Color", description = "Color of the dose text", position = 2, section = potionSection)
	default Color potionColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(keyName = "potionOutline", name = "Text Outline", description = "Draw a thick black outline for better readability", position = 3, section = potionSection)
	default boolean potionOutline()
	{
		return true;
	}

	@ConfigItem(keyName = "potionPosition", name = "Position", description = "Where to place the dose text", position = 4, section = potionSection)
	default ItemPosition potionPosition()
	{
		return ItemPosition.TOP_LEFT;
	}

	@Range(min = 9, max = 20)
	@ConfigItem(keyName = "potionSize", name = "Font Size", description = "Size of the dose text", position = 5, section = potionSection)
	default int potionSize()
	{
		return 15;
	}

	@ConfigItem(keyName = "showPotionsInContainers", name = "Show In Banks", description = "Also show potion doses in bank and other inventory-style containers", position = 6, section = potionSection)
	default boolean showPotionsInContainers()
	{
		return true;
	}

	@ConfigItem(keyName = "potionContainerPosition", name = "Bank Position", description = "Where to place the dose text in banks and shops", position = 7, section = potionSection)
	default ItemPosition potionContainerPosition()
	{
		return ItemPosition.BOTTOM_LEFT;
	}

	@ConfigSection(name = "Teleport Tablets", description = "Settings for teleport tablets", position = 20)
	String teleportSection = "teleportSection";

	@ConfigItem(keyName = "showTeleports", name = "Show Teleport Tablets", description = "Show names on teleport tablets", position = 21, section = teleportSection)
	default boolean showTeleports()
	{
		return true;
	}

	@ConfigItem(keyName = "teleportColor", name = "Color", description = "Color of the teleport text", position = 22, section = teleportSection)
	default Color teleportColor()
	{
		return new Color(255, 200, 0);
	}

	@ConfigItem(keyName = "teleportOutline", name = "Text Outline", description = "Draw a thick black outline", position = 23, section = teleportSection)
	default boolean teleportOutline()
	{
		return true;
	}

	@ConfigItem(keyName = "teleportPosition", name = "Position", description = "Where to place the teleport text", position = 24, section = teleportSection)
	default ItemPosition teleportPosition()
	{
		return ItemPosition.CENTER;
	}

	@Range(min = 8, max = 20)
	@ConfigItem(keyName = "teleportSize", name = "Font Size", description = "Size of the teleport text", position = 25, section = teleportSection)
	default int teleportSize()
	{
		return 15;
	}

	@ConfigItem(keyName = "showTeleportsInContainers", name = "Show In Banks", description = "Also show teleport tablets in bank and other inventory-style containers", position = 26, section = teleportSection)
	default boolean showTeleportsInContainers()
	{
		return true;
	}

	@ConfigItem(keyName = "teleportContainerPosition", name = "Bank Position", description = "Where to place the teleport tablet text in banks and shops", position = 27, section = teleportSection)
	default ItemPosition teleportContainerPosition()
	{
		return ItemPosition.BOTTOM_LEFT;
	}

	@ConfigSection(name = "Staves", description = "Settings for staff runes", position = 30)
	String staffSection = "staffSection";

	@ConfigItem(keyName = "showRunes", name = "Show Runes", description = "Show runes on staves", position = 31, section = staffSection)
	default boolean showRunes()
	{
		return true;
	}

	enum StaffMode
	{
		ICON,
		TEXT
	}

	@ConfigItem(keyName = "staffMode", name = "Display Mode", description = "Icons or Letters", position = 32, section = staffSection)
	default StaffMode staffMode()
	{
		return StaffMode.ICON;
	}

	enum StaffLayout
	{
		DIAGONAL("Diagonal"),
		REVERSE_DIAGONAL("Reverse Diagonal"),
		LEFT("Left"),
		RIGHT("Right"),
		TOP("Top"),
		BOTTOM("Bottom");

		private final String name;

		StaffLayout(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	@ConfigItem(keyName = "staffLayout", name = "Layout", description = "Where to place staff runes", position = 33, section = staffSection)
	default StaffLayout staffLayout()
	{
		return StaffLayout.DIAGONAL;
	}

	@ConfigItem(keyName = "staffColor", name = "Text Color", description = "Color for rune letters", position = 34, section = staffSection)
	default Color staffColor()
	{
		return Color.CYAN;
	}

	@Range(min = 10, max = 25)
	@ConfigItem(keyName = "iconSize", name = "Icon Size", description = "Size of the rune icons", position = 35, section = staffSection)
	default int iconSize()
	{
		return 15;
	}

	@Range(min = -5, max = 10)
	@ConfigItem(keyName = "staffOffset", name = "Corner Offset", description = "Nudge runes", position = 36, section = staffSection)
	default int staffOffset()
	{
		return 0;
	}

	@ConfigItem(keyName = "showRunesInContainers", name = "Show In Banks", description = "Also show staff runes in bank and other inventory-style containers", position = 37, section = staffSection)
	default boolean showRunesInContainers()
	{
		return true;
	}

	@ConfigItem(keyName = "staffContainerLayout", name = "Bank Layout", description = "Where to place staff runes in banks and shops", position = 38, section = staffSection)
	default StaffLayout staffContainerLayout()
	{
		return StaffLayout.BOTTOM;
	}

	@ConfigItem(keyName = "showRunesOnEquipment", name = "Show On Equipment", description = "Also show staff runes on the equipment screen", position = 39, section = staffSection)
	default boolean showRunesOnEquipment()
	{
		return true;
	}

	@ConfigItem(keyName = "staffEquipmentLayout", name = "Equipment Layout", description = "Where to place staff runes on the equipment screen", position = 40, section = staffSection)
	default StaffLayout staffEquipmentLayout()
	{
		return StaffLayout.DIAGONAL;
	}

	@Range(min = 9, max = 20)
	@ConfigItem(keyName = "staffTextSize", name = "Text Size", description = "Size of the rune letters in text mode", position = 41, section = staffSection)
	default int staffTextSize()
	{
		return 15;
	}

	enum CombatStyleLabelMode
	{
		SHORT("Short"),
		FULL("Full");

		private final String name;

		CombatStyleLabelMode(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	@ConfigSection(name = "Combat Styles", description = "Settings for combat style XP labels", position = 40)
	String combatStyleSection = "combatStyleSection";

	@ConfigItem(keyName = "showCombatStyleXp", name = "Show XP Labels", description = "Show XP type labels on combat style buttons", position = 41, section = combatStyleSection)
	default boolean showCombatStyleXp()
	{
		return true;
	}

	@ConfigItem(keyName = "combatStyleLabelMode", name = "Label Mode", description = "Show short or full XP labels", position = 42, section = combatStyleSection)
	default CombatStyleLabelMode combatStyleLabelMode()
	{
		return CombatStyleLabelMode.SHORT;
	}

	@ConfigItem(keyName = "combatStyleColor", name = "Color", description = "Color of the combat style label text", position = 43, section = combatStyleSection)
	default Color combatStyleColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(keyName = "combatStyleOutline", name = "Text Outline", description = "Draw a thick black outline for better readability", position = 44, section = combatStyleSection)
	default boolean combatStyleOutline()
	{
		return true;
	}

	@ConfigItem(keyName = "combatStylePosition", name = "Position", description = "Where to place the XP label on the combat style button", position = 45, section = combatStyleSection)
	default ItemPosition combatStylePosition()
	{
		return ItemPosition.BOTTOM_RIGHT;
	}

	@Range(min = 9, max = 18)
	@ConfigItem(keyName = "combatStyleSize", name = "Font Size", description = "Size of the combat style label text", position = 46, section = combatStyleSection)
	default int combatStyleSize()
	{
		return 15;
	}

	@Range(min = -20, max = 20)
	@ConfigItem(keyName = "combatStyleXOffset", name = "X Offset", description = "Horizontal nudge for the combat style label", position = 47, section = combatStyleSection)
	default int combatStyleXOffset()
	{
		return -2;
	}

	@Range(min = -20, max = 20)
	@ConfigItem(keyName = "combatStyleYOffset", name = "Y Offset", description = "Vertical nudge for the combat style label", position = 48, section = combatStyleSection)
	default int combatStyleYOffset()
	{
		return -2;
	}
}
