import java.io.BufferedWriter;
import java.io.FileWriter;

public class Main
{
    public static void main(String[] args)throws Exception
    {
        runRCPSP sample1;
        String fileName;
    	String dir =  "Data/BL/";
        for(int j = 19; j<= 20; j++) {
            fileName = dir + "bl25_" + j + ".rcp";
            String name = "bl25_" + j;
            System.out.print(name + ".rcp" + " | ");
            for (int prop = 0; prop < 3; prop++) {
                for (int branch = 0; branch < 1; branch++) {
                    sample1 = new runRCPSP(fileName, prop, branch);
                    System.out.print(+sample1.howMuchTime() + " | " + sample1.howManyBacktracks() + " | " + sample1.makeSpanSolution() + " | " + sample1.howManyAdjustments() + " | ");
                }
            }
            System.out.println(" ");
        }
    }
}
