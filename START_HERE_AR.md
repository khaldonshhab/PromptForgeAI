# PromptForge AI — ابدأ من هنا

## تشغيل التطبيق
1. افتح المستودع في Android Studio.
2. استخدم JDK 17.
3. ثبّت Android SDK 37.
4. شغّل التطبيق على هاتف أو Emulator.
5. لبناء APK محلياً: Build → Build APK(s).

## APK عبر GitHub
كل Push إلى main يشغل GitHub Actions، وبعد نجاح البناء نزّل artifact باسم promptforge-debug-apk.

## تفعيل AI الحقيقي
شغّل backend على خادم HTTPS، ثم ضع رابطه داخل:
الإعدادات → Backend URL → حفظ → اختبار الاتصال.

## Premium
في Google Play Console أنشئ:
premium_monthly
premium_yearly
واختبر أولاً على Internal testing.

## الأمان
مفتاح مزود AI لا يوضع داخل APK أبداً.
ولا ترفع .env أو .jks إلى GitHub.
