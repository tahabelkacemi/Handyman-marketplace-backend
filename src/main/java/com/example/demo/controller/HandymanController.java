package com.example.demo.controller;

import com.example.demo.entity.Handyman;
import com.example.demo.repository.HandymanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/handymen")
@CrossOrigin(origins = "http://localhost:3000") // منفذ تطبيق React الافتراضي (Vite) لتجنب حظر CORS
public class HandymanController {

    @Autowired
    private HandymanRepository handymanRepository;


    // وسم يخبر Spring Boot بأن هذه الدالة تستقبل طلبات POST لإنشاء مورد جديد (حرفي مع صورته)
@PostMapping(value = "/register", consumes = {"multipart/form-data"})
public ResponseEntity<Handyman> registerHandymanWithPhoto(
        @RequestParam("photo") org.springframework.web.multipart.MultipartFile file,
        @RequestParam("handyman") String handymanJson) { // استقباله كنص JSON صريح
    try {
        // 1. تحويل النص القادم من الـ React إلى كائن Handyman حقيقي
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Handyman handyman = objectMapper.readValue(handymanJson, Handyman.class);

        // 2. إدارة مجلد التخزين (uploads)
        java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads");
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }

        // 3. توليد اسم فريد للصورة وحفظها
        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.nio.file.Path filePath = uploadPath.resolve(uniqueFileName);
        java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // 4. تعيين رابط الصورة داخل الكائن
        String photoUrl = "http://localhost:8080/uploads/" + uniqueFileName;
        handyman.setAvatarUrl(photoUrl);
        
        // التقييم الافتراضي
        if (handyman.getRating() == null) {
            handyman.setRating(5.0);
        }

        // 5. الحفظ في قاعدة البيانات
        Handyman savedWorker = handymanRepository.save(handyman);
        
        return new ResponseEntity<>(savedWorker, org.springframework.http.HttpStatus.CREATED);

    } catch (Exception e) {
        e.printStackTrace(); // لطباعة الخطأ بالتفصيل في ترمينال السيرفر
        return new ResponseEntity<>(null, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
    // استقبال المعلمات ديناميكياً من شريط البحث في الـ Frontend
    @GetMapping("/search")
    public ResponseEntity<List<Handyman>> getHandymen(
            @RequestParam(required = false) String skill, // @RequestParam: تخبر Spring Boot بأن يبحث عن متغيرات في الرابط (URL) بنفس هذه الأسماء.
            @RequestParam(required = false) String location) { // required = false): مهمة جداً! تعني أن هذه الفلاتر اختيارية. إذا دخل المستخدم ولم يختر مهارة أو موقعاً، فلن ينهار النظام، بل سيستقبلها كـ null.
        
        // تنظيف المعلمات إذا أرسلها الـ Frontend كنصوص فارغة "" وتحويلها إلى null للـ Database
        String skillParam = (skill != null && !skill.trim().isEmpty()) ? skill.trim() : null;
        String locationParam = (location != null && !location.trim().isEmpty()) ? location.trim() : null;

        // تنفيذ الاستعلام في قاعدة البيانات MySQL وإرجاع النتيجة
        List<Handyman> results = handymanRepository.searchProfessionals(skillParam, locationParam);
        
        return ResponseEntity.ok(results);
    }


    // دالة تحديث تقييم الحرفي ديناميكياً
    // وسم يخبر Spring Boot بأن هذه الدالة تستقبل طلبات من نوع PUT (تحديث بيانات) على هذا المسار الفرعي
    @PutMapping("/{id}/rate")
    // تعريف الدالة: تستقبل معرف الحرفي (id) من الرابط، وتستقبل التقييم الجديد داخل جسم الطلب (Body)
    public ResponseEntity<Handyman> rateHandyman(@PathVariable Long id, @RequestBody java.util.Map<String, Double> payload) {
        
        // البحث في قاعدة البيانات عن الحرفي باستخدام الـ ID الممرر
        return handymanRepository.findById(id).map(worker -> {
            
            // استخراج قيمة التقييم الرقمي المرسل من ملف الـ React (مثلاً: 4.0) من داخل الـ Map
            Double newRating = payload.get("rating");
            
            // شرط فحص: إذا كان الحرفي جديداً وليس له أي تقييم سابق في قاعدة البيانات (null أو 0)
            if (worker.getRating() == null || worker.getRating() == 0) {
                // نقوم بتعيين التقييم الجديد الذي أرسله المستخدم مباشرة كتقييم أساسي له
                worker.setRating(newRating);
            } else { // أما إذا كان لديه تقييمات سابقة مخزنة
                // نطبق معادلة المتوسط الحسابي: (التقييم القديم + التقييم الجديد) مقسوماً على 2
                double updatedAverage = (worker.getRating() + newRating) / 2;
                // نقوم بتقريب الرقم الحسابي لمرتبة عشرية واحدة فقط (مثال: من 4.6666 إلى 4.7) لجمالية العرض
                worker.setRating(Math.round(updatedAverage * 10.0) / 10.0);
            }
            
            // أمر برمجى لحفظ التحديثات الجديدة للحرفي داخل جدول MySQL
            Handyman updatedWorker = handymanRepository.save(worker);
            
            // إرجاع كائن الحرفي المحدث بالكامل إلى الـ Frontend مع كود نجاح 200 OK
            return ResponseEntity.ok(updatedWorker);
            
        // في حال عدم العثور على الـ ID في قاعدة البيانات، يتم إرجاع استجابة تفيد بأن العنصر غير موجود (404 Not Found)
        }).orElse(ResponseEntity.notFound().build());
    }



    // وسم يخبر السيرفر بفتح نقطة اتصال لاستقبال طلبات الحذف عبر بروتوكول DELETE
@DeleteMapping("/{id}")
public ResponseEntity<HttpStatus> deleteHandyman(@PathVariable("id") Long id) {
    try {
        // فحص: إذا كان الحرفي موجوداً فعلاً في قاعدة البيانات، نقوم بحذفه
        if (handymanRepository.existsById(id)) {
            handymanRepository.deleteById(id);
            // إرجاع كود نجاح 204 No Content (وهو الكود القياسي لعمليات الحذف الناجحة)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            // إذا لم يجد المعرّف، يرجع 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    } catch (Exception e) {
        // في حال حدوث خطأ سيرفر داخلي
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


// وسم يخبر السيرفر بفتح مسار لاستقبال طلبات التعديل عبر بروتوكول PUT مدمجاً معه الـ id في الرابط
@PutMapping("/update/{id}")
public ResponseEntity<Handyman> updateHandyman(
        @PathVariable("id") Long id, // التقاط الرقم المعرف من الرابط
        @RequestBody Handyman updatedData) { // استقبال البيانات الجديدة المرسلة بجسم الطلب كـ JSON
    try {
        // 1. البحث عن الحرفي الحالي في قاعدة البيانات بواسطة الـ id
        java.util.Optional<Handyman> existingHandymanOptional = handymanRepository.findById(id);
        
        if (existingHandymanOptional.isPresent()) {
            Handyman currentHandyman = existingHandymanOptional.get();
            
            // 2. تحديث الحقول النصية فقط، مع الحفاظ على البيانات القديمة إذا كانت المدخلات فارغة
            currentHandyman.setFullName(updatedData.getFullName());
            currentHandyman.setSkillName(updatedData.getSkillName());
            currentHandyman.setCity(updatedData.getCity());
            currentHandyman.setPhoneNumber(updatedData.getPhoneNumber());
            currentHandyman.setBio(updatedData.getBio());
            
            // نتحقق من عدم تصفير رابط الصورة الشخصية الحالي أثناء التحديث العادي للبيانات
            if (updatedData.getAvatarUrl() != null) {
                currentHandyman.setAvatarUrl(updatedData.getAvatarUrl());
            }

            // 3. حفظ التعديلات الجديدة نهائياً داخل قاعدة بيانات MySQL
            Handyman savedWorker = handymanRepository.save(currentHandyman);
            
            // إرجاع الكائن المحدث مع كود حالة 200 OK
            return new ResponseEntity<>(savedWorker, HttpStatus.OK);
        } else {
            // إذا لم يتم العثور على الحرفي في قاعدة البيانات، نرجع 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    } catch (Exception e) {
        // طباعة الخطأ في تيرمينال السيرفر للحماية والمراقبة
        e.printStackTrace();
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



// نقطة اتصال لجلب البروفايل الحرفي للمستخدم المسجل حالياً بناءً على اسمه
@GetMapping("/my-profile/{username}")
public ResponseEntity<Handyman> getMyProfile(@PathVariable("username") String username) {
    // جلب الحرفي الذي يطابق الـ ownerUsername
    // (ملاحظة: تحتاج لإضافة هذه الدالة findByOwnerUsername في الـ HandymanRepository)
    return handymanRepository.findByOwnerUsername(username)
            .map(handyman -> new ResponseEntity<>(handyman, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // يرجع 404 إذا لم يملأ بيانات حرفية بعد
}
 
  }
