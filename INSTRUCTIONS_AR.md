# مرسال الديون الذكي - Smart Debt Messenger

## حل مشكلة البناء في Klencod IDE / GitHub Actions

### سبب المشكلة السابقة:
في تقرير البناء السابق:
```
[OK] Setup Flutter 53s
[X] Get dependencies 6s
[ ] Build APK
[خطأ] Process completed with exit code 1.
```
كانت منصة البناء تحاول تشغيل **Flutter** وأمر `flutter pub get` لجلب الحزم، لكن هذا التطبيق هو **تطبيق أندرويد أصلي Native (Kotlin + Jetpack Compose + Room Database + Gradle)** وليس مشروع Flutter. عدم وجود ملف `pubspec.yaml` تسبب في فشل الخطوة `Get dependencies`.

---

### الحل الذي تم تطبيقه (تم الإصلاح بالكامل):
1. **تصحيح مسار البناء الآلي (`.github/workflows/build-apk.yml`):**
   - تم استبدال إعدادات Flutter بإعدادات **Java 17 (JDK) + Gradle 8/9**.
   - تم إضافة خطوة `./gradlew assembleDebug` لبناء الـ APK الأصلي بأعلى كفاءة وسرعة.
   - يتم رفع ملف الـ APK النهائي تلقائياً في قسم التحميلات (Artifacts) باسم `Smart-Debt-Messenger-APK`.

2. **تجهيز ملفات الـ Wrapper (`gradlew` & `gradlew.bat`):**
   - تم توليد ملفات التشغيل المباشر لتسهيل البناء في أي بيئة سحابية أو محلية.

---

### طرق الحصول على ملف الـ APK:

#### 1. التنزيل المباشر من Google AI Studio:
- يمكنك النقر على قائمة الإعدادات (Settings / Export) ثم اختيار **Generate / Export APK**.

#### 2. عبر GitHub / Klencod IDE:
- بمجرد رفع الكود (Push)، سيعمل الـ Workflow الجديد المخصص للأندرويد تلقائياً.
- ستظهر لك علامة النجاح الخضراء `[OK] Build Debug APK` و `[OK] Upload Smart Debt Messenger APK`.
- ستجد ملف الـ APK جاهزاً للتحميل من تبويب **Actions -> Summary -> Artifacts**.

#### 3. البناء المحلي (على جهازك):
- في نظام Linux / Mac:
  ```bash
  ./gradlew assembleDebug
  ```
- في نظام Windows:
  ```cmd
  gradlew.bat assembleDebug
  ```
- ستجد ملف الـ APK في المسار:
  `app/build/outputs/apk/debug/app-debug.apk`
