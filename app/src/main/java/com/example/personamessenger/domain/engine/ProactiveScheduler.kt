package com.example.personamessenger.domain.engine

import com.example.personamessenger.data.local.entity.ContactEntity
import com.example.personamessenger.data.local.entity.ModelConfigEntity
import java.util.Calendar

class ProactiveScheduler {

    data class ValidationResult(
        val isAllowed: Boolean,
        val reason: String
    )

    /**
     * Checks whether an automated proactive or reactive message is permissible.
     */
    fun validateMessageAttempt(
        contact: ContactEntity,
        modelConfig: ModelConfigEntity,
        isProactive: Boolean,
        recentMessagesInLastHourCount: Int
    ): ValidationResult {
        // 1. Global Master Emergency Switch
        if (!modelConfig.isGlobalAutomationActive) {
            return ValidationResult(false, "Global Automation is paused (Master Switch disabled)")
        }

        // 2. Explicit Contact Authorization
        if (!contact.isAuthorizedAccount || !contact.isAiEnabled) {
            return ValidationResult(false, "Contact '${contact.name}' is not authorized for AI automation")
        }

        // 3. Proactive Authorization check
        if (isProactive && !contact.proactiveMessagingAllowed) {
            return ValidationResult(false, "Proactive messaging is disabled for '${contact.name}'")
        }

        // 4. Rate Limiting Check
        if (recentMessagesInLastHourCount >= contact.maxMessagesPerHour) {
            return ValidationResult(false, "Rate limit reached (${contact.maxMessagesPerHour} msgs/hr)")
        }

        // 5. Quiet Hours Check
        if (contact.quietHoursEnabled) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isQuietHour = if (contact.quietHoursStartHour > contact.quietHoursEndHour) {
                // e.g. 23:00 to 08:00
                currentHour >= contact.quietHoursStartHour || currentHour < contact.quietHoursEndHour
            } else {
                // e.g. 01:00 to 06:00
                currentHour in contact.quietHoursStartHour until contact.quietHoursEndHour
            }

            if (isQuietHour) {
                return ValidationResult(false, "Quiet hours active (${contact.quietHoursStartHour}:00 - ${contact.quietHoursEndHour}:00)")
            }
        }

        return ValidationResult(true, "Authorized and validated")
    }
}
