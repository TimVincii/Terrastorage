package me.timvinci.terrastorage.item;

import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Compact helper class to encapsulate the stack checking and processing logic.
 */
public class StackProcessor {
    private final Predicate<ItemStack> shouldProcess;
    private final Consumer<ItemStack> process;

    public StackProcessor(Predicate<ItemStack> shouldProcess, Consumer<ItemStack> process) {
        this.shouldProcess = shouldProcess;
        this.process = process;
    }

    public boolean tryProcess(ItemStack stack) {
        if (shouldProcess.test(stack)) {
            process.accept(stack);
            return true;
        }

        return false;
    }
}
