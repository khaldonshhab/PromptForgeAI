import crypto from "node:crypto";
const password=process.argv[2];
if(!password){console.error("Usage: node scripts/hash-password.mjs YOUR_PASSWORD");process.exit(1);}
console.log(crypto.createHash("sha256").update(password).digest("hex"));
