package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// قمنا بإضافة استثناء حزمة الأمان التلقائية هنا لفتح مسار البيانات لـ React مؤقتاً
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class }) 
public class DemoApplication {

    public static void main(String[] args) {
        // هذا السطر هو المسؤول عن تشغيل نظام Spring Boot بالكامل
        SpringApplication.run(DemoApplication.class, args);
    }
}