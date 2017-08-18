/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package liu_network_latency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.mathworks.engine.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import matlabcontrol.*;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

/**
 *
 * @author thuydt
 * This code is an implementation of the algorithm 1 in the paper Network Latency Prediction for Personal Devices: Distance-Feature Decomposition from 3D Sampling
 * input: 1. file name of a complete matrix (no missing values) in the format of csv file
 *        2. file name of a known matrix (including values of 0 or 1: 0: missing values in the complete matrix, 1: known values)
 * for example:
 * 1. planetlab_data1: the file name of the latency complete matrix, call D
 * 2. W20N490: the file name of the known matrix, call W. This means that 20% entries in planetlab_data1 are known, 
 * the locations of the known values are (i,j) st W(i,j) = 1.
 * output: The complete latency matrix in a csv file and the error in RMSE and MAE printed on the screen.
 * 
 * input 1: /home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/PlanetLabData_1 
 * input 2: /home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/W80N490 
 
*/
public class Liu_Network_Latency {
    
    public static double[][] myMAT;
    // the location you want to put the output file of the compelte matrix
    private static String out_path = "/home/phuongdm/Documents/THUYDT/NetBeansProjects/Liu_Network_Latency/Data";
    /**
     * @param args the command line arguments
     */
    
    public static double[][] nodejsVivaldi(String data_fname, String known_fname, String iterations) throws IOException{
        // cd to the location you put the vivaldi code file
        String cd_path = "cd /home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data";        
        String node_cmd = "; nodejs Liu_vivaldi.js " + data_fname + " " + iterations + " " + known_fname;
        //ProcessBuilder builder = new ProcessBuilder("bash", "-c", "cd /home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data; nodejs vivaldi.js");
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", cd_path + node_cmd);
        
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = r.readLine(); if (line == null) return null;
        int N = Integer.parseInt(line); if (N<0) return null; //PlanetLabdata_1 => N= 490
        
        double[][] ret = new double[N][N]; int i =-1;
        while (true){
            line = r.readLine();
            if (line == null) break;
            String[] vals = line.split(" ");
            i = i + 1;
            for (int j = 0; j< vals.length; j++) ret[i][j] = Double.parseDouble(vals[j]);
            
            //System.out.println(line);
        }
        return ret;
    }
    // to call MatrixCompletion in matlab
    //input 2 matrices: matrix of values and known-value matrix
    public static double[][] matMC(double[][] A, double[][] B) throws MatlabConnectionException, MatlabInvocationException{
        MatlabProxyFactory fac = new MatlabProxyFactory();
        MatlabProxy proxy = fac.getProxy();       
        // cd to the location you put the matlab code file for matrix completion
        String path = "cd '/home/phuongdm/Documents/THUYDT/matlab/MatrixCompletion/'";
        proxy.eval(path);

        MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
        processor.setNumericArray("A", new MatlabNumericArray(A, null));
        processor.setNumericArray("B", new MatlabNumericArray(B, null));
        proxy.eval("ret = Liu_MC(A,B)");
        double[][] ret = processor.getNumericArray("ret").getRealArray2D();
// for(int i = 0; i < ret.length; i++)
 //{
//     System.out.println(Arrays.toString(ret[i]));
 //}

//Disconnect the proxy from MATLAB
        //proxy.disconnect();
        //myMAT = ret.clone();
//close matlab       
proxy.exit();
/*        try {
            proxy.
        proxy.exit();
        } 
        catch (Exception e) {
            
        }
*/
        return ret;
        //proxy.exit();
    }
    
    //to do DF decomposition
    public static double[][] DF_Liu(String data_fname, String known_fname, int maxIter, String iterations_vivaldi) throws IOException, MatlabConnectionException, MatlabInvocationException{
        String D_fname = data_fname; // 1. D_0 = M
        double[][] M = getData(data_fname); int N = M.length;
        double[][] knownMat = getData(known_fname);
        double[][] D_hat=null, F_hat=null;
        for (int k = 1; k<=maxIter; k++){ //2.
            D_hat = nodejsVivaldi(D_fname, known_fname, iterations_vivaldi); //3. perform Euclidean Embedding on D_k_1 to get the complete matrix D_hat__k
            double[][] F = division(M, D_hat); //4.
            F_hat = matMC(F, knownMat);//5
            if (k == maxIter) break;
            double[][] D = division(M, F_hat);//6
            //D_fname = "/home/phuongdm/Documents/THUYDT/NetBeansProjects/Liu_Network_Latency/Data/D.txt";
            D_fname = out_path + "/D.txt";
            save_file(D, D_fname);
        }
        double[][] M_hat = product(D_hat, F_hat);
        double[] errors = computeError(M, M_hat);
        System.out.println("\nRMSE (M, M_hat): " + String.valueOf(errors[1]));
        System.out.println("\nMAE (M, M_hat): " + String.valueOf(errors[0]));
        return M_hat;
    }
    public static double[] computeError(double[][] actualMat, double[][] predictedMat){
        double RMSE = 0, MAE = 0;
        int N = predictedMat.length;
        for (int i = 0; i<N; i++) for(int j=0; j<N; j++){
            MAE = MAE + Math.abs(predictedMat[i][j] - actualMat[i][j]);
            RMSE = RMSE + Math.pow(predictedMat[i][j] - actualMat[i][j], 2);
        }
        if (N > 0){MAE = MAE /(N*N); RMSE = Math.sqrt(RMSE/(N*N));} double[] ret_val = new double[2];ret_val[0] = MAE; ret_val[1] = RMSE;        
        return ret_val; 
        
    }
       
        //read the data in a csv file into a matrix of doubles
    private static double[][] getData(String data_fname) throws FileNotFoundException {
        Scanner scan = new Scanner(new File (data_fname));       
        double[][] ret = null;
        if (scan.hasNextLine()){           
            String line = scan.nextLine();                        
            String[] vals = line.split(",");
            
            int N = vals.length; if (N == 0) return null;
            ret = new double[N][N];
            for (int j= 0; j<N;j++) ret[0][j] = Double.parseDouble(vals[j]);
            int i = 0;
            while (scan.hasNextLine()){           
                line = scan.nextLine();                        
                vals = line.split(",");
                if (vals.length == 0) break; i = i + 1;
                for (int j=0; j<vals.length;j++)ret[i][j] = Double.parseDouble(vals[j]);
            }
        }
        scan.close();
        return ret;
        
        }                            
        
    

    private static double[][] division(double[][] A, double[][] B) {
        int N = A.length; double[][] ret = new double[N][N];
        for (int i=0; i<N; i++)for (int j=0; j<N; j++) if (B[i][j] != 0)ret[i][j] = A[i][j]/B[i][j]; else ret[i][j] = 0;
        return ret;
    }
    private static double[][] product(double[][] A, double[][] B) {
        int N = A.length;double[][] ret = new double[N][N];
        for (int i=0; i<N; i++)for (int j=0; j<N; j++) ret[i][j] = A[i][j]*B[i][j]; 
        return ret;
    }
    private static void save_file(double[][] D, String D_fname) throws IOException {
        FileWriter fout = new FileWriter(D_fname , false);
        int N= D.length;
        for (int i = 0; i<N; i++){
            String s = String.valueOf(D[i][0]);
            for (int j=1; j<N;j++) if ((j != N -1)|| (i== N-1))s = s + "," + String.valueOf(D[i][j]); else s = s + "," + String.valueOf(D[i][j]) + "\n";
            fout.write(s);
        }
        fout.close();
                        
    }

    

            
    
    public static void main(String[] args) throws IOException, EngineException, InterruptedException, RejectedExecutionException, ExecutionException, MatlabConnectionException, MatlabInvocationException {
        /*int n = 500; 
        double[][] A = new double[n][n], B = new double[n][n];
        for (int i = 0; i < n ; i++) for (int j = 0; j<n; j++){
            A[i][j] = i * j;
            if (j % 3 == 0) B[i][j] = 0; else B[i][j] =1;
        }
       
        A = matMC(A, B);
        for (int i=0; i<A.length; i++)System.out.println(Arrays.toString(A[i]));
        
        */
        
        String data_fname = args[0];//"/home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/PlanetLabData_1";
        String iterations_vivaldi = "1000"; // 1000
        int maxIter = 5;
        String known_fname = args[1];// "/home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/W80N490";
        //String M_hat_fname = args[2]; //"/home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/PlanetLabData_1";
        //double[][] D_hat = nodejsVivaldi(data_fname,known_fname,iterations_vivaldi);
  //      for (int i =0; i<490; i++) System.out.println(Arrays.toString(D_hat[i]));            
        double[][] M_hat  = DF_Liu(data_fname, known_fname, maxIter, iterations_vivaldi);
        save_file(M_hat, out_path + "/M_hat.txt");
        //for (int i=0; i<M_hat.length; i++)System.out.println(Arrays.toString(M_hat[i]));
                     
    }

}    
        
        /*ProcessBuilder builder = new ProcessBuilder("bash", "-c", "cd /home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data; nodejs vivaldi.js");
        //ProcessBuilder pb = new ProcessBuilder(command)
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true){
            line = r.readLine();
            if (line == null) break;
            System.out.println(line);
        }
        */
/*        MatlabProxyFactory fac = new MatlabProxyFactory();
        //MatlabProxy matPro = fac.getProxy();
        MatlabProxy proxy = fac.getProxy();
        String path = "cd '/home/phuongdm/Documents/THUYDT/matlab/MatrixCompletion/'";
        proxy.eval(path);
//matPro.eval(path);
        //matPro.eval("hello"); ok
        //matPro.disconnect();
        for (int i = 1; i<=5; i++){
        double[] a = new double[2]; a[0] = 1; a[1] = 289;
        //Object[] inmem = proxy.returningFeval("inmem", 3);
        Object[] s;
       s = proxy.returningFeval("mysum", 1, a[0], a[1]);
       
       MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
       

System.out.println(String.valueOf(s[0]));

//Retrieve MATLAB's release date by providing the -date argument
//Object[] releaseDate = proxy.returningFeval("version", 1, "-date");
//System.out.println("MATLAB Release Date: " + releaseDate[0]);

//Disconnect the proxy from MATLAB

        }
        //proxy.disconnect();
     }
    
    

    
    
}        
        /*        proxy.eval("array = randn(4,3,2)");

//Print a value of the array into the MATLAB Command Window
proxy.eval("disp(['entry: ' num2str(array(3, 2, 1))])");

//Get the array from MATLAB
MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
MatlabNumericArray array = processor.getNumericArray("array");

//Print out the same entry, using Java's 0-based indexing
System.out.println("entry: " + array.getRealValue(2, 1, 0));

//Convert to a Java array and print the same value again    
double[][][] javaArray = array.getRealArray3D();
System.out.println("entry: " + javaArray[2][1][0]);

//Disconnect the proxy from MATLAB
proxy.disconnect();
        
        
        //MatlabEngine eng = new MatlabEngine();
        MatlabEngine eng = MatlabEngine.startMatlab();
        //MatlabControl matControl = new MatlabControl();
        //matControl.eval("display(5)");
        //args = new Object[1];
//args[0]=new Double(5);
//double[] a = {1,20};
/*Object[] a = new Object[2];
a[0]=new Double(5);
a[1] = new Double(100);
Double returnVals = (Double)matControl.blockingFeval("mysum", a);
System.out.println(returnVals.toString());*/


    
             
        //eng.startMatlab();
       //double[] a = {2.0 ,4.0};
       //double[] roots = eng.feval("sqrt", a);
        
       //double[] roots = eng.feval("mysum", a);
       //double roots = eng.feval("mysum", a);
       //String roots = eng.feval("hello");
       //System.out.println(roots);
       //for (double e: roots) {
//           System.out.println(e);
       //}
       //eng.close();
        
        /*String D_fname = "'/home/phuongdm/Documents/THUYDT/NetBeansProjects/Liu_Network_Latency/Data/D_hat.txt'";
        String known_fname = "'/home/phuongdm/Documents/THUYDT/nodejs/vivaldi-coordinates-master/vivaldi-coordinates-master/data/PlanetLab/W80N490'";
        String F_fname = "'/home/phuongdm/Documents/THUYDT/NetBeansProjects/Liu_Network_Latency/Data/F_mat.txt'";
        String matlabcmd = "matlab MC(" + D_fname + "," + known_fname + "," + F_fname + ")";
        ProcessBuilder builder1 = new ProcessBuilder("bash", "-c", "cd /home/phuongdm/Documents/THUYDT/matlab/MatrixCompletion; "+matlabcmd);
        //ProcessBuilder pb = new ProcessBuilder(command)
        builder1.redirectErrorStream(true);
        Process p1 = builder1.start();
        BufferedReader r1 = new BufferedReader(new InputStreamReader(p1.getInputStream()));
        String line;
        while (true){
            line = r1.readLine();
            if (line == null) break;
            System.out.println(line);
        }
        */
   
