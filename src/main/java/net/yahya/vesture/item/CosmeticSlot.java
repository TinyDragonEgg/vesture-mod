package net.yahya.vesture.item;

public enum CosmeticSlot {
    HEAD("head", "cosmetic"),
    CHEST("chest", "cosmetic"),
    LEGS("legs", "cosmetic"),
    FEET("feet", "cosmetic"),
    HAND("hand", "cosmetic"),
    BACK("back", "wings");

    public final String group;
    public final String name;

    CosmeticSlot(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public String trinketsId() {
        return group + "/" + name;
    }
}
