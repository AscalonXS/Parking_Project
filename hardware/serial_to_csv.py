"""
Bridge between the Arduino and the Java server.

The Arduino (tpark.ino) prints "EVENT:IN" when a car enters and "EVENT:OUT" when a car leaves
over USB serial (9600 baud). This script listens to that, and appends one line per event to
data/cars.csv. The Java server (services/HardwareCounter.java) reads that file.

Install once:  pip install pyserial
Run:           python hardware/serial_to_csv.py --port COM3 --lot "Lot A"
               (leave --port out to auto-detect the Arduino)

Close the Arduino IDE Serial Monitor and the "Connect Uno R3" button in the web page first,
only one program can use the serial port at a time.
"""
import argparse
import csv
import os
import sys
from datetime import datetime

import serial
import serial.tools.list_ports

# data/cars.csv in the project root, no matter where you start the script from
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV_FILE = os.path.join(ROOT, "data", "cars.csv")


def parse_event(line):
    """Return "IN" or "OUT" for the Arduino lines EVENT:IN / EVENT:OUT, otherwise None."""
    line = line.strip().upper()
    if "EVENT:IN" in line:
        return "IN"
    if "EVENT:OUT" in line:
        return "OUT"
    return None


def find_port():
    """Pick the first port that looks like an Arduino."""
    for p in serial.tools.list_ports.comports():
        text = f"{p.description} {p.manufacturer}".lower()
        if "arduino" in text or "ch340" in text or "usb serial" in text:
            return p.device
    return None


def append_row(writer, f, lot, event):
    writer.writerow([datetime.now().isoformat(timespec="seconds"), lot, event])
    f.flush()  # so the Java server sees the line right away


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", help="serial port, for example COM3")
    parser.add_argument("--baud", type=int, default=9600)
    parser.add_argument("--lot", default="Lot A", help="must match parking_lots.name in the database")
    args = parser.parse_args()

    port = args.port or find_port()
    if port is None:
        print("No Arduino found. Plug it in or use --port COM3")
        for p in serial.tools.list_ports.comports():
            print("  available:", p.device, "-", p.description)
        sys.exit(1)

    os.makedirs(os.path.dirname(CSV_FILE), exist_ok=True)
    new_file = not os.path.exists(CSV_FILE) or os.path.getsize(CSV_FILE) == 0

    with open(CSV_FILE, "a", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        if new_file:
            writer.writerow(["timestamp", "lot", "event"])

        # Opening the serial port restarts the Arduino, and it starts again with 0 cars inside.
        # RESET makes the Java side start from 0 too, so both sides always agree.
        with serial.Serial(port, args.baud, timeout=1) as board:
            append_row(writer, f, args.lot, "RESET")
            print(f"Listening on {port}, writing to {CSV_FILE} (Ctrl+C to stop)")

            try:
                while True:
                    line = board.readline().decode("utf-8", errors="ignore")
                    event = parse_event(line)
                    if event:
                        append_row(writer, f, args.lot, event)
                        print(event)
            except KeyboardInterrupt:
                print("Stopped")


if __name__ == "__main__":
    main()