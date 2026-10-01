import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import axios from "axios";

// تهيئة تطبيق Firebase Admin للوصول الآمن لبيانات الطلاب المسجلة في Firestore
if (admin.apps.length === 0) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * وظيفة مجدولة تفاعلية (Daily Cron Schedule) تعمل كل ليلة عند الساعة 02:00 صباحاً
 * تقوم بجلب تقدم الطالب ورفعها احتياطياً ومزامنتها مع مستودع جيت هاب الخاص بالمنصة
 */
export const scheduledDailyGitHubSync = functions.pubsub
  .schedule("0 2 * * *") // جدولة يومية (02:00 AM) كما هو مطلوب بالشرط
  .timeZone("Asia/Riyadh") // ضبط التوقيت حسب مكة المكرمة للتوافق الشرعي والأكاديمي
  .onRun(async (context) => {
    console.log("بدء عملية المزامنة اليومية لبيانات الطلاب مع GitHub...");

    try {
      // 1. جلب مفتاح الوصول الشخصي (Personal Access Token) المشفر بأمان من Secret Manager
      const githubToken = process.env.GITHUB_PAT_TOKEN;
      if (!githubToken) {
        throw new Error("عذرًا، لم يتم العثور على رمز الوصول GITHUB_PAT_TOKEN في متغيرات البيئة الآمنة.");
      }

      // 2. الاستعلام عن جميع الطلاب النشطين الذين لديهم تقدم دراسي غير متزامن
      const studentsSnapshot = await db
        .collection("students")
        .where("lastSyncedTimestamp", "<", admin.firestore.Timestamp.now().toMillis() - 86400000) // لم تتم مزامنته خلال آخر 24 ساعة
        .get();

      if (studentsSnapshot.empty) {
        console.log("الحمد لله، جميع السجلات الأكاديمية متزامنة بالكامل ومحمية حالياً.");
        return null;
      }

      console.log(`تم رصد عدد ${studentsSnapshot.size} طلاب يتطلبون مزامنة البيانات حالياً.`);

      // 3. المزامنة والرفع لكل طالب بصورة متسلسلة لضمان معالجة حالات الـ API Rate Limit
      for (const doc of studentsSnapshot.docs) {
        const studentData = doc.data();
        const uid = doc.id;

        const payload = {
          uid: uid,
          email: studentData.email || "unknown@mishkat.academy",
          level: studentData.level || "مبتدئ",
          completedLessons: studentData.completedLessonsCount || 0,
          perfectScores: studentData.perfectScoresCount || 0,
          academicRank: studentData.academicRank || "قارئ مبادر",
          lastSeen: studentData.lastSeen || Date.now(),
          syncedAt: Date.now(),
        };

        const jsonPayloadString = JSON.stringify(payload);
        // تشفير المحتوى بـ Base64 كما تشترط واجهة GitHub REST API v3
        const base64Content = Buffer.from(jsonPayloadString).toString("base64");

        const owner = "MishkatAcademy";
        const repo = "students-data";
        const path = `students/${uid}/${new Date().toISOString().split("T")[0]}.json`;
        const url = `https://api.github.com/repos/${owner}/${repo}/contents/${path}`;

        // أ) التحقق من وجود ملف قديم لجلب الـ SHA الأحدث لتفادي تعارض الرفع (409 Conflict)
        let sha: string | null = null;
        try {
          const getResponse = await axios.get(url, {
            headers: {
              Authorization: `token ${githubToken}`,
              Accept: "application/vnd.github.v3+json",
            },
          });
          if (getResponse.status === 200) {
            sha = getResponse.data.sha;
          }
        } catch (err: any) {
          if (err.response && err.response.status !== 404) {
            console.error(`خطأ أثناء جلب SHA للمستند ${path}:`, err.message);
          }
        }

        // ب) تنفيذ طلب الرفع الفعلي (HTTP PUT)
        try {
          const putBody: any = {
            message: `الأكاديمية: مزامنة ليلية مجدولة لتقدّم الطالب - ${uid}`,
            content: base64Content,
          };
          if (sha) {
            putBody.sha = sha;
          }

          const putResponse = await axios.put(url, putBody, {
            headers: {
              Authorization: `token ${githubToken}`,
              Accept: "application/vnd.github.v3+json",
              "Content-Type": "application/json",
            },
          });

          if (putResponse.status === 200 || putResponse.status === 201) {
            console.log(`[نجاح] تم مزامنة ورفع تقدّم الطالب: ${studentData.email} إلى مستودع GitHub.`);
            // تحديث حالة الطالب في قاعدة البيانات لضمان عدم التكرار
            await doc.ref.update({
              lastSyncedTimestamp: Date.now(),
            });
          }
        } catch (uploadErr: any) {
          console.error(`[فشل] تعذرت مزامنة تقدم الطالب ${uid} بسبب:`, uploadErr.message);
          // في حال حدوث خطأ تجاوز حد الطلبات من جيت هاب (Rate Limit) نقوم بالخروج مباشرة وتأجيل المزامنة
          if (uploadErr.response && uploadErr.response.status === 403) {
            console.warn("تم بلوغ حد رفع الطلبات المسموح به من جيت هاب (API Rate Limit). تعليق بقية المزامنة مؤقتاً.");
            break;
          }
        }
      }

    } catch (globalError: any) {
      console.error("فشل شامل في تشغيل المزامنة المجدولة للجيت هاب بسبب:", globalError.message);
    }

    return null;
  });
