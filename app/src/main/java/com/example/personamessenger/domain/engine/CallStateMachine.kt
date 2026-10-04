package com.example.personamessenger.domain.engine

import com.example.personamessenger.data.local.entity.AudioAssetEntity
import com.example.personamessenger.data.local.entity.CallSessionEntity
import java.util.Locale

class CallStateMachine {

    enum class State {
        IDLE,
        INCOMING,
        ACCEPTED,
        GREETING,
        LISTENING,
        PROCESSING,
        RESPONSE,
        ENDED
    }

    data class TransitionResult(
        val newSession: CallSessionEntity,
        val outputSpeechTranscript: String?,
        val selectedAudio: AudioAssetEntity?,
        val logEvent: String
    )

    fun processEvent(
        currentSession: CallSessionEntity,
        event: CallEvent,
        availableAudios: List<AudioAssetEntity>
    ): TransitionResult {
        val currentState = try {
            State.valueOf(currentSession.state)
        } catch (e: Exception) {
            State.IDLE
        }

        return when (event) {
            is CallEvent.IncomingCall -> {
                val newSession = currentSession.copy(
                    state = State.INCOMING.name,
                    activeSpeaker = "caller",
                    startedAtEpoch = System.currentTimeMillis()
                )
                TransitionResult(newSession, null, null, "Incoming call received from ${currentSession.contactName}")
            }
            is CallEvent.AcceptCall -> {
                // Find greeting audio if available
                val greetingAudio = availableAudios.firstOrNull { it.tags.contains("greeting", ignoreCase = true) }
                val newSession = currentSession.copy(
                    state = State.GREETING.name,
                    activeSpeaker = "ai_assistant",
                    selectedAudioId = greetingAudio?.id,
                    selectedAudioName = greetingAudio?.name ?: "Default Greeting"
                )
                TransitionResult(
                    newSession,
                    greetingAudio?.transcript ?: "Hey! What's up?",
                    greetingAudio,
                    "Call accepted, playing greeting audio clip"
                )
            }
            is CallEvent.CallerSpoke -> {
                val transcript = event.transcript
                val newSession = currentSession.copy(
                    state = State.PROCESSING.name,
                    activeSpeaker = "ai_assistant",
                    latestTranscript = transcript
                )
                // Select best response audio
                val matchedAudio = selectBestAudioForTranscript(transcript, availableAudios)
                TransitionResult(
                    newSession,
                    matchedAudio?.transcript ?: "Sounds great, totally agree with you!",
                    matchedAudio,
                    "Caller spoke: '$transcript'. AI analyzing and matching response audio."
                )
            }
            is CallEvent.AiFinishedSpeaking -> {
                val newSession = currentSession.copy(
                    state = State.LISTENING.name,
                    activeSpeaker = "caller"
                )
                TransitionResult(newSession, null, null, "AI response audio completed. Listening for caller response...")
            }
            is CallEvent.EndCall -> {
                val newSession = currentSession.copy(
                    state = State.ENDED.name,
                    activeSpeaker = "none",
                    endedAtEpoch = System.currentTimeMillis()
                )
                TransitionResult(newSession, null, null, "Call session ended and logged.")
            }
        }
    }

    private fun selectBestAudioForTranscript(
        transcript: String,
        audios: List<AudioAssetEntity>
    ): AudioAssetEntity? {
        if (audios.isEmpty()) return null
        val lower = transcript.lowercase(Locale.ROOT)

        if (lower.contains("bye") || lower.contains("later") || lower.contains("gotta go")) {
            return audios.firstOrNull { it.tags.contains("busy") || it.tags.contains("wrap-up") } ?: audios.first()
        }
        if (lower.contains("plan") || lower.contains("meet") || lower.contains("cafe") || lower.contains("yes")) {
            return audios.firstOrNull { it.tags.contains("excited") || it.tags.contains("agreement") } ?: audios.first()
        }
        if (lower.contains("kya") || lower.contains("yaar") || lower.contains("scene")) {
            return audios.firstOrNull { it.language.equals("Hinglish", ignoreCase = true) } ?: audios.first()
        }

        return audios.maxByOrNull { it.usageCount } ?: audios.first()
    }
}

sealed class CallEvent {
    data class IncomingCall(val callerName: String) : CallEvent()
    object AcceptCall : CallEvent()
    data class CallerSpoke(val transcript: String) : CallEvent()
    object AiFinishedSpeaking : CallEvent()
    object EndCall : CallEvent()
}
