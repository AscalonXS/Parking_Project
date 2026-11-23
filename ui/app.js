const container = document.querySelector(".slots");

const testSlots = [
  { num: "01", state: "available" },
  { num: "02", state: "available" },
  { num: "03", state: "occupied" },
  { num: "04", state: "occupied" },
  { num: "05", state: "reserved" },
];

for (const s of testSlots) {
  const box = document.createElement("div");
  box.className = `slotbox ${s.state}`;
  box.innerHTML = `
    <div>${s.num}</div>
    <img src="turtle1.png" alt="Spot ${s.num}" />
    <div>${s.state}</div>
  `;
  container.appendChild(box);
}
