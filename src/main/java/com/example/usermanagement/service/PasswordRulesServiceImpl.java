package com.example.usermanagement.service;

import com.example.usermanagement.dto.PasswordRulesRequest;
import com.example.usermanagement.dto.PasswordRulesResponse;
import com.example.usermanagement.entity.PasswordRuleConfig;
import com.example.usermanagement.exception.PasswordRuleViolationException;
import com.example.usermanagement.repository.PasswordRuleConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordRulesServiceImpl implements PasswordRulesService {

    private static final long SINGLETON_RULES_ID = 1L;

    private final PasswordRuleConfigRepository passwordRuleConfigRepository;

    public PasswordRulesServiceImpl(PasswordRuleConfigRepository passwordRuleConfigRepository) {
        this.passwordRuleConfigRepository = passwordRuleConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordRulesResponse getRules() {
        return toResponse(getOrCreateRules());
    }

    @Override
    @Transactional
    public PasswordRulesResponse updateRules(PasswordRulesRequest request) {
        PasswordRuleConfig rules = getOrCreateRules();
        rules.setMinLength(request.minLength());
        rules.setRequireUppercase(request.requireUppercase());
        rules.setRequireLowercase(request.requireLowercase());
        rules.setRequireDigit(request.requireDigit());
        rules.setRequireSpecial(request.requireSpecial());
        rules.setMinSpecialCount(request.minSpecialCount());

        if (rules.isRequireSpecial() && rules.getMinSpecialCount() < 1) {
            throw new PasswordRuleViolationException("REJECTED: minSpecialCount must be >= 1 when requireSpecial is enabled");
        }

        return toResponse(passwordRuleConfigRepository.save(rules));
    }

    @Override
    @Transactional(readOnly = true)
    public void validateOrThrow(String password) {
        PasswordRuleConfig rules = getOrCreateRules();

        if (password.length() < rules.getMinLength()) {
            throw new PasswordRuleViolationException("REJECTED: password does not meet minimum length");
        }
        if (rules.isRequireUppercase() && password.chars().noneMatch(Character::isUpperCase)) {
            throw new PasswordRuleViolationException("REJECTED: password must contain an uppercase letter");
        }
        if (rules.isRequireLowercase() && password.chars().noneMatch(Character::isLowerCase)) {
            throw new PasswordRuleViolationException("REJECTED: password must contain a lowercase letter");
        }
        if (rules.isRequireDigit() && password.chars().noneMatch(Character::isDigit)) {
            throw new PasswordRuleViolationException("REJECTED: password must contain a digit");
        }
        if (rules.isRequireSpecial()) {
            long specialCount = password.chars().filter(ch -> !Character.isLetterOrDigit(ch)).count();
            if (specialCount < rules.getMinSpecialCount()) {
                throw new PasswordRuleViolationException("REJECTED: password must contain enough special characters");
            }
        }
    }

    private PasswordRuleConfig getOrCreateRules() {
        return passwordRuleConfigRepository.findById(SINGLETON_RULES_ID)
                .orElseGet(() -> passwordRuleConfigRepository.save(new PasswordRuleConfig()));
    }

    private PasswordRulesResponse toResponse(PasswordRuleConfig config) {
        return new PasswordRulesResponse(
                config.getMinLength(),
                config.isRequireUppercase(),
                config.isRequireLowercase(),
                config.isRequireDigit(),
                config.isRequireSpecial(),
                config.getMinSpecialCount()
        );
    }
}
