// Copia pdf.js (lector de PDF) dentro de www/vendor para que la app funcione sin internet.
const fs = require("fs"), path = require("path");
const src = path.join(__dirname, "..", "node_modules", "pdfjs-dist", "build");
const dst = path.join(__dirname, "..", "www", "vendor");
fs.mkdirSync(dst, { recursive: true });
for (const f of ["pdf.min.js", "pdf.worker.min.js"]) {
  fs.copyFileSync(path.join(src, f), path.join(dst, f));
  console.log("copiado", f);
}

// Sella la versión y la fecha dentro de www/index.html cuando GitHub Actions la calcula
// (variables APP_VERSION y APP_DATE). Así la app instalada sabe exactamente qué versión es
// y puede compararla con la del último Release. En local, sin esas variables, no cambia nada.
if (process.env.APP_VERSION) {
  const idx = path.join(__dirname, "..", "www", "index.html");
  let html = fs.readFileSync(idx, "utf8");
  const before = html;
  html = html.replace(/APP_VER="[^"]*"/, `APP_VER="${process.env.APP_VERSION}"`);
  if (process.env.APP_DATE) html = html.replace(/APP_DATE="[^"]*"/, `APP_DATE="${process.env.APP_DATE}"`);
  if (!html.includes(`APP_VER="${process.env.APP_VERSION}"`)) {
    console.error("No pude poner la versión en www/index.html (no encontré APP_VER).");
    process.exit(1);
  }
  fs.writeFileSync(idx, html);
  console.log("Versión sellada:", process.env.APP_VERSION, before === html ? "(ya estaba)" : "");
}
