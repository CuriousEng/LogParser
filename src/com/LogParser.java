package com;

import com.query.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogParser implements IPQuery, UserQuery, DateQuery, EventQuery, QLQuery {
    public List<Path> filePathsCollector;
    public List<String> infoCollector;
    Path logDir;


    public LogParser(Path logDir){
        this.logDir = logDir;
        filePathsCollector = this.getPathsList(logDir);
        infoCollector = this.getInfo(filePathsCollector);
    }

    private List<Path> getPathsList(Path logDir){
        List<Path> listOfPaths = new ArrayList<>();
        try {
            Stream<Path> pathStream = Files.walk(logDir);
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(x -> x.toFile().getName().endsWith(".log"))
                    .forEach(listOfPaths::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listOfPaths;
    }

    private List<String> getInfo (List<Path> listOfPaths){
        List<String> logInfo = new ArrayList<>();
        try {
            for(Path path: listOfPaths) {
                logInfo.addAll(Files.readAllLines(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logInfo;
    }

    private String getIP(String logLine){
        return logLine.split("\\t")[0];
    }

    private String getUser(String logString){
        return logString.split("\\t")[1];
    }

    private String getStatus(String s){
        return s.split("\\t")[4];
    }

    private String getEvent(String s){
        return s.split("\\t")[3].split(" ")[0];
    }

    private Event toEvent(String str) {
        if (str.equals(Event.LOGIN.toString())) return Event.LOGIN;
        else if (str.equals(Event.DOWNLOAD_PLUGIN.toString())) return Event.DOWNLOAD_PLUGIN;
        else if (str.equals(Event.WRITE_MESSAGE.toString())) return Event.WRITE_MESSAGE;
        else if (str.equals(Event.SOLVE_TASK.toString())) return Event.SOLVE_TASK;
        else if (str.equals(Event.DONE_TASK.toString())) return Event.DONE_TASK;
        return null;
    }
    private Status toStatus(String str) {
        if (str.equals(Status.OK.toString())) return Status.OK;
        else if (str.equals(Status.FAILED.toString())) return Status.FAILED;
        else if (str.equals(Status.ERROR.toString())) return Status.ERROR;
        return null;
    }

    private int getEventNumber(String s){
        if (s.split("\\t")[3].split(" ").length == 2) {
            return Integer.parseInt(s.split("\\t")[3].split(" ")[1]);
        } else {
            return 0;
        }

    }

    private Date getDate(String logString){
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date date = null;
        try {
            return format.parse(logString.split("\\t")[2]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //получение из общего списка списка по датам
    private List<String> getListDate(List<String> logInfo, Date after, Date before){
        if (after == null && before == null){
            return logInfo;
        } else if (after == null) {
            return logInfo.stream()
                    .filter(s -> getDate(s).getTime() <= before.getTime())
                    .collect(Collectors.toList());
        } else if (before == null) {
            return logInfo.stream()
                    .filter(s -> getDate(s).getTime() >= after.getTime())
                    .collect(Collectors.toList());
        } else {
            return logInfo.stream()
                    .filter(s -> getDate(s).getTime() >= after.getTime() && getDate(s).getTime() <= before.getTime())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public int getNumberOfUniqueIPs(Date after, Date before) {
        return getUniqueIPs(after, before).size();
    }

    @Override
    public Set<String> getUniqueIPs(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .map(s -> s.split("\\t")[0])
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIPsForUser(String user, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(s -> getUser(s).equals(user))
                .map(this::getIP)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIPsForEvent(Event event, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(s -> getEvent(s).equals(event.toString()))
                .map(this::getIP)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIPsForStatus(Status status, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(s -> getStatus(s).equals(status.toString()))
                .map(this::getIP)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAllUsers() {
        return infoCollector.stream()
                .map(s -> s.split("\\t")[1])
                .collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfUsers(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .map(this::getUser)
                .collect(Collectors.toSet()).size();
    }

    @Override
    public int getNumberOfUserEvents(String user, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .map(this::getEvent)
                .distinct()
                .toArray().length;
    }

    @Override
    public Set<String> getUsersForIP(String ip, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getIP(x).equals(ip))
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getLoggedUsers(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.LOGIN.toString()))
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDownloadedPluginUsers(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.DOWNLOAD_PLUGIN.toString()))
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getWroteMessageUsers(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.WRITE_MESSAGE.toString()))
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.SOLVE_TASK.toString()))
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSolvedTaskUsers(Date after, Date before, int task) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.SOLVE_TASK.toString()))
                .filter(x -> getEventNumber(x) == task)
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.DONE_TASK.toString()))
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getDoneTaskUsers(Date after, Date before, int task) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.DONE_TASK.toString()))
                .filter(x -> getEventNumber(x) == task)
                .map(this::getUser)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesForUserAndEvent(String user, Event event, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .filter(x -> getEvent(x).equals(event.toString()))
                .map(this::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenSomethingFailed(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getStatus(x).equals(Status.FAILED.toString()))
                .map(this::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenErrorHappened(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getStatus(x).equals(Status.ERROR.toString()))
                .map(this::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public Date getDateWhenUserLoggedFirstTime(String user, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .filter(x -> getEvent(x).equals(Event.LOGIN.toString()))
                .map(this::getDate)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Date getDateWhenUserSolvedTask(String user, int task, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .filter(x -> getEvent(x).equals(Event.SOLVE_TASK.toString()))
                .filter(x -> getEventNumber(x) == task)
                .map(this::getDate)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Date getDateWhenUserDoneTask(String user, int task, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .filter(x -> getEvent(x).equals(Event.DONE_TASK.toString()))
                .filter(x -> getEventNumber(x) == task)
                .map(this::getDate)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<Date> getDatesWhenUserWroteMessage(String user, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .filter(x -> getEvent(x).equals(Event.WRITE_MESSAGE.toString()))
                .map(this::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Date> getDatesWhenUserDownloadedPlugin(String user, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .filter(x -> getEvent(x).equals(Event.DOWNLOAD_PLUGIN.toString()))
                .map(this::getDate)
                .collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfAllEvents(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .map(this::getEvent)
                .collect(Collectors.toSet()).size();

    }


    @Override
    public Set<Event> getAllEvents(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .map(this::getEvent)
                .map(this::toEvent)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getEventsForIP(String ip, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getIP(x).equals(ip))
                .map(x -> toEvent(getEvent(x)))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getEventsForUser(String user, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getUser(x).equals(user))
                .map(x -> toEvent(getEvent(x)))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getFailedEvents(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getStatus(x).equals(Status.FAILED.toString()))
                .map(x -> toEvent(getEvent(x)))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Event> getErrorEvents(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getStatus(x).equals(Status.ERROR.toString()))
                .map(x -> toEvent(getEvent(x)))
                .collect(Collectors.toSet());
    }

    @Override
    public int getNumberOfAttemptToSolveTask(int task, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.SOLVE_TASK.toString()))
                .filter(x -> getEventNumber(x) == task)
                .map(e -> 1)
                .reduce(0, Integer::sum);

    }

    @Override
    public int getNumberOfSuccessfulAttemptToSolveTask(int task, Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.DONE_TASK.toString()))
                .filter(x -> getEventNumber(x) == task)
                .map(e -> 1)
                .reduce(0, Integer::sum);
    }

    @Override
    public Map<Integer, Integer> getAllSolvedTasksAndTheirNumber(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.SOLVE_TASK.toString()))
                .map(this::getEventNumber)
                .collect(Collectors.groupingBy(
                        Function.identity(), Collectors.reducing(0, e -> 1, Integer::sum))
                );
    }

    @Override
    public Map<Integer, Integer> getAllDoneTasksAndTheirNumber(Date after, Date before) {
        return getListDate(infoCollector, after, before).stream()
                .filter(x -> getEvent(x).equals(Event.DONE_TASK.toString()))
                .map(this::getEventNumber)
                .collect(Collectors.groupingBy(
                        Function.identity(), Collectors.reducing(0, e -> 1, Integer::sum))
                );
    }

    @Override
    public Set<Object> execute(String query) {
        switch (query) {
            case "get ip":
                return new HashSet<>(getUniqueIPs(null, null));
            case "get user":
                return new HashSet<>(getAllUsers());
            case "get date":
                return infoCollector.stream()
                        .map(this::getDate)
                        .collect(Collectors.toSet());
            case "get event":
                return infoCollector.stream()
                        .map(x -> toEvent(getEvent(x)))
                        .collect(Collectors.toSet());
            case "get status":
                return infoCollector.stream()
                        .map(x -> toStatus(getStatus(x)))
                        .collect(Collectors.toSet());
        }
        return null;
    }
}