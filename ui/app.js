const container = document.querySelector(".slots");

const testSlots = [
  { num: "01", state: "available", img: "turtle4.png" },
  { num: "02", state: "available", img: "turtle3.png" },
  { num: "03", state: "occupied", img: "turtle2.png" },
  { num: "04", state: "occupied", img: "turtle1.png" },
  { num: "05", state: "reserved", img: "turtle5.png" },
];

for (const s of testSlots) {
  const box = document.createElement("div");
  box.className = `slotbox ${s.state}`;
  box.innerHTML = `
    <div>${s.num}</div>
    <img src="${s.img}" alt="Spot ${s.num}" />
    <div>${s.state}</div>
  `;
  container.appendChild(box);
}
