import com.darkifov.thaumcraft.aura.TC4ThaumometerParity;

public final class TC4ThaumometerFullClosureSelfTest {
    private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        require(TC4ThaumometerParity.timingContractMatchesTc4(),"timing");
        require(TC4ThaumometerParity.soundContractMatchesTc4(),"sound");
        require(TC4ThaumometerParity.targetingContractMatchesTc4(),"targeting");
        require(TC4ThaumometerParity.cappedAspectReward(99,9,false)==9,"below cap");
        require(TC4ThaumometerParity.cappedAspectReward(100,9,false)==3,"sqrt cap");
        require(TC4ThaumometerParity.cappedAspectReward(125,9,false)==1,"hard cap");
        require(TC4ThaumometerParity.cappedAspectReward(100,7,true)==3,"discovery before cap");
        require(TC4ThaumometerParity.rowCapacity(0)==5&&TC4ThaumometerParity.rowCapacity(4)==1,"layout");
        require(Math.abs(TC4ThaumometerParity.titleScale(110)-0.0045F)<0.000001F,"title");
        System.out.println("TC4ThaumometerFullClosureSelfTest: PASS");
    }
}
