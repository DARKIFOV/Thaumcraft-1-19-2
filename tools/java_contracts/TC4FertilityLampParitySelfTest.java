import com.darkifov.thaumcraft.block.TC4FertilityLampParity;
public final class TC4FertilityLampParitySelfTest {
  private static void req(boolean v,String m){if(!v)throw new AssertionError(m);}
  public static void main(String[] args){
    req("11.64.30".equals(TC4FertilityLampParity.CONTRACT_VERSION),"version");
    req(TC4FertilityLampParity.RADIUS==7,"radius");
    req(TC4FertilityLampParity.MAX_CHARGES==4,"max charges");
    req(TC4FertilityLampParity.BREEDING_COST==2,"cost");
    req(TC4FertilityLampParity.isBreedingTick(0) && TC4FertilityLampParity.isBreedingTick(300) && !TC4FertilityLampParity.isBreedingTick(1),"cadence");
    req(TC4FertilityLampParity.populationAllowed(7) && !TC4FertilityLampParity.populationAllowed(8),"population");
    req(TC4FertilityLampParity.eligibleAnimal(0,false) && !TC4FertilityLampParity.eligibleAnimal(0,true) && !TC4FertilityLampParity.eligibleAnimal(-1,false),"eligibility");
    req(TC4FertilityLampParity.suction(0,true)==128 && TC4FertilityLampParity.suction(4,true)==88 && TC4FertilityLampParity.suction(0,false)==0,"suction");
    System.out.println("TC4FertilityLampParitySelfTest: PASS");
  }
}
