package org.monogram.presentation.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.monogram.domain.models.ChatModel
import org.monogram.domain.models.UserModel
import org.monogram.domain.models.UserTypeEnum
import org.monogram.presentation.R
import org.monogram.presentation.core.ui.AvatarHeader
import org.monogram.presentation.features.stickers.ui.view.StickerImage

@Composable
fun ProfileHeaderTransformed(
    avatarPath: String?,
    avatarFallbackPath: String?,
    title: String,
    subtitle: String,
    avatarSize: Dp,
    userModel: UserModel?,
    avatarCornerPercent: Int,
    isOnline: Boolean,
    isVerified: Boolean,
    isSponsor: Boolean,
    isBot: Boolean,
    isScam: Boolean,
    isFake: Boolean,
    statusEmojiPath: String?,
    progress: Float,
    contentPadding: PaddingValues,
    onAvatarClick: () -> Unit,
    chatModel: ChatModel? = null,
    slowModeDelay: Int = 0,
    onActionClick: () -> Unit = {}
) {
    val containerSize = LocalWindowInfo.current.containerSize
    val screenHeight = with(LocalDensity.current) { containerSize.height.toDp() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding)
    ) {
        val headerHeight = maxWidth.coerceAtMost(screenHeight * 0.6f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .alpha(progress)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(avatarCornerPercent))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAvatarClick
                    )
            ) {
                AvatarHeader(
                    path = avatarPath,
                    fallbackPath = avatarFallbackPath,
                    identityKey = userModel?.id ?: chatModel?.id ?: title,
                    name = title,
                    size = avatarSize.coerceAtMost(headerHeight),
                    avatarCornerPercent = avatarCornerPercent
                )
            }

            val scrimColor = Color.Black.copy(alpha = 0.7f)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(progress / 1.5f)
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to scrimColor,
                                0.15f to Color.Transparent,
                                0.7f to Color.Transparent,
                                1.0f to scrimColor
                            )
                        ),
                        shape = RoundedCornerShape(avatarCornerPercent)
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 8f
                                )
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = stringResource(R.string.cd_verified),
                                modifier = Modifier.size(28.dp),
                                tint = Color(0xFF31A6FD)
                            )
                        }
                        if (isSponsor) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = stringResource(R.string.cd_sponsor),
                                modifier = Modifier.size(28.dp),
                                tint = Color(0xFFE53935)
                            )
                        }

                        val displayedStatusEmojiPath = statusEmojiPath ?: userModel?.statusEmojiPath
                        if (!displayedStatusEmojiPath.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StickerImage(
                                path = displayedStatusEmojiPath,
                                modifier = Modifier.size(26.dp),
                                animate = false
                            )
                        } else if (userModel?.isPremium == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color(0xFF31A6FD)
                            )
                        }

                    }


                }

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = if (isOnline) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        )
                    )
                }

                ProfileMetaBadges(
                    userModel = userModel,
                    chatModel = chatModel,
                    isBot = isBot,
                    isSponsor = isSponsor,
                    isScam = isScam,
                    isFake = isFake,
                    slowModeDelay = slowModeDelay
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.BottomCenter)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(
                        topStart = (30 * progress).dp,
                        topEnd = (30 * progress).dp
                    )
                )
        )
    }
}

@Composable
private fun ProfileMetaBadges(
    userModel: UserModel?,
    chatModel: ChatModel?,
    isBot: Boolean,
    isSponsor: Boolean,
    isScam: Boolean,
    isFake: Boolean,
    slowModeDelay: Int
) {
    val isGroupOrChannel = chatModel?.isGroup == true || chatModel?.isChannel == true
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (userModel != null && !isGroupOrChannel) {
            if (userModel.type == UserTypeEnum.BOT || isBot) {
                HeaderStatusBadge(
                    text = stringResource(R.string.label_bot_badge),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (userModel.isPremium) {
                HeaderStatusBadge(
                    text = stringResource(R.string.profile_badge_premium),
                    containerColor = Color(0xFF31A6FD),
                    contentColor = Color.White
                )
            }
            if (isSponsor) {
                HeaderStatusBadge(
                    text = stringResource(R.string.profile_badge_sponsor),
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                )
            }
        }

        if (chatModel != null && isGroupOrChannel) {
            HeaderStatusBadge(
                text = if (chatModel.isChannel) {
                    stringResource(R.string.profile_badge_channel)
                } else {
                    stringResource(R.string.profile_badge_group)
                },
                containerColor = Color.Black.copy(alpha = 0.42f),
                contentColor = Color.White
            )
            if (chatModel.isAdmin) {
                HeaderStatusBadge(
                    text = stringResource(R.string.profile_badge_admin),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (chatModel.hasProtectedContent) {
                HeaderStatusBadge(
                    text = stringResource(R.string.protected_content_title),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (slowModeDelay > 0) {
                HeaderStatusBadge(
                    text = stringResource(R.string.slow_mode_title),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        if (isScam) {
            HeaderStatusBadge(
                text = stringResource(R.string.label_scam_badge),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        if (isFake) {
            HeaderStatusBadge(
                text = stringResource(R.string.label_fake_badge),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun HeaderStatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
