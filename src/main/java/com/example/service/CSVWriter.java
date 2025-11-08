package com.example.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVWriter {
    public PrintWriter writer;
    
    public CSVWriter(String filename) throws IOException {
        writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
        writer.println("Source,Section,Headline,Description,Time,Category,Link,ImageLink");
    }
    
    public synchronized void writeRow(String source, String section, String headline, 
                                     String description, String time, String category, 
                                     String link, String imageLink) {
        writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
            escape(source), escape(section), escape(headline), escape(description),
            escape(time), escape(category), escape(link), escape(imageLink));
        writer.flush();
    }
    
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "'").replace("\n", " ").replace("\r", " ").trim();
    }
    
    public void close() {
        if (writer != null) writer.close();
    }
}