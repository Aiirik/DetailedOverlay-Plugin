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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.ItemID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class DetailedOverlayOverlay extends WidgetItemOverlay
{
	private static final int[] EMPTY_RUNES = new int[0];
	private static final String[] JEWELRY_KEYWORDS =
	{
		"bracelet", "amulet", "ring", "necklace", "glory", "wealth", "skills", "games", "dueling", "binding"
	};
	private static final String[] POTION_KEYWORDS =
	{
		"potion", "mix", "brew", "serum", "balm", "restore", "antipoison", "antidote",
		"antifire", "attack", "strength", "defence", "defense", "combat", "energy",
		"prayer", "magic", "ranging", "bastion", "battlemage", "sanfew", "overload",
		"revitalisation", "revitalization", "replenishment", "aggression", "forgotten brew"
	};

	private final DetailedOverlayConfig config;
	private final ItemManager itemManager;
	private final Map<Integer, OverlayItem> itemCache = new HashMap<>();

	@Inject
	public DetailedOverlayOverlay(DetailedOverlayConfig config, ItemManager itemManager)
	{
		this.config = config;
		this.itemManager = itemManager;
		showOnInventory();
		showOnEquipment();
		showOnBank();
		showOnInterfaces(
			InterfaceID.BANKMAIN,
			InterfaceID.BANKSIDE,
			InterfaceID.BANK_DEPOSITBOX,
			InterfaceID.GE_OFFERS,
			InterfaceID.GE_OFFERS_SIDE,
			InterfaceID.GE_PRICECHECKER,
			InterfaceID.GE_PRICECHECKER_SIDE,
			InterfaceID.SEED_VAULT,
			InterfaceID.SEED_VAULT_DEPOSIT,
			InterfaceID.SHOPMAIN,
			InterfaceID.SHOPSIDE,
			InterfaceID.SMITHING
		);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		Widget widget = itemWidget.getWidget();
		boolean inContainer = isContainerWidget(widget);
		boolean inEquipment = isEquipmentWidget(widget);
		Rectangle bounds = itemWidget.getCanvasBounds();
		OverlayItem overlayItem = itemCache.computeIfAbsent(itemId, this::classifyItem);

		if (overlayItem.potionDoses != null
			&& config.showPotions()
			&& shouldRenderInLocation(inContainer, false, config.showPotionsInContainers(), false))
		{
			renderText(
				graphics,
				bounds,
				overlayItem.potionDoses,
				config.potionColor(),
				config.potionSize(),
				getPotionPosition(inContainer),
				0,
				config.potionOutline(),
				inContainer
			);
			return;
		}

		if (overlayItem.teleportCode != null
			&& config.showTeleports()
			&& shouldRenderInLocation(inContainer, false, config.showTeleportsInContainers(), false))
		{
			renderText(
				graphics,
				bounds,
				overlayItem.teleportCode,
				config.teleportColor(),
				config.teleportSize(),
				getTeleportPosition(inContainer),
				0,
				config.teleportOutline(),
				inContainer
			);
			return;
		}

		if (overlayItem.staffRunes.length > 0
			&& config.showRunes()
			&& shouldRenderInLocation(inContainer, inEquipment, config.showRunesInContainers(), config.showRunesOnEquipment()))
		{
			if (config.staffMode() == DetailedOverlayConfig.StaffMode.ICON)
			{
				renderStaffIcons(graphics, bounds, overlayItem.staffRunes, inContainer, inEquipment);
			}
			else
			{
				renderStaffText(graphics, bounds, overlayItem.staffRunes, inContainer, inEquipment);
			}
		}
	}

	private OverlayItem classifyItem(int itemId)
	{
		String name = itemManager.getItemComposition(itemId).getName();
		String lowerName = name.toLowerCase();

		String potionDoses = getPotionDoses(name, lowerName);
		if (potionDoses != null)
		{
			return new OverlayItem(potionDoses, null, EMPTY_RUNES);
		}

		String teleportCode = getTeleportTabText(lowerName);
		if (teleportCode != null)
		{
			return new OverlayItem(null, teleportCode, EMPTY_RUNES);
		}

		return new OverlayItem(null, null, getRuneIdsForStaff(lowerName));
	}

	private DetailedOverlayConfig.ItemPosition getPotionPosition(boolean inContainer)
	{
		return inContainer ? config.potionContainerPosition() : config.potionPosition();
	}

	private DetailedOverlayConfig.ItemPosition getTeleportPosition(boolean inContainer)
	{
		return inContainer ? config.teleportContainerPosition() : config.teleportPosition();
	}

	private boolean shouldRenderInLocation(boolean inContainer, boolean inEquipment, boolean enabledInContainers, boolean enabledOnEquipment)
	{
		if (inEquipment)
		{
			return enabledOnEquipment;
		}

		return !inContainer || enabledInContainers;
	}

	private boolean isContainerWidget(Widget widget)
	{
		return isContainerWidgetId(widget) || isContainerWidgetId(widget != null ? widget.getParent() : null);
	}

	private boolean isEquipmentWidget(Widget widget)
	{
		return isEquipmentWidgetId(widget) || isEquipmentWidgetId(widget != null ? widget.getParent() : null);
	}

	private boolean isContainerWidgetId(Widget widget)
	{
		if (widget == null)
		{
			return false;
		}

		int groupId = widget.getId() >>> 16;
		return groupId == InterfaceID.BANKMAIN
			|| groupId == InterfaceID.BANKSIDE
			|| groupId == InterfaceID.BANK_DEPOSITBOX
			|| groupId == InterfaceID.GE_OFFERS
			|| groupId == InterfaceID.GE_OFFERS_SIDE
			|| groupId == InterfaceID.GE_PRICECHECKER
			|| groupId == InterfaceID.GE_PRICECHECKER_SIDE
			|| groupId == InterfaceID.SEED_VAULT
			|| groupId == InterfaceID.SEED_VAULT_DEPOSIT
			|| groupId == InterfaceID.SHOPMAIN
			|| groupId == InterfaceID.SHOPSIDE
			|| groupId == InterfaceID.SMITHING;
	}

	private boolean isEquipmentWidgetId(Widget widget)
	{
		if (widget == null)
		{
			return false;
		}

		int groupId = widget.getId() >>> 16;
		return groupId == InterfaceID.WORNITEMS
			|| groupId == InterfaceID.EQUIPMENT_SIDE;
	}

	private String getPotionDoses(String name, String lowerName)
	{
		int openParen = name.lastIndexOf('(');
		int closeParen = name.lastIndexOf(')');
		if (openParen == -1 || closeParen <= openParen + 1)
		{
			return null;
		}

		String suffix = name.substring(openParen + 1, closeParen).trim();
		if (!suffix.chars().allMatch(Character::isDigit))
		{
			return null;
		}

		int doses = Integer.parseInt(suffix);
		if (doses < 1 || doses > 8)
		{
			return null;
		}

		for (String keyword : JEWELRY_KEYWORDS)
		{
			if (lowerName.contains(keyword))
			{
				return null;
			}
		}

		for (String keyword : POTION_KEYWORDS)
		{
			if (lowerName.contains(keyword))
			{
				return suffix;
			}
		}

		return null;
	}

	private String getTeleportTabText(String lowerName)
	{
		if (!(lowerName.contains("teleport") || lowerName.contains("teletab")))
		{
			return null;
		}

		if (lowerName.contains("varrock")) return "VAR";
		if (lowerName.contains("lumbridge")) return "LUM";
		if (lowerName.contains("falador")) return "FAL";
		if (lowerName.contains("camelot")) return "CAM";
		if (lowerName.contains("ardougne")) return "ARD";
		if (lowerName.contains("watchtower")) return "WAT";
		if (lowerName.contains("house")) return "POH";
		if (lowerName.contains("draynor")) return "DRA";
		if (lowerName.contains("barrows")) return "BAR";
		if (lowerName.contains("rellekka")) return "REL";
		if (lowerName.contains("kourend")) return "KOU";
		if (lowerName.contains("annakarl")) return "ANN";
		if (lowerName.contains("ghorrock")) return "GHO";
		return null;
	}

	private void renderText(Graphics2D graphics, Rectangle bounds, String text, Color color, int size, DetailedOverlayConfig.ItemPosition position, int offset, boolean outline, boolean inContainer)
	{
		Object oldTextAntialias = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(getOverlayFont(size, inContainer));

		FontMetrics fontMetrics = graphics.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(text);
		int textHeight = fontMetrics.getAscent();

		int x = bounds.x;
		int y = bounds.y;
		int width = bounds.width;
		int height = bounds.height;

		int drawX;
		int drawY;

		switch (position)
		{
			case TOP_RIGHT:
				drawX = x + width - textWidth - offset;
				drawY = y + textHeight + offset;
				break;
			case BOTTOM_LEFT:
				drawX = x + offset;
				drawY = y + height - offset;
				break;
			case BOTTOM_RIGHT:
				drawX = x + width - textWidth - offset;
				drawY = y + height - offset;
				break;
			case CENTER:
				drawX = x + (width / 2) - (textWidth / 2);
				drawY = y + (height / 2) + (textHeight / 2) - 1;
				break;
			default:
				drawX = x + offset;
				drawY = y + textHeight + offset;
				break;
		}

		if (outline)
		{
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, drawX + 1, drawY);
			graphics.drawString(text, drawX - 1, drawY);
			graphics.drawString(text, drawX, drawY + 1);
			graphics.drawString(text, drawX, drawY - 1);
		}
		else
		{
			graphics.setColor(Color.BLACK);
			graphics.drawString(text, drawX + 1, drawY + 1);
		}

		graphics.setColor(color);
		graphics.drawString(text, drawX, drawY);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldTextAntialias);
	}

	private Font getOverlayFont(int size, boolean inContainer)
	{
		Font baseFont;
		if (inContainer || size <= 10)
		{
			baseFont = FontManager.getRunescapeSmallFont();
		}
		else if (size >= 12)
		{
			baseFont = FontManager.getRunescapeBoldFont();
		}
		else
		{
			baseFont = FontManager.getRunescapeFont();
		}

		return baseFont.deriveFont(Font.PLAIN, (float) size);
	}

	private void renderStaffIcons(Graphics2D graphics, Rectangle bounds, int[] runeIds, boolean inContainer, boolean inEquipment)
	{
		int size = config.iconSize();
		DetailedOverlayConfig.ItemPosition[] positions = getStaffPositions(inContainer, inEquipment);

		for (int i = 0; i < runeIds.length && i < positions.length; i++)
		{
			BufferedImage icon = itemManager.getImage(runeIds[i], 1, false);
			if (icon != null)
			{
				Point point = getIconPoint(bounds, positions[i], size, config.staffOffset());
				graphics.drawImage(icon, point.x, point.y, size, size, null);
			}
		}
	}

	private void renderStaffText(Graphics2D graphics, Rectangle bounds, int[] runeIds, boolean inContainer, boolean inEquipment)
	{
		DetailedOverlayConfig.ItemPosition[] positions = getStaffPositions(inContainer, inEquipment);

		for (int i = 0; i < runeIds.length && i < positions.length; i++)
		{
			renderText(graphics, bounds, getLetter(runeIds[i]), config.staffColor(), config.staffTextSize(), positions[i], config.staffOffset(), true, inContainer);
		}
	}

	private DetailedOverlayConfig.ItemPosition[] getStaffPositions(boolean inContainer, boolean inEquipment)
	{
		DetailedOverlayConfig.StaffLayout layout = inEquipment
			? config.staffEquipmentLayout()
			: (inContainer ? config.staffContainerLayout() : config.staffLayout());

		switch (layout)
		{
			case REVERSE_DIAGONAL:
				return new DetailedOverlayConfig.ItemPosition[]
				{
					DetailedOverlayConfig.ItemPosition.TOP_RIGHT,
					DetailedOverlayConfig.ItemPosition.BOTTOM_LEFT
				};
			case LEFT:
				return new DetailedOverlayConfig.ItemPosition[]
				{
					DetailedOverlayConfig.ItemPosition.TOP_LEFT,
					DetailedOverlayConfig.ItemPosition.BOTTOM_LEFT
				};
			case RIGHT:
				return new DetailedOverlayConfig.ItemPosition[]
				{
					DetailedOverlayConfig.ItemPosition.TOP_RIGHT,
					DetailedOverlayConfig.ItemPosition.BOTTOM_RIGHT
				};
			case TOP:
				return new DetailedOverlayConfig.ItemPosition[]
				{
					DetailedOverlayConfig.ItemPosition.TOP_LEFT,
					DetailedOverlayConfig.ItemPosition.TOP_RIGHT
				};
			case BOTTOM:
				return new DetailedOverlayConfig.ItemPosition[]
				{
					DetailedOverlayConfig.ItemPosition.BOTTOM_LEFT,
					DetailedOverlayConfig.ItemPosition.BOTTOM_RIGHT
				};
			default:
				return new DetailedOverlayConfig.ItemPosition[]
				{
					DetailedOverlayConfig.ItemPosition.TOP_LEFT,
					DetailedOverlayConfig.ItemPosition.BOTTOM_RIGHT
				};
		}
	}

	private Point getIconPoint(Rectangle bounds, DetailedOverlayConfig.ItemPosition position, int size, int offset)
	{
		int leftX = bounds.x - 1 + offset;
		int rightX = bounds.x + bounds.width - size + 1 - offset;
		int topY = bounds.y - 1 + offset;
		int bottomY = bounds.y + bounds.height - size + 1 - offset;

		switch (position)
		{
			case TOP_RIGHT:
				return new Point(rightX, topY);
			case BOTTOM_LEFT:
				return new Point(leftX, bottomY);
			case BOTTOM_RIGHT:
				return new Point(rightX, bottomY);
			default:
				return new Point(leftX, topY);
		}
	}

	private int[] getRuneIdsForStaff(String lowerName)
	{
		if (!isStaff(lowerName))
		{
			return EMPTY_RUNES;
		}

		if (lowerName.contains("lava")) return new int[]{ItemID.FIRE_RUNE, ItemID.EARTH_RUNE};
		if (lowerName.contains("mud")) return new int[]{ItemID.WATER_RUNE, ItemID.EARTH_RUNE};
		if (lowerName.contains("steam")) return new int[]{ItemID.FIRE_RUNE, ItemID.WATER_RUNE};
		if (lowerName.contains("smoke")) return new int[]{ItemID.FIRE_RUNE, ItemID.AIR_RUNE};
		if (lowerName.contains("mist")) return new int[]{ItemID.WATER_RUNE, ItemID.AIR_RUNE};
		if (lowerName.contains("dust")) return new int[]{ItemID.EARTH_RUNE, ItemID.AIR_RUNE};
		if (lowerName.contains("fire")) return new int[]{ItemID.FIRE_RUNE};
		if (lowerName.contains("water")) return new int[]{ItemID.WATER_RUNE};
		if (lowerName.contains("air")) return new int[]{ItemID.AIR_RUNE};
		if (lowerName.contains("earth")) return new int[]{ItemID.EARTH_RUNE};
		return EMPTY_RUNES;
	}

	private boolean isStaff(String lowerName)
	{
		return lowerName.contains("staff") || lowerName.contains("battlestaff");
	}

	private String getLetter(int runeId)
	{
		if (runeId == ItemID.FIRE_RUNE) return "F";
		if (runeId == ItemID.WATER_RUNE) return "W";
		if (runeId == ItemID.AIR_RUNE) return "A";
		return "E";
	}

	private static final class OverlayItem
	{
		private final String potionDoses;
		private final String teleportCode;
		private final int[] staffRunes;

		private OverlayItem(String potionDoses, String teleportCode, int[] staffRunes)
		{
			this.potionDoses = potionDoses;
			this.teleportCode = teleportCode;
			this.staffRunes = staffRunes;
		}
	}
}
