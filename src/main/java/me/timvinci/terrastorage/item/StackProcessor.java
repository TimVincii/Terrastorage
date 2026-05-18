package me.timvinci.terrastorage.item;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Compact helper class to encapsulate the stack checking and processing logic.
 */
public class StackProcessor {
    private final Predicate<ItemStack> shouldProcess;
    private final Predicate<ItemStack> process;

    public StackProcessor(Predicate<ItemStack> shouldProcess, Predicate<ItemStack> process) {
        this.shouldProcess = shouldProcess;
        this.process = process;
    }

    public boolean tryProcess(ItemStack stack) {
        if (shouldProcess.test(stack)) {
            return process.test(stack);
        }

        return false;
    }
}
