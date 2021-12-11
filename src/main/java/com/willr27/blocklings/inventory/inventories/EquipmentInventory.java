package com.willr27.blocklings.inventory.inventories;

import com.willr27.blocklings.entity.entities.blockling.BlocklingEntity;
import com.willr27.blocklings.item.ToolType;
import com.willr27.blocklings.network.NetworkHandler;
import com.willr27.blocklings.network.messages.EquipmentInventoryMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class EquipmentInventory extends AbstractInventory
{
    public static final int UTILITY_1 = 0;
    public static final int UTILITY_2 = 1;
    public static final int TOOL_MAIN_HAND = 2;
    public static final int TOOL_OFF_HAND = 3;

    public EquipmentInventory(BlocklingEntity blockling)
    {
        super(blockling, 22);
    }

    public boolean hasToolsEquipped(ToolType toolType1, ToolType toolType2, boolean handsDoNotMatter)
    {
        if (handsDoNotMatter)
        {
            return hasToolEquipped(toolType1) && hasToolEquipped(toolType2);
        }
        else
        {
            return hasToolEquipped(Hand.MAIN_HAND, toolType1) && hasToolEquipped(Hand.OFF_HAND, toolType2);
        }
    }

    public boolean hasToolEquipped(ToolType toolType)
    {
        return hasToolEquipped(Hand.MAIN_HAND, toolType) || hasToolEquipped(Hand.OFF_HAND, toolType);
    }

    public boolean hasToolEquipped(Hand hand, ToolType toolType)
    {
        return toolType.is(getHandStack(hand));
    }

    public ItemStack getHandStack(Hand hand)
    {
        return hand == Hand.MAIN_HAND ? getItem(TOOL_MAIN_HAND) : getItem(TOOL_OFF_HAND);
    }

    public void detectAndSendChanges()
    {
        if (!world.isClientSide)
        {
            for (int i = 0; i < invSize; i++)
            {
                ItemStack oldStack = stacksCopy[i];
                ItemStack newStack = stacks[i];

                if (!ItemStack.isSame(oldStack, newStack))
                {
                    if (newStack.isEmpty() && oldStack.isEmpty())
                    {
                        stacks[i] = ItemStack.EMPTY;
                        stacksCopy[i] = ItemStack.EMPTY;
                    }
                    else
                    {
                        NetworkHandler.sync(blockling.level, new EquipmentInventoryMessage(i, newStack, blockling.getId()));
                        stacksCopy[i] = newStack.copy();
                    }
                }
            }
        }
    }
}