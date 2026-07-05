package com.darkifov.thaumcraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class AspectList {
    private final EnumMap<Aspect, Integer> aspects = new EnumMap<>(Aspect.class);

    public AspectList add(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return this;
        }

        aspects.put(aspect, aspects.getOrDefault(aspect, 0) + amount);
        return this;
    }

    public AspectList addAll(AspectList other) {
        for (Map.Entry<Aspect, Integer> entry : other.aspects.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }

        return this;
    }

    public int get(Aspect aspect) {
        return aspects.getOrDefault(aspect, 0);
    }

    public boolean remove(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return true;
        }

        int current = get(aspect);

        if (current < amount) {
            return false;
        }

        int next = current - amount;

        if (next <= 0) {
            aspects.remove(aspect);
        } else {
            aspects.put(aspect, next);
        }

        return true;
    }

    public int removeUpTo(Aspect aspect, int maxAmount) {
        if (aspect == null || maxAmount <= 0) {
            return 0;
        }

        int current = get(aspect);
        int removed = Math.min(current, maxAmount);

        if (removed > 0) {
            remove(aspect, removed);
        }

        return removed;
    }

    public Aspect firstAspect() {
        for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
            if (entry.getValue() > 0) {
                return entry.getKey();
            }
        }

        return null;
    }

    public Map<Aspect, Integer> entries() {
        return Collections.unmodifiableMap(aspects);
    }

    public List<AspectStack> all() {
        List<AspectStack> result = new ArrayList<>();

        for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
            result.add(new AspectStack(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    public boolean contains(Aspect aspect, int amount) {
        return get(aspect) >= amount;
    }

    public boolean containsAll(AspectList other) {
        for (Map.Entry<Aspect, Integer> entry : other.aspects.entrySet()) {
            if (!contains(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    public boolean removeAll(AspectList other) {
        if (!containsAll(other)) {
            return false;
        }

        for (Map.Entry<Aspect, Integer> entry : other.aspects.entrySet()) {
            remove(entry.getKey(), entry.getValue());
        }

        return true;
    }

    public void clear() {
        aspects.clear();
    }

    public int totalAmount() {
        int total = 0;

        for (int value : aspects.values()) {
            total += value;
        }

        return total;
    }

    public boolean isEmpty() {
        return aspects.isEmpty();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
            tag.putInt(entry.getKey().name(), entry.getValue());
        }

        return tag;
    }

    public void load(CompoundTag tag) {
        aspects.clear();

        for (Aspect aspect : Aspect.values()) {
            int amount = tag.getInt(aspect.name());

            if (amount > 0) {
                aspects.put(aspect, amount);
            }
        }
    }

    public MutableComponent toComponent() {
        if (aspects.isEmpty()) {
            return Component.literal("No aspects detected");
        }

        MutableComponent result = Component.literal("");

        boolean first = true;

        for (Map.Entry<Aspect, Integer> entry : aspects.entrySet()) {
            if (!first) {
                result.append(Component.literal(", "));
            }

            Aspect aspect = entry.getKey();
            int amount = entry.getValue();

            result.append(Component.literal(aspect.displayName() + " " + amount).withStyle(style -> style.withColor(aspect.textColor())));
            first = false;
        }

        return result;
    }
}
