package com.github.dovoloo.surwer.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ItemStackSerializer {

    public static String toBase64(List<ItemStack> items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.size());
            for (ItemStack item : items) dataOutput.writeObject(item);

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    @NotNull
    public static List<ItemStack> fromBase64(String data) throws IllegalStateException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            List<ItemStack> items = new ArrayList<>();

            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) items.add((ItemStack) dataInput.readObject());

            dataInput.close();
            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load item stacks.", e);
        }
    }
}
