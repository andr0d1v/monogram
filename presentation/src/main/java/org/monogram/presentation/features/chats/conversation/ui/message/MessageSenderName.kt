package org.monogram.presentation.features.chats.conversation.ui.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.monogram.domain.models.MessageModel
import org.monogram.presentation.R

@Composable
fun MessageSenderName(
    msg: MessageModel,
    modifier: Modifier = Modifier,
    toProfile: (Long) -> Unit = {}
) {
    val tag = msg.senderCustomTitle?.trim()?.takeIf { it.isNotEmpty() }
    val isLongTag = (tag?.length ?: 0) > 16
    val badgeShape = RoundedCornerShape(if (msg.isSenderAdmin) 10.dp else 999.dp)
    val badgeContainerColor = if (msg.isSenderAdmin) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val badgeContentColor = if (msg.isSenderAdmin) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier.padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Sender Name
        Text(
            text = msg.senderName,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .clickable { toProfile(msg.senderId) }
        )

        // 2. Verified Icon
        if (msg.isSenderVerified) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = stringResource(R.string.cd_verified),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // 3. Member Tag / Custom Title
        if (tag != null) {
            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = badgeShape,
                color = badgeContainerColor,
                contentColor = badgeContentColor,
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (msg.isSenderAdmin) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = if (isLongTag) 10.sp else 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .widthIn(max = if (msg.isSenderAdmin) 120.dp else 104.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
