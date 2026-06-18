package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.mindrot.jbcrypt.BCrypt;// تشفير

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // للسماح للـ React بالاتصال بالسيرفر
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // 🌟 نقطة اتصال تسجيل الدخول
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // 1. البحث عن المستخدم في الداتابيز باستخدام Optional
        Optional<User> userOptional = userRepository.findByUsername(username);

        // التحقق الذكي والمحمي من وجود القيمة داخل الـ Optional
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // 2. 🌟 استخدام BCrypt لمطابقة كلمة السر القادمة مع الهاش المشفر المخزن
            if (BCrypt.checkpw(password, user.getPassword())) {
                
                // إذا تطابقت، نرسل للـ React دور المستخدم وبياناته الشخصية
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "username", user.getUsername(),
                    "role", user.getRole()
                ));
            }
        }
        
        // إذا كان الاسم غير موجود في الـ Optional أو الباسورد المشفر لم يتطابق
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password!"));
    }

    // 📝 مسار استقبال طلبات إنشاء حساب مستخدم جديد ومشفر
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {
        try {
            // التحقق من أن الاسم غير محجوز مسبقاً باستخدام Optional
            if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Username is already taken!"));
            }

            // 🌟 تشفير كلمة السر قبل الحفظ في قاعدة البيانات لحمايتها
            String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
            newUser.setPassword(hashedPassword); 

            newUser.setRole("USER"); // تعيين الدور التلقائي للمستخدم الجديد
            userRepository.save(newUser);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User registered successfully with secure encryption!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error during registration."));
        }
    }
}