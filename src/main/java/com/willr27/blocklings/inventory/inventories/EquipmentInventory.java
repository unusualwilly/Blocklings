package com.willr27.blocklings.inventory.inventories;

import com.willr27.blocklings.entity.entities.blockling.BlocklingEntity;
import com.willr27.blocklings.entity.entities.blockling.BlocklingHand;
import com.willr27.blocklings.entity.entities.blockling.BlocklingStats;
import com.willr27.blocklings.item.ToolType;
import com.willr27.blocklings.item.ToolUtil;
import com.willr27.blocklings.network.NetworkHandler;
import com.willr27.blocklings.network.messages.EquipmentInventoryMessage;
import net.minecraft.block.BlockState;
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

    public BlocklingHand findHandToolEquipped(ToolType toolType)
    {
        boolean hasInMain = hasToolEquipped(Hand.MAIN_HAND);
        boolean hasInOff = hasToolEquipped(Hand.OFF_HAND);

        if (hasInMain && hasInOff)
        {
            return BlocklingHand.BOTH;
        }
        else if (hasInMain)
        {
            return BlocklingHand.MAIN;
        }
        else if (hasInOff)
        {
            return BlocklingHand.OFF;
        }
        else
        {
            return BlocklingHand.NONE;
        }
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

    public boolean hasToolEquipped(Hand hand)
    {
        return ToolUtil.isTool(getHandStack(hand));
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

    public void setHandStack(Hand hand, ItemStack stack)
    {
        if (hand == Hand.MAIN_HAND)
        {
            setItem(TOOL_MAIN_HAND, stack);
        }
        else if (hand == Hand.OFF_HAND)
        {
            setItem(TOOL_OFF_HAND, stack);
        }
    }

    public boolean isAttackingWith(BlocklingHand hand)
    {
        BlocklingHand attackingHand = findAttackingHand();

        if (hand == BlocklingHand.BOTH && attackingHand == BlocklingHand.BOTH)
        {
            return true;
        }
        else if (hand == BlocklingHand.MAIN && (attackingHand == BlocklingHand.BOTH || attackingHand == BlocklingHand.MAIN))
        {
            return true;
        }
        else if (hand == BlocklingHand.OFF && (attackingHand == BlocklingHand.BOTH || attackingHand == BlocklingHand.OFF))
        {
            return true;
        }
        else if (hand == BlocklingHand.NONE && attackingHand == BlocklingHand.NONE)
        {
            return true;
        }

        return false;
    }

    public BlocklingHand findAttackingHand()
    {
        if (hasToolEquipped(Hand.MAIN_HAND, ToolType.WEAPON) && !hasToolEquipped(Hand.OFF_HAND, ToolType.WEAPON))
        {
            return BlocklingHand.MAIN;
        }
        else if (!hasToolEquipped(Hand.MAIN_HAND, ToolType.WEAPON) && hasToolEquipped(Hand.OFF_HAND, ToolType.WEAPON))
        {
            return BlocklingHand.OFF;
        }
        else if (!getHandStack(Hand.MAIN_HAND).isEmpty() && getHandStack(Hand.OFF_HAND).isEmpty())
        {
            return BlocklingHand.MAIN;
        }
        else if (getHandStack(Hand.MAIN_HAND).isEmpty() && !getHandStack(Hand.OFF_HAND).isEmpty())
        {
            return BlocklingHand.OFF;
        }

        return BlocklingHand.BOTH;
    }

    public boolean canHarvestBlockWithEquippedTools(BlockState blockState)
    {
        return canHarvestBlockWithEquippedTool(Hand.MAIN_HAND, blockState) || canHarvestBlockWithEquippedTool(Hand.OFF_HAND, blockState);
    }

    public boolean canHarvestBlockWithEquippedTool(Hand hand, BlockState blockState)
    {
        return ToolUtil.canToolHarvestBlock(getHandStack(hand), blockState);
    }

    @Override
    public ItemStack addItem(ItemStack stack)
    {
        int maxStackSize = stack.getMaxStackSize();

        for (int i = 0; i < invSize && !stack.isEmpty(); i++)
        {
            if (i >= UTILITY_1 && i <= UTILITY_2)
            {
                continue;
            }
            else if (i >= TOOL_MAIN_HAND && i <= TOOL_OFF_HAND)
            {
                if (!ToolUtil.isTool(stack))
                {
                    continue;
                }
            }

            ItemStack slotStack = getItem(i);

            if (ItemStack.isSame(stack, slotStack))
            {
                int amountToAdd = stack.getCount();
                amountToAdd = Math.min(amountToAdd, maxStackSize - slotStack.getCount());
                stack.shrink(amountToAdd);
                slotStack.grow(amountToAdd);
                setItem(i, slotStack);
            }
        }

        for (int i = 0; i < invSize && !stack.isEmpty(); i++)
        {
            if (i >= UTILITY_1 && i <= UTILITY_2)
            {
                continue;
            }
            else if (i >= TOOL_MAIN_HAND && i <= TOOL_OFF_HAND)
            {
                if (!ToolUtil.isTool(stack))
                {
                    continue;
                }
            }

            ItemStack slotStack = getItem(i);

            if (slotStack.isEmpty())
            {
                setItem(i, stack.copy());
                stack.setCount(0);

                break;
            }
        }

        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack)
    {
        super.setItem(index, stack);

        if (index == TOOL_MAIN_HAND || index == TOOL_OFF_HAND)
        {
            updateToolAttributes();
        }
    }

    public void updateToolAttributes()
    {
        BlocklingStats stats = blockling.getStats();

        stats.mainHandAttackDamage.removeModifier(stats.mainHandAttackDamageToolModifier);
        stats.offHandAttackDamage.removeModifier(stats.offHandAttackDamageToolModifier);
        stats.attackSpeed.removeModifier(stats.attackSpeedMainHandModifier);
        stats.attackSpeed.removeModifier(stats.attackSpeedOffHandModifier);

        stats.miningSpeed.removeModifier(stats.miningSpeedMainHandModifier);
        stats.miningSpeed.removeModifier(stats.miningSpeedOffHandModifier);
        stats.woodcuttingSpeed.removeModifier(stats.woodcuttingSpeedMainHandModifier);
        stats.woodcuttingSpeed.removeModifier(stats.woodcuttingSpeedOffHandModifier);
        stats.farmingSpeed.removeModifier(stats.farmingSpeedMainHandModifier);
        stats.farmingSpeed.removeModifier(stats.farmingSpeedOffHandModifier);

        if (isAttackingWith(BlocklingHand.MAIN) && hasToolEquipped(Hand.MAIN_HAND))
        {
            stats.mainHandAttackDamageToolModifier.setValue(ToolUtil.getToolBaseDamage(blockling.getMainHandItem()), false);
            stats.mainHandAttackDamage.addModifier(stats.mainHandAttackDamageToolModifier);
            stats.attackSpeedMainHandModifier.setValue(ToolUtil.getToolAttackSpeed(blockling.getMainHandItem()), false);
            stats.attackSpeed.addModifier(stats.attackSpeedMainHandModifier);

            stats.miningSpeedMainHandModifier.setValue(ToolUtil.getToolMiningSpeed(blockling.getMainHandItem()), false);
            stats.miningSpeed.addModifier(stats.miningSpeedMainHandModifier);
            stats.woodcuttingSpeedMainHandModifier.setValue(ToolUtil.getToolWoodcuttingSpeed(blockling.getMainHandItem()), false);
            stats.woodcuttingSpeed.addModifier(stats.woodcuttingSpeedMainHandModifier);
            stats.farmingSpeedMainHandModifier.setValue(ToolUtil.getToolFarmingSpeed(blockling.getMainHandItem()), false);
            stats.farmingSpeed.addModifier(stats.farmingSpeedMainHandModifier);
        }

        if (isAttackingWith(BlocklingHand.OFF) && hasToolEquipped(Hand.OFF_HAND))
        {
            stats.offHandAttackDamageToolModifier.setValue(ToolUtil.getToolBaseDamage(blockling.getOffhandItem()), false);
            stats.offHandAttackDamage.addModifier(stats.offHandAttackDamageToolModifier);
            stats.attackSpeedOffHandModifier.setValue(ToolUtil.getToolAttackSpeed(blockling.getOffhandItem()), false);
            stats.attackSpeed.addModifier(stats.attackSpeedOffHandModifier);

            stats.miningSpeedMainHandModifier.setValue(ToolUtil.getToolMiningSpeed(blockling.getOffhandItem()), false);
            stats.miningSpeed.addModifier(stats.miningSpeedMainHandModifier);
            stats.woodcuttingSpeedMainHandModifier.setValue(ToolUtil.getToolWoodcuttingSpeed(blockling.getOffhandItem()), false);
            stats.woodcuttingSpeed.addModifier(stats.woodcuttingSpeedMainHandModifier);
            stats.farmingSpeedMainHandModifier.setValue(ToolUtil.getToolFarmingSpeed(blockling.getOffhandItem()), false);
            stats.farmingSpeed.addModifier(stats.farmingSpeedMainHandModifier);
        }
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
