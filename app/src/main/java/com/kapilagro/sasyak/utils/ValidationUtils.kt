package com.kapilagro.sasyak.utils


object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailRegex.toRegex())
    }

    fun isValidPassword(password: String): Boolean {
        // At least 8 characters, one uppercase letter, one lowercase letter, one number
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Simple validation, adapt to your country's phone number format
        return phoneNumber.length >= 10 && phoneNumber.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }
    }

    fun isValidName(name: String): Boolean {
        return name.length >= 2 && name.all { it.isLetter() || it.isWhitespace() || it == '.' || it == '-' || it == '\'' }
    }

    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.EMPTY

        var score = 0

        // Length check
        if (password.length >= 8) score += 1
        if (password.length >= 12) score += 1

        // Character mix checks
        if (password.any { it.isUpperCase() }) score += 1
        if (password.any { it.isLowerCase() }) score += 1
        if (password.any { it.isDigit() }) score += 1
        if (password.any { !it.isLetterOrDigit() }) score += 1

        return when (score) {
            0, 1 -> PasswordStrength.WEAK
            2, 3 -> PasswordStrength.MODERATE
            4, 5 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }

    enum class PasswordStrength {
        EMPTY, WEAK, MODERATE, STRONG, VERY_STRONG
    }
}
