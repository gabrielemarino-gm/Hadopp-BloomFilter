package it.unipi.hadoop;

import org.apache.hadoop.util.hash.Hash;
import org.apache.hadoop.util.hash.MurmurHash;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BloomFilterTest
{


    public static void main(String[] args) throws IOException
    {
        // Variables
        final double p = 0.01; //not used
        final int k = 7; //number of hash functions to use with the given bloom filters

        double falsePositives[] = new double[10];
        double trueNegatives [] = new double[10];

        // Take all input file

        //TEST STEFANO
        String filePath_data = "C:\\Users\\stefa\\OneDrive\\Documenti\\GitHub\\Hadoop-BloomFilter\\data.txt";
        String filePath_m = "C:\\Users\\stefa\\OneDrive\\Documenti\\GitHub\\Hadoop-BloomFilter\\Array_m_output";
        String filePath_BF = "C:\\Users\\stefa\\OneDrive\\Documenti\\GitHub\\Hadoop-BloomFilter\\BloomFilter_output";

        //TEST GABRIELE
        /*
        String filePath_data = "D:\\Università\\Magistrale\\Primo anno\\Cloud Computing\\Ciuchino Team\\Data\\data.txt";
        String filePath_m = "D:\\Università\\Magistrale\\Primo anno\\Cloud Computing\\Ciuchino Team\\Data\\Array_m_output";
        String filePath_BF = "D:\\Università\\Magistrale\\Primo anno\\Cloud Computing\\Ciuchino Team\\Data\\BloomFilter_output";
        */

        BufferedReader dataBr = new BufferedReader(new FileReader(filePath_data)); //to read the dataset
        BufferedReader bloomFilterBr= new BufferedReader(new FileReader(filePath_BF)); //to read the filters
        BufferedReader mBR= new BufferedReader(new FileReader(filePath_m)); //to read the filters

        int[] m = new int[10];
        try
        {
            String line;
            line=mBR.readLine();

            while (line != null)
            {
                if (line.startsWith(""))
                {
                    //split the input
                    String[] inputs = line.split("\t");
                    //take the rating
                    int i = Integer.parseInt(inputs[0]);
                    //assing to the correct position the m value associated to the rating
                    m[i-1] = Integer.parseInt(inputs[1]);
                }

                // be sure to read the next line otherwise we get an infinite loop
                line = mBR.readLine();
            }
        }
        finally
        {
            // close out the BufferedReader
            mBR.close();
        }

        Hash h  = new MurmurHash(); //hash function family to use for the test
        int[][] bloomFilter = new int[10][]; //to store the bloom filters
        //for each filter we set the length to the value specified by m
        for (int i = 0; i < bloomFilter.length; ++i) {
            int tmp = m[i];
            bloomFilter[i] = new int[tmp];
        }

        try
        {
            String line;
            line = bloomFilterBr.readLine();
            while (line != null)
            {
                String[] inputs = line.split("\t"); //key value split
                //we take the key and assing the bloom filter to the corresponding entry
                int i = Integer.parseInt(inputs[0]);
                String[] bfArray = inputs[1].split(" ");
                for(int j = 0; j < bfArray.length; j++) {
                    bloomFilter[i-1][j] = Integer.parseInt(bfArray[j]);
                }
                // be sure to read the next line otherwise we get an infinite loop
                line = bloomFilterBr.readLine();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            // close out the BufferedReader
            bloomFilterBr.close();
        }

        try
        {
            String line;
            line = dataBr.readLine();
            while (line != null)
            {
                String[] inputs = line.split("\t"); //take the values from the input entry
                String movie_name = inputs[0]; //movie id
                double rate = Double.parseDouble(inputs[1]); //take the rating
                int rating = (int) Math.round((rate)); //round the rating
                Boolean positive;
                for(int l = 0; l < bloomFilter.length; l++)
                {
                    positive = true; //initialize to true
                    for (int j = 0; j < k; j++)
                    {
                        //take the hash value for checking the elements
                        int pos = (h.hash(movie_name.getBytes(StandardCharsets.UTF_8), movie_name.length(), j) % m[l] + m[l]) % m[l];

                        //if there is not an element but it's not supposed to be there, then the element is a true negative
                        if ((bloomFilter[l][pos] == 0) && (l != rating - 1))
                        {
                            trueNegatives[l]++;
                            positive = false; //set to false in case we have a negative
                            break;
                        }
                    }
                    //if the element is in the filter but it shouldn't be there is a false positive
                    if(positive && l != rating -1) //positive is true if the value was not 0
                    {
                        falsePositives[l]++;
                    }
                }

                // be sure to read the next line otherwise we get an infinite loop
                line = dataBr.readLine();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            // close out the BufferedReader
            dataBr.close();
        }

        System.out.println("\n\n**********RESULTS**********\n\n");
        for(int i = 0; i < 10; i++)
        {
            //compute the false positive rate
            double fp_rate = falsePositives[i] / (falsePositives[i] + trueNegatives[i]);
            int j = i + 1;
            System.out.println("Rate " + j + ": False positives =  " + falsePositives[i] + ", FPR =  " + fp_rate  + "\n");
        }


    }

}

