package com.willr27.blocklings.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.willr27.blocklings.gui.GuiTexture;
import com.willr27.blocklings.gui.GuiUtil;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;

public class TexturedControl extends Widget
{
    public int textureX, textureY;
    public GuiTexture texture;

    @Deprecated
    public TexturedControl(FontRenderer font, int x, int y, int width, int height, int textureX, int textureY)
    {
        super(font, x, y, width, height);
        this.textureX = textureX;
        this.textureY = textureY;
        this.texture = null;
    }

    @Deprecated
    public TexturedControl(FontRenderer font, int x, int y, int width, int height, GuiTexture texture)
    {
        super(font, x, y, width, height);
        this.textureX = texture.x;
        this.textureY = texture.y;
        this.texture = texture;
    }

    public TexturedControl(int x, int y, GuiTexture texture)
    {
        super(x, y, texture.width, texture.height);
        this.textureX = texture.x;
        this.textureY = texture.y;
        this.texture = texture;
    }

    @Deprecated
    public TexturedControl(FontRenderer font, int x, int y, GuiTexture texture)
    {
        super(font, x, y, texture.width, texture.height);
        this.textureX = texture.x;
        this.textureY = texture.y;
        this.texture = texture;
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY)
    {
        if (texture != null)
        {
            GuiUtil.bindTexture(texture.texture);
            blit(matrixStack, screenX, screenY, textureX, textureY, texture.width, texture.height);
        }

        blit(matrixStack, screenX, screenY, textureX, textureY, width, height);
    }
}