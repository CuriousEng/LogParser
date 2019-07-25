package com;

import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;

public class Solution {
    public static void main(String[] args) {
        LogParser logParser = new LogParser(Paths.get("/home/code/src/Projects/LogParser/src/com/logs/"));
        System.out.println(logParser.getNumberOfUniqueIPs(null, new Date()));
        Set<Object> result = logParser.execute("get event for date = \"30.01.2014 12:56:22\"");
        Set<Object> result2 = logParser.execute("get ip for user = \"Eduard Petrovich Morozko\" and date between \"11.12.2013 0:00:00\" and \"03.01.2014 23:59:59\"");

        for (Object o : result){
            System.out.println(o);
        }
    }
}