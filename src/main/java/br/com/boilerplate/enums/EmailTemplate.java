package br.com.boilerplate.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EmailTemplate {
    FORGOT_PASSWORD("forgot-password.html"),
    EMAIL_VALIDATION("email-validation.html");

    public final String templateName;

    public String getPath() {
        return this.templateName;
    }
}
