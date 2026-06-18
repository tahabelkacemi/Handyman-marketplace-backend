package com.example.demo.config; 

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// وسم يخبر الإطار بأن هذا الملف يحتوي على إعدادات نظام تشغيل السيرفر
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // دالة للتحكم بالملفات والموارد الثابتة المعروضة على الويب
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // إذا جاء طلب للمتصفح يبدأ بـ /uploads/، قم بتوجيهه فوراً لقراءة الملفات من مجلد uploads الحقيقي
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}