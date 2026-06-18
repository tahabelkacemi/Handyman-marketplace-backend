package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // دالة حيوية للبحث عن المستخدم في قاعدة البيانات عند محاولة تسجيل الدخول
    Optional<User> findByUsername(String username);
}