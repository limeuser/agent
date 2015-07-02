package cn.oasistech.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    private List<PrintStream> printers;

    public Logger() {
        printers = new ArrayList<PrintStream>();
    }
    
    public Logger addPrinter(PrintStream printer) {
        printers.add(printer);
        return this;
    }
    
    public Logger addFilePrinter(String name) {
        try {
            File file = new File(name);
            if (!file.exists()) {
                file.createNewFile();
            }
            
            addPrinter(new PrintStream(new FileOutputStream(file)));
        } catch (IOException e) {
            System.out.println("add file log error: ");
            e.printStackTrace();
        }
        
        return this;
    }
    
    public Logger log(String format, Object ...args) {
        for (PrintStream printer : printers)
            printer.println(String.format(format, args));
        
        return this;
    }
    
    public Logger log(String format, Throwable e, Object ...args) {
        log(format, args);
        log("", e);
        return this;
    }
    
    public Logger log(String msg, Throwable e) {
        for (PrintStream printer : printers) {
            printer.println(msg);
            e.printStackTrace(printer);
        }
        return this;
    }
}
