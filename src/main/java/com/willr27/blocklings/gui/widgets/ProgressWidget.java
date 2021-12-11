package com.willr27.blocklings.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;

public class ProgressWidget extends TexturedWidget
{
    public ProgressWidget(FontRenderer font, int x, int y, int width, int height, int textureX, int textureY)
    {
        super(font, x, y, width, height, textureX, textureY);
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float progress, boolean vertical, boolean reverseDirection)
    {
        if (vertical) // TODO: THIS IS WRONG
        {
            if (reverseDirection) blit(matrixStack, x, y + ((int) (height * progress)), textureX, textureY + ((int) (height * progress)), width, (int) (height * (1 - progress)));
            else blit(matrixStack, x, y, textureX, textureY, width, (int) (height * progress));
        }
        else
        {
            if (reverseDirection) blit(matrixStack, x + ((int) (width * progress)), y, textureX + ((int) (width * progress)), textureY, (int) (width * (1 - progress)), height);
            else blit(matrixStack, x, y, textureX, textureY, (int) (width * progress), height);
        }
    }
}