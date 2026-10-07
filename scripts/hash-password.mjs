import crypto from "node:crypto";
const password=process.argv[2];
if(!password){console.error("Usage: node scripts/hash-password.mjs YOUR_PASSWORD");process.exit(1);}
const salt=crypto.randomBytes(16);
const derived=crypto.scryptSync(password,salt,64,{N:131072,r:8,p:1,maxmem:256*1024*1024});
console.log("scrypt$131072$8$1$"+salt.toString("base64url")+"$"+derived.toString("base64url"));
