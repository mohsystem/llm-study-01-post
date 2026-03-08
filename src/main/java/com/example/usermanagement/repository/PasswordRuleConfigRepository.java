package com.example.usermanagement.repository;

import com.example.usermanagement.entity.PasswordRuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRuleConfigRepository extends JpaRepository<PasswordRuleConfig, Long> {
}
