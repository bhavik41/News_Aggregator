// package com.example;

// import java.io.PrintWriter;
// import java.io.BufferedWriter;
// import java.io.FileWriter;
// import java.io.IOException;

// class CSVWriter {
//     private PrintWriter writer;
    
//     public CSVWriter(String filename) throws IOException {
//         writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
//         writer.println("Source,Section,Headline,Description,Time,Category,Link,ImageLink");
//     }
    
//     public synchronized void writeRow(String source, String section, String headline, 
//                                      String description, String time, String category, 
//                                      String link, String imageLink) {
//         try {
//             writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
//                 escape(source), escape(section), escape(headline), escape(description),
//                 escape(time), escape(category), escape(link), escape(imageLink));
//             writer.flush(); // Force write immediately to disk
//         } catch (Exception e) {
//             System.err.println("⚠️ Failed to write row: " + e.getMessage());
//         }
//     }
    
//     private String escape(String s) {
//         if (s == null) return "";
//         return s.replace("\"", "'").replace("\n", " ").replace("\r", " ").trim();
//     }
    
//     public void close() {
//         if (writer != null) writer.close();
//     }
// }

package com.example;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

class CSVWriter {
    private PrintWriter writer;
    private final String filename;
    private final AtomicInteger rowCount = new AtomicInteger(0);
    private volatile boolean closed = false;
    
    public CSVWriter(String filename) throws IOException {
        this.filename = filename;
        // Use append=false to create new file, autoFlush=true for immediate writing
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)), true);
        // Write header immediately
        writer.println("Source,Section,Headline,Description,Time,Category,Link,ImageLink");
        writer.flush();
        System.out.println("📄 CSV file created: " + filename);
    }
    
    /**
     * Write a row to CSV - synchronized to ensure thread safety
     * Data is written immediately to disk (autoFlush is enabled)
     */
    public synchronized void writeRow(String source, String section, String headline, 
                                     String description, String time, String category, 
                                     String link, String imageLink) {
        if (closed) {
            System.err.println("⚠️ Attempted to write to closed CSV file");
            return;
        }
        
        try {
            // Format the row
            String row = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                escape(source), escape(section), escape(headline), escape(description),
                escape(time), escape(category), escape(link), escape(imageLink));
            
            // Write and flush immediately
            writer.println(row);
            writer.flush(); // Explicit flush to ensure data is written to disk
            
            int count = rowCount.incrementAndGet();
            
            // Log every 10 rows
            if (count % 10 == 0) {
                System.out.println("💾 " + count + " articles saved to CSV");
            }
            
        } catch (Exception e) {
            System.err.println("⚠️ Failed to write row to CSV: " + e.getMessage());
            e.printStackTrace();
            // Try to recover by recreating writer
            try {
                recoverWriter();
            } catch (IOException ioe) {
                System.err.println("❌ Failed to recover CSV writer: " + ioe.getMessage());
            }
        }
    }
    
    /**
     * Escape special characters for CSV format
     */
    private String escape(String s) {
        if (s == null || s.isEmpty()) return "";
        // Replace quotes with single quotes, remove newlines and carriage returns
        return s.replace("\"", "'")
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();
    }
    
    /**
     * Try to recover the writer in case of error
     */
    private synchronized void recoverWriter() throws IOException {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                // Ignore close errors
            }
        }
        // Reopen in append mode to continue writing
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)), true);
        System.out.println("♻️ CSV writer recovered");
    }
    
    /**
     * Close the CSV writer and print final statistics
     */
    public synchronized void close() {
        if (closed) return;
        
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
                closed = true;
                System.out.println("📊 Total rows written to CSV: " + rowCount.get());
                System.out.println("💾 CSV file saved: " + filename);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error closing CSV writer: " + e.getMessage());
        }
    }
    
    /**
     * Get the number of rows written
     */
    public int getRowCount() {
        return rowCount.get();
    }
    
    /**
     * Check if writer is still open
     */
    public boolean isOpen() {
        return !closed && writer != null;
    }
}