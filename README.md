# อธิบายโค้ด MiniTest

เว็บไซต์ static (HTML ไฟล์เดียว ไม่ต้อง build) อธิบายโปรเจกต์ MiniTest "CBT & BST's Performance"
พร้อมโค้ด `MiniTest.java` ทั้งหมดท้ายหน้า

## ไฟล์
- `index.html` — หน้าเว็บทั้งหมด (CSS/JS อยู่ในไฟล์เดียว)
- `MiniTest.java` — ไฟล์โค้ดสำหรับปุ่มดาวน์โหลด
- `vercel.json` — ตั้งค่า Vercel (cleanUrls + security headers)

## Push ขึ้น GitHub
```bash
git init
git add .
git commit -m "Add MiniTest code explanation site"
git branch -M main
git remote add origin https://github.com/<username>/<repo>.git
git push -u origin main
```

## Deploy บน Vercel
1. เข้า https://vercel.com/new แล้ว Import repo นี้
2. Framework Preset เลือก **Other** — Build Command และ Output Directory ปล่อยว่าง (root คือ `./`)
3. กด Deploy

ใช้ Vercel CLI ได้เช่นกัน: `npx vercel --prod`

## ดูในเครื่อง
เปิด `index.html` ในเบราว์เซอร์ได้เลย หรือรัน `npx serve .`
