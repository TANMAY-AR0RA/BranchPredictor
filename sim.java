import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class sim {

    public static void main(String[] args) {
        if (args.length > 0) {
            switch(args[0]) {
                case "smith": {
                    int base = parse(args[1]);
                    String trace_file = args[2];
                    Smith_predictor(base, trace_file); //Calling Smith n-bit counter predictor function by passing parsed command-line arguments
                    break;
                }
                case "bimodal": {
                    int base = parse(args[1]);
                    String trace_file = args[2];
                    Bimodal_predictor(base, trace_file); //Calling bimodal branch predictor function by passing parsed command-line arguments
                    break;
                }
                case "gshare": {
                    int base_M = parse(args[1]);
                    int base_N = parse(args[2]);
                    String trace_file = args[3];
                    Gshare_predictor(base_M, base_N, trace_file); //Calling gshare branch predictor function by passing parsed command-line arguments
                    break;
                }
                case "hybrid":{
                    int base_K = parse(args[1]);
                    int base_M1 = parse(args[2]);
                    int base_N1 = parse(args[3]);
                    int base_M2 = parse(args[4]);
                    String trace_file = args[5];
                    Hybrid_predictor(base_K, base_M1, base_N1, base_M2, trace_file); //Calling Hybrid Branch predictor function by passing parsed command-line arguments
                    break;
                }
            }
        }
    } //main function
    private static int parse(String arg){
        return Integer.parseInt(arg);
    } // parse function to cast String arguments into Integer
    public static void Smith_predictor(int base, String trace_file){ //Smith n-bit counter predictor
        int lineCount = 0;
        int mispredictionCount = 0;
        char branchPredictor = 't';
        int initialValue = (int) Math.pow(2, base) / 2;
        int counterValue = initialValue;
        int maxValue = (int) Math.pow(2, base) - 1;
        String filePath =  "traces/" + trace_file;
        try(BufferedReader readFile = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = readFile.readLine()) != null) {
                lineCount++;
                String[] mispredictionLines = line.trim().split(" ");
                char branchActualOutcome = mispredictionLines[1].charAt(0);

                if (!(branchActualOutcome == branchPredictor)) {
                    mispredictionCount++;
                }
                switch (branchActualOutcome) {
                    case 't' -> {
                        if (counterValue < maxValue) {
                            counterValue++;
                        }
                        if (initialValue <= counterValue && counterValue <= maxValue) {
                            branchPredictor = 't';
                        } else {
                            branchPredictor = 'n';
                        }
                    }

                    case 'n' -> {
                        if (counterValue > 0) {
                            counterValue--;
                        }
                        if (0 <= counterValue && counterValue <= initialValue - 1) {
                            branchPredictor = 'n';
                        } else {
                            branchPredictor = 't';
                        }
                    }
                }
            }
        }catch (Exception e){System.out.println("wrong input");}


        double mispredictionRate = (double) mispredictionCount / lineCount * 100;
        System.out.println("COMMAND" + "\n"+
                           "./sim smith "+ base + " " + trace_file + "\n"+
                           "OUTPUT" + "\n"+
                           "number of predictions:\t\t" + lineCount + "\n"+
                           "number of mispredictions:\t" + mispredictionCount + "\n"+
                           "misprediction rate:\t\t" + String.format("%.2f", mispredictionRate) + "%" + "\n"+
                           "FINAL COUNTER CONTENT:\t\t" + counterValue);
    }

    public static void Bimodal_predictor(int baseValue, String trace_file) { //Bimodal branch predictor, for this n=0
        int[] bimodalCounter = new int[(int) Math.pow(2, baseValue)];

        for (int i = 0; i < bimodalCounter.length; i++) {
            bimodalCounter[i] = 4;
        }

        char branchPredictor = ' ';
        int lineCount = 0;
        int mispredictionCount = 0;

        String filePath =  "traces/" + trace_file;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] hexValues = line.trim().split(" ");
                String binaryAddress = hexToBinary(hexValues[0]);
                String tempBinAddress = binaryAddress.substring(0, binaryAddress.length() - 2);
                String newBinaryAddress = tempBinAddress.substring(tempBinAddress.length() - baseValue);
                int indexBimodal = Integer.parseInt(newBinaryAddress, 2);
                lineCount++;
                char branchActualOutcome = hexValues[1].charAt(0);

                if (bimodalCounter[indexBimodal] >= 4) {
                    branchPredictor = 't';
                } else {
                    branchPredictor = 'n';
                }

                if (branchActualOutcome != branchPredictor) {
                    mispredictionCount++;
                }

                if (branchActualOutcome == 'n') {
                    if (bimodalCounter[indexBimodal] > 0) {
                        bimodalCounter[indexBimodal]--;
                    }
                    if (0 <= bimodalCounter[indexBimodal] && bimodalCounter[indexBimodal] <= 3) {
                        branchPredictor = 'n';
                    } else {
                        branchPredictor = 't';
                    }
                }

                if (branchActualOutcome == 't') {
                    if (bimodalCounter[indexBimodal] < 7) {
                        bimodalCounter[indexBimodal]++;
                    }
                    if (4 <= bimodalCounter[indexBimodal] && bimodalCounter[indexBimodal] <= 7) {
                        branchPredictor = 't';
                    } else {
                        branchPredictor = 'n';
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("wrong input");
        }

        double mispredictionRate = ((double) mispredictionCount / lineCount) * 100;
        System.out.println("COMMAND" + "\n"+
                           "./sim bimodal "+ baseValue + " " + trace_file + "\n"+
                           "OUTPUT" + "\n"+
                           "number of predictions:\t\t" + lineCount + "\n"+
                           "number of mispredictions:\t" + mispredictionCount + "\n"+
                           "misprediction rate:\t\t" + String.format("%.2f", mispredictionRate) + "%" + "\n"+
                           "FINAL BIMODAL CONTENTS");
        for (int i = 0; i < bimodalCounter.length; i++) {
            System.out.println(i + "\t" + bimodalCounter[i]);
        }
    }

    public static void Gshare_predictor(int M, int N, String trace_file) { //Gshare Branch Predictor
        int[] gshareCounter = new int[(int) Math.pow(2, M)];
        char[] gbhr_old = new char[N]; // Array for Global branch history register

        for (int i = 0; i < gbhr_old.length; i++) {
            gbhr_old[i] = '0';
        } // storing 0 value initially in the branch history register

        StringBuilder gbhr_new = new StringBuilder(new String(gbhr_old));

        for (int i = 0; i < gshareCounter.length; i++) {
            gshareCounter[i] = 4;
        }

        char branchPredictor = ' ';
        int lineCount = 0;
        int mispredictionCount = 0;

        String filePath =  "traces/" + trace_file;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] hexValues = line.trim().split(" ");
                String binaryAddress = hexToBinary(hexValues[0]);
                String tempBinAddress = binaryAddress.substring(0, binaryAddress.length() - 2);
                String newBinaryAddress = tempBinAddress.substring(tempBinAddress.length() - M);
                String firstPart = newBinaryAddress.substring(0, newBinaryAddress.length() - N);
                String secondPart = newBinaryAddress.substring(newBinaryAddress.length() - N);

                StringBuilder temp = new StringBuilder();
                for (int i = 0; i < N; i++) {
                    temp.append(secondPart.charAt(i) == gbhr_new.charAt(i) ? "0" : "1");
                }

                firstPart += temp.toString();
                int index = Integer.parseInt(firstPart, 2);
                lineCount++;
                char branchActualOutcome = hexValues[1].charAt(0);

                if (gshareCounter[index] >= 4) {
                    branchPredictor = 't';
                } else {
                    branchPredictor = 'n';
                }

                if (branchActualOutcome != branchPredictor) {
                    mispredictionCount++;
                }

                switch (branchActualOutcome) {
                    case 't' -> {
                        gbhr_new = new StringBuilder("1" + gbhr_new.substring(0, N - 1));
                        if (gshareCounter[index] < 7) {
                            gshareCounter[index]++;
                        }
                    }

                    case 'n' -> {
                        gbhr_new = new StringBuilder("0" + gbhr_new.substring(0, N - 1));
                        if (gshareCounter[index] > 0) {
                            gshareCounter[index]--;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("wrong input");
        }
        double mispredictionRate = (double) mispredictionCount / lineCount * 100;
        System.out.println("COMMAND" + "\n"+
                           "./sim gshare "+ M + " " + N + " " + trace_file + "\n"+
                           "OUTPUT" + "\n"+
                           "number of predictions:\t\t" + lineCount + "\n"+
                           "number of mispredictions:\t" + mispredictionCount + "\n"+
                           "misprediction rate:\t\t" + String.format("%.2f", mispredictionRate) + "%" + "\n"+
                           "FINAL GSHARE CONTENTS");
        for (int i = 0; i < gshareCounter.length; i++) {
            System.out.println(i + "\t" + gshareCounter[i]);
        }
    }

    public static void Hybrid_predictor(int K, int M1, int N1, int M2, String trace_file) { //Hybrid Branch Predictor
        int[] BranchChooserCounter = new int[(int) Math.pow(2, K)];
        int[] gshareCounter = new int[(int) Math.pow(2, M1)];
        char[] gbhr_old = new char[N1]; // Array for Global branch history register

        for (int i = 0; i < gbhr_old.length; i++) {
            gbhr_old[i] = '0';
        } // storing 0 value initially in the branch history register

        StringBuilder gbhr_new = new StringBuilder(new String(gbhr_old));
        int[] bimodalCounter = new int[(int) Math.pow(2, M2)];

        for (int i = 0; i < BranchChooserCounter.length; i++) {
            BranchChooserCounter[i] = 1;
        }

        for (int i = 0; i < gshareCounter.length; i++) {
            gshareCounter[i] = 4;
        }

        for (int i = 0; i < bimodalCounter.length; i++) {
            bimodalCounter[i] = 4;
        }

        char branchPredictorBimodal = ' ';
        char branchPredictorGshare = ' ';
        int lineCount = 0;
        int mispredictionCount = 0;

        String filePath =  "traces/" + trace_file;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] hexValues = line.trim().split(" ");
                String binAddress = hexToBinary(hexValues[0]);
                String tempBinAddress = binAddress.substring(0, binAddress.length() - 2);
                String newBinAddressHybrid = tempBinAddress.substring(tempBinAddress.length() - K);
                String newBinAddressBimodal = tempBinAddress.substring(tempBinAddress.length() - M2);
                String newBinAddressGshare = tempBinAddress.substring(tempBinAddress.length() - M1);
                String firstPartGshare = newBinAddressGshare.substring(0, newBinAddressGshare.length() - N1);
                String secondPartGshare = newBinAddressGshare.substring(newBinAddressGshare.length() - N1);

                StringBuilder tempGshare = new StringBuilder();
                for (int i = 0; i < N1; i++) {
                    tempGshare.append(secondPartGshare.charAt(i) == gbhr_new.charAt(i) ? '0' : '1');
                }

                firstPartGshare += tempGshare.toString();
                int indexGshare = Integer.parseInt(firstPartGshare, 2);
                int indexBimodal = Integer.parseInt(newBinAddressBimodal, 2);
                int indexHybrid = Integer.parseInt(newBinAddressHybrid, 2);
                lineCount++;
                char branchActualOutcome = hexValues[1].charAt(0);

                if (bimodalCounter[indexBimodal] >= 4) {
                    branchPredictorBimodal = 't';
                } else {
                    branchPredictorBimodal = 'n';
                }

                if (gshareCounter[indexGshare] >= 4) {
                    branchPredictorGshare = 't';
                } else {
                    branchPredictorGshare = 'n';
                }

                if (BranchChooserCounter[indexHybrid] >= 2) {
                    if (!(branchPredictorGshare==branchActualOutcome)) {
                        mispredictionCount++;
                    }

                    switch (branchActualOutcome) {
                        case 'n' -> {
                            gbhr_new = new StringBuilder('0' + gbhr_new.substring(0, N1 - 1));
                            if (gshareCounter[indexGshare] > 0) {
                                gshareCounter[indexGshare]--;
                            }
                        }
                        case 't' -> {
                            gbhr_new = new StringBuilder('1' + gbhr_new.substring(0, N1 - 1));
                            if (gshareCounter[indexGshare] < 7) {
                                gshareCounter[indexGshare]++;
                            }
                        }
                    }
                }
                else {
                    if (!(branchPredictorBimodal==branchActualOutcome)) {
                        mispredictionCount++;
                    }

                    switch (branchActualOutcome) {
                        case 'n' -> {
                            gbhr_new = new StringBuilder('0' + gbhr_new.substring(0, N1 - 1));
                            if (bimodalCounter[indexBimodal] > 0) {
                                bimodalCounter[indexBimodal]--;
                            }
                        }


                        case 't' -> {
                            gbhr_new = new StringBuilder('1' + gbhr_new.substring(0, N1 - 1));
                            if (bimodalCounter[indexBimodal] < 7) {
                                bimodalCounter[indexBimodal]++;
                            }
                        }
                    }
                }

                if (branchPredictorGshare==branchActualOutcome && !(branchPredictorBimodal==branchActualOutcome)) {
                    if (BranchChooserCounter[indexHybrid] < 3) {
                        BranchChooserCounter[indexHybrid]++;
                    }
                }

                if (branchPredictorBimodal==branchActualOutcome && !(branchPredictorGshare==branchActualOutcome)) {
                    if (BranchChooserCounter[indexHybrid] > 0) {
                        BranchChooserCounter[indexHybrid]--;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("wrong input");
        }

        double mispredictionRate = (double) mispredictionCount / lineCount * 100;

        System.out.println("COMMAND" + "\n"+
                           "./sim hybrid "+ K + " " + M1 + " " + N1 + " " + M2 + " " + trace_file + "\n"+
                           "OUTPUT" + "\n"+
                           "number of predictions:\t\t" + lineCount + "\n"+
                           "number of mispredictions:\t" + mispredictionCount + "\n"+
                           "misprediction rate:\t\t" + String.format("%.2f", mispredictionRate) + "%" + "\n"+
                           "FINAL CHOOSER CONTENTS");

        for (int i = 0; i < BranchChooserCounter.length; i++) {
            System.out.println(i + "\t" + BranchChooserCounter[i]);
        }
        System.out.println("FINAL GSHARE CONTENTS");
        for (int i = 0; i < gshareCounter.length; i++) {
            System.out.println(i + "\t" + gshareCounter[i]);
        }
        System.out.println("FINAL BIMODAL CONTENTS");
        for (int i = 0; i < bimodalCounter.length; i++) {
            System.out.println(i + "\t" + bimodalCounter[i]);
        }
    }


    public static String hexToBinary(String inputHex) {
        StringBuilder binaryString = new StringBuilder();
        for (int i = 0; i < inputHex.length(); i++) {
            char hexChar = inputHex.charAt(i);
            String binaryValue = Integer.toBinaryString(Integer.parseInt(String.valueOf(hexChar), 16));
            binaryString.append("0000", binaryValue.length(), 4).append(binaryValue);
        }
        return binaryString.toString();
    } // Function to convert hexadecimal address value into equivalent Binary Value
}
