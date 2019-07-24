package com;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class Solution {
    public static void main(String[] args) {
        LogParser logParser = new LogParser(Paths.get("/home/code/src/Projects/LogParser/src/com/logs/"));
        for(Path p: logParser.filePathsCollector){
            System.out.println(p.getFileName());
        }
        System.out.println();
        for(String str: logParser.infoCollector){
            System.out.println(str);
        }
        System.out.println();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Set<String> res, result;
        try {
            Date after = format.parse("30.08.2012 16:08:13");
            Date before = format.parse("03.01.2014 03:45:23");
             res = logParser.getUniqueIPs(null, null);
             for(String s: res){
                 System.out.println(s);
             }
            System.out.println(logParser.getNumberOfUniqueIPs(null, null));
            System.out.println();

            result = logParser.getIPsForUser("Eduard Petrovich Morozko", null, null);
            for(String s: result){
                System.out.println(s);
            }

            System.out.println();

            result = logParser.getIPsForEvent(Event.SOLVE_TASK, null, null);
            for(String s: result){
                System.out.println(s);
            }
            System.out.println();

            result = logParser.getIPsForStatus(Status.OK, null, null);
            for(String s: result){
                System.out.println(s);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }


        //System.out.println(logParser.getNumberOfUniqueIPs(null, new Date()));
    }
}