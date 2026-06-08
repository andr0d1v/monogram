#include <jni.h>
#include <android/bitmap.h>

#include <algorithm>
#include <cstdint>
#include <vector>

namespace {

    constexpr int NATIVE_NO_BACKGROUND = 0;
    constexpr int NATIVE_CLEANED = 1;
    constexpr int MODE_BLACK_AND_WHITE = 0;
    constexpr int MODE_BLACK_ONLY = 1;
    constexpr uint8_t BLACK_THRESHOLD = 12;
    constexpr uint8_t BLACK_FRINGE_THRESHOLD = 48;
    constexpr uint8_t WHITE_THRESHOLD = 232;
    constexpr uint8_t WHITE_FRINGE_THRESHOLD = 200;
    constexpr uint8_t WHITE_MAX_CHANNEL_DELTA = 24;
    constexpr uint8_t WHITE_FRINGE_MAX_CHANNEL_DELTA = 36;
    constexpr uint8_t MIN_ALPHA = 16;
    constexpr float MIN_BACKGROUND_RATIO = 0.02f;

    enum class BackgroundKind {
        None,
        Black,
        White
    };

    class StickerBackgroundCleanerSession {
    public:
        StickerBackgroundCleanerSession(int width, int height)
                : width(width),
                  height(height),
                  pixelCount(width > 0 && height > 0 ? width * height : 0),
                  visited(pixelCount),
                  background(pixelCount),
                  queue(pixelCount) {
        }

        int clean(JNIEnv *env, jobject bitmap, int mode) {
            if (bitmap == nullptr || width <= 0 || height <= 0 || pixelCount <= 0) {
                return NATIVE_NO_BACKGROUND;
            }

            AndroidBitmapInfo info;
            if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS ||
                    info.width != static_cast<uint32_t>(width) ||
                    info.height != static_cast<uint32_t>(height) ||
                    info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
                return NATIVE_NO_BACKGROUND;
            }

            void *pixels = nullptr;
            if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS ||
                    pixels == nullptr) {
                return NATIVE_NO_BACKGROUND;
            }

            auto *base = static_cast<uint8_t *>(pixels);
            nextMask();

            int left = 0;
            int top = 0;
            int right = width - 1;
            int bottom = height - 1;
            if (!findOpaqueBounds(base, info.stride, left, top, right, bottom)) {
                AndroidBitmap_unlockPixels(env, bitmap);
                return NATIVE_NO_BACKGROUND;
            }

            const BackgroundKind kind = detectBackgroundKind(base, info.stride, left, top, right, bottom, mode);
            if (kind == BackgroundKind::None) {
                AndroidBitmap_unlockPixels(env, bitmap);
                return NATIVE_NO_BACKGROUND;
            }

            int backgroundCount = 0;
            int head = 0;
            int tail = 0;

            for (int x = left; x <= right; ++x) {
                tail = enqueueIfBackground(base, info.stride, top * width + x, tail, kind);
                tail = enqueueIfBackground(base, info.stride, bottom * width + x, tail, kind);
            }
            for (int y = top + 1; y < bottom; ++y) {
                tail = enqueueIfBackground(base, info.stride, y * width + left, tail, kind);
                tail = enqueueIfBackground(base, info.stride, y * width + right, tail, kind);
            }

            while (head < tail) {
                const int index = queue[head++];
                backgroundCount++;

                const int x = index % width;
                if (x > 0) tail = enqueueIfBackground(base, info.stride, index - 1, tail, kind);
                if (x < width - 1) tail = enqueueIfBackground(base, info.stride, index + 1, tail, kind);
                if (index >= width) tail = enqueueIfBackground(base, info.stride, index - width, tail, kind);
                if (index < pixelCount - width) {
                    tail = enqueueIfBackground(base, info.stride, index + width, tail, kind);
                }
            }

            if (backgroundCount < static_cast<int>(pixelCount * MIN_BACKGROUND_RATIO)) {
                AndroidBitmap_unlockPixels(env, bitmap);
                return NATIVE_NO_BACKGROUND;
            }

            applyTransparency(base, info.stride, kind);
            AndroidBitmap_unlockPixels(env, bitmap);
            return NATIVE_CLEANED;
        }

    private:
        int width;
        int height;
        int pixelCount;
        std::vector<int> visited;
        std::vector<int> background;
        std::vector<int> queue;
        int maskId = 1;

        void nextMask() {
            if (maskId == INT32_MAX) {
                std::fill(visited.begin(), visited.end(), 0);
                std::fill(background.begin(), background.end(), 0);
                maskId = 1;
            } else {
                maskId++;
            }
        }

        int enqueueIfBackground(
                uint8_t *base,
                uint32_t stride,
                int index,
                int tail,
                BackgroundKind kind
        ) {
            if (visited[index] == maskId || !isBackgroundColor(pixelAt(base, stride, index), kind)) {
                return tail;
            }

            visited[index] = maskId;
            background[index] = maskId;
            queue[tail] = index;
            return tail + 1;
        }

        bool findOpaqueBounds(uint8_t *base, uint32_t stride, int &left, int &top, int &right, int &bottom) const {
            left = width;
            top = height;
            right = -1;
            bottom = -1;

            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    const uint8_t *pixel = base + y * stride + x * 4;
                    if (pixel[3] < MIN_ALPHA) continue;
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }

            return left <= right && top <= bottom;
        }

        BackgroundKind detectBackgroundKind(
                uint8_t *base,
                uint32_t stride,
                int left,
                int top,
                int right,
                int bottom,
                int mode
        ) const {
            int blackCount = 0;
            int whiteCount = 0;
            int sampledCount = 0;

            auto sample = [&](int index) {
                const uint8_t *pixel = pixelAt(base, stride, index);
                if (pixel[3] < MIN_ALPHA) return;
                sampledCount++;
                if (isBackgroundColor(pixel, BackgroundKind::Black)) blackCount++;
                if (mode != MODE_BLACK_ONLY &&
                        isNearWhite(pixel, WHITE_THRESHOLD, WHITE_MAX_CHANNEL_DELTA)) {
                    whiteCount++;
                }
            };

            for (int x = left; x <= right; ++x) {
                sample(top * width + x);
                sample(bottom * width + x);
            }
            for (int y = top + 1; y < bottom; ++y) {
                sample(y * width + left);
                sample(y * width + right);
            }

            if (sampledCount == 0) return BackgroundKind::None;

            if (blackCount >= whiteCount && blackCount >= static_cast<int>(sampledCount * 0.35f)) {
                return BackgroundKind::Black;
            }
            if (mode != MODE_BLACK_ONLY &&
                    whiteCount > blackCount &&
                    whiteCount >= static_cast<int>(sampledCount * 0.35f)) {
                return BackgroundKind::White;
            }
            return BackgroundKind::None;
        }

        void applyTransparency(uint8_t *base, uint32_t stride, BackgroundKind kind) const {
            for (int index = 0; index < pixelCount; ++index) {
                uint8_t *pixel = pixelAt(base, stride, index);
                if (background[index] == maskId) {
                    pixel[3] = 0;
                    continue;
                }

                if (!isFringeColor(pixel, kind)) continue;

                const int backgroundNeighbors = countBackgroundNeighbors(index);
                if (backgroundNeighbors >= 4) {
                    scaleAlpha(pixel, 0.20f);
                } else if (backgroundNeighbors >= 2) {
                    scaleAlpha(pixel, 0.45f);
                } else if (backgroundNeighbors == 1 && isBackgroundColor(pixel, kind)) {
                    scaleAlpha(pixel, 0.70f);
                }
            }
        }

        int countBackgroundNeighbors(int index) const {
            int count = 0;
            const int x = index % width;
            const bool left = x > 0;
            const bool right = x < width - 1;
            const bool top = index >= width;
            const bool bottom = index < pixelCount - width;

            if (left && background[index - 1] == maskId) count++;
            if (right && background[index + 1] == maskId) count++;
            if (top && background[index - width] == maskId) count++;
            if (bottom && background[index + width] == maskId) count++;
            if (left && top && background[index - width - 1] == maskId) count++;
            if (right && top && background[index - width + 1] == maskId) count++;
            if (left && bottom && background[index + width - 1] == maskId) count++;
            if (right && bottom && background[index + width + 1] == maskId) count++;
            return count;
        }

        uint8_t *pixelAt(uint8_t *base, uint32_t stride, int index) const {
            const int y = index / width;
            const int x = index - y * width;
            return base + y * stride + x * 4;
        }

        static bool isBackgroundColor(const uint8_t *pixel, BackgroundKind kind) {
            switch (kind) {
                case BackgroundKind::Black:
                    return isNearBlack(pixel, BLACK_THRESHOLD);
                case BackgroundKind::White:
                    return isNearWhite(pixel, WHITE_THRESHOLD, WHITE_MAX_CHANNEL_DELTA);
                case BackgroundKind::None:
                    return false;
            }
            return false;
        }

        static bool isFringeColor(const uint8_t *pixel, BackgroundKind kind) {
            switch (kind) {
                case BackgroundKind::Black:
                    return isNearBlack(pixel, BLACK_FRINGE_THRESHOLD);
                case BackgroundKind::White:
                    return isNearWhite(pixel, WHITE_FRINGE_THRESHOLD, WHITE_FRINGE_MAX_CHANNEL_DELTA);
                case BackgroundKind::None:
                    return false;
            }
            return false;
        }

        static bool isNearBlack(const uint8_t *pixel, uint8_t threshold) {
            return pixel[3] >= MIN_ALPHA &&
                    pixel[0] <= threshold &&
                    pixel[1] <= threshold &&
                    pixel[2] <= threshold;
        }

        static bool isNearWhite(const uint8_t *pixel, uint8_t threshold, uint8_t maxChannelDelta) {
            const uint8_t minChannel = std::min(pixel[0], std::min(pixel[1], pixel[2]));
            const uint8_t maxChannel = std::max(pixel[0], std::max(pixel[1], pixel[2]));
            return pixel[3] >= MIN_ALPHA &&
                    minChannel >= threshold &&
                    maxChannel - minChannel <= maxChannelDelta;
        }

        static void scaleAlpha(uint8_t *pixel, float scale) {
            pixel[3] = static_cast<uint8_t>(std::clamp(static_cast<int>(pixel[3] * scale), 0, 255));
        }
    };

} // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_org_monogram_presentation_features_stickers_core_StickerBackgroundCleaner_createSession(
        JNIEnv *,
        jobject,
        jint width,
        jint height
) {
    if (width <= 0 || height <= 0) return 0;
    return reinterpret_cast<jlong>(new StickerBackgroundCleanerSession(width, height));
}

JNIEXPORT jint JNICALL
Java_org_monogram_presentation_features_stickers_core_StickerBackgroundCleaner_cleanNative(
        JNIEnv *env,
        jobject,
        jlong handle,
        jobject bitmap,
        jint mode
) {
    auto *session = reinterpret_cast<StickerBackgroundCleanerSession *>(handle);
    if (session == nullptr) return NATIVE_NO_BACKGROUND;
    return session->clean(env, bitmap, mode == MODE_BLACK_ONLY ? MODE_BLACK_ONLY : MODE_BLACK_AND_WHITE);
}

JNIEXPORT void JNICALL
Java_org_monogram_presentation_features_stickers_core_StickerBackgroundCleaner_destroySession(
        JNIEnv *,
        jobject,
        jlong handle
) {
    auto *session = reinterpret_cast<StickerBackgroundCleanerSession *>(handle);
    delete session;
}

}
