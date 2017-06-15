package com.shoeboxscientist.goto10.LocalInstanceClassesForTesting;

import com.shoeboxscientist.goto10.ContentStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Implementation of content store for running locally (e.g. saves files to current directory.
 */
public class LocalContentStore implements ContentStore {

    private static final String DEFAULT_FILE_NAME = "current_script.txt";

    @Override
    public boolean savescript(String script) throws IOException {
        File directory = new File(".");
        File file = new File(directory, DEFAULT_FILE_NAME);
        FileWriter wr = new FileWriter(file);
        try {
            wr.write(script);
            wr.flush();
        } finally {
            wr.close();
        }
        return true;
    }

    @Override
    public String readscript() throws IOException {
        File directory = new File(".");
        File file = new File(directory, DEFAULT_FILE_NAME);
        FileReader r = new FileReader(file);
        BufferedReader br = new BufferedReader(r);
        StringBuilder script = new StringBuilder();
        try {
            String line;
            while ( (line = br.readLine()) != null) {
                script.append(line).append("\n");
            }
        } finally {
            r.close();
        }
        return script.toString();
    }
}
