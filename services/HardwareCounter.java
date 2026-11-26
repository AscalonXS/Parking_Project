package services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HardwareCounter
{
    // the CSV file the Arduino bridge writes, one line per event:
    //   timestamp,lot,event
    //   2026-11-26T10:00:05,Lot A,IN        -> a car came in
    //   2026-11-26T10:05:12,Lot A,OUT       -> a car left
    //   2026-11-26T10:00:00,Lot A,RESET     -> the Arduino restarted, this lot goes back to 0
    private final Path csvPath;

    // lot name -> cars inside
    private final Map<String, Integer> counts = new ConcurrentHashMap<>();

    // how many CSV lines were already counted, so we only read the new ones
    private long linesRead = 0;

    // false until the first read of the file, the old events in it are counted but not printed one by one
    private boolean loaded = false;

    // true after the "waiting for the file" message was printed, so it is printed only once
    private boolean waitingPrinted = false;

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "hardware-csv-reader");
            t.setDaemon(true);   // do not keep the server alive just for this thread
            return t;
        });

    public HardwareCounter(String csvFile)
    {
        this.csvPath = Paths.get(csvFile);
    }

    // check the CSV file every 2 seconds
    public void start()
    {
        System.out.println("[Hardware] Watching " + csvPath.toAbsolutePath().normalize());
        scheduler.scheduleWithFixedDelay(this::poll, 0, 2, TimeUnit.SECONDS);
    }

    public void stop()
    {
        scheduler.shutdownNow();
    }

    private synchronized void poll()
    {
        try
        {
            if (!Files.exists(csvPath))
            {
                if (!waitingPrinted)
                {
                    System.out.println("[Hardware] Waiting for " + csvPath + " (start hardware/serial_to_csv.py)");
                    waitingPrinted = true;
                }
                return;
            }

            String content = Files.readString(csvPath);

            // use complete lines only, the bridge may be writing the last line right now
            int end = content.lastIndexOf('\n');
            if (end < 0)
            {
                return;
            }
            String[] lines = content.substring(0, end).split("\r?\n", -1);

            // the file was cleared or replaced, so start again from the top
            if (lines.length < linesRead)
            {
                linesRead = 0;
                counts.clear();
                System.out.println("[Hardware] " + csvPath + " was cleared, counting again from 0");
            }

            // the first read only counts the old events, the new ones after that are printed one by one
            boolean printEvents = loaded;

            for (int i = (int) linesRead; i < lines.length; i++)
            {
                apply(lines[i], printEvents);
            }
            linesRead = lines.length;

            if (!loaded)
            {
                loaded = true;
                printSummary();
            }
        }
        catch (Exception e)
        {
            // an error here must not stop the scheduled task
            System.err.println("HardwareCounter: cannot read " + csvPath + " -> " + e.getMessage());
        }
    }

    // apply one CSV line, the header, empty lines and broken lines are skipped
    // print = true writes the event to the server console
    private void apply(String line, boolean print)
    {
        line = line.trim();
        if (line.isEmpty() || line.toLowerCase().startsWith("timestamp"))
        {
            return;
        }

        String[] p = line.split(",");
        if (p.length < 3)
        {
            return;
        }

        String lot = p[1].trim();
        String event = p[2].trim().toUpperCase();

        if (event.equals("RESET"))
        {
            counts.put(lot, 0);
            log(print, lot, "RESET", "restarted, 0 cars inside");
        }
        else if (event.equals("IN"))
        {
            int now = counts.merge(lot, 1, Integer::sum);
            log(print, lot, "IN", "car came in -> " + carsText(now) + " inside");
        }
        else if (event.equals("OUT"))
        {
            // never go below 0
            int before = counts.getOrDefault(lot, 0);
            int now = counts.compute(lot, (k, v) -> Math.max(0, (v == null ? 0 : v) - 1));

            if (before == 0)
            {
                log(print, lot, "OUT", "ignored, 0 cars inside already");
            }
            else
            {
                log(print, lot, "OUT", "car left -> " + carsText(now) + " inside");
            }
        }
    }

    // [Hardware] 10:15:03  Lot A  IN  car came in -> 3 cars inside
    private void log(boolean print, String lot, String event, String text)
    {
        if (print)
        {
            System.out.println("[Hardware] " + LocalTime.now().withNano(0) + "  " + lot + "  " + event + "  " + text);
        }
    }

    // "1 car" or "3 cars"
    private String carsText(int n)
    {
        return n + (n == 1 ? " car" : " cars");
    }

    // printed once, after the first read of the file
    private void printSummary()
    {
        if (counts.isEmpty())
        {
            System.out.println("[Hardware] " + csvPath + " has no events yet");
            return;
        }
        for (Map.Entry<String, Integer> e : new TreeMap<>(counts).entrySet())
        {
            System.out.println("[Hardware] Loaded " + csvPath + ": " + e.getKey() + " = " + carsText(e.getValue()) + " inside");
        }
    }

    // cars inside one lot, 0 if the lot has no events yet
    public int getCount(String lotName)
    {
        return counts.getOrDefault(lotName, 0);
    }

    // cars inside for every lot in the CSV, sorted by lot name
    public List<LotCount> getAllCounts()
    {
        List<LotCount> result = new ArrayList<>();

        for (Map.Entry<String, Integer> e : new TreeMap<>(counts).entrySet())
        {
            result.add(new LotCount(e.getKey(), e.getValue()));
        }
        return result;
    }
}
