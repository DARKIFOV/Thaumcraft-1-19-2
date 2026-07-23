import com.darkifov.thaumcraft.blockentity.TC4HungryChestParity;

public final class TC4HungryChestParitySelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void close(double actual, double expected, double eps, String message) {
        if (Math.abs(actual - expected) > eps) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        require("11.64.16".equals(TC4HungryChestParity.CONTRACT_VERSION), "contract version");
        require(TC4HungryChestParity.INVENTORY_SIZE == 27, "inventory size");
        require(TC4HungryChestParity.MAX_STACK_SIZE == 64, "stack size");
        close(TC4HungryChestParity.HORIZONTAL_MIN, 0.0625D, 1.0E-12, "horizontal min");
        close(TC4HungryChestParity.HORIZONTAL_MAX, 0.9375D, 1.0E-12, "horizontal max");
        close(TC4HungryChestParity.OUTLINE_Y_MAX, 0.875D, 1.0E-12, "outline max Y");
        close(TC4HungryChestParity.COLLISION_Y_MAX, 0.9375D, 1.0E-12, "collision max Y");
        require(TC4HungryChestParity.BLOCK_HARDNESS == 2.5F, "hardness");
        require(TC4HungryChestParity.BLOCK_EXPLOSION_RESISTANCE == 12.5F, "resistance");

        close(TC4HungryChestParity.nextLidAngle(0.0F, 1), 0.1F, 1.0E-6, "open step");
        close(TC4HungryChestParity.nextLidAngle(0.95F, 1), 1.0F, 1.0E-6, "open clamp");
        close(TC4HungryChestParity.nextLidAngle(0.1F, 0), 0.0F, 1.0E-6, "close step");
        close(TC4HungryChestParity.nextLidAngle(0.0F, -1), 0.0F, 1.0E-6, "negative opener behavior");
        require(TC4HungryChestParity.shouldPlayOpenSound(1, 0.0F), "open sound transition");
        require(!TC4HungryChestParity.shouldPlayOpenSound(1, 0.1F), "no repeated open sound");
        require(TC4HungryChestParity.shouldPlayCloseSound(0.5F, 0.4F), "close threshold");
        close(TC4HungryChestParity.easedLid(0.0F), 0.0F, 1.0E-6, "closed easing");
        close(TC4HungryChestParity.easedLid(0.5F), 0.875F, 1.0E-6, "half easing");
        close(TC4HungryChestParity.easedLid(1.0F), 1.0F, 1.0E-6, "open easing");
        close(TC4HungryChestParity.chestSoundPitch(0.0F), 0.9F, 1.0E-6, "chest pitch min");
        close(TC4HungryChestParity.chestSoundPitch(1.0F), 1.0F, 1.0E-6, "chest pitch max");
        close(TC4HungryChestParity.eatSoundPitch(1.0F, 0.0F), 1.2F, 1.0E-6, "eat pitch max");
        close(TC4HungryChestParity.eatSoundPitch(0.0F, 1.0F), 0.8F, 1.0E-6, "eat pitch min");

        require(TC4HungryChestParity.nextDropCount(64, 0) == 10, "drop minimum");
        require(TC4HungryChestParity.nextDropCount(64, 20) == 30, "drop maximum");
        require(TC4HungryChestParity.nextDropCount(7, 20) == 7, "final drop remainder");
        require(TC4HungryChestParity.intersectsCollision(0.1, 0.0, 0.1, 0.2, 0.2, 0.2, 0, 0, 0),
                "inside collision");
        require(!TC4HungryChestParity.intersectsCollision(-0.2, 0.0, 0.2, 0.05, 0.2, 0.3, 0, 0, 0),
                "outside collision X");
        require(!TC4HungryChestParity.intersectsCollision(0.2, 0.95, 0.2, 0.3, 1.1, 0.3, 0, 0, 0),
                "outside collision Y");
        System.out.println("TC4HungryChestParitySelfTest: PASS");
    }
}
