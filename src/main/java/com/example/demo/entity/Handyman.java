package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "handymen")
@Data // لتوليد الـ Getters و Setters تلقائياً عبر Lombok
public class Handyman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "skill_name", nullable = false)
    private String skillName; // مثل: PLUMBER, ELECTRICIAN

    @Column(nullable = false)
    private String city;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private Double rating = 5.0; // التقييم الافتراضي عند التسجيل

    @Column(name = "avatar_url")
    private String avatarUrl;

    // أضف هذا الحقل داخل الكلاس مع باقي الحقول (مثل fullName, city...)
@Column(name = "owner_username", unique = true)
private String ownerUsername; // لربط الحرفي باسم المستخدم الذي سجل الدخول

// --- أضف الـ Getter والـ Setter الخاصة به في أسفل الملف ---
public String getOwnerUsername() { return ownerUsername; }
public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}