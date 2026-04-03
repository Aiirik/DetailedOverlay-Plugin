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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.GameState;
import net.runelite.api.ParamID;
import net.runelite.api.StructComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class CombatStyleOverlay extends Overlay
{
	private static final int[] STYLE_WIDGETS =
	{
		InterfaceID.CombatInterface._0,
		InterfaceID.CombatInterface._1,
		InterfaceID.CombatInterface._2,
		InterfaceID.CombatInterface._3,
		InterfaceID.CombatInterface.AUTOCAST_NORMAL,
		InterfaceID.CombatInterface.AUTOCAST_DEFENSIVE
	};

	private final Client client;
	private final DetailedOverlayConfig config;
	private final Map<Integer, CombatStyleXp[]> weaponTypeCache = new HashMap<>();

	@Inject
	public CombatStyleOverlay(Client client, DetailedOverlayConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return null;
		}

		if (!config.showCombatStyleXp())
		{
			return null;
		}

		CombatStyleXp[] styles = weaponTypeCache.computeIfAbsent(
			client.getVarbitValue(VarbitID.COMBAT_WEAPON_CATEGORY),
			this::getWeaponTypeStyles
		);
		if (styles.length == 0)
		{
			return null;
		}

		if (config.showCombatStyleXp())
		{
			for (int i = 0; i < styles.length && i < STYLE_WIDGETS.length; i++)
			{
				CombatStyleXp style = styles[i];
				if (style == null)
				{
					continue;
				}

				Widget widget = client.getWidget(STYLE_WIDGETS[i]);
				if (widget == null || widget.isHidden())
				{
					continue;
				}

				Rectangle bounds = widget.getBounds();
				if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
				{
					continue;
				}

				renderLabel(graphics, bounds, getLabel(style));
			}
		}

		return null;
	}

	private CombatStyleXp[] getWeaponTypeStyles(int weaponType)
	{
		int weaponStyleEnum = client.getEnum(EnumID.WEAPON_STYLES).getIntValue(weaponType);
		if (weaponStyleEnum == -1)
		{
			if (weaponType == 22)
			{
				return new CombatStyleXp[]
				{
					CombatStyleXp.ATTACK,
					CombatStyleXp.STRENGTH,
					null,
					CombatStyleXp.DEFENCE,
					CombatStyleXp.MAGIC,
					CombatStyleXp.MAGIC_DEFENCE
				};
			}

			if (weaponType == 30)
			{
				return new CombatStyleXp[]
				{
					CombatStyleXp.ATTACK,
					CombatStyleXp.STRENGTH,
					CombatStyleXp.STRENGTH,
					CombatStyleXp.DEFENCE
				};
			}

			return new CombatStyleXp[0];
		}

		EnumComposition stylesEnum = client.getEnum(weaponStyleEnum);
		if (stylesEnum == null)
		{
			return new CombatStyleXp[0];
		}

		int[] styleStructs = stylesEnum.getIntVals();
		CombatStyleXp[] styles = new CombatStyleXp[styleStructs.length];

		for (int i = 0; i < styleStructs.length; i++)
		{
			StructComposition struct = client.getStructComposition(styleStructs[i]);
			if (struct == null)
			{
				continue;
			}

			String styleName = struct.getStringValue(ParamID.ATTACK_STYLE_NAME);
			if (styleName == null || styleName.isEmpty())
			{
				continue;
			}

			CombatStyleXp style = CombatStyleXp.fromStyleName(styleName, i == 5);
			if (style != CombatStyleXp.OTHER)
			{
				styles[i] = style;
			}
		}

		return styles;
	}

	private void renderLabel(Graphics2D graphics, Rectangle bounds, String label)
	{
		renderText(
			graphics,
			bounds,
			label,
			config.combatStyleColor(),
			config.combatStyleSize(),
			config.combatStylePosition(),
			config.combatStyleXOffset(),
			config.combatStyleYOffset(),
			config.combatStyleOutline()
		);
	}

	private void renderText(
		Graphics2D graphics,
		Rectangle bounds,
		String text,
		Color color,
		int size,
		DetailedOverlayConfig.ItemPosition position,
		int xOffset,
		int yOffset,
		boolean outline
	)
	{
		Object oldAntialias = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setFont(getFont(size));

		FontMetrics metrics = graphics.getFontMetrics();
		int textWidth = metrics.stringWidth(text);
		int textHeight = metrics.getAscent();
		int drawX = getX(bounds, textWidth, position, xOffset);
		int drawY = getY(bounds, textHeight, position, yOffset);

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
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAntialias);
	}

	private Font getFont(int size)
	{
		Font baseFont = size >= 12
			? FontManager.getRunescapeBoldFont()
			: FontManager.getRunescapeSmallFont();
		return baseFont.deriveFont(Font.PLAIN, (float) size);
	}

	private int getX(Rectangle bounds, int textWidth, DetailedOverlayConfig.ItemPosition position, int offset)
	{
		switch (position)
		{
			case TOP_RIGHT:
			case BOTTOM_RIGHT:
				return bounds.x + bounds.width - textWidth + offset;
			case CENTER:
				return bounds.x + (bounds.width - textWidth) / 2 + offset;
			default:
				return bounds.x + offset;
		}
	}

	private int getY(Rectangle bounds, int textHeight, DetailedOverlayConfig.ItemPosition position, int offset)
	{
		switch (position)
		{
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
				return bounds.y + bounds.height + offset;
			case CENTER:
				return bounds.y + (bounds.height + textHeight) / 2 + offset - 1;
			default:
				return bounds.y + textHeight + offset;
		}
	}

	private String getLabel(CombatStyleXp style)
	{
		if (config.combatStyleLabelMode() == DetailedOverlayConfig.CombatStyleLabelMode.FULL)
		{
			return style.getFullLabel().toUpperCase();
		}

		return style.getShortLabel().toUpperCase();
	}

	private enum CombatStyleXp
	{
		ATTACK("atk", "attack"),
		STRENGTH("str", "strength"),
		DEFENCE("def", "defence"),
		CONTROLLED("all", "shared"),
		RANGED("rng", "ranged"),
		RANGED_DEFENCE("rng/def", "ranged/def"),
		MAGIC("mag", "magic"),
		MAGIC_DEFENCE("mag/def", "magic/def"),
		OTHER("", "");

		private final String shortLabel;
		private final String fullLabel;

		CombatStyleXp(String shortLabel, String fullLabel)
		{
			this.shortLabel = shortLabel;
			this.fullLabel = fullLabel;
		}

		private static CombatStyleXp fromStyleName(String styleName, boolean defensiveCastingSlot)
		{
			switch (styleName.toUpperCase())
			{
				case "ACCURATE":
					return ATTACK;
				case "AGGRESSIVE":
					return STRENGTH;
				case "DEFENSIVE":
					return defensiveCastingSlot ? MAGIC_DEFENCE : DEFENCE;
				case "CONTROLLED":
					return CONTROLLED;
				case "RANGING":
					return RANGED;
				case "LONGRANGE":
					return RANGED_DEFENCE;
				case "CASTING":
					return MAGIC;
				default:
					return OTHER;
			}
		}

		private String getShortLabel()
		{
			return shortLabel;
		}

		private String getFullLabel()
		{
			return fullLabel;
		}
	}
}
