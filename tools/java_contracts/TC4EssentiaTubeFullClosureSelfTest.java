import com.darkifov.thaumcraft.essentia.TC4EssentiaTubeParity;

public final class TC4EssentiaTubeFullClosureSelfTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        require("11.64.36".equals(TC4EssentiaTubeParity.CONTRACT_VERSION), "contract version");
        require(TC4EssentiaTubeParity.propagatedSuction(32, false) == 31, "normal suction");
        require(TC4EssentiaTubeParity.propagatedSuction(32, true) == 16, "restricted suction");
        require(TC4EssentiaTubeParity.propagatedSuction(1, false) == 0, "one-step floor");
        require(TC4EssentiaTubeParity.bufferSuction(0, 0) == 1, "buffer no bellows");
        require(TC4EssentiaTubeParity.bufferSuction(6, 0) == 192, "buffer bellows");
        require(TC4EssentiaTubeParity.bufferSuction(6, 1) == 1, "blue choke");
        require(TC4EssentiaTubeParity.bufferSuction(6, 2) == 0, "red choke");
        require(TC4EssentiaTubeParity.bufferComparator(0) == 0, "empty comparator");
        require(TC4EssentiaTubeParity.bufferComparator(1) == 2, "one comparator");
        require(TC4EssentiaTubeParity.bufferComparator(8) == 15, "full comparator");
        require(TC4EssentiaTubeParity.nextChoke(0) == 1
                && TC4EssentiaTubeParity.nextChoke(1) == 2
                && TC4EssentiaTubeParity.nextChoke(2) == 0, "choke cycle");
        require(TC4EssentiaTubeParity.nextValveRotation(0, false) == 20, "valve close step");
        require(TC4EssentiaTubeParity.nextValveRotation(360, true) == 340, "valve open step");
        require(TC4EssentiaTubeParity.nextValveRotation(355, false) == 360, "valve upper clamp");
        require(TC4EssentiaTubeParity.MINIMUM_SUCTION == 0, "minimum suction");
        System.out.println("TC4 essentia tube full-closure Java 17 self-test: PASS");
    }
}
