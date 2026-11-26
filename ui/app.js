const container = document.querySelector(".slots");

const Slots = [
  { num: "01", state: "available", img: "turtle4.png" },
  { num: "02", state: "available", img: "turtle3.png" },
  { num: "03", state: "occupied", img: "turtle2.png" },
  { num: "04", state: "occupied", img: "turtle1.png" },
  { num: "05", state: "reserved", img: "turtle5.png" },
];

function twoDigits(n) {
  return String(n).padStart(2, "0");
}

function syncStatBoxes() {
  const totalEl = document.querySelector(".stat-total");
  const occEl = document.querySelector(".stat-occupied");
  const availEl = document.querySelector(".stat-available");

  if (totalEl) totalEl.textContent = twoDigits(Slots.length);
  if (occEl) {
    occEl.textContent = twoDigits(
      Slots.filter((s) => s.state === "occupied").length,
    );
  }
  if (availEl) {
    availEl.textContent = twoDigits(
      Slots.filter((s) => s.state === "available").length,
    );
  }
}

function renderSlots() {
  if (!container) return;
  container.innerHTML = "";

  for (const s of Slots) {
    const box = document.createElement("div");
    box.className = `slotbox ${s.state}`;
    box.innerHTML = `
      <div>${s.num}</div>
      <img src="${s.img}" alt="Spot ${s.num}" />
      <div>${s.state}</div>
    `;
    container.appendChild(box);
  }

  syncStatBoxes();
}

function handleArduinoLine(line) {
  const clean = line.trim();
  if (clean.includes("EVENT:IN")) {
    const openSlot = Slots.find((s) => s.state === "available");
    if (openSlot) openSlot.state = "occupied";
    renderSlots();
  } else if (clean.includes("EVENT:OUT")) {
    const occupied = Slots.filter((s) => s.state === "occupied");
    const lastOccupied = occupied[occupied.length - 1];
    if (lastOccupied) lastOccupied.state = "available";
    renderSlots();
  }
}

async function connectSerial() {
  if (!("serial" in navigator)) {
    alert("Use Chrome or Edge over http://localhost or http://127.0.0.1.");
    return;
  }

  const port = await navigator.serial.requestPort();
  await port.open({ baudRate: 9600 });

  const decoder = new TextDecoderStream();
  port.readable.pipeTo(decoder.writable);
  const reader = decoder.readable.getReader();

  let buffer = "";
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += value;

    const parts = buffer.split("\n");
    buffer = parts.pop();
    for (const part of parts) {
      handleArduinoLine(part);
    }
  }
}

const serialBtn = document.createElement("button");
serialBtn.textContent = "Connect Uno R3";
serialBtn.style.marginBottom = "12px";
serialBtn.addEventListener("click", () => {
  connectSerial().catch((err) => console.error("Serial error:", err));
});
document.querySelector(".LotCard")?.prepend(serialBtn);

renderSlots();
