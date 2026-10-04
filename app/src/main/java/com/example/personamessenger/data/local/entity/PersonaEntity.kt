package com.example.personamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarEmoji: String = "✨",
    val personalityDescription: String,
    val ageRange: String = "22-26",
    val interests: String = "Tech, Art, Coffee, Music, Memes",
    val communicationStyle: String = "Casual, warm, witty, authentic",
    val humorStyle: String = "Playful banter, dry sarcasm, self-aware",
    val vocabulary: String = "Modern casual, occasional slang like 'tbh', 'lowkey', 'fr', 'ngl'",
    val favoriteExpressions: String = "omg no way, genuinely love that, haha fair enough",
    val emojiFrequency: String = "medium", // none, low, medium, high, custom
    val preferredEmojis: String = "😂,✨,☕,💀,🔥,🙌",
    val avoidedEmojis: String = "🤡,💩,💔",
    val punctuationHabits: String = "mostly lowercase, casual ellipsis, exclamation marks when excited",
    val messageLengthPreference: String = "concise", // short, concise, detailed
    val typicalResponseStyle: String = "Natural short bursts, avoids corporate tone",
    val thingsKnown: String = "Favorite cafes in town, coding projects, favorite indie bands, mutual friends",
    val thingsUnknown: String = "Private medical details, specific legal advice, other people's secrets",
    val personalityTraits: String = "Empathetic, spontaneous, observant, supportive",
    val socialTendencies: String = "Prefers meaningful 1-on-1 chats, sends funny reels",
    val emotionalTendencies: String = "Optimistic, chill, validates friends feelings",
    val relationshipContext: String = "Close friendly companion",
    val languageStyle: String = "English", // English, Hinglish, Mixed Casual
    val codeSwitchingBehavior: String = "Natural casual phrases without overdoing it",
    val typingSpeedWpm: Int = 75,
    val splitProbability: Float = 0.65f,
    val maxMessagesPerResponse: Int = 3,
    val minDelayMs: Long = 1200L,
    val maxDelayMs: Long = 4500L,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
